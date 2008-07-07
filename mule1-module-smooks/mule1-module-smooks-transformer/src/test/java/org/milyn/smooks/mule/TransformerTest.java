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
 * Unit test for {@link Transformer}
 * <p/>
 * The test in the class intentionally only test the configuration and <br>
 * execution of {@link Transformer} and not the actual tranformations<br>
 * that Smooks performs as these are covered in the Smooks project.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class TransformerTest extends AbstractMuleTestCase
{
	private Transformer transformer;

	private UMOEventContext eventContext;

	private final String smooksConfigFile = "/smooks-config.xml";

	public void testInitWithoutSmooksConfigFile() throws InitialisationException
	{
		boolean thrown = false;
		try {
			transformer.setSmooksConfigFile( null );
			transformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

	public void testIllegalResultType()
	{
		boolean thrown = false;
		try {
			transformer.setSmooksConfigFile( smooksConfigFile );
			transformer.setResultType( "badResultType" );
			transformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

	public void testJavaResultBeanId()
	{
		transformer.setSmooksConfigFile( smooksConfigFile );
		transformer.setResultType( "JAVA" );
		transformer.setJavaResultBeanId( "beanId" );
		try
		{
			transformer.initialise();
		}
		catch (InitialisationException e)
		{
			fail( "Should not have thrown A InitializationException");
		}
	}


	public void testDoTransformation() throws TransformerException
	{
		testDoTransformation(null, null);
		testDoTransformation(false, null);
		testDoTransformation(true, null);
		testDoTransformation(true, "executionContextSmooks");
	}

	private void testDoTransformation(Boolean setExecuctionContextAsMessageKey, String executionContextMessagePropertyKey) throws TransformerException
	{
		transformer.setSmooksConfigFile( smooksConfigFile );
		transformer.setExcludeNonSerializables( false );
		if(setExecuctionContextAsMessageKey != null) {
			transformer.setExecutionContextAsMessageProperty(setExecuctionContextAsMessageKey);
		}
		if(executionContextMessagePropertyKey != null) {
			transformer.setExecutionContextMessagePropertyKey(executionContextMessagePropertyKey);
		} else {
			executionContextMessagePropertyKey = Transformer.MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT;
		}

		byte[] inputMessage = readInputMessage();
		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );

		Object attributes = eventContext.getMessage().getProperty( executionContextMessagePropertyKey );

		if(setExecuctionContextAsMessageKey != null && setExecuctionContextAsMessageKey) {
			assertNotNull( attributes );
		} else {
			assertNull( attributes );
		}
	}


	public void testDoTransformationWithSmooksReportGeneration() throws TransformerException, InitialisationException
	{
		File reportFile = new File ( "target" + File.separator + "smooks-report.html" );
		transformer.setSmooksConfigFile( smooksConfigFile );
		transformer.setReportPath( reportFile.getAbsolutePath() );
		transformer.initialise();
		byte[] inputMessage = readInputMessage();
		try
		{
    		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
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
    	transformer = new Transformer();
		transformer.setSmooksConfigFile( smooksConfigFile );
		transformer.initialise();
		RequestContext.setEvent( getTestEvent ( "Test!" ) );
		eventContext = RequestContext.getEventContext();
	}

	//	private

	private static byte[] readInputMessage()
	{
        try
        {
            return StreamUtils.readStream( TransformerTest.class.getResourceAsStream( "/input-message.xml"));
        }
        catch (IOException e)
        {
        	e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }

}
