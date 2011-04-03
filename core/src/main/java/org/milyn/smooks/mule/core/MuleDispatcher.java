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

package org.milyn.smooks.mule.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.milyn.SmooksException;
import org.milyn.cdr.Parameter;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AnnotationConstants;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.annotation.VisitAfterIf;
import org.milyn.delivery.annotation.VisitBeforeIf;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DataDecoder;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.context.BeanIdStore;
import org.milyn.javabean.repository.BeanId;
import org.milyn.smooks.mule.core.message.MVELEvaluatingMessagePropertyValue;
import org.milyn.smooks.mule.core.message.MessageProperty;
import org.milyn.smooks.mule.core.message.MessagePropertyValue;
import org.milyn.smooks.mule.core.message.StaticMessagePropertyValue;
import org.milyn.util.CollectionsUtil;
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
 * <li><i>expression</i> - A MVEL script. The result of the script is used as the message payload. This overrides the beanId property.
 * <li><i>beanId</i> - The bean id of the bean which will be used as the message payload. The context of the MVEL script contains all the beans of the bean map.
 * <li><i>resultBeanId</i> - If the endpoint returns a result then the payload of that result is bounded to the resultBeanId.
 * 							 When the resultBeanId isn't set then the result is discarded. If the resultBeanId is set then the
 * 							 endpoint is forced to distpatch synchronous.
 * <li><i>messagePropertiesBeanId</i> -
 * <li><i>messageProperties</i> - Properties that will be set on the message before dispatching.
 * 								  The MuleDispatcher messageProperties attribute section explains how to set the message properties
 * </ul>
 *
 * <h3>MuleDispatcher messageProperties attribute</h3>
 * Message properties are defined as folows:
 * <pre>
 * &lt;resource-config selector="order"&gt;
 *     &lt;resource&gt;org.milyn.smooks.mule.MuleDispatcher&lt;/resource&gt;
 *     &lt;param name="endpointName"&gt;testEndpoint&lt;/param&gt;
 *     &lt;param name="messageBeanId"&gt;test&lt;/param&gt;
 *     &lt;param name="messageProperties"&gt;
 *         &lt;property name="prop1" value="prop1Value" /&gt;
 *         &lt;property name="prop2"&gt;"prop2Value"&lt;/property&gt;
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
 * <li>The second property takes the property element content and parses it as a MVEL expression.
 * <li>The third property uses the type attribute to define a Integer type. This will convert the value into an Integer. This uses the standard DataDecoder feature from Smooks.
 * <li>The fourth property uses the type attribute to define a custom configured Date type. This will convert the value into a Date. The configuration is set in the resource-config with the "decoder:DateTime" selector. This uses the standard DataDecoder feature from Smooks.
 * </ol>
 *
 * <h3>Description of property attributes</h3>
 * <ul>
 * <li><i>name</i> - The message property name (required)
 * <li><i>value</i> - The value of the property. The property element content can also be set. If both are set then the value attribute is used.
 * <li><i>type</i> - The type of the property value. This uses the standard DataDecoder feature from Smooks. DataDecoders can be configured using
 * 					 a "decoder:decoderName" resource-config selector. The decoderName must be set as type then. (Default: String)
 * </ul>
 *
 * <h3>Description of the property element content</h3>
 * The property element content is parsed as a MVEL expression. This gives
 * great flexibility. The MVEL context contains all the beans from the bean map.
 * The type and value attribute are ignored when the element content is set.
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 */
@VisitBeforeIf(condition = "parameters.containsKey('executeBefore') && parameters.executeBefore.value == 'true'")
@VisitAfterIf(condition = "!parameters.containsKey('executeBefore') || parameters.executeBefore.value != 'true'")
@VisitBeforeReport(summary = "Dispatch to endpoint '${resource.parameters.endpointName}'.", detailTemplate = "reporting/MuleDispatcher.html")
@VisitAfterReport(summary = "Dispatch to endpoint '${resource.parameters.endpointName}'.", detailTemplate = "reporting/MuleDispatcher.html")
public class MuleDispatcher implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter, Consumer, Producer {

	private static final String MESSAGE_PROPERTIES_PARAMETER_TYPE = "type";

	private static final String MESSAGE_PROPERTIES_PARAMETER_VALUE = "value";

	private static final String MESSAGE_PROPERTIES_PARAMETER_DECODER = "decoder";

	private static final String MESSAGE_PROPERTIES_ATTRIBUTE_NAME = "name";

	private static final String MESSAGE_PROPERTIES_NODE_NAME = "property";

	private static final String MESSAGE_EXPRESSION_NODE_NAME = "expression";

	public static final String PARAMETER_MESSAGE_PROPERTIES = "messageProperties";

	private static final Logger log = LoggerFactory.getLogger(MuleDispatcher.class);

	@ConfigParam(use=Use.REQUIRED)
	private String endpointName;

	@ConfigParam(name="beanId", use=Use.OPTIONAL)
	private String beanIdName;

	private BeanId beanId;

	@ConfigParam(name="resultBeanId", use=Use.OPTIONAL)
	private String resultBeanIdName;

	private BeanId resultBeanId;

	@ConfigParam(name="messagePropertiesBeanId", use=Use.OPTIONAL)
	private String messagePropertiesBeanIdName;

	private BeanId messagePropertiesBeanId;

	@ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private MVELExpressionEvaluator expression;

	private List<MessageProperty> staticMessageProperties;

    @ConfigParam(defaultVal = "false")
    private boolean copyOriginalMessageProperties;

    @ConfigParam(defaultVal = "true")
    private boolean overrideOriginalMessageProperties;

    @ConfigParam(defaultVal = "true")
    private boolean ignorePropertiesWithNullValues;

    @ConfigParam(defaultVal = "false")
    private boolean copyOriginalMessageAttachments;

	@Config
    private SmooksResourceConfiguration config;

	@AppContext
	private ApplicationContext appContext;

	@Initialize
	public void initialize() {
		BeanIdStore beanIdStore = appContext.getBeanIdStore();

		if(beanIdName != null) {
			beanId = beanIdStore.register(beanIdName);
		}
		if(resultBeanIdName != null) {
			resultBeanId = beanIdStore.register(resultBeanIdName);
		}
		if(messagePropertiesBeanIdName != null) {
			messagePropertiesBeanId = beanIdStore.register(messagePropertiesBeanIdName);
		}

	}

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

		BeanContext beanRepository = executionContext.getBeanContext();

		if(expression != null) {
			Map<?, ?> beanMap = beanRepository.getBeanMap();
			payload = expression.getValue(beanMap);

		} else if(beanIdName != null) {

			payload = beanRepository.getBean(beanId);

		}

		if(log.isInfoEnabled()) {
			String payloadMsg;
			if(payload == null) {
				payloadMsg = " with no payload";
			} else {
				payloadMsg = " with a " + payload.getClass().getName() + " payload";

				if(expression != null) {
					payloadMsg += " from an expression";
				} else {
					payloadMsg += " from beanId '" + beanIdName + "'";
				}
			}
			log.info("Dispatching Mule message to endpoint '" + endpointName + "'" + payloadMsg );
		}

		NamedEndpointMuleDispatcher dispatcher = (NamedEndpointMuleDispatcher) executionContext.getAttribute(NamedEndpointMuleDispatcher.SMOOKS_CONTEXT);

		if(dispatcher == null) {
			throw new IllegalStateException("The executionContext doesn't have the MuleDispatcher object as the attribute with the key '" + NamedEndpointMuleDispatcher.SMOOKS_CONTEXT + "'");
		}
		boolean forceSynchronous = resultBeanId != null;

		Map<String, Object> messageProperties = createMessagePropertiesMap(executionContext);

		Object result = dispatcher.dispatch(endpointName, payload, messageProperties, forceSynchronous, copyOriginalMessageProperties, overrideOriginalMessageProperties, ignorePropertiesWithNullValues, copyOriginalMessageAttachments);

		if(result != null && resultBeanId != null) {
			if(log.isInfoEnabled()) {
				log.info("Received result from endpoint '" + endpointName + "'. Adding it to the bean map under beanId '" + resultBeanId.getName()+ "'");
			}

			beanRepository.addBean(resultBeanId, result);
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

		HashMap<String, Object> props = evaluateStaticMessageProperties(executionContext);

		if(messagePropertiesBeanId != null) {

			BeanContext beanContext = executionContext.getBeanContext();

			Map<String, Object> bProperties = (Map<String, Object>) beanContext.getBean(messagePropertiesBeanId);

			if(bProperties == null) {
				throw new SmooksConfigurationException("No properties map could be found under the beanId '" + messagePropertiesBeanId.getName() + "'");
			}

			props.putAll(bProperties);
		}

		return props;

	}

