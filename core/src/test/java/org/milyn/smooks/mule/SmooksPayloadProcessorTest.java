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

import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.same;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;

import junit.framework.TestCase;

import org.milyn.Smooks;
import org.milyn.container.MockExecutionContext;
import org.milyn.payload.StringResult;
import org.milyn.smooks.mule.core.ClassNameResultFactory;
import org.milyn.smooks.mule.core.GenericSourceResultFactory;
import org.milyn.smooks.mule.core.ResultType;
import org.milyn.smooks.mule.core.SmooksPayloadProcessor;
import org.milyn.smooks.mule.core.SourceResultFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class SmooksPayloadProcessorTest extends TestCase {

	private Smooks mockSmooks;


	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		mockSmooks = createMock(Smooks.class);
	}

	public void testProcessStringResult() {

		MockExecutionContext context =  new MockExecutionContext();

		mockSmooks.filterSource(same(context), isA(Source.class), isA(StringResult.class));

		replay(mockSmooks);

		SmooksPayloadProcessor payloadProcessor = new SmooksPayloadProcessor(mockSmooks, ResultType.STRING);

		payloadProcessor.process("payload",context);

		verify(mockSmooks);
	}

	public void testProcessDOMResult() {

		MockExecutionContext context =  new MockExecutionContext();

		mockSmooks.filterSource(same(context), isA(Source.class), isA(DOMResult.class));

		replay(mockSmooks);

		SourceResultFactory sourceResultFactory = new GenericSourceResultFactory(new ClassNameResultFactory(DOMResult.class));
		SmooksPayloadProcessor payloadProcessor = new SmooksPayloadProcessor(mockSmooks, ResultType.RESULT, sourceResultFactory);

		payloadProcessor.process("payload",context);

		verify(mockSmooks);
	}

}
