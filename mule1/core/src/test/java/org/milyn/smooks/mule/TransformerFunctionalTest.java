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
		return "transformer-mule-config.xml";
	}

	public void testTransformer() throws Exception
    {
		InputStream in = getClass().getResourceAsStream("/transformer-input-message.xml");

        MuleClient client = new MuleClient();
        UMOMessage reply = client.send("vm://messageInput", new MuleMessage(in));

        assertNotNull(reply);
        assertNotNull(reply.getPayload());
        assertFalse("The payload is a NullPayload", "<org.mule.providers.NullPayload></org.mule.providers.NullPayload>".equals(reply.getPayload()));

    }

}
