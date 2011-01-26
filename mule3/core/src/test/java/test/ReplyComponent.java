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

package test;

import java.util.Date;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

/**
 * 
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class ReplyComponent implements Callable {


	@SuppressWarnings("unchecked")
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		Map<String, Object> payload = (Map<String, Object>) message.getPayload();

		String test = (String) message.getInboundProperty("test");
		String test2 = (String) message.getInboundProperty("test2");
		Integer testInt = (Integer) message.getInboundProperty("testInt");
		Date testDate = (Date) message.getInboundProperty("testDate");
		String xmlTest1 = (String) message.getInboundProperty("xmlTest1");
		String xmlTest2 = (String) message.getInboundProperty("xmlTest2");
		String testOverwritten = (String) message.getInboundProperty("testOverwritten");
        String correlationId = message.getCorrelationId();

		return payload.get("value").toString() + "," +
				test  + "," +
				test2 + "," +
				testInt + "," +
				testDate.getTime() + "," +
				xmlTest1 + "," +
				xmlTest2 + "," +
				testOverwritten + "," +
                correlationId;
	}

}
