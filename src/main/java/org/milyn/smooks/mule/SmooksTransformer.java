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
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * SmooksTransformer intended to be used with the Mule ESB.
 * <p/>
 * Usage: <br>
 * <pre>
 * Declare the tranformer in the Mule configuration file:
 * &lt;transformers&gt;
 *      &lt;transformer name="SmooksTransformer" className="org.milyn.smooks.mule.SmooksTransformer"/&gt;
 * &lt;/transformers&gt;
 *   
 * Declare the tranformer in the Mule configuration file:
 * &lt;inbound-router&gt;
 *     &lt;endpoint address="stream://System.in"  transformers="SmooksTransformer"/&gt;
 * &lt;/inbound-router&gt;
 *  
 * Optional properties:
 * &lt;property name="smooksConfig" value="smooks-config.xml" /&gt;
 * &lt;property name="resultType" value="STRING" /&gt;
 * </pre>
 * 
 * Description of configuration properties:
 * <ul>
 * <li><i>smooksConfig</i> - the Smooks configuration file. Can be a path on the file system or on the classpath.
 * <li><i>resultType</i> - type of result expected from Smooks ("STRING", "BYTES", "JAVA", "NORESULT"). Default is "STRING".
 * <li><i>excludeNonSerializables</i> - if true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
 * <li><i>reportPath</i> - specifies the path and file name for generating a Smooks Execution Report.  This is a development tool.
 * </ul>
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>				
 *
 */
public class SmooksTransformer extends org.mule.transformers.AbstractTransformer
{
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
    private String smooksConfigFile = "smooks-config.xml";

    /*
     * If true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
     */
	private boolean excludeNonSerializables = true;

	/*
	 * Path where the Smooks Report will be generated.
	 */
	private String reportPath;

    //	public
	
	public void initialise() throws InitialisationException
	{
		ResultType resultType = getResultType();
		smooks = createSmooksInstance();
		payloadProcessor = new PayloadProcessor( smooks, resultType );
	}

	public String getSmooksConfig()
	{
		return smooksConfigFile;
	}
	
	public void setSmooksConfig( final String smooksResFile )
	{
		this.smooksConfigFile = smooksResFile;
	}
	
    public Object clone() throws CloneNotSupportedException
    {
    	return this;
    }

	public void setResultType( final String resultType )
	{
		this.resultType = resultType;
	}
	
    /**
     * @param excludeNonSerializables  - If true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
     */
	public void setExcludeNonSerializables( boolean excludeNonSerializables )
	{
		this.excludeNonSerializables = excludeNonSerializables;
	}
	
	//	protected
	
	@Override
	protected Object doTransform( Object message, String encoding ) throws TransformerException
	{
        //	Create Smooks ExecutionContext.
		ExecutionContext executionContext = createExecutionContext( smooks );
		
		//	add smooks reporting if configured
		addReportingSupport( executionContext );
		
        //	Use the Smooks PayloadProcessor to execute the transformation....	
        final Object newPayload = payloadProcessor.process( extractPayload( message), executionContext );
        
		return packagePayload( newPayload );
	}
	
	/*
     * 	Hook for subclasses to control how the execution context is created. 
     * 	Might be useful for Actions that use profiles for example. 
     */
	protected ExecutionContext createExecutionContext( final Smooks smooks )
	{
		return smooks.createExecutionContext();
	}
	
	/*
     * 	Hook for subclasses to extract the message payload in any way they see fit.
     */
    protected Object extractPayload( final Object object ) 
    {
    	return object;
    }
    
    /*
     * Hook for subclasses so they can control what manipulate the payload
     * if needed. 
     */
    protected Object packagePayload( final Object payload )
    {
    	return payload;
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
    	if ( !excludeNonSerializables )
    		return smooksAttribuesMap;
    	
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
            	log.info( "Using Smooks Reporting. Will generate smooks-report.html in directory: " + reportPath  + "Do not use in production evironment as this will have negative impact on performance!");
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