package org.milyn.smooks.mule;

import java.io.InputStream;

import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;

/**
 * Unit test for {@link SmooksTransformer}
 * <p/>
 * The test in the class intentionally only test the configuration and <br>
 * execution of {@link SmooksTransformer} and not the actual tranformations<br>
 * that Smooks performs as these are covered in the Smooks project.
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class SmooksTransformerFunctionalTest extends FunctionalTestCase
{
	@Override
	protected String getConfigResources() {
		return "org/milyn/smooks/mule/mule-config.xml";
	}

	@Test
	public void testSmooks() throws Exception
    {
		InputStream in = getClass().getResourceAsStream("input-message.xml");

        MuleClient client = new MuleClient();
        MuleMessage reply = client.send("vm://inputMessage", new DefaultMuleMessage(in));

        assertNotNull(reply);
        assertNotNull(reply.getPayload());

    }

}
