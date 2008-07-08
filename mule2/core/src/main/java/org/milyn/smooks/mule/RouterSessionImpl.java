package org.milyn.smooks.mule;

import org.mule.api.MuleSession;

public class RouterSessionImpl implements RouterSession {

	private final MuleSession muleSession;

	public RouterSessionImpl(MuleSession muleSession) {
		this.muleSession = muleSession;
	}

	public MuleSession getMuleSession() {
		return this.muleSession;
	}

}
