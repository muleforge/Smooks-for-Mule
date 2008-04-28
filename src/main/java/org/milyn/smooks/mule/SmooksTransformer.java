package org.milyn.smooks.mule;


import java.io.IOException;

import org.apache.log4j.Logger;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.container.plugin.PayloadProcessor;
import org.milyn.container.plugin.ResultType;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;
import org.xml.sax.SAXException;

/**
 *  SmooksTransformer indended to be used with the Mule ESB
 * 	</p>
 * 	Usage:
 *  Declare the tranformer in the Mule configuration file:
 *  <pre>
 *  &lt;transformers&gt;
 *       &lt;transformer name="SmooksTransformer" 
 *		className="org.milyn.smooks.mule.SmooksTransformer"/&gt;
 *   &lt;/transformers&gt;
 *   
 *  Declare the tranformer in the Mule configuration file:
 *  &lt;inbound-router&gt;
 *      &lt;endpoint address="stream://System.in"  transformers="SmooksTransformer"/&gt;
 *  &lt;/inbound-router&gt;
 *  </pre>
 * 	</p> 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>				
 *
 */
public class SmooksTransformer extends org.mule.transformers.AbstractTransformer
{
	private static final long serialVersionUID = 1L;
	
	private Logger log = Logger.getLogger( SmooksTransformer.class );
	
	/**
	 * Smooks payload processor 
	 */
	private PayloadProcessor payloadProcessor;
	
	/**
	 * Smooks instance
	 */
	private Smooks smooks;
	
	/**
	 * Filename for smooks configuration. Default is smooks-config.xml
	 */
    private String smooksConfigFile = "smooks-config.xml";

	@Override
	protected Object doTransform( Object message, String encoding ) throws TransformerException
	{
		ExecutionContext executionContext = smooks.createExecutionContext();
		return payloadProcessor.process( message , executionContext );
	}
	
	private final Smooks getSmooks() throws TransformerException
	{
		try
		{
			log.info("Using smooksConfigFile :" + smooksConfigFile );
			smooks = new Smooks( smooksConfigFile );
		} 
		catch (IOException e)
		{
			log.error( "IOException while trying to get smooks instance: ", e);
			throw new TransformerException( this, e );
		} catch (SAXException e)
		{
			log.error( "SAXException while trying to get smooks instance: ", e);
			throw new TransformerException( this, e );
		}
		return smooks;
	}
	
	public void initialise() throws InitialisationException
	{
		try
		{
			smooks = getSmooks();
			payloadProcessor = new PayloadProcessor( smooks, ResultType.STRING );
		} 
		catch (TransformerException e)
		{
			throw new InitialisationException( e, this );
		}
	}

	public String getSmooksConfigFile()
	{
		return smooksConfigFile;
	}
	
	public void setSmooksConfigFile( final String smooksResFile )
	{
		this.smooksConfigFile = smooksResFile;
	}
	
    public Object clone() throws CloneNotSupportedException
    {
    	SmooksTransformer smooksTransformer = new SmooksTransformer();
    	smooksTransformer.smooks = this.smooks;
		return smooksTransformer;
    }
	
	
}