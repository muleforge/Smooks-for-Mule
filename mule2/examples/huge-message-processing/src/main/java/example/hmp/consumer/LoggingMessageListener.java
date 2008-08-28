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

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class LoggingMessageListener implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(LoggingMessageListener.class.getName());

	public void onMessage(Message message) {
		try {
			String text;
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;

				text = textMessage.getText();
			} else if (message instanceof ObjectMessage) {
				ObjectMessage objectMessage = (ObjectMessage) message;

				text = objectMessage.getObject().toString();
			} else {
				text = message.toString();
			}

			logger.info("Received Message:\n" + text.trim());

		} catch (Throwable e) {
			if (logger.isErrorEnabled()) {
				logger.error("Exception thrown while receiving jms message", e);
			}
		}
	}
}
