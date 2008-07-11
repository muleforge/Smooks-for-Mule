/*
 *  Copyright 2008 Maurice Zeijen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.milyn.smooks.mule;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.milyn.SmooksException;
import org.milyn.cdr.Parameter;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.VisitAfterIf;
import org.milyn.delivery.annotation.VisitBeforeIf;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.javabean.BeanAccessor;
import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DataDecoder;
import org.milyn.xml.DomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@VisitBeforeIf(condition = "parameters.containsKey('executeBefore') && parameters.executeBefore.value == 'true'")
@VisitAfterIf(condition = "!parameters.containsKey('executeBefore') || parameters.executeBefore.value != 'true'")
@VisitBeforeReport(summary = "Dispatch to endpoint '${resource.parameters.endpointName}'.", detailTemplate = "reporting/MuleDispatcher.html")
@VisitAfterReport(summary = "Dispatch to endpoint '${resource.parameters.endpointName}'.", detailTemplate = "reporting/MuleDispatcher.html")
public class MuleDispatcher implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter {

	private static final Logger log = LoggerFactory.getLogger(MuleDispatcher.class);

	@ConfigParam(use=Use.REQUIRED)
	private String endpointName;

	@ConfigParam(use=Use.OPTIONAL)
	private String beanId;

	@ConfigParam(use=Use.OPTIONAL)
	private String resultBeanId;

	@ConfigParam(use=Use.OPTIONAL)
	private String messagePropertiesBeanId;

	private HashMap<String, Object> staticMessageProperties;

	@Config
    private SmooksResourceConfiguration config;


	public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
		dispatch(executionContext);
	}

	public void visitBefore(SAXElement element,	ExecutionContext executionContext) throws SmooksException,	IOException {

		dispatch(executionContext);
	}

	public void visitAfter(Element element, ExecutionContext executionContext)	throws SmooksException {
		dispatch(executionContext);
	}

	public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		dispatch(executionContext);
	}

	private void dispatch(ExecutionContext executionContext) {
		Object payload = null;
		if(beanId != null) {
			payload = BeanAccessor.getBean(executionContext, beanId);
		}

		if(log.isInfoEnabled()) {
			String payloadMsg;
			if(beanId == null) {
				payloadMsg = " with no payload (beanId not set)";
			} else {
				payloadMsg = " with a " + payload.getClass().getName() + " payload from beanId '" + beanId + "'";
			}
			log.info("Dispatching Mule message to endpoint '" + endpointName + "'" + payloadMsg );
		}

		NamedEndpointMuleDispatcher dispatcher = (NamedEndpointMuleDispatcher) executionContext.getAttribute(NamedEndpointMuleDispatcher.SMOOKS_CONTEXT);

		if(dispatcher == null) {
			throw new IllegalStateException("The executionContext doesn't have the MuleDispatcher object as the attribute with the key '" + NamedEndpointMuleDispatcher.SMOOKS_CONTEXT + "'");
		}
		boolean forceSynchronous = resultBeanId != null;

		Map<String, Object> messageProperties = createMessagePropertiesMap(executionContext);

		Object result = dispatcher.dispatch(endpointName, payload, messageProperties, forceSynchronous);

		if(result != null && resultBeanId != null) {
			if(log.isInfoEnabled()) {
				log.info("Received result from endpoint '" + endpointName + "'. Adding it to the bean map under beanId '" + resultBeanId + "'");
			}

			BeanAccessor.addBean(executionContext, resultBeanId, result);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> createMessagePropertiesMap(ExecutionContext executionContext) {

		HashMap<String, Object> props = (HashMap<String, Object>) getStaticMessageProperties(executionContext).clone();

		if(messagePropertiesBeanId != null) {

			Map bProperties = (Map<String, Object>) BeanAccessor.getBean(executionContext, messagePropertiesBeanId);

			if(bProperties == null) {
				throw new SmooksConfigurationException("No properties map could be found under the beanId '" + messagePropertiesBeanId + "'");
			}

			props.putAll(bProperties);
		}

		return props;

	}

	private HashMap<String, Object> getStaticMessageProperties(ExecutionContext executionContext) {

		if(staticMessageProperties == null) {
			HashMap<String, Object> lMessageProperties = new HashMap<String, Object>();

			Parameter messagePropertiesParam = config.getParameter("messageProperties");

	        if (messagePropertiesParam != null) {
	            Element messagePropertiesParamElement = messagePropertiesParam.getXml();

	            if(messagePropertiesParamElement != null) {
	                NodeList properties = messagePropertiesParamElement.getElementsByTagName("property");

                    for (int i = 0; properties != null && i < properties.getLength(); i++) {
                    	Element node = (Element)properties.item(i);

                    	String name = DomUtils.getAttributeValue(node, "name");

                    	if(StringUtils.isBlank(name)) {
                    		throw new SmooksConfigurationException("The 'name' attribute isn't a defined or empty for the message property: " + node);
                    	}
                    	name = name.trim();

                    	String rawValue = DomUtils.getAttributeValue(node, "value");
                    	if(rawValue == null) {
                    		rawValue = DomUtils.getAllText(node, true);
                    		if(StringUtils.isBlank(rawValue)) {
                    			rawValue = null;
                    		}
                    	}

                    	Object value = null;
                    	if(rawValue != null) {
                    		rawValue = rawValue.trim();

                    		DataDecoder dataDecoder = null;

                        	String type = DomUtils.getAttributeValue(node, "type");
                        	if(type != null) {
                        		type = type.trim();

                        		dataDecoder = getDecoder(executionContext, type);
                        	}

                        	if(dataDecoder != null) {
                        		value = dataDecoder.decode(rawValue);
                        	} else {
                        		value = rawValue;
                        	}
                    	}

                    	lMessageProperties.put(name, value);
                    }

	            } else {
	            	log.error("Sorry, the Javabean populator bindings must be available as XML DOM.  Please configure using XML.");
	            }
	        }
	        staticMessageProperties = lMessageProperties;
		}
		return staticMessageProperties;
	}

	private DataDecoder getDecoder(ExecutionContext executionContext, String type) throws DataDecodeException {
		@SuppressWarnings("unchecked")
		List<DataDecoder> decoders = executionContext.getDeliveryConfig().getObjects("decoder:" + type);

		DataDecoder decoder;
        if (decoders == null || decoders.isEmpty()) {
            decoder = DataDecoder.Factory.create(type);
        } else if (!(decoders.get(0) instanceof DataDecoder)) {
            throw new DataDecodeException("Configured decoder '" + type + ":" + decoders.get(0).getClass().getName() + "' is not an instance of " + DataDecoder.class.getName());
        } else {
            decoder = decoders.get(0);
        }

        return decoder;
    }

}
