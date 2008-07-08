package org.milyn.smooks.mule;

import java.io.IOException;

import org.milyn.SmooksException;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
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

	private MuleOutboundEndpointDispatcher dispatcher;

	@AppContext
	private ApplicationContext applicationContext;

	@Initialize
	public void initialize() {

		dispatcher = (MuleOutboundEndpointDispatcher) applicationContext.getAttribute(MuleOutboundEndpointDispatcher.SMOOKS_APPCONTEXT_CONTEXT);

		if(dispatcher == null) {
			throw new IllegalStateException("The ApplicationContext doesn't have the Router class in the attribute with the key '" + MuleOutboundEndpointDispatcher.SMOOKS_APPCONTEXT_CONTEXT + "'");
		}
	}

	public void visitAfter(Element element, ExecutionContext executionContext)
			throws SmooksException {
		visitAfter(executionContext);
	}

	public void visitAfter(SAXElement element, ExecutionContext executionContext)
			throws SmooksException, IOException {
		visitAfter(executionContext);
	}

	private void visitAfter(ExecutionContext executionContext) {
		RouterSession routerSession = (RouterSession) executionContext.getAttribute(RouterSession.SMOOKS_EXECCONTEXT_CONTEXT);

		Object payload = BeanAccessor.getBean(executionContext, beanId);

		dispatcher.dispatch(executionContext, endpointName, payload, routerSession);
	}

}
