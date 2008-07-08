package org.milyn.smooks.mule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.milyn.container.ExecutionContext;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.endpoint.OutboundEndpoint;


public abstract class AbstractMuleOutboundEndpointDispatcher implements MuleOutboundEndpointDispatcher {

	private final Map<String, OutboundEndpoint> endpointMap;

	public AbstractMuleOutboundEndpointDispatcher(List<OutboundEndpoint> endpointList) {

		endpointMap = new HashMap<String, OutboundEndpoint>();
		for(OutboundEndpoint endpoint : endpointList) {
			String name = endpoint.getName();

			if(StringUtils.isEmpty(name)) {
				throw new IllegalArgumentException("The outbound endpoint list may only contain endpoints which have a name");
			}

			endpointMap.put(name, endpoint);
		}
	}

	public AbstractMuleOutboundEndpointDispatcher(Map<String, OutboundEndpoint> endpointMap) {

		this.endpointMap = endpointMap;
	}

	public void dispatch(ExecutionContext executionContext, String endpointName, Object payload, RouterSession routerSession) {
		OutboundEndpoint outboundEndpoint = endpointMap.get(endpointName);

		if(outboundEndpoint == null) {
			throw new IllegalArgumentException("The outbound endpoint with the name '" + endpointName + "' isn't declared in the outbound endpoint map");
		}

		MuleSession muleSession = ((RouterSessionImpl)routerSession).getMuleSession();

		MuleMessage muleMessage = new DefaultMuleMessage(payload);

		dispatch(executionContext, muleSession, muleMessage, outboundEndpoint);
	}

	public abstract void dispatch(ExecutionContext executionContext, MuleSession session, MuleMessage message, OutboundEndpoint endpoint);
}
