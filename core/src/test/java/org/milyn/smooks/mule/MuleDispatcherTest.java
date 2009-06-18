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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.xml.transform.stream.StreamSource;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.payload.JavaResult;
import org.milyn.smooks.mule.core.NamedEndpointMuleDispatcher;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * TODO: Write Unit Tests for MuleDispatcher
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MuleDispatcherTest extends TestCase {

	NamedEndpointMuleDispatcher dispatcher;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		dispatcher = createMock(NamedEndpointMuleDispatcher.class);
	}

	public void test_no_payload_plain_config() throws IOException, SAXException {
		test_no_payload("/test-config-dispatcher-plain-01.xml");
	}

	public void test_no_payload_extended_config() throws IOException, SAXException {
		test_no_payload("/test-config-dispatcher-extended-01.xml");
	}

	private void test_no_payload(String config) throws IOException, SAXException {

		expect(dispatcher.dispatch(eq("endpoint"), isNull(), eq(new HashMap<String, Object>()), eq(false)))
			.andReturn(null);

		replay(dispatcher);

		Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        ExecutionContext execContext = smooks.createExecutionContext();
        execContext.setAttribute(NamedEndpointMuleDispatcher.SMOOKS_CONTEXT, dispatcher);

        smooks.filter(new StreamSource(getClass().getResourceAsStream("/test-data-01.xml")), null, execContext);

        verify(dispatcher);
    }

	public void test_with_payload_with_beanId_plain_config() throws IOException, SAXException {
		test_with_payload_with_beanId("/test-config-dispatcher-plain-02.xml");
	}

	public void test_with_payload_with_beanId_extended_config() throws IOException, SAXException {
		test_with_payload_with_beanId("/test-config-dispatcher-extended-02.xml");
	}

	private void test_with_payload_with_beanId(String config) throws IOException, SAXException {

		Object payload = new Object();

		expect(dispatcher.dispatch(eq("endpoint"), eq(payload), eq(new HashMap<String, Object>()), eq(false)))
			.andReturn(null);

		replay(dispatcher);

		Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
		ExecutionContext execContext = smooks.createExecutionContext();
		execContext.setAttribute(NamedEndpointMuleDispatcher.SMOOKS_CONTEXT, dispatcher);

		JavaResult result = new JavaResult();

		result.getResultMap().put("payload", payload);

        smooks.filter(new StreamSource(getClass().getResourceAsStream("/test-data-01.xml")), result, execContext);

        verify(dispatcher);
    }

	public void test_with_payload_with_expression_plain_config() throws IOException, SAXException {
		test_with_payload_with_expression("/test-config-dispatcher-plain-03.xml");
	}

	public void test_with_payload_with_expression_extended_config() throws IOException, SAXException {
		test_with_payload_with_expression("/test-config-dispatcher-extended-03.xml");
	}

	private void test_with_payload_with_expression(String config) throws IOException, SAXException {

		expect(dispatcher.dispatch(eq("endpoint"), eq("payload"), eq(new HashMap<String, Object>()), eq(false)))
			.andReturn(null);

		replay(dispatcher);

		Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
		ExecutionContext execContext = smooks.createExecutionContext();
		execContext.setAttribute(NamedEndpointMuleDispatcher.SMOOKS_CONTEXT, dispatcher);

        smooks.filter(new StreamSource(getClass().getResourceAsStream("/test-data-01.xml")), null, execContext);

        verify(dispatcher);
    }

	public void test_with_resultBeanId_plain_config() throws IOException, SAXException {
		test_with_resultBeanId("/test-config-dispatcher-plain-04.xml");
	}

	public void test_with_resultBeanId_extended_config() throws IOException, SAXException {
		test_with_resultBeanId("/test-config-dispatcher-extended-04.xml");
	}

	private void test_with_resultBeanId(String config) throws IOException, SAXException {

		Object result = new Object();

		expect(dispatcher.dispatch(eq("endpoint"), isNull(), eq(new HashMap<String, Object>()), eq(true)))
			.andReturn(result);

		replay(dispatcher);

		Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
		ExecutionContext execContext = smooks.createExecutionContext();
		execContext.setAttribute(NamedEndpointMuleDispatcher.SMOOKS_CONTEXT, dispatcher);

		JavaResult javaResult = new JavaResult();
        smooks.filter(new StreamSource(getClass().getResourceAsStream("/test-data-01.xml")), javaResult, execContext);

        verify(dispatcher);

        assertSame(result, javaResult.getBean("result"));

    }

	public void test_with_messageBeanProperties_plain_config() throws IOException, SAXException, ParseException {
		//test_with_messageBeanProperties("test-config-dispatcher-plain-05.xml");
	}

	public void test_with_messageBeanProperties_extended_config() throws IOException, SAXException, ParseException {
		test_with_messageBeanProperties("/test-config-dispatcher-extended-05.xml");
	}

	private void test_with_messageBeanProperties(String config) throws IOException, SAXException, ParseException {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("prop1", "prop1Value");
		properties.put("prop2", "prop2Value");
		properties.put("intProp", new Integer(10));
		properties.put("dateProp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-07-11 12:30:56"));
		properties.put("beanIdProp", "beanIdPropValue");

		Object result = new Object();

		expect(dispatcher.dispatch(eq("endpoint"), isNull(), eq(properties), eq(false)))
			.andReturn(result);

		replay(dispatcher);

		HashMap<String, Object> propertiesToInject = new HashMap<String, Object>();
		propertiesToInject.put("beanIdProp", "beanIdPropValue");

		Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
		ExecutionContext execContext = smooks.createExecutionContext();
		execContext.setAttribute(NamedEndpointMuleDispatcher.SMOOKS_CONTEXT, dispatcher);

		JavaResult javaResult = new JavaResult();
		javaResult.getResultMap().put("messageProperties", propertiesToInject);

        smooks.filter(new StreamSource(getClass().getResourceAsStream("/test-data-01.xml")), javaResult, execContext);

        verify(dispatcher);
    }
}
