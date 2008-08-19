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

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class JMSService implements Service {

	private static final Logger logger = LoggerFactory.getLogger(JMSService.class.getName());

	private BrokerService broker;

	/* (non-Javadoc)
	 * @see example.service.Service#start()
	 */
	public void start() {
		try {
			if (logger.isInfoEnabled()) {
				logger.info("creating JMS Broker Service");
			}

			//broker = BrokerFactory.createBroker(getClass().getClassLoader().getResource("activemq.xml").toURI());
			BrokerService broker = new BrokerService();
			broker.setPersistent(false);
			broker.addConnector("tcp://localhost:61616");
			broker.setUseShutdownHook(false);
			broker.start();
		} catch (Exception e) {
			throw new RuntimeException("Could not start the BrokerService");
		}
	}

	/* (non-Javadoc)
	 * @see example.service.Service#stop()
	 */
	public void stop() {
		try {
			broker.stop();
		} catch(Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("Exeption while trying to stop the JMS service broker", e);
			}
		}
	}

}
