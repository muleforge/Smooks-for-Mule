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

package example.mule.service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class StockService {

	private static final Random random = new Random(System.currentTimeMillis());

	private static final Logger logger = LoggerFactory.getLogger(StockService.class);

	public Integer getStock(Long productId) {

		if(logger.isInfoEnabled()) {
			logger.info("Stock request for product '" + productId + "'");
		}

		Integer stock = random.nextInt(1000);

		if(logger.isInfoEnabled()) {
			logger.info("Stock for product '" + productId + "' is " + stock);
		}

		return stock;

	}

}
