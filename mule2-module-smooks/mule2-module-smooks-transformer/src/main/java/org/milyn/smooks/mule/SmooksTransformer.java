package org.milyn.smooks.mule;

import static org.mule.config.i18n.MessageFactory.createStaticMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
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
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.Message;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.xml.sax.SAXException;

/**
 * SmooksTransformer intended to be used with the Mule ESB.
 *
 *
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 *
 */
public class SmooksTransformer extends AbstractMessageAwareTransformer {

	private static final Log log = LogFactory.getLog(SmooksTransformer.class);

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
	 * The expected result type.
	 */
	private String resultType;

	/*
	 * Filename for smooks configuration. Default is smooks-config.xml
	 */
    private String smooksConfigFile;

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

	/*
	 * Bean Id under which the JavaResult bean will be bound
	 */
	private String javaResultBeanId;

	public SmooksTransformer() {
	}

//	public

	@Override
	public void initialise() throws InitialisationException
	{
		//	determine the ResultType
		ResultType resultType = getResultType();

		//	Create the Smooks instance
		smooks = createSmooksInstance();

		//	Create the Smooks payload processor
		payloadProcessor = new PayloadProcessor( smooks, resultType );

		switch(resultType) {
		case STRING:
			setReturnClass(String.class);
			break;
		case BYTES:
			setReturnClass(byte[].class);
			break;
		case JAVA:
			if ( javaResultBeanId != null )
            {
                payloadProcessor.setJavaResultBeanId( javaResultBeanId );
            }
		}
	}


	public String getSmooksConfigFile()
	{
		return smooksConfigFile;
	}

	public void setSmooksConfigFile( final String smooksConfigFile )
	{
		this.smooksConfigFile = smooksConfigFile;
	}

    @Override
	public Object clone() throws CloneNotSupportedException
    {
    	return this;
    }

	public void setResultType( final String resultType )
	{
		this.resultType = resultType;
	}

    /**
	 * @param setExecutionContextMessageProperty the setExecutionContextMessageProperty to set
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

	public void setJavaResultBeanId( final String javaResultBeanId )
	{
		this.javaResultBeanId = javaResultBeanId;
	}

	@Override
	public Object transform(MuleMessage message, String encoding)
			throws TransformerException {

		// Retrieve the payload from the message
		Object payload = message.getPayload();

        //	Create Smooks ExecutionContext.
		ExecutionContext executionContext = smooks.createExecutionContext();

		//	Add smooks reporting if configured
		addReportingSupport( executionContext );

        //	Use the Smooks PayloadProcessor to execute the transformation....
        final Object transformedPayload = payloadProcessor.process( payload, executionContext );

        if(executionContextAsMessageProperty) {
        	// Set the Smooks Excecution properties on the Mule Message object
        	message.setProperty(executionContextMessagePropertyKey, getSerializableObjectsMap( executionContext.getAttributes()) );
        }

		return transformedPayload;
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

	//	private

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


	private ResultType getResultType() throws InitialisationException
	{
		ResultType resultType = ResultType.STRING;
		if ( this.resultType != null )
		{
	        try
	        {
	            resultType = ResultType.valueOf( this.resultType );
	        }
	        catch ( final IllegalArgumentException e )
	        {
    			final Message errorMsg = createStaticMessage( "Invalid 'resultType' config value '" + resultType + "'.  Valid values are: " + Arrays.asList(ResultType.values() ) );
	            throw new InitialisationException(errorMsg, e,  this);
	        }
		}
		return resultType;
	}

	private void addReportingSupport( final ExecutionContext executionContext ) throws TransformerException
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
	            throw new TransformerException( errorMsg, e );
            }
        }
	}
}