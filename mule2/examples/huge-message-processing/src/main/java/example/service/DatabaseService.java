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

import org.milyn.util.HsqlServer;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class DatabaseService implements Service{

	private HsqlServer hsqlServer;

	/* (non-Javadoc)
	 * @see example.service.Service#start()
	 */
	public void start() {
		try {
			hsqlServer = new HsqlServer(9992);
			hsqlServer.execScript(getClass().getResourceAsStream("/init.sql"));
		} catch (Exception e) {
			throw new RuntimeException("Could not start HsqlServer", e);
		}

	}

	/* (non-Javadoc)
	 * @see example.service.Service#stop()
	 */
	public void stop() {
		try {
			hsqlServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
