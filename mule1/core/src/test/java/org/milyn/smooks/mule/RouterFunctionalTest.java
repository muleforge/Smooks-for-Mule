/**
 *
 */
package org.milyn.smooks.mule;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
import org.mule.tck.FunctionalTestCase;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class RouterFunctionalTest extends FunctionalTestCase {

	private final File routingTestDir = new File("target/routing-test");

	private final File test1File = new File(routingTestDir, "file1.dat");

	private final File[] test2Files = new File[] {
			new File(routingTestDir, "file2-0.dat"),
			new File(routingTestDir, "file2-1.dat"),
			new File(routingTestDir, "file2-2.dat"),
	};

	@Override
	protected String getConfigResources() {
		return "router-mule-config.xml";
	}

	@Test
	public void testSmooks() throws Exception
    {
		InputStream in = getClass().getResourceAsStream("/router-input-message.xml");

        MuleClient client = new MuleClient();
        client.send("vm://messageInput", new MuleMessage(in));

        assertTrue("File '" + test1File + "' doesn't exist.", test1File.exists());

        for(int i = 0; i < test2Files.length; i++) {
        	assertTrue("File '" + test2Files[i] + "' doesn't exist.", test2Files[i].exists());
        }
    }

	/* (non-Javadoc)
	 * @see org.mule.tck.FunctionalTestCase#doPreFunctionalSetUp()
	 */
	@Override
	protected void doPreFunctionalSetUp() throws Exception {
		super.doPreFunctionalSetUp();

		routingTestDir.delete();
	}

	/* (non-Javadoc)
	 * @see org.mule.tck.FunctionalTestCase#doFunctionalTearDown()
	 */
	@Override
	protected void doFunctionalTearDown() throws Exception {
		super.doFunctionalTearDown();

		routingTestDir.delete();
	}
}
