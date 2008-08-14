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

package example;

import java.util.Map;

import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class AComponent  implements Callable {

	private static final Logger log = LoggerFactory.getLogger(AComponent.class);

	/* (non-Javadoc)
	 * @see org.mule.umo.lifecycle.Callable#onCall(org.mule.umo.UMOEventContext)
	 */

	public Object onCall(UMOEventContext eventContext) throws Exception {

		UMOMessage message = eventContext.getMessage();

		@SuppressWarnings("unchecked")
		Map<String, String> payload = (Map<String, String>) message.getPayload();

		String id = payload.get("id");
		String value = payload.get("value");
		String idValue = id + "=" + value;

		log.info("Component A is called with the id and value: "+ idValue);

		return idValue;
	}

}
