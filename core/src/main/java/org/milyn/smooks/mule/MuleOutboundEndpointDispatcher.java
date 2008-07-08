package org.milyn.smooks.mule;

import org.milyn.container.ExecutionContext;

public interface MuleOutboundEndpointDispatcher {

	static String SMOOKS_APPCONTEXT_CONTEXT = MuleOutboundEndpointDispatcher.class.getName() + "#CONTEXT";

	void dispatch(ExecutionContext executionContext, String endpointName, Object payload, RouterSession routerSession);

}
