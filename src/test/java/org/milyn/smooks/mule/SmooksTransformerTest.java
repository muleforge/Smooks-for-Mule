package org.milyn.smooks.mule;


import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.milyn.io.StreamUtils;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

/**
 * Unit test for {@link SmooksTransformer}
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>				
 *
 */
public class SmooksTransformerTest extends TestCase
{
	private static Logger log = Logger.getLogger( SmooksTransformerTest.class );
	
	private final String smooksConfigFileName = "smooks-config.xml";
	
	private SmooksTransformer smooksTransformer = new SmooksTransformer();
	
	public void test_getSmooksConfigFile() throws TransformerException
	{
		smooksTransformer.setSmooksConfigFile( smooksConfigFileName );
		assertEquals( smooksConfigFileName, smooksTransformer.getSmooksConfigFile() );
	}
	
	public void test_doTransformation() throws TransformerException
	{
		smooksTransformer.setSmooksConfigFile( smooksConfigFileName );
		byte[] inputMessage = readInputMessage();
		Object transformedObject = smooksTransformer.doTransform( inputMessage, "UTF-8" );
		assertNotNull ( transformedObject );
	}
	
	public void setUp() throws InitialisationException
	{
		smooksTransformer.initialise();
	}
	
	private static byte[] readInputMessage() 
	{
        try 
        {
            return StreamUtils.readStream( SmooksTransformerTest.class.getResourceAsStream( "/input-message.xml"));
        } 
        catch (IOException e) 
        {
        	log.error( "IOException while trying to read input-message.xml", e );
            return "<no-message/>".getBytes();
        }
    }

}
