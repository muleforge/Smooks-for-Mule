/*
 *  Copyright 2008 Maurice Zeijen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.milyn.smooks.mule.component;

import java.util.Date;
import java.util.Map;

import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.lifecycle.Callable;

public class ReplyComponent implements Callable{

	@SuppressWarnings("unchecked")
	public Object onCall(UMOEventContext eventContext) throws Exception {
		UMOMessage message = eventContext.getMessage();
		Map<String, Object> payload = (Map<String, Object>) message.getPayload();

		String test = (String) message.getProperty("test");
		String test2 = (String) message.getProperty("test2");
		Integer testInt = (Integer) message.getProperty("testInt");
		Date testDate = (Date) message.getProperty("testDate");
		String xmlTest1 = (String) message.getProperty("xmlTest1");
		String xmlTest2 = (String) message.getProperty("xmlTest2");
		String testOverwritten = (String) message.getProperty("testOverwritten");

		return payload.get("value").toString() + "," +
				test  + "," +
				test2 + "," +
				testInt + "," +
				testDate.getTime() + "," +
				xmlTest1 + "," +
				xmlTest2 + "," +
				testOverwritten;
	}

}
