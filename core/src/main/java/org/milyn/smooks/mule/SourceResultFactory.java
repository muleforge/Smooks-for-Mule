package org.milyn.smooks.mule;

import org.milyn.container.plugin.SourceResult;

public interface SourceResultFactory {

	public abstract SourceResult createSourceResult(Object payload);

}