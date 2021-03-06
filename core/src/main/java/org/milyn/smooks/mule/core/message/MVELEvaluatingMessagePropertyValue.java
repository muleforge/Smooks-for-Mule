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

package org.milyn.smooks.mule.core.message;

import java.util.Map;

import org.milyn.container.ExecutionContext;
import org.milyn.expression.MVELExpressionEvaluator;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MVELEvaluatingMessagePropertyValue implements MessagePropertyValue {

	private final MVELExpressionEvaluator evaluator;

	/**
	 * @param value
	 */
	public MVELEvaluatingMessagePropertyValue(MVELExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	/* (non-Javadoc)
	 * @see org.milyn.smooks.mule.MessagePropertyValue#getValue()
	 */
	public Object getValue(ExecutionContext executionContext) {
		Map<?, ?> beanMap = executionContext.getBeanContext().getBeanMap();
		return evaluator.getValue(beanMap);
	}

}
