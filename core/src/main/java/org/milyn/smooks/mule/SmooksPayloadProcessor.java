package org.milyn.smooks.mule;

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.container.plugin.PayloadProcessor;
import org.milyn.container.plugin.SourceResult;

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

	 public final Object process(final Object message_payload, final ExecutionContext executionContext) throws SmooksException {

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
