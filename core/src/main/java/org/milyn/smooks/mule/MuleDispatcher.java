package org.milyn.smooks.mule;


public interface MuleDispatcher {

	static String SMOOKS_CONTEXT = MuleDispatcher.class.getName() + "#CONTEXT";

	void dispatch(String endpointName, Object payload);

}
