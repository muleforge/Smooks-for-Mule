package org.milyn.smooks.mule;

import java.util.Map;

import org.milyn.container.ExecutionContext;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.routing.outbound.AbstractOutboundRouter;


public class NamedOutboundEndpointMuleDispatcher implements NamedEndpointMuleDispatcher {

	private final Map<String, OutboundEndpoint> endpointMap;

	private boolean synchronous;

	private boolean honorSynchronicity;

	private boolean executionContextAsMessageProperty;

	private String executionContextMessagePropertyKey;

	private ExecutionContext executionContext;

	private MuleSession muleSession;

	private boolean excludeNonSerializables;

	private AbstractOutboundRouter router;


	public NamedOutboundEndpointMuleDispatcher(Map<String, OutboundEndpoint> endpointMap,
			AbstractOutboundRouter router, MuleSession muleSession,
			ExecutionContext executionContext,
			boolean executionContextAsMessageProperty,
			String executionContextMessagePropertyKey,
			boolean excludeNonSerializables, boolean honorSynchronicity,
			boolean synchronous) {

		this.endpointMap = endpointMap;
		this.router = router;
		this.muleSession = muleSession;
		this.executionContext = executionContext;
		this.executionContextAsMessageProperty = executionContextAsMessageProperty;
		this.executionContextMessagePropertyKey = executionContextMessagePropertyKey;
		this.excludeNonSerializables = excludeNonSerializables;
		this.honorSynchronicity = honorSynchronicity;
		this.synchronous = synchronous;
	}

	public boolean isSynchronous() {
		return synchronous;
	}

	public void setSynchronous(boolean synchronous) {
		this.synchronous = synchronous;
	}

	public boolean isHonorSynchronicity() {
		return honorSynchronicity;
	}

	public void setHonorSynchronicity(boolean honorSynchronicity) {
		this.honorSynchronicity = honorSynchronicity;
	}

	public boolean isExecutionContextAsMessageProperty() {
		return executionContextAsMessageProperty;
	}

	public void setExecutionContextAsMessageProperty(
			boolean executionContextAsMessageProperty) {
		this.executionContextAsMessageProperty = executionContextAsMessageProperty;
	}

	public String getExecutionContextMessagePropertyKey() {
		return executionContextMessagePropertyKey;
	}

	public void setExecutionContextMessagePropertyKey(
			String executionContextMessagePropertyKey) {
		this.executionContextMessagePropertyKey = executionContextMessagePropertyKey;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public MuleSession getMuleSession() {
		return muleSession;
	}

	public void setMuleSession(MuleSession muleSession) {
		this.muleSession = muleSession;
	}

	public boolean isExcludeNonSerializables() {
		return excludeNonSerializables;
	}

	public void setExcludeNonSerializables(boolean excludeNonSerializables) {
		this.excludeNonSerializables = excludeNonSerializables;
	}

	public AbstractOutboundRouter getRouter() {
		return router;
	}

	public void setRouter(AbstractOutboundRouter router) {
		this.router = router;
	}

	public Map<String, OutboundEndpoint> getEndpointMap() {
		return endpointMap;
	}

	public Object dispatch(String endpointName, Object payload, Map<?, ?> messageProperties, boolean forceSynchronous) {
		OutboundEndpoint outboundEndpoint = endpointMap.get(endpointName);

		if(outboundEndpoint == null) {
			throw new IllegalArgumentException("The outbound endpoint with the name '" + endpointName + "' isn't declared in the outbound endpoint map");
		}

		MuleMessage muleMessage;
		if(messageProperties == null || messageProperties.size() == 0) {
			muleMessage = new DefaultMuleMessage(payload);
		} else {
			muleMessage = new DefaultMuleMessage(payload, messageProperties);
		}

		MuleMessage resultMessage = dispatch(outboundEndpoint, muleMessage, forceSynchronous);

		Object result = null;
		if(resultMessage != null) {
			result = resultMessage.getPayload();
		}
		return result;
	}

	public MuleMessage dispatch(OutboundEndpoint endpoint, MuleMessage message, boolean forceSynchronous) {
		boolean synchr = synchronous || forceSynchronous;
		if (!forceSynchronous && honorSynchronicity)
        {
			synchr = endpoint.isSynchronous();
        }

		if(executionContextAsMessageProperty) {
        	// Set the Smooks Excecution properties on the Mule Message object
        	message.setProperty(executionContextMessagePropertyKey, ExecutionContextUtil.getSerializableObjectsMap(executionContext.getAttributes(), excludeNonSerializables) );
        }

		try {

			if (!forceSynchronous && honorSynchronicity)
            {
                message.setBooleanProperty(MuleProperties.MULE_REMOTE_SYNC_PROPERTY, endpoint.isRemoteSync());
            }

			if(synchr) {
				return router.send(muleSession, message, endpoint);
			} else {
				router.dispatch(muleSession, message, endpoint);
			}

		} catch (MuleException e) {

			//TODO: Fixme?
			throw new RuntimeException(new CouldNotRouteOutboundMessageException(message, endpoint, e));

		}
		return null;
	}

}
