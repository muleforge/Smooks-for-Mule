package org.milyn.smooks.mule;

import java.io.InputStream;

import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;

/**
 * Unit test for {@link Transformer} to see if it works
 * within the Mule environment.
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class TransformerFunctionalTest extends FunctionalTestCase
{
	@Override
	protected String getConfigResources() {
		return "transformer-mule-config.xml";
	}

	@Test
	public void testSmooks() throws Exception
    {
		InputStream in = getClass().getResourceAsStream("/transformer-input-message.xml");

        MuleClient client = new MuleClient();
        MuleMessage reply = client.send("vm://messageInput", new DefaultMuleMessage(in));

        assertNotNull(reply);
        assertNotNull(reply.getPayload());
        assertFalse("The payload is a NullPayload", "<org.mule.transport.NullPayload></org.mule.transport.NullPayload>".equals(reply.getPayload()));

    }

}
