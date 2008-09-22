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

import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class PCsAndLaptopsProductsConsumer extends JMSConsumer {

	private static final Logger logger = LoggerFactory.getLogger(PCsAndLaptopsProductsConsumer.class.getName());

	/**
	 *
	 */
	public PCsAndLaptopsProductsConsumer() {
		super("ConnectionFactory", "PCsAndLaptops", new SystemOutMessageListener());
	}

	public PCsAndLaptopsProductsConsumer(MessageListener messageListener) {
		super("ConnectionFactory", "PCsAndLaptops", messageListener);
	}


	public static void main(String[] args) throws Exception {

		System.out.println("Starting the \"PC's and Laptops consumer\"");
		System.out.println("From command line, use [Ctrl]+[C] to stop the application");
		new PCsAndLaptopsProductsConsumer().run();
	}

}
