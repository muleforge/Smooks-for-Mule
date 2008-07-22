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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class AComponent  {

	private static final Logger log = LoggerFactory.getLogger(AComponent.class);

	/**
	 * Receives the Map payload and log's the contents of it.
	 *
	 * @param payload
	 * @return
	 * @throws Exception
	 */
	public Object onCall(Map<String, String> payload) throws Exception {
		String id = payload.get("id");
		String value = payload.get("value");
		String idValue = id + "=" + value;

		log.info("Component A is called with the id and value: "+ idValue);

		return idValue;
	}

}
