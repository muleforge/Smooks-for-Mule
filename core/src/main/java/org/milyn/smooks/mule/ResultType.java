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

package org.milyn.smooks.mule;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public enum ResultType {
	STRING(org.milyn.container.plugin.ResultType.STRING),
    BYTES(org.milyn.container.plugin.ResultType.BYTES),
    JAVA(org.milyn.container.plugin.ResultType.JAVA),
    RESULT(null),
    NORESULT(org.milyn.container.plugin.ResultType.NORESULT);

    private org.milyn.container.plugin.ResultType smooksResultType;

    public org.milyn.container.plugin.ResultType getSmooksResultType() {
		return smooksResultType;
	}

	private ResultType(org.milyn.container.plugin.ResultType smooksResultType) {
    	this.smooksResultType = smooksResultType;
	}
}
