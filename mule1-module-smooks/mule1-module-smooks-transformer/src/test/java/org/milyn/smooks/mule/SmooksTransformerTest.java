package org.milyn.smooks.mule;


import java.io.File;
import java.io.IOException;

import org.milyn.io.StreamUtils;
import org.mule.impl.RequestContext;
import org.mule.tck.AbstractMuleTestCase;
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
public class SmooksTransformerTest extends AbstractMuleTestCase
{
	private SmooksTransformer smooksTransformer;

	private UMOEventContext eventContext;

	private final String smooksConfigFile = "/smooks-config.xml";

	public void testInitWithoutSmooksConfigFile() throws InitialisationException
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

	public void testIllegalResultType()
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

	public void testJavaResultBeanId()
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


	public void testDoTransformation() throws TransformerException
	{
		smooksTransformer.setSmooksConfigFile( smooksConfigFile );
		smooksTransformer.setExcludeNonSerializables( false );
		byte[] inputMessage = readInputMessage();
		Object transformedObject = smooksTransformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );
		Object attributes = eventContext.getMessage().getProperty( SmooksTransformer.EXECUTION_CONTEXT_ATTR_MAP_KEY );
		assertNotNull( attributes );
	}


	public void testDoTransformationWithSmooksReportGeneration() throws TransformerException, InitialisationException
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

	@Override
	protected void doSetUp() throws Exception
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

}
