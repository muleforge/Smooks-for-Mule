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

package org.milyn.smooks.mule.core;

import java.util.Map;

import junit.framework.TestCase;

import org.milyn.container.ExecutionContext;
import org.milyn.container.MockExecutionContext;
import org.milyn.smooks.mule.core.ExecutionContextUtil;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class ExecutionContextUtilTest extends TestCase {

	public void testGetAtrributesMap() {

		final String attrKey1 = "key1";
		final Object attrValue1 =  "value1";

		final String attrKey2 = "key2";
		final Object attrValue2 = new Object();

		ExecutionContext executionContext = new MockExecutionContext();
		executionContext.setAttribute(attrKey1, attrValue1);
		executionContext.setAttribute(attrKey2, attrValue2);
        executionContext.getBeanContext().addBean(attrKey1, attrValue1);
        executionContext.getBeanContext().addBean(attrKey2, attrValue2);

		Map<?, ?> attributesMap = ExecutionContextUtil.getAtrributesMap(executionContext, false);

		assertEquals("Not all values are in the map.", 3, attributesMap.size());
		assertSame("Value of key1 is not the same as the one we entered .", attrValue1, attributesMap.get(attrKey1));
		assertSame("Value of key2 is not the same as the one we entered .", attrValue2, attributesMap.get(attrKey2));

        Map<String, Object> beanContextMap = (Map<String, Object>) attributesMap.get(ExecutionContextUtil.BEAN_CONTEXT_KEY);

        assertSame("Bean context value of key1 is not the same as the one we entered .", attrValue1, beanContextMap.get(attrKey1));
        assertSame("Bean context value of key2 is not the same as the one we entered .", attrValue2, beanContextMap.get(attrKey2));

		attributesMap = ExecutionContextUtil.getAtrributesMap(executionContext, true);

		assertEquals("There is more or less then 2 values in the map.", 2, attributesMap.size());
		assertSame("Value of key1 is not the same as the one we entered .", attrValue1, attributesMap.get(attrKey1));

        beanContextMap = (Map<String, Object>) attributesMap.get(ExecutionContextUtil.BEAN_CONTEXT_KEY);

        assertSame("Bean context value of key1 is not the same as the one we entered .", attrValue1, beanContextMap.get(attrKey1));
        assertFalse("Bean context key2 should not be there.", beanContextMap.containsKey(attrKey2));

	}



	@SuppressWarnings("unchecked")
	public void testGetAtrributesMapIsImmutable() {

		boolean thrown = false;

		ExecutionContext executionContext = new MockExecutionContext();

		Map attributesMap = ExecutionContextUtil.getAtrributesMap(executionContext, false);

		try {

			attributesMap.put("someKey", "someValue");

		} catch(Exception e) {

			thrown = true;

		} finally {

			assertTrue("Exception not thrown", thrown);

		}

	}

}
