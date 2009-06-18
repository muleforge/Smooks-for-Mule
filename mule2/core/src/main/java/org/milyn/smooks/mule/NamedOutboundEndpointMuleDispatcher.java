/**
 * Copyright (C) 2008 Maurice Zeijen <maurice@zeijen.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.milyn.smooks.mule;

import java.util.Map;

import org.milyn.container.ExecutionContext;
import org.milyn.smooks.mule.core.ExecutionContextUtil;
import org.milyn.smooks.mule.core.MuleDispatcher;
import org.milyn.smooks.mule.core.NamedEndpointMuleDispatcher;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.routing.outbound.AbstractOutboundRouter;

/**
 * This is the Mule 2.x implementation of the NamedEndpointMuleDispatcher. It
 * is used by the Smook {@link MuleDispatcher} to actually dispatch a message.
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 */
public class NamedOutboundEndpointMuleDispatcher implements NamedEndpointMuleDispatcher {

	private final Map<String, OutboundEndpoint> endpointMap;

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
			boolean excludeNonSerializables) {

		this.endpointMap = endpointMap;
		this.router = router;
		this.muleSession = muleSession;
		this.executionContext = executionContext;
		this.executionContextAsMessageProperty = executionContextAsMessageProperty;
		this.executionContextMessagePropertyKey = executionContextMessagePropertyKey;
		this.excludeNonSerializables = excludeNonSerializables;
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
		boolean synchr = endpoint.isSynchronous() || forceSynchronous;

		if(executionContextAsMessageProperty) {
        	// Set the Smooks Excecution properties on the Mule Message object
        	message.setProperty(executionContextMessagePropertyKey, ExecutionContextUtil.getAtrributesMap(executionContext, excludeNonSerializables) );
        }

		try {

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
