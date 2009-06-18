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

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.container.plugin.PayloadProcessor;
import org.milyn.container.plugin.SourceResult;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 */
public class SmooksPayloadProcessor {

	ResultType resultType;

	PayloadProcessor smooksPayloadProcessor;

	SourceResultFactory sourceResultFactory;

	public SmooksPayloadProcessor(final Smooks smooks, final ResultType resultType) {
		this.resultType = resultType;

		smooksPayloadProcessor = new PayloadProcessor(smooks, getSmooksResultType(resultType));
	}

	public SmooksPayloadProcessor(final Smooks smooks, final ResultType resultType, SourceResultFactory sourceResultFactory) {
		this(smooks, resultType);

		this.sourceResultFactory = sourceResultFactory;
	}


	protected org.milyn.container.plugin.ResultType getSmooksResultType(ResultType resultType) {
		org.milyn.container.plugin.ResultType smooksResultType = resultType.getSmooksResultType();
		if(smooksResultType == null) {
			smooksResultType = org.milyn.container.plugin.ResultType.NORESULT;
		}

		return smooksResultType;
	}

	public Object process(final Object message_payload, final ExecutionContext executionContext) throws SmooksException {

		 Object payload;
		 if(message_payload instanceof SourceResult == false && resultType == ResultType.RESULT) {
			 payload = sourceResultFactory.createSourceResult(message_payload);
		 } else {
			 payload = message_payload;
		 }

		 return smooksPayloadProcessor.process(payload, executionContext);
	 }

	public void setJavaResultBeanId(final String javaResultBeanId) {
		smooksPayloadProcessor.setJavaResultBeanId(javaResultBeanId);
	}

}
