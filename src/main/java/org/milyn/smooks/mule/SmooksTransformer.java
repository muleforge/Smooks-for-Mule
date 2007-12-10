package org.milyn.smooks.mule;


import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.milyn.Smooks;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;
import org.xml.sax.SAXException;

/**
 *  SmooksTransformer indended to be used with the Mule ESB
 * 	<p>
 * 	Usage:
 *  Declare the tranformer in the Mule configuration file:
 *  <pre>
 *  &lt;transformers&gt;
 *       &lt;transformer name="SmooksTransformer" 
 *		className="org.milyn.smooks.mule.SmooksTransformer"/&gt;
 *   &lt;/transformers&gt;
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
        byte[] bytes = getBytesFromMessageObject( message );
        if ( bytes == null )
        	return null;
	        
        CharArrayWriter outputWriter = new CharArrayWriter();
        smooks.filter(new StreamSource(new ByteArrayInputStream(bytes), encoding), new StreamResult(outputWriter), smooks.createExecutionContext());
        
        return outputWriter.toString();
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
	
	/**
	 * 
	 * @param smooksResFile
	 */
	public void setSmooksConfigFile( final String smooksResFile )
	{
		this.smooksConfigFile = smooksResFile;
	}
	
	byte[] getBytesFromMessageObject( final Object object )
	{
		if ( object instanceof String )
			return ( (String) object).getBytes();
		else if ( object instanceof byte[] )
			return (byte[]) object;
		else
			return null;
	}
	
    public Object clone() throws CloneNotSupportedException
    {
    	SmooksTransformer smooksTransformer = new SmooksTransformer();
    	smooksTransformer.smooks = this.smooks;
		return smooksTransformer;
    }
	
	
}