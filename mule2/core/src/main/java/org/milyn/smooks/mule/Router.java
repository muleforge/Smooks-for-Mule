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

import static org.milyn.smooks.mule.Constants.MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT;
import static org.milyn.smooks.mule.Constants.MESSAGE_PROPERTY_KEY_PROFILE;
import static org.mule.config.i18n.MessageFactory.createStaticMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.container.plugin.PayloadProcessor;
import org.milyn.container.plugin.ResultType;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.smooks.mule.core.ExecutionContextUtil;
import org.milyn.smooks.mule.core.MuleDispatcher;
import org.milyn.smooks.mule.core.NamedEndpointMuleDispatcher;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.api.routing.RoutingException;
import org.mule.config.i18n.Message;
import org.mule.routing.outbound.AbstractOutboundRouter;
import org.mule.routing.outbound.FilteringOutboundRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The Router intended to be used with the Mule ESB.
 * <p/>
 * <h3>Usage:</h3>
 * Declare the router in the Mule configuration file:
 * <pre>
 * &lt;outbound&gt;
 * 	&lt;smooks:router configFile="/smooks-config.xml"&gt;
 * 		&lt;jms:outbound-endpoint name="order" queue="order.queue"/&gt;
 * 		&lt;jms:outbound-endpoint name="order-important" queue="order.important.queue"/&gt;
 *	&lt;/smooks:router&gt;
 * &lt;/outbound&gt;</pre>
 *
 * <h3>Description of configuration attributes/properties</h3>
 * <ul>
 * <li><i>configFile</i> - The Smooks configuration file. Can be a path on the file system or on the classpath.
 * <li><i>profile</i> - The Smooks profile to execute. If a profile name was found on the message then that one is used.
 * <li><i>profileMessagePropertyKey</i> - The name of the message property which could contain a possible profile name.
 * 										  If the property is set and the value is a string then that value is used as profile name.
 * 										  Default "MessageProfile".
 * <li><i>executionContextAsMessageProperty</i> - If set to "true" then the attributes map of the Smooks execution context is added to the message
 * 												  properties of every message that gets created by this router. The property key is defined with
 * 												  the executionContextMessagePropertyKey property. Default is "false"
 * <li><i>executionContextMessagePropertyKey</i> - The property key under which the execution context is put. Default is "SmooksExecutionContext"
 * <li><i>excludeNonSerializables</i> - if true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
 * <li><i>reportPath</i> - specifies the path and file name for generating a Smooks Execution Report.  This is a development tool.
 * </ul>
 *
 * <h3>Defining Endpoints</h3>
 * As normal with Mule router you must at least define one endpoint to route the messages to. For this router it is
 * also required that you define the name of the router. That name is used as reference within the Smooks configuration
 * so that it knows which endpoint to use.
 *
 * <h3>Using the Smooks MuleDispatcher</h3>
 * Within the Smooks Router the {@link org.milyn.smooks.mule.core.MuleDispatcher} resource is used to actually route the message parts
 * to one of the defined endpoints (using the endpoint name). The following example demonstrates how to configure a MuleDispatcher:
 *
 * <pre>
 * &lt;resource-config selector="order"&gt;
 * 	&lt;resource&gt;org.milyn.smooks.mule.MuleDispatcher&lt;/resource&gt;
 * 	&lt;param name="endpointName"&gt;orderEndpoint&lt;/param&gt;
 * 	&lt;param name="beanId"&gt;orderBean&lt;/param&gt;
 * &lt;/resource-config&gt;
 * </pre>
 *
 * In this example Smooks calls the {@link MuleDispatcher} when it is in the visit after phase of the "order" element. The {@link MuleDispatcher}
 * creates a new Mule message and it will use the object found under the beanId "orderBean" in the Smooks BeanMap as it's payload. It
 * then dispatches that message to the endpoint with the name "orderEndpoint".
 * <p/>
 * More information on the configuration options of the {@link MuleDispatcher} can be found in the javadoc of the {@link MuleDispatcher} itself.
 *
 * <h3>Accessing the inbound MuleMessage within Smooks</h3>
 * The inbound MuleMessage is added to the Smooks bean context under the key <b>MULE_MESSAGE</b>. This means that you can access the Message from within
 * the Smooks filter process.
 *
 * <h3>Accessing Smooks ExecutionContext attributes</h3>
 * When the {@link MuleDispatcher} dispatches a message and if the "executionContextAsMessageProperty" property
 * is set to <code>true</code> then the MuleDispatcher will make the attributes that have been set in the
 * ExecutionContext at that moment available for other actions in the Mule ESB by setting the attributes map as a property of the message.
 * The attributes can be accessed by using the key defined under the property "executionContextMessagePropertyKey". Default
 * "SmooksExecutionContext" is used, which is set under the constant {@link Constants#MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT}.
 * An example of accessing the attributes map is:
 * <pre>
 * message.getProperty( Constants.MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT );
 * </pre>
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class Router extends FilteringOutboundRouter {

	private static final Logger log = LoggerFactory.getLogger(Router.class);

	private static final long serialVersionUID = 1L;

	/*
	 * Smooks payload processor
	 */
	private PayloadProcessor payloadProcessor;

	/*
	 * Smooks instance
	 */
	private Smooks smooks;

	/*
	 * Filename for smooks configuration. Default is smooks-config.xml
	 */
    private String configFile;

    /*
	 * The smooks profile to be used
	 */
    private String profile;

	/*
	 * The key name where the message profile can be located
	 */
    private String profileMessagePropertyKey = MESSAGE_PROPERTY_KEY_PROFILE;

    /*
     * If true, then the execution context is set as property on the message
     */
    private boolean executionContextAsMessageProperty = false;

	/*
     * The key name of the execution context message property
     */
    private String executionContextMessagePropertyKey = MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT;

    /*
     * If true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
     */
	private boolean excludeNonSerializables = true;

	/*
	 * Path where the Smooks Report will be generated.
	 */
	private String reportPath;

	private final Map<String, OutboundEndpoint> endpointMap = new HashMap<String, OutboundEndpoint>();

	@Override
	public void initialise() throws InitialisationException
	{
		//	Create the Smooks instance
		smooks = createSmooksInstance();

		//	Create the Smooks payload processor
		payloadProcessor = new PayloadProcessor( smooks, ResultType.NORESULT );

		initialiseEndpointMap();
	}

	private void initialiseEndpointMap() {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing endpoint map");
		}

		for(Object e :  getEndpoints()) {
			OutboundEndpoint endpoint = (OutboundEndpoint) e;

			String name = endpoint.getName();
			if(StringUtils.isEmpty(name)) {
				throw new IllegalArgumentException("The outbound endpoint list may only contain endpoints which have a name");
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Registered endpoint '" + name + "' in endpoint map.");
			}

			endpointMap.put(name, endpoint);
		}
	}

	public String getConfigFile()
	{
		return configFile;
	}

    /**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * @return the profileMessagePropertyKey
	 */
	public String getProfileMessagePropertyKey() {
		return profileMessagePropertyKey;
	}

	/**
	 * @param profileMessagePropertyKey the profileMessagePropertyKey to set
	 */
	public void setProfileMessagePropertyKey(String profileMessagePropertyKey) {
		this.profileMessagePropertyKey = profileMessagePropertyKey;
	}

	/**
	 * @return the executionContextAsMessageProperty
	 */
	public boolean isExecutionContextAsMessageProperty() {
		return executionContextAsMessageProperty;
	}

	/**
	 * @return the executionContextMessagePropertyKey
	 */
	public String getExecutionContextMessagePropertyKey() {
		return executionContextMessagePropertyKey;
	}

	/**
	 * @return the excludeNonSerializables
	 */
	public boolean isExcludeNonSerializables() {
		return excludeNonSerializables;
	}

	/**
	 * @return the reportPath
	 */
	public String getReportPath() {
		return reportPath;
	}


	public void setConfigFile( final String configFile )
	{
		this.configFile = configFile;
	}

    /**
	 * @param executionContextMessageProperty the setExecutionContextMessageProperty to set
	 */
	public void setExecutionContextAsMessageProperty(
			boolean executionContextMessageProperty) {
		this.executionContextAsMessageProperty = executionContextMessageProperty;
	}

	/**
	 * @param executionContextMessagePropertyKey the executionContextMessagePropertyKey to set
	 */
	public void setExecutionContextMessagePropertyKey(
			String executionContextMessagePropertyKey) {

		if ( executionContextMessagePropertyKey == null )
		{
			throw new IllegalArgumentException( "'executionContextMessagePropertyKey' can not be set to null." );
		}
		if ( executionContextMessagePropertyKey.length() == 0 )
		{
			throw new IllegalArgumentException( "'executionContextMessagePropertyKey' can not be set to an empty string." );
		}

		this.executionContextMessagePropertyKey = executionContextMessagePropertyKey;
	}

    /**
     * @param excludeNonSerializables  - If true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
     */
	public void setExcludeNonSerializables( boolean excludeNonSerializables )
	{
		this.excludeNonSerializables = excludeNonSerializables;
	}

	public void setReportPath( final String reportPath )
	{
		this.reportPath = reportPath;
	}


	@Override
	public MuleMessage route(MuleMessage message, MuleSession session) throws RoutingException {
		if (logger.isDebugEnabled()) {
			logger.debug("Routing message '" + message.toString() + "'");
		}

		// Retrieve the payload from the message
		Object payload = message.getPayload();

        //	Create Smooks ExecutionContext.
		ExecutionContext executionContext;

		String profile = retrieveProfile(message);
		if(profile != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating execution context with profile '" + profile + "'");
			}

			executionContext = smooks.createExecutionContext(profile);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating execution context");
			}

			executionContext = smooks.createExecutionContext();
		}

		// Create the dispatcher which handles the dispatching of messages
		Dispatcher dispatcher = new Dispatcher(session, message, executionContext);

		// make the dispatcher available for Smooks
		executionContext.setAttribute(NamedEndpointMuleDispatcher.SMOOKS_CONTEXT, dispatcher);

        // Make the inbound Mule message accessable via the Smooks bean context
        executionContext.getBeanContext().addBean(org.milyn.smooks.mule.core.Constants.SMOOKS_BEAN_MULE_MESSAGE, message);

		//	Add smooks reporting if configured
		addReportingSupport(message, executionContext );

		if (logger.isDebugEnabled()) {
			logger.debug("Processing message '" + message.toString() + "'");
		}

        //	Use the Smooks PayloadProcessor to execute the routing....
        payloadProcessor.process( payload, executionContext );

		return null;
	}

	private String retrieveProfile(MuleMessage message) {

		Object messageProfile = message.getProperty(profileMessagePropertyKey);

		if(messageProfile != null && messageProfile instanceof String) {

			return (String) messageProfile;

		}

		return profile;
	}

	private Smooks createSmooksInstance() throws InitialisationException
	{
		if ( configFile == null )
		{
			final Message errorMsg = createStaticMessage( "'smooksConfigFile' parameter must be specified" );
			throw new InitialisationException( errorMsg, this );
		}

		try
		{
			return new Smooks ( configFile );
		}
		catch ( final IOException e)
		{
			final Message errorMsg = createStaticMessage( "IOException while trying to get smooks instance: " );
			throw new InitialisationException( errorMsg, e, this);
		}
		catch ( final SAXException e)
		{
			final Message errorMsg = createStaticMessage( "SAXException while trying to get smooks instance: " );
			throw new InitialisationException( errorMsg, e, this);
		}
	}

	private void addReportingSupport(final MuleMessage message, final ExecutionContext executionContext ) throws RoutingException
	{
		if( reportPath != null )
		{
            try
            {
            	log.info( "Using Smooks Reporting. Will generate report in file [" + reportPath  + "]. Do not use in production evironment as this will have negative impact on performance!");
                executionContext.setEventListener( new HtmlReportGenerator( reportPath ) );
            }
            catch ( final IOException e)
            {
    			final Message errorMsg = createStaticMessage( "Failed to create HtmlReportGenerator instance." );
	            throw new RoutingException(errorMsg, message, (ImmutableEndpoint)null, e);
            }
        }
	}

    private class Dispatcher implements NamedEndpointMuleDispatcher {

        private final ExecutionContext executionContext;

        private final MuleSession muleSession;

        private final MuleMessage inboundMessage;


        public Dispatcher(MuleSession muleSession, MuleMessage inboundMessage, ExecutionContext executionContext) {
            this.muleSession = muleSession;
            this.inboundMessage = inboundMessage;
            this.executionContext = executionContext;
        }

        public Object dispatch(String endpointName, Object payload, Map<String, Object> messageProperties, boolean forceSynchronous) {
            OutboundEndpoint outboundEndpoint = endpointMap.get(endpointName);

            if(outboundEndpoint == null) {
                throw new IllegalArgumentException("The outbound endpoint with the name '" + endpointName + "' isn't declared in the outbound endpoint map");
            }

            MuleMessage muleMessage;
            if(messageProperties == null || messageProperties.size() == 0) {
                muleMessage = new DefaultMuleMessage(payload);
            } else {
                muleMessage = new DefaultMuleMessage(payload, messageProperties);
            }

            muleMessage.setCorrelationId(inboundMessage.getUniqueId());

            MuleMessage resultMessage = dispatch(outboundEndpoint, muleMessage, forceSynchronous);

            Object result = null;
            if(resultMessage != null) {
                result = resultMessage.getPayload();
            }
            return result;
        }

        public MuleMessage dispatch(OutboundEndpoint endpoint, MuleMessage message, boolean forceSynchronous) {
            boolean synchr = endpoint.isSynchronous() || forceSynchronous;

            if(executionContextAsMessageProperty) {
                // Set the Smooks Excecution properties on the Mule Message object
                message.setProperty(executionContextMessagePropertyKey, ExecutionContextUtil.getAtrributesMap(executionContext, excludeNonSerializables) );
            }

            try {

                if(synchr) {
                    return Router.this.send(muleSession, message, endpoint);
                } else {
                    Router.this.dispatch(muleSession, message, endpoint);
                }

            } catch (MuleException e) {

                throw new RuntimeException(new CouldNotRouteOutboundMessageException(message, endpoint, e));

            }
            return null;
        }

    }


}
