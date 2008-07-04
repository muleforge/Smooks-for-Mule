package org.milyn.smooks.mule;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.milyn.io.StreamUtils;
import org.mule.impl.RequestContext;
import org.mule.tck.MuleTestUtils;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

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
	private SmooksTransformer smooksTransformer;

	private UMOEventContext eventContext;

	private final String smooksConfigFile = "smooks-config.xml";

	@Test ( expected = InitialisationException.class )
	public void initWithoutSmooksConfigFile() throws TransformerException, InitialisationException
	{
		smooksTransformer.setSmooksConfigFile( null );
		smooksTransformer.initialise();
	}

	@Test ( expected = InitialisationException.class )
	public void illegalResultType() throws TransformerException, InitialisationException
	{
		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
		smooksTransformer.setResultType( "badResultType" );
		smooksTransformer.initialise();
	}

	@Test
	public void javaResultBeanId() throws TransformerException
	{
		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
		smooksTransformer.setResultType( "JAVA" );
		smooksTransformer.setJavaResultBeanId( "beanId" );
		try
		{
			smooksTransformer.initialise();
		}
		catch (InitialisationException e)
		{
			fail( "Should not have thrown A InitializationException");
		}
	}

	@Test
	public void doTransformation() throws TransformerException
	{
		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
		smooksTransformer.setExcludeNonSerializables( false );
		byte[] inputMessage = readInputMessage();
		Object transformedObject = smooksTransformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );
		Object attributes = eventContext.getMessage().getProperty( SmooksTransformer.EXECUTION_CONTEXT_ATTR_MAP_KEY );
		assertNotNull( attributes );
	}

	@Test
	public void doTransformationWithSmooksReportGeneration() throws TransformerException, InitialisationException
	{
		File reportFile = new File ( "target" + File.separator + "smooks-report.html" );
		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
		smooksTransformer.setReportPath( reportFile.getAbsolutePath() );
		smooksTransformer.initialise();
		byte[] inputMessage = readInputMessage();
		try
		{
    		Object transformedObject = smooksTransformer.transform( inputMessage, "UTF-8", eventContext );
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
	public void setUp() throws Exception
	{
    	smooksTransformer = new SmooksTransformer();
		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
		smooksTransformer.initialise();
		RequestContext.setEvent( getTestEvent ( "Test!" ) );
		eventContext = RequestContext.getEventContext();
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
        	e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }

	private static UMOEvent getTestEvent(Object data) throws Exception
    {
        return MuleTestUtils.getTestEvent(data);
    }

}
