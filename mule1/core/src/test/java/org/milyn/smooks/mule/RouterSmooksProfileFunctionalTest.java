/**
 * Copyright (C) 2008 Maurice Zeijen <maurice@zeijen.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.milyn.smooks.mule;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
import org.mule.tck.FunctionalTestCase;
import org.mule.util.FileUtils;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class RouterSmooksProfileFunctionalTest extends FunctionalTestCase {

	private final File routingTestDir = new File("target/routing-test");

	private final File test1File = new File(routingTestDir, "file1.dat");

	private final File test2File = new File(routingTestDir, "file2.dat");

	private final File test3File = new File(routingTestDir, "file3.dat");

	@Override
	protected String getConfigResources() {
		return "router-mule-config-profiled.xml";
	}

	@Test
	public void testRoutingUsingProfileInConfig() throws Exception
    {
		InputStream in = getClass().getResourceAsStream("/router-input-message.xml");

        MuleClient client = new MuleClient();
        client.send("vm://messageInput", new MuleMessage(in));

        assertFalse("File '" + test1File + "' exists.", test1File.exists());

        assertTrue("File '" + test2File + "' doesn't exist.", test2File.exists());

    }

	@Test
	public void testRoutingUsingProfileInMessage() throws Exception
    {
		InputStream in = getClass().getResourceAsStream("/router-input-message.xml");

		MuleMessage message = new MuleMessage(in);
		message.setStringProperty("smooksMessageProfile", "profile2");

        MuleClient client = new MuleClient();
        client.send("vm://messageInput", message);

        assertFalse("File '" + test1File + "' exists.", test1File.exists());

        assertTrue("File '" + test3File + "' doesn't exist.", test3File.exists());

    }


	/* (non-Javadoc)
	 * @see org.mule.tck.FunctionalTestCase#doPreFunctionalSetUp()
	 */
	@Override
	protected void doPreFunctionalSetUp() throws Exception {
		super.doPreFunctionalSetUp();

		FileUtils.deleteDirectory(routingTestDir);
	}

}
