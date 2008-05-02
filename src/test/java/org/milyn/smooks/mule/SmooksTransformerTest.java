package org.milyn.smooks.mule;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.milyn.io.StreamUtils;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for {@link SmooksTransformer}
 * <p/>
 * The test in the class intentionally only test the configuration and <br>
 * execution of {@link SmooksTransformer} and not the actual tranformations<br>
 * that Smooks performs as these are covered in the Smooks project.
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>				
 *
 */
public class SmooksTransformerTest 
{
	private static Logger log = LoggerFactory.getLogger( SmooksTransformerTest.class );
	
	private SmooksTransformer smooksTransformer;
	
	private final String smooksConfigFile = "smooks-config.xml";
	
	@Test
	public void getSmooksConfigFile() throws TransformerException
	{
		smooksTransformer.setSmooksConfig( smooksConfigFile );
		assertEquals( smooksConfigFile, smooksTransformer.getSmooksConfig() );
	}
	
	@Test ( expected = InitialisationException.class )
	public void illegalResultType() throws TransformerException, InitialisationException
	{
		smooksTransformer.setSmooksConfig( smooksConfigFile );
		smooksTransformer.setResultType( "badResultType" );
		smooksTransformer.initialise();
	}
	
	@Test
	public void doTransformation() throws TransformerException
	{
		smooksTransformer.setSmooksConfig( smooksConfigFile );
		byte[] inputMessage = readInputMessage();
		Object transformedObject = smooksTransformer.doTransform( inputMessage, "UTF-8" );
		assertNotNull ( transformedObject );
	}
	
	@Test
	public void doTransformationWithSmooksReportGeneration() throws TransformerException, InitialisationException
	{
		File reportFile = new File ( "target" + File.separator + "smooks-report.html" );
		smooksTransformer.setSmooksConfig( smooksConfigFile );
		smooksTransformer.setReportPath( reportFile.getAbsolutePath() );
		smooksTransformer.initialise();
		byte[] inputMessage = readInputMessage();
		try
		{
    		Object transformedObject = smooksTransformer.doTransform( inputMessage, "UTF-8" );
    		assertNotNull ( transformedObject );
			assertTrue( reportFile.exists() );
		}
		finally
		{
			if ( reportFile.exists() )
			{
				reportFile.delete();
			}
		}
	}
	
	@Before
	public void setUp() throws InitialisationException
	{
    	smooksTransformer = new SmooksTransformer();
		smooksTransformer.initialise();
	}
	
	//	private 
	
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
