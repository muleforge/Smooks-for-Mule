package org.milyn.smooks.mule;


import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.milyn.io.StreamUtils;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.TransformerException;
import org.mule.tck.AbstractMuleTestCase;


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
public class SmooksTransformerTest extends AbstractMuleTestCase
{
	private SmooksTransformer smooksTransformer;

	private MuleEventContext eventContext;

	private final String smooksConfigFile = "/smooks-config.xml";

	public void test_initWithoutSmooksConfigFile() throws InitialisationException
	{
		boolean thrown = false;
		try {
			smooksTransformer.setSmooksConfigFile( null );
			smooksTransformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

	public void test_illegalResultType()
	{
		boolean thrown = false;
		try {
			smooksTransformer.setSmooksConfigFile( smooksConfigFile );
			smooksTransformer.setResultType( "badResultType" );
			smooksTransformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

	public void test_javaResultBeanId()
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


	public void test_doTransformation() throws TransformerException
	{

		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
		smooksTransformer.setExcludeNonSerializables( false );
		byte[] inputMessage = readInputMessage();

		//Is this correct?
		MuleMessage message = new DefaultMuleMessage(inputMessage);
		Object transformedObject = smooksTransformer.transform( message );
		assertNotNull ( transformedObject );

		Object attributes = message.getProperty( SmooksTransformer.EXECUTION_CONTEXT_ATTR_MAP_KEY );
		assertNotNull( attributes );
	}

	public void test_doTransformationWithSmooksReportGeneration() throws InitialisationException, TransformerException
	{
		File reportFile = new File ( "target" + File.separator + "smooks-report.html" );
		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
		smooksTransformer.setReportPath( reportFile.getAbsolutePath() );
		smooksTransformer.initialise();
		byte[] inputMessage = readInputMessage();
		try
		{
    		Object transformedObject = smooksTransformer.transform( inputMessage );
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

	@Override
	@Before
	public void doSetUp() throws Exception
	{
    	smooksTransformer = new SmooksTransformer();
		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
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
        	e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }
}
