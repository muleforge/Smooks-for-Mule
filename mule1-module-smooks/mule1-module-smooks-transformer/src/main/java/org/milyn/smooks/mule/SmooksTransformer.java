package org.milyn.smooks.mule;

import static org.mule.config.i18n.MessageFactory.createStaticMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.container.plugin.PayloadProcessor;
import org.milyn.container.plugin.ResultType;
import org.milyn.event.report.HtmlReportGenerator;
import org.mule.config.i18n.Message;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * SmooksTransformer intended to be used with the Mule ESB.
 * <p/>
 * <h3>Usage:</h3>
 * <pre>
 * Declare the tranformer in the Mule configuration file:
 * &lt;transformers&gt;
 *      &lt;transformer name="SmooksTransformer" className="org.milyn.smooks.mule.SmooksTransformer"/&gt;
 * &lt;/transformers&gt;
 *
 * Configure the transformer with a router:
 * &lt;inbound-router&gt;
 *     &lt;endpoint address="stream://System.in"  transformers="SmooksTransformer"/&gt;
 * &lt;/inbound-router&gt;
 *
 * Optional properties:
 * &lt;property name="smooksConfig" value="smooks-config.xml" /&gt;
 * &lt;property name="resultType" value="STRING" /&gt;
 * &lt;property name="excludeNonSerializables" value="false" /&gt;
 * &lt;property name="reportPath" value="/tmp/smooks-report.html" /&gt;
 * &lt;property name="javaResultBeanId" value="orderBean" /&gt;
 * </pre>
 *
 * <h3>Description of configuration properties</h3>
 * <ul>
 * <li><i>smooksConfig</i> - the Smooks configuration file. Can be a path on the file system or on the classpath.
 * <li><i>resultType</i> - type of result expected from Smooks ("STRING", "BYTES", "JAVA", "NORESULT"). Default is "STRING".
 * <li><i>excludeNonSerializables</i> - if true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
 * <li><i>reportPath</i> - specifies the path and file name for generating a Smooks Execution Report.  This is a development tool.
 * <li><i>javaResultBeanId</i> - specifies the Smooks bean context beanId to be mapped as the result when the resultType is "JAVA".  If not specified,
 *                               the whole bean context bean Map is mapped as the result.
 * </ul>
 *
 * <h3>Accessing Smooks ExecutionContext attributes</h3>
 * After Smooks has performed the filtering the transform method will make the attributes that have been set in the
 * the ExecutionContext available for other actions in the Mule ESB.
 * The attributes (Map) can be accessed by using the {@link #MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT} key like this:
 * <pre>
 * umoEventContext.getMessage().get( SmooksTransformer.EXECUTION_CONTEXT_ATTR_MAP_KEY );
 * </pre>
 * <h3>Specifying the Source and Result Types</h3>
 * From the object payload data type, this Transformer is able to automatically determine the type of
 * {@link javax.xml.transform.Source} to use (via the Smooks {@link PayloadProcessor}).  The
 * {@link javax.xml.transform.Result} type to be used can be specified via the "resultType"
 * property, as outlined above.
 * <p/>
 * It is expected that the above mechanism will be satisfactory for most usecase, but not all.
 * For the other usecases, this action supports {@link org.milyn.container.plugin.SourceResult}
 * payloads. This allows you to manually specify other Source and Result
 * types, which is of particular interest with respect to the Result type e.g. for streaming
 * the Result to a file etc.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class SmooksTransformer extends AbstractEventAwareTransformer
{
	public static final String MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT = "SmooksExecutionContext";

	private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger( SmooksTransformer.class );

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

		//	set the JavaResult beanId if specified
		if ( resultType == ResultType.JAVA )
		{
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



	//	protected

	@Override
	public Object transform( final Object payload, String encoding, UMOEventContext umoEventContext ) throws TransformerException
	{
        //	Create Smooks ExecutionContext.
		ExecutionContext executionContext = smooks.createExecutionContext();

		//	Add smooks reporting if configured
		addReportingSupport( executionContext );

        //	Use the Smooks PayloadProcessor to execute the transformation....
        final Object transformedPayload = payloadProcessor.process( payload, executionContext );

        if(executionContextAsMessageProperty) {

        	//	Set the Smooks Excecution properties on the Mule Message object
        	umoEventContext.getMessage().setProperty( executionContextMessagePropertyKey, getSerializableObjectsMap( executionContext.getAttributes() ) );
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
			throw new InitialisationException( errorMsg, e);
		}
		catch ( final SAXException e)
		{
			final Message errorMsg = createStaticMessage( "SAXException while trying to get smooks instance: " );
			throw new InitialisationException( errorMsg, e );
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
	            throw new InitialisationException(errorMsg, e );
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