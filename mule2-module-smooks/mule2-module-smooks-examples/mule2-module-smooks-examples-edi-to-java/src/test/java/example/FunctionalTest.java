package example;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.milyn.smooks.mule.SmooksTransformer;
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
public class FunctionalTest extends FunctionalTestCase
{
	@Override
	protected String getConfigResources() {
		return "mule-edi-to-java-config.xml";
	}

	@Test
	public void testSmooks() throws Exception
    {
		InputStream in = new FileInputStream(new File("data/out/message01.edi"));

        MuleClient client = new MuleClient();
        MuleMessage reply = client.send("vm://messageInput", new DefaultMuleMessage(in));

        assertNotNull(reply);
        assertNotNull(reply.getPayload());
        assertFalse("The payload is a NullPayload", "<org.mule.transport.NullPayload></org.mule.transport.NullPayload>".equals(reply.getPayload()));

    }

}
