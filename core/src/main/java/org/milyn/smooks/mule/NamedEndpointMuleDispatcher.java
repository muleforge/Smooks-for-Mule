package org.milyn.smooks.mule;

import java.util.Map;


public interface NamedEndpointMuleDispatcher {

	static String SMOOKS_CONTEXT = NamedEndpointMuleDispatcher.class.getName() + "#CONTEXT";

	Object dispatch(String endpointName, Object payload, Map<?, ?> messageProperties, boolean forceSynchronous);

}
