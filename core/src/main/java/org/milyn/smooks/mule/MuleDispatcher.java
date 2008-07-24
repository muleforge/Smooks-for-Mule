/**
 * Copyright (C) 2008 Maurice Zeijen <maurice@zeijen.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/**
 * The MuleDispatcher is a Smooks Resource visitor to dispatch message
 * parts to endpoints in Mule.
 * <p/>
 * <h3>Usage:</h3>
 * A MuleDispatcher is defined as follows:
 * <pre>
 * &lt;resource-config selector="order"&gt;
 *     &lt;resource&gt;org.milyn.smooks.mule.MuleDispatcher&lt;/resource&gt;
 *     &lt;param name="endpointName"&gt;order&lt;/param&gt;
 *     &lt;param name="beanId"&gt;order&lt;/param&gt;
 * &lt;/resource-config&gt;
 * </pre>
 *
 * <b>Required parameters:</b>
 * &lt;property name="endpointName" value="someEndpoint" /&gt;
 *
 * <b>Optional parameters:</b>
 * &lt;param name="beanId"&gt;someBeanId&lt;/param&gt;
 * &lt;param name="resultBeanId"&gt;someBeanIdToBindTheResultTo&lt;/param&gt;
 * &lt;param name="messagePropertiesBeanId"&gt;someBeanIdToGetTheMessagePropertiesFrom&lt;/param&gt;
 * &lt;param name="messageProperties" &gt;
 *     &lt;property name="somePropertyName" value="somePropertyValue" type="someType" /&gt;
 * &lt;/param&gt;
 * </pre>
 *
 * <h3>Description of configuration parameters</h3>
 * <ul>
 * <li><i>endpointName</i> - The name of the endpoint which will be used when routing the message. If no endpoint can be found under that name then an exception is thrown.
 * <li><i>beanId</i> - The bean id of the bean which will be used as the message payload.
 * <li><i>resultBeanId</i> - If the endpoint returns a result then the payload of that result is bounded to the resultBeanId.
 * 							 When the resultBeanId isn't set then the result is discarded. If the resultBeanId is set then the
 * 							 endpoint is forced to distpatch synchronous.
 * <li><i>messagePropertiesBeanId</i> - Properties that will be set on the message before dispatching.
 * 										The MuleDispatcher messageProperties attribute section explains how to set the message properties
 * <li><i>messageProperties</i> - if true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
 * </ul>
 *
 * <h3>MuleDispatcher messageProperties attribute</h3>
 * Message properties are defined as folows:
 * <pre>
 * &lt;resource-config selector="order"&gt;
 *     &lt;resource&gt;org.milyn.smooks.mule.MuleDispatcher&lt;/resource&gt;
 *     &lt;param name="endpointName"&gt;testEndpoint&lt;/param&gt;
 *     &lt;param name="beanId"&gt;test&lt;/param&gt;
 *     &lt;param name="messageProperties"&gt;
 *         &lt;property name="prop1" value="prop1Value" /&gt;
 *         &lt;property name="prop2"&gt;prop2Value&lt;/property&gt;
 *         &lt;property name="intProp" value="10" type="Integer" /&gt;
 *         &lt;property name="dateProp" value="2008-07-11 12:30:56" type="DateTime" /&gt;
 *     &lt;/param&gt;
 * &lt;/resource-config&gt;
 *
 * &lt;resource-config selector="decoder:DateTime"&gt;
 *    &lt;resource&gt;org.milyn.javabean.decoders.DateDecoder&lt;/resource&gt;
 *    &lt;param name="format"&gt;yyyy-MM-dd HH:mm:ss&lt;/param&gt;
 * &lt;/resource-config&gt;
 * </pre>
 *
 * The following points explain the four properties:
 * <ol>
 * <li>The first property uses a attribute to set the value.
 * <li>The second property uses the property element content to set the value.
 * <li>The third property uses the type attribute to define a Integer type. This will convert the value into an Integer. This uses the standard DataDecoder feature from Smooks.
 * <li>The fourth property uses the type attribute to define a custom configured Date type. This will convert the value into a Date. The configuration is set in the resource-config with the "decoder:DateTime" selector. This uses the standard DataDecoder feature from Smooks.
 * </ol>
 *
 * <h3>Description of property attributes</h3>
 * <ul>
 * <li><i>name</i> - The message property name (required)
 * <li><i>value</i> - 	 The value of the property. The property element content can also be set. If both are set then the value attribute is used.
 * <li><i>type</i> - The type of the property value. This uses the standard DataDecoder feature from Smooks. DataDecoders can be configured using
 * 					 a "decoder:decoderName" resource-config selector. The decoderName must be set as type then. (Default: String)
 * </ul>
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 */
@VisitBeforeIf(condition = "parameters.containsKey('executeBefore') && parameters.executeBefore.value == 'true'")
@VisitAfterIf(condition = "!parameters.containsKey('executeBefore') || parameters.executeBefore.value != 'true'")
@VisitBeforeReport(summary = "Dispatch to endpoint '${resource.parameters.endpointName}'.", detailTemplate = "reporting/MuleDispatcher.html")
@VisitAfterReport(summary = "Dispatch to endpoint '${resource.parameters.endpointName}'.", detailTemplate = "reporting/MuleDispatcher.html")
public class MuleDispatcher implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter {

