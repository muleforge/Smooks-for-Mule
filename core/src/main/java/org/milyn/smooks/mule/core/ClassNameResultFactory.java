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

package org.milyn.smooks.mule.core;

import javax.xml.transform.Result;

import org.apache.commons.lang.ClassUtils;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 */
public class ClassNameResultFactory implements ResultFactory {

	private final Class<? extends Result> resultClass;



	public ClassNameResultFactory(Class<? extends Result> resultClass) {
		this.resultClass = resultClass;
	}

	@SuppressWarnings("unchecked")
	public ClassNameResultFactory(String className) throws ClassNotFoundException {
		this.resultClass = ClassUtils.getClass(this.getClass().getClassLoader(), className);

		if(!Result.class.isAssignableFrom(resultClass)) {
			throw new IllegalArgumentException("The class '" + className + "' does not implement the 'javax.xml.transform.Result' interface.");
		}
	}

	public Result createResult(){

		try {
			return resultClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Couldn't create an instance of '" + resultClass.getName() + "'" , e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Couldn't create an instance of '" + resultClass.getName() + "'" , e);
		}
	}

}
