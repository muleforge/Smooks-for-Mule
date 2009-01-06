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

package example;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jms.TextMessage;

import org.junit.Test;
import org.milyn.io.StreamUtils;
import org.mule.DefaultMuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.util.IOUtils;

import example.hmp.consumer.AllProductsConsumer;
import example.hmp.consumer.PCsAndLaptopsProductsConsumer;
import example.hmp.service.ServiceManager;

/**
 * Unit test for this example
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class FunctionalTest extends FunctionalTestCase
{
	private ServiceManager serviceManager;

	private AllProductsConsumer allProductsConsumer;

	private PersistingMessageListener allProductsConsumerListener;

	private PCsAndLaptopsProductsConsumer pcsAndLaptopsProductsConsumer;

	private PersistingMessageListener pcsAndLaptopsProductsConsumerListener;

	@Override
	protected String getConfigResources() {
		return "mule-config.xml";
	}


	@Test
	public void testSmooks() throws Exception {
		InputStream in = IOUtils.getResourceAsStream("test-message01.edi", this.getClass());

		MuleClient client = new MuleClient();
		client.send("vm://TestMessageIn",	new DefaultMuleMessage(in));

		//Sleep a second in the hope that all messages are processed.
		Thread.sleep(1000);

		assertEquals("Didn't receive exactly two messages in the allProductsConsumerListener", 2, allProductsConsumerListener.getMessages().size());

		assertTrue("First message in allProductsConsumerListener isn't a TextMessage", allProductsConsumerListener.getMessages().get(0) instanceof TextMessage);
		assertTrue("Second message in allProductsConsumerListener isn't a TextMessage", allProductsConsumerListener.getMessages().get(1) instanceof TextMessage);

		assertMessageContent("The message content of the first message isn't what is expected", "expected-allProductsConsumer-01.xml", (TextMessage) allProductsConsumerListener.getMessages().get(0));
		assertMessageContent("The message content of the second message isn't what is expected", "expected-allProductsConsumer-02.xml", (TextMessage) allProductsConsumerListener.getMessages().get(1));


		assertEquals("Didn't receive exactly one message in the pcsAndLaptopsProductsConsumerListener", 1, pcsAndLaptopsProductsConsumerListener.getMessages().size());

		assertTrue("First message in allProductsConsumerListener isn't a TextMessage", pcsAndLaptopsProductsConsumerListener.getMessages().get(0) instanceof TextMessage);

		assertMessageContent("The message content of the second message isn't what is expected", "expected-pcsAndLaptopsProductsConsumer.txt", (TextMessage) pcsAndLaptopsProductsConsumerListener.getMessages().get(0));
	}

	private void assertMessageContent(String description, String expectedFileName, TextMessage message) throws Exception {

		byte[] expected = StreamUtils.readStream(IOUtils.getResourceAsStream(expectedFileName, this.getClass()));

		assertTrue(description, StreamUtils.compareCharStreams(new ByteArrayInputStream(expected), new ByteArrayInputStream(message.getText().getBytes())));

	}

	@Override
	protected void suitePreSetUp() throws Exception {
		serviceManager = new ServiceManager();
		serviceManager.start();

		allProductsConsumerListener = new PersistingMessageListener("allProductsConsumerListener");

		allProductsConsumer = new AllProductsConsumer(allProductsConsumerListener);
		allProductsConsumer.start();

		pcsAndLaptopsProductsConsumerListener = new PersistingMessageListener("pcsAndLaptopsProductsConsumerListener");

		pcsAndLaptopsProductsConsumer = new PCsAndLaptopsProductsConsumer(pcsAndLaptopsProductsConsumerListener);
		pcsAndLaptopsProductsConsumer.start();

		super.suitePreSetUp();
	}


	@Override
	protected void suitePostTearDown() throws Exception {
		super.suitePostTearDown();

		pcsAndLaptopsProductsConsumer.stop();
		allProductsConsumer.stop();
		serviceManager.stop();
	}

}
