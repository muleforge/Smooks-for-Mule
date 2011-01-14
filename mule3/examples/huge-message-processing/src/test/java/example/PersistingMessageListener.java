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

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
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
public class PersistingMessageListener implements MessageListener{

	private static final Logger logger = LoggerFactory.getLogger(PersistingMessageListener.class.getName());

	private final String name;

	/**
	 * @param name
	 */
	public PersistingMessageListener(String name) {
		this.name = name;
	}

	private List<Message> messages = new ArrayList<Message>();

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message message) {

		logger.info("{} received a message", name);

		if(logger.isDebugEnabled() && message instanceof TextMessage) {
			try {
				logger.debug("Message content {}", ((TextMessage)message).getText());
			} catch (JMSException e) {
				logger.error("Tried to read the TextMessage but got an exception", e);
			}
		}
		if(logger.isDebugEnabled() && message instanceof ObjectMessage) {
			try {
				logger.debug("Message content {}", ((ObjectMessage)message).getObject());
			} catch (JMSException e) {
				logger.error("Tried to read the TextMessage but got an exception", e);
			}
		}

		messages.add(message);
	}

	/**
	 * @return the messages
	 */
	public List<Message> getMessages() {
		return messages;
	}

}
