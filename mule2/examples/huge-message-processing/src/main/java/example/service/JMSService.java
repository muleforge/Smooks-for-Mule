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

package example.service;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class JMSService implements Service {

	@SuppressWarnings("unused")
	private ConnectionFactory connectionFactory;

	/* (non-Javadoc)
	 * @see example.service.Service#start()
	 */
	public void start() {
		try {
			connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");

		} catch (Exception e) {
			throw new RuntimeException("Could not start the BrokerService");
		}
	}

	/* (non-Javadoc)
	 * @see example.service.Service#stop()
	 */
	public void stop() {
	}

}
