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
package example.consumer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.util.Application;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class AllProductsConsumer extends JMSConsumer {

	private static final Logger logger = LoggerFactory.getLogger(AllProductsConsumer.class.getName());

	/**
	 *
	 */
	public AllProductsConsumer() {
		super("ConnectionFactory", "AllProducts", new LoggingMessageListener());
	}


	public static void main(String[] args) throws Exception {
		logger.info("Starting the \"All products consumer\"");
		logger.info("From command line, use [Ctrl]+[C] to stop the application");
		new AllProductsConsumer().run();
	}

}
