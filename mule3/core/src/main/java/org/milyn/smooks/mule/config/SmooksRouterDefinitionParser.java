package org.milyn.smooks.mule.config;

import org.milyn.smooks.mule.SmooksRouter;
import org.mule.config.spring.parsers.specific.RouterDefinitionParser;

public class SmooksRouterDefinitionParser extends RouterDefinitionParser {

	public SmooksRouterDefinitionParser() {
		super(SmooksRouter.class);
	}

}