	private HashMap<String, Object> evaluateStaticMessageProperties(ExecutionContext executionContext) {


		HashMap<String, Object> props = new HashMap<String, Object>();
		for(MessageProperty messageProperty : getStaticMessageProperties(executionContext) ) {
			props.put(messageProperty.getName(), messageProperty.getValue(executionContext));
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
	private List<MessageProperty> getStaticMessageProperties(ExecutionContext executionContext) {

		if(staticMessageProperties == null) {
			List<MessageProperty> lMessageProperties = new ArrayList<MessageProperty>();

			Parameter messagePropertiesParam = config.getParameter(PARAMETER_MESSAGE_PROPERTIES);

	        if (messagePropertiesParam != null) {

	            Element messagePropertiesParamElement = messagePropertiesParam.getXml();

	            if(messagePropertiesParamElement != null) {
	            	boolean extendedConfig = messagePropertiesParamElement.getNamespaceURI().equals(Constants.MULE_SMOOKS_NAMESPACE);

	            	if(extendedConfig) {
	            		resolvePropertiesExtendedConfig(executionContext, lMessageProperties, messagePropertiesParamElement);
	            	} else {
	            		resolvePropertiesNormalConfig(executionContext, lMessageProperties, messagePropertiesParamElement);
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
	 * @param executionContext
	 * @param lMessageProperties
	 * @param messagePropertiesParamElement
	 */
	private void resolvePropertiesNormalConfig(ExecutionContext executionContext, List<MessageProperty> lMessageProperties, Element messagePropertiesParamElement) {
		NodeList properties = messagePropertiesParamElement.getElementsByTagName(MESSAGE_PROPERTIES_NODE_NAME);

		for (int i = 0; properties != null && i < properties.getLength(); i++) {

			Element node = (Element)properties.item(i);

			String name = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_ATTRIBUTE_NAME);

			if(StringUtils.isBlank(name)) {
				throw new SmooksConfigurationException("The 'name' attribute isn't a defined or empty for the message property: " + node);
			}
			name = name.trim();

			MessagePropertyValue messageValue = null;
			String rawValue = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_PARAMETER_VALUE);
			if(rawValue == null) {
				String expression = DomUtils.getAllText(node, true);
				if(StringUtils.isNotBlank(expression)) {
					MVELExpressionEvaluator evaluator = new MVELExpressionEvaluator();
					try {
						evaluator.setExpression(expression);
			        } catch (RuntimeException e) {
						throw new RuntimeException("Exception while setting the expression on the MVELExpressionEvaluator", e);
					}

					messageValue = new MVELEvaluatingMessagePropertyValue(evaluator);
				}
			} else {
		    	rawValue = rawValue.trim();

		    	Object value = null;
		    	String type = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_PARAMETER_TYPE);
		    	if(type != null) {
		    		type = type.trim();

		    		value = getDecoder(executionContext, type).decode(rawValue);
		    	} else {
		    		value = rawValue;
		    	}
		    	messageValue = new StaticMessagePropertyValue(value);
			}

			lMessageProperties.add(new MessageProperty(name, messageValue));
		}
	}

	/**
	 * @param executionContext
	 * @param lMessageProperties
	 * @param messagePropertiesParamElement
	 */
	private void resolvePropertiesExtendedConfig(ExecutionContext executionContext, List<MessageProperty> lMessageProperties, Element messagePropertiesParamElement) {
		NodeList properties = messagePropertiesParamElement.getElementsByTagNameNS(Constants.MULE_SMOOKS_NAMESPACE, MESSAGE_PROPERTIES_NODE_NAME);

		for (int i = 0; properties != null && i < properties.getLength(); i++) {

			Element node = (Element)properties.item(i);

			String name = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_ATTRIBUTE_NAME);

			if(StringUtils.isBlank(name)) {
				throw new SmooksConfigurationException("The 'name' attribute isn't a defined or empty for the message property: " + node);
			}
			name = name.trim();

			MessagePropertyValue messageValue = null;
			String rawValue = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_PARAMETER_VALUE);
			if(rawValue == null) {
				Element expressionElement = (Element) DomUtils.getElement(node, MESSAGE_EXPRESSION_NODE_NAME, 1, Constants.MULE_SMOOKS_NAMESPACE);
				String expression = DomUtils.getAllText(expressionElement, true);
				if(StringUtils.isNotBlank(expression)) {
					MVELExpressionEvaluator evaluator = new MVELExpressionEvaluator();
					try {
						evaluator.setExpression(expression);
			        } catch (RuntimeException e) {
						throw new RuntimeException("Exception while setting the expression on the MVELExpressionEvaluator", e);
					}

					messageValue = new MVELEvaluatingMessagePropertyValue(evaluator);
				}
			} else {
		    	rawValue = rawValue.trim();

		    	Object value = null;
		    	String decoder = DomUtils.getAttributeValue(node, MESSAGE_PROPERTIES_PARAMETER_DECODER);
		    	if(decoder != null) {
		    		decoder = decoder.trim();

		    		value = getDecoder(executionContext, decoder).decode(rawValue);
		    	} else {
		    		value = rawValue;
		    	}
		    	messageValue = new StaticMessagePropertyValue(value);
			}

			lMessageProperties.add(new MessageProperty(name, messageValue));
		}
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

	/* (non-Javadoc)
	 * @see org.milyn.delivery.ordering.Consumer#consumes(java.lang.Object)
	 */
	public boolean consumes(Object object) {
		if(object.toString().equals(beanIdName)) {
            return true;
        }
		if(object.toString().equals(messagePropertiesBeanIdName)) {
            return true;
        }

        return false;
	}

	/* (non-Javadoc)
	 * @see org.milyn.delivery.ordering.Producer#getProducts()
	 */
	public Set<? extends Object> getProducts() {
		if(resultBeanIdName == null) {
			return Collections.emptySet();
		}
		return CollectionsUtil.toSet(resultBeanIdName);
	}

}
