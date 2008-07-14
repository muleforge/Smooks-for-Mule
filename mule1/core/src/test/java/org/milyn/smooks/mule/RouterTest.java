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

import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.lifecycle.InitialisationException;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 */
public class RouterTest extends AbstractMuleTestCase {

	public void testInitWithoutSmooksConfigFile()
	{
		boolean thrown = false;
		try {
			Router router = new Router();
			router.setConfigFile( null );
		} catch (IllegalStateException e) {
			if(e.getCause() instanceof InitialisationException) {
				thrown = true;
			}
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

}
