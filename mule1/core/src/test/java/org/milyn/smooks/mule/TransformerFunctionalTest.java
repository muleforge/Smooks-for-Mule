package org.milyn.smooks.mule;

import java.io.InputStream;

import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;

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
		return "mule-config.xml";
	}

	public void testSmooks() throws Exception
    {
		InputStream in = getClass().getResourceAsStream("/transformer-input-message.xml");

        MuleClient client = new MuleClient();
        UMOMessage reply = client.send("vm://messageInput", new MuleMessage(in));

        assertNotNull(reply);
        assertNotNull(reply.getPayload());
        assertFalse("The payload is a NullPayload", "<org.mule.providers.NullPayload></org.mule.providers.NullPayload>".equals(reply.getPayload()));

    }

}
