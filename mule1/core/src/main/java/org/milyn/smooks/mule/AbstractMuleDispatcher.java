package org.milyn.smooks.mule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mule.impl.MuleMessage;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;



public abstract class AbstractMuleDispatcher implements MuleDispatcher {

	private final Map<String, UMOEndpoint> endpointMap;

	public AbstractMuleDispatcher(List<UMOEndpoint> endpointList) {

		endpointMap = new HashMap<String, UMOEndpoint>();
		for(UMOEndpoint endpoint : endpointList) {
			String name = endpoint.getName();

			if(StringUtils.isEmpty(name)) {
				throw new IllegalArgumentException("The outbound endpoint list may only contain endpoints which have a name");
			}

			endpointMap.put(name, endpoint);
		}
	}

	public AbstractMuleDispatcher(Map<String, UMOEndpoint> endpointMap) {

		this.endpointMap = endpointMap;
	}

	public void dispatch(String endpointName, Object payload) {
		UMOEndpoint outboundEndpoint = endpointMap.get(endpointName);

		if(outboundEndpoint == null) {
			throw new IllegalArgumentException("The outbound endpoint with the name '" + endpointName + "' isn't declared in the outbound endpoint map");
		}

		UMOMessage muleMessage = new MuleMessage(payload);

		dispatch(outboundEndpoint, muleMessage);
	}

	public abstract void dispatch(UMOEndpoint endpoint, UMOMessage message);
}
