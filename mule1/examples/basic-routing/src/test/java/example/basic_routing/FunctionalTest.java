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

package example.basic_routing;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
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
		client.send("vm://BasicRouting", new MuleMessage(in));

		assertTrue("The report file wasn't created", getReportFile().exists());
	}


	private File getReportFile() {
		return new File("target/smooks-report/report.html");
	}

	private void deleteReportFile() {
		getReportFile().delete();
	}

	/* (non-Javadoc)
	 * @see org.mule.tck.FunctionalTestCase#doPreFunctionalSetUp()
	 */
	@Override
	protected void doPreFunctionalSetUp() throws Exception {
		deleteReportFile();
	}

	/* (non-Javadoc)
	 * @see org.mule.tck.FunctionalTestCase#doFunctionalTearDown()
	 */
	@Override
	protected void doFunctionalTearDown() throws Exception {
		super.doFunctionalTearDown();

		deleteReportFile();
	}

}
