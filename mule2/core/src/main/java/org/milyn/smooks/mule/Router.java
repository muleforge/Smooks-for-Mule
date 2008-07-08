/**
 *
 */
package org.milyn.smooks.mule;

import static org.mule.config.i18n.MessageFactory.createStaticMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.container.plugin.PayloadProcessor;
import org.milyn.container.plugin.ResultType;
import org.milyn.event.report.HtmlReportGenerator;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.routing.RoutingException;
import org.mule.config.i18n.Message;
import org.mule.routing.outbound.FilteringOutboundRouter;
import org.xml.sax.SAXException;

/**
 * @author maurice_zeijen
 *
 */
public class Router extends FilteringOutboundRouter {

	private static final Log log = LogFactory.getLog(Transformer.class);

	public static final String MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT = "SmooksExecutionContext";

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
    private String smooksConfigFile;

    /*
     * If true, then the execution context is set as property on the message
     */
    private final boolean executionContextAsMessageProperty = false;

	/*
     * The key name of the execution context message property
     */
    private final String executionContextMessagePropertyKey = MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT;

    /*
     * If true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
     */
	private final boolean excludeNonSerializables = true;

	/*
	 * Path where the Smooks Report will be generated.
	 */
	private String reportPath;



	@Override
	@SuppressWarnings("unchecked")
	public void initialise() throws InitialisationException
	{
		//	Create the Smooks instance
		smooks = createSmooksInstance();

		// Create the dispatcher which handles the dispatching of messages
		AbstractMuleOutboundEndpointDispatcher dispatcher = createDispatcher();

		// make the dispatcher available for Smooks
		smooks.getApplicationContext().getAttributes().put(MuleOutboundEndpointDispatcher.SMOOKS_APPCONTEXT_CONTEXT, dispatcher);

		//	Create the Smooks payload processor
		payloadProcessor = new PayloadProcessor( smooks, ResultType.NORESULT );
	}


	@Override
	public MuleMessage route(MuleMessage message, MuleSession session, boolean synchronous) throws RoutingException {

		// Retrieve the payload from the message
		Object payload = message.getPayload();

        //	Create Smooks ExecutionContext.
		ExecutionContext executionContext = smooks.createExecutionContext();

		RouterSession routerSession = new RouterSessionImpl(session);

		executionContext.setAttribute(RouterSession.SMOOKS_EXECCONTEXT_CONTEXT, routerSession);

		//	Add smooks reporting if configured
		addReportingSupport(message, executionContext );

        //	Use the Smooks PayloadProcessor to execute the routing....
        payloadProcessor.process( payload, executionContext );

		return null;
	}

	/**
     * Will return a Map containing only the Serializable objects
     * that exist in the passed-in Map if {@link #excludeNonSerializables} is true.
     *
     * @param smooksAttribuesMap 	- Map containing attributes from the Smooks ExecutionContext
     * @return Map	- Map containing only the Serializable objects from the passed-in map.
     */
    @SuppressWarnings( "unchecked" )
	protected Map getSerializableObjectsMap( final Map smooksAttribuesMap )
	{
    	if ( !excludeNonSerializables ) {
			return smooksAttribuesMap;
		}

		Map smooksExecutionContextMap = new HashMap();

		Set<Map.Entry> s = smooksAttribuesMap.entrySet();
		for (Map.Entry me : s)
		{
			Object value = me.getValue();
			if( value instanceof Serializable )
			{
				smooksExecutionContextMap.put( me.getKey(), value );
			}
		}
		return smooksExecutionContextMap;
	}

	private Smooks createSmooksInstance() throws InitialisationException
	{
		if ( smooksConfigFile == null )
		{
			final Message errorMsg = createStaticMessage( "'smooksConfigFile' parameter must be specified" );
			throw new InitialisationException( errorMsg, this );
		}

		try
		{
			return new Smooks ( smooksConfigFile );
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


	private AbstractMuleOutboundEndpointDispatcher createDispatcher() {
		//Create the dispatcher which will dispatch the messages provided by Smooks
		AbstractMuleOutboundEndpointDispatcher dispatcher = new AbstractMuleOutboundEndpointDispatcher(getEndpoints()) {

			@Override
			public void dispatch(ExecutionContext executionContext, MuleSession session, MuleMessage message, OutboundEndpoint endpoint) {

				if(executionContextAsMessageProperty) {
		        	// Set the Smooks Excecution properties on the Mule Message object
		        	message.setProperty(executionContextMessagePropertyKey, getSerializableObjectsMap( executionContext.getAttributes()) );
		        }

				try {
					Router.this.dispatch(session, message, endpoint);
				} catch (MuleException e) {

					//FIXME: Throw a meaningful exception
					throw new RuntimeException("Couldn't dispatch the message", e);
				}
			}
		};
		return dispatcher;
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

}
