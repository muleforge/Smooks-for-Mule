package org.milyn.smooks.mule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;


public abstract class AbstractMuleDispatcher implements MuleDispatcher {

	private final Map<String, OutboundEndpoint> endpointMap;

	public AbstractMuleDispatcher(List<OutboundEndpoint> endpointList) {

		endpointMap = new HashMap<String, OutboundEndpoint>();
		for(OutboundEndpoint endpoint : endpointList) {
			String name = endpoint.getName();

			if(StringUtils.isEmpty(name)) {
				throw new IllegalArgumentException("The outbound endpoint list may only contain endpoints which have a name");
			}

			endpointMap.put(name, endpoint);
		}
	}

	public AbstractMuleDispatcher(Map<String, OutboundEndpoint> endpointMap) {

		this.endpointMap = endpointMap;
	}

	public void dispatch(String endpointName, Object payload) {
		OutboundEndpoint outboundEndpoint = endpointMap.get(endpointName);

		if(outboundEndpoint == null) {
			throw new IllegalArgumentException("The outbound endpoint with the name '" + endpointName + "' isn't declared in the outbound endpoint map");
		}

		MuleMessage muleMessage = new DefaultMuleMessage(payload);

		dispatch(outboundEndpoint, muleMessage);
	}

	public abstract void dispatch(OutboundEndpoint endpoint, MuleMessage message);
}
