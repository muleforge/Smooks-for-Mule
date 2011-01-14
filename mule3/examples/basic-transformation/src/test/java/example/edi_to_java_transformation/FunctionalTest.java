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

package example.edi_to_java_transformation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import org.milyn.io.StreamUtils;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.util.IOUtils;


/**
 * Unit test for this example
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class FunctionalTest extends FunctionalTestCase
{
	@Override
	protected String getConfigResources() {

		return "mule-config.xml";
	}


	@Test
	public void testSmooks() throws Exception {
		InputStream in = IOUtils.getResourceAsStream("test-message01.xml", this.getClass());

		MuleClient client = new MuleClient();
		MuleMessage reply = client.send("vm://BasicProcessor",	new DefaultMuleMessage(in));

		assertNotNull(reply);
		assertNotNull(reply.getPayload());

		Object payload = reply.getPayload();

		assertTrue("The message payload is not an instance of String", payload instanceof String);

		String result = (String) payload;

		byte[] expected = StreamUtils.readStream(IOUtils.getResourceAsStream("expected.xml", this.getClass()));

		assertTrue(StreamUtils.compareCharStreams(new ByteArrayInputStream(expected), new ByteArrayInputStream(result.getBytes())));

		assertTrue("The report file wasn't created", getReportFile().exists());
	}


	private File getReportFile() {
		return new File("target/smooks-report/report.html");
	}

	private void deleteReportFile() {
		getReportFile().delete();
	}


	/* (non-Javadoc)
	 * @see org.mule.tck.AbstractMuleTestCase#doSetUp()
	 */
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		Locale.setDefault(new Locale("en","IE"));
		deleteReportFile();
	}

	/* (non-Javadoc)
	 * @see org.mule.tck.AbstractMuleTestCase#doTearDown()
	 */
	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();

		deleteReportFile();
	}
}
