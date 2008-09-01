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
package example.hmp.consumer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.hmp.util.Application;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public abstract class JMSConsumer extends Application {

	private static final Logger logger = LoggerFactory.getLogger(JMSConsumer.class.getName());

	private Connection connection;

	private Session session;

	private String connectionFactoryName;

	private String queueName;

	private MessageListener messageListener;

	/**
	 * @param connectionFactory
	 * @param queue
	 */
	public JMSConsumer(String connectionFactoryName, String queueName, MessageListener messageListener)  {
		this.connectionFactoryName = connectionFactoryName;
		this.queueName = queueName;
		this.messageListener = messageListener;
	}

	public void start() throws Exception{

		logger.trace("Creating initial context");
		Context ctx = new InitialContext();

		logger.trace("Looking up connection factory");
		ConnectionFactory cf = (ConnectionFactory) ctx.lookup(connectionFactoryName);

		logger.trace("Creating connection");
		connection = cf.createConnection();

		logger.trace("Creating session");
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		logger.trace("Looking up queue");
		Queue queue = (Queue)ctx.lookup(queueName);

		logger.trace("Creating consumer");
		MessageConsumer consumer = session.createConsumer(queue);
		consumer.setMessageListener(messageListener);

		logger.trace("Starting connection");
		connection.start();
	}

	public void stop() {
		if(connection != null) {
			try {
				connection.stop();
			} catch (JMSException e) {
				logger.info("Couldn't stop the jms connection");
			}
		}
		if(session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				logger.info("Couldn't stop the jms session");
			}
		}
		if(connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				logger.info("Couldn't close the jms connection");
			}
		}
	}

}
