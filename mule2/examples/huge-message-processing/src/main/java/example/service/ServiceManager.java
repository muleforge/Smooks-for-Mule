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

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class ServiceManager{

	private final Service[] services = {
			new DatabaseService(),
			new JMSService()
	};

	private final CountDownLatch waitForShutdown = new CountDownLatch(1);

	public static void main(String[] args) {
		new ServiceManager().run();
	}

	public void run() {

		start();

		Runtime.getRuntime().addShutdownHook(new ShutdownHook());

		while(waitForShutdown.getCount() != 0) {
			try {
				waitForShutdown.await();
			} catch (InterruptedException e) {
			}
		}

		stop();

	}

	private void start() {

		for(Service service : services) {
			service.start();
		}


	}

	private void stop() {

		for(int i = services.length-1; 0 <= i; i--) {
			services[i].stop();
		}

	}

	class ShutdownHook extends Thread {
	    @Override
		public void run() {
	    	waitForShutdown.countDown();
	    }
	}

}
