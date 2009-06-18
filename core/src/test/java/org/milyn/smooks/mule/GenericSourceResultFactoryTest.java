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

import static org.easymock.EasyMock.*;

import javax.xml.transform.Result;

import junit.framework.TestCase;

import org.milyn.container.plugin.SourceResult;
import org.milyn.smooks.mule.core.GenericSourceResultFactory;
import org.milyn.smooks.mule.core.ResultFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class GenericSourceResultFactoryTest extends TestCase {

	private ResultFactory mockResultFactory;

	private Result mockResult;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		mockResultFactory = createMock(ResultFactory.class);
		mockResult = createMock(Result.class);;
	}

	/**
	 *
	 */
	public void testCreateSourceResult() {

		expect(mockResultFactory.createResult()).andReturn(mockResult);

		replay(mockResultFactory, mockResult);

		String payload = "PayLoad";

		GenericSourceResultFactory factory = new GenericSourceResultFactory(mockResultFactory);

		SourceResult sourceResult = factory.createSourceResult(payload);

		verify(mockResultFactory, mockResult);

		assertNotNull(sourceResult);
		assertNotNull(sourceResult.getSource());
		assertNotNull(sourceResult.getResult());

		assertSame("The mock result wasn't set in the SourceResult", mockResult, sourceResult.getResult());
	}

}