	private static final String MESSAGE_PROPERTIES_PARAMETER_TYPE = "type";

	private static final String MESSAGE_PROPERTIES_PARAMETER_VALUE = "value";

	private static final String MESSAGE_PROPERTIES_ATTRIBUTE_NAME = "name";

	private static final String MESSAGE_PROPERTIES_NODE_NAME = "property";

	private static final String PARAMETER_MESSAGE_PROPERTIES = "messageProperties";

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

	/**
	 * {@inheritDoc}
	 */
	public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
		dispatch(executionContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitBefore(SAXElement element,	ExecutionContext executionContext) throws SmooksException,	IOException {

		dispatch(executionContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitAfter(Element element, ExecutionContext executionContext)	throws SmooksException {
		dispatch(executionContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		dispatch(executionContext);
	}

	/**
	 * Dispatches a message to an endpoint
	 *
	 * @param executionContext
	 */
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

	/**
	 * Creates the message properties map from the static message properties and the possible
	 * properties from the Map found under the messageProperties bean Id.
	 *
	 * @param executionContext
	 * @return
	 */
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

	/**
	 * Processes the static message properties.
	 * Because these are static it is only done the first time. From that point on
	 * a cached result is used.
	 *
	 * @param executionContext
	 * @return
	 */
	private HashMap<String, Object> getStaticMessageProperties(ExecutionContext executionContext) {

		if(staticMessageProperties == null) {
			HashMap<String, Object> lMessageProperties = new HashMap<String, Object>();

			Parameter messagePropertiesParam = config.getParameter(PARAMETER_MESSAGE_PROPERTIES);

	        if (messagePropertiesParam != null) {
	            Element messagePropertiesParamElement = messagePropertiesParam.getXml();

	            if(messagePropertiesParamElement != null) {
	                NodeList properties = messagePropertiesParamElement.getElementsByTagName(MESSAGE_PROPERTIES_NODE_NAME);

                    for (int i = 0; properties != null && i < properties.getLength(); i++) {
                    	Element node = (Element)properties.item(i);

                    	String name = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_ATTRIBUTE_NAME);

                    	if(StringUtils.isBlank(name)) {
                    		throw new SmooksConfigurationException("The 'name' attribute isn't a defined or empty for the message property: " + node);
                    	}
                    	name = name.trim();

                    	String rawValue = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_PARAMETER_VALUE);
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

                        	String type = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_PARAMETER_TYPE);
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

	/**
	 * Retrieves the decoder of a certain type.
	 *
	 * @param executionContext
	 * @param type
	 * @return
	 * @throws DataDecodeException
	 */
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
