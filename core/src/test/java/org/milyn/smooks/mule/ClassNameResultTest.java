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

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import junit.framework.TestCase;

import org.milyn.smooks.mule.core.ClassNameResultFactory;
import org.milyn.smooks.mule.core.ResultFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class ClassNameResultTest extends TestCase {


	public void testCreateResultFromClass() {

		ResultFactory resultFactory = new ClassNameResultFactory(DOMResult.class);
		Result result = resultFactory.createResult();

		assertNotNull("Result object not created", result != null);
		assertTrue("Result not an instance of DOMResult", result instanceof DOMResult);

	}

	public void testCreateResultFromClassName() throws ClassNotFoundException {

		ResultFactory resultFactory = new ClassNameResultFactory(DOMResult.class.getName());
		Result result = resultFactory.createResult();

		assertNotNull("Result object not created", result != null);
		assertTrue("Result not an instance of DOMResult", result instanceof DOMResult);

	}


}
