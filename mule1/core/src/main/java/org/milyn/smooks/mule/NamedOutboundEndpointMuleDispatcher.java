/*
 *  Copyright 2008 Maurice Zeijen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.milyn.smooks.mule;

import java.util.Map;

import org.milyn.container.ExecutionContext;
import org.mule.config.MuleProperties;
import org.mule.impl.MuleMessage;
import org.mule.routing.outbound.AbstractOutboundRouter;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOSession;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.routing.CouldNotRouteOutboundMessageException;



public class NamedOutboundEndpointMuleDispatcher implements NamedEndpointMuleDispatcher {

	private final Map<String, UMOEndpoint> endpointMap;

	private boolean synchronous;

	private boolean honorSynchronicity;

	private boolean executionContextAsMessageProperty;

	private String executionContextMessagePropertyKey;

	private ExecutionContext executionContext;

	private UMOSession muleSession;

	private boolean excludeNonSerializables;

	private AbstractOutboundRouter router;


	public NamedOutboundEndpointMuleDispatcher(Map<String, UMOEndpoint> endpointMap,
			AbstractOutboundRouter router, UMOSession muleSession,
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

	public UMOSession getMuleSession() {
		return muleSession;
	}

	public void setMuleSession(UMOSession muleSession) {
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

	public Map<String, UMOEndpoint> getEndpointMap() {
		return endpointMap;
	}

	public Object dispatch(String endpointName, Object payload, Map<?, ?> messageProperties, boolean forceSynchronous) {
		UMOEndpoint outboundEndpoint = endpointMap.get(endpointName);

		if(outboundEndpoint == null) {
			throw new IllegalArgumentException("The outbound endpoint with the name '" + endpointName + "' isn't declared in the outbound endpoint map");
		}

		UMOMessage muleMessage;
		if(messageProperties == null || messageProperties.size() == 0) {
			muleMessage = new MuleMessage(payload);
		} else {
			muleMessage = new MuleMessage(payload, messageProperties);
		}

		UMOMessage resultMessage = dispatch(outboundEndpoint, muleMessage, forceSynchronous);

		Object result = null;
		if(resultMessage != null) {
			result = resultMessage.getPayload();
		}
		return result;
	}

	public UMOMessage dispatch(UMOEndpoint endpoint, UMOMessage message, boolean forceSynchronous) {
		boolean synchr = synchronous || forceSynchronous;
		if (!forceSynchronous && honorSynchronicity)
        {
			synchr = endpoint.isSynchronous();
        }

		if(executionContextAsMessageProperty) {
        	// Set the Smooks Excecution properties on the Mule Message object
        	message.setProperty(executionContextMessagePropertyKey, ExecutionContextUtil.getSerializableObjectsMap(executionContext.getAttributes(), excludeNonSerializables));
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

		} catch (UMOException e) {

			//TODO: Fixme?
			throw new RuntimeException(new CouldNotRouteOutboundMessageException(message, endpoint, e));

		}
		return null;
	}



}
