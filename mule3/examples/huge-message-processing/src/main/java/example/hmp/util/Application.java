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
package example.hmp.util;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public abstract class Application {


	private static final Logger logger = LoggerFactory.getLogger(Application.class.getName());

	private final CountDownLatch waitForShutdown = new CountDownLatch(1);

	public void run() {

		try {
			start();
		} catch (Exception e) {
			throw new RuntimeException("Couldn't start application", e);
		}

		Runtime.getRuntime().addShutdownHook(new LatchShutdownHook(waitForShutdown));

		while(waitForShutdown.getCount() != 0) {
			try {
				waitForShutdown.await();
			} catch (InterruptedException e) {
			}
		}

		try {
			stop();
		} catch (Exception e) {
			logger.error("The application wasn't stopped correctly", e);
		}

	}

	protected abstract void start() throws Exception;

	protected abstract void stop() throws Exception;

}
