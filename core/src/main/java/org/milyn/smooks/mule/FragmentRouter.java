package org.milyn.smooks.mule;

import java.io.IOException;

import org.milyn.SmooksException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.javabean.BeanAccessor;
import org.w3c.dom.Element;

public class FragmentRouter implements DOMVisitAfter, SAXVisitAfter {

	@ConfigParam(use=Use.REQUIRED)
	private String endpointName;

	@ConfigParam(use=Use.REQUIRED)
	private String beanId;

	public void visitAfter(Element element, ExecutionContext executionContext)
			throws SmooksException {
		dispatch(executionContext);
	}

	public void visitAfter(SAXElement element, ExecutionContext executionContext)
			throws SmooksException, IOException {
		dispatch(executionContext);
	}

	private void dispatch(ExecutionContext executionContext) {
		Object payload = BeanAccessor.getBean(executionContext, beanId);

		MuleDispatcher dispatcher = (MuleDispatcher) executionContext.getAttribute(MuleDispatcher.SMOOKS_CONTEXT);

		if(dispatcher == null) {
			throw new IllegalStateException("The executionContext doesn't have the MuleDispatcher object as the attribute with the key '" + MuleDispatcher.SMOOKS_CONTEXT + "'");
		}

		dispatcher.dispatch(endpointName, payload);
	}

}
