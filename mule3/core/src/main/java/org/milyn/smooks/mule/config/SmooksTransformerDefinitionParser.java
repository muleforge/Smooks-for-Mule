package org.milyn.smooks.mule.config;

import org.milyn.smooks.mule.SmooksTransformer;
import org.mule.config.spring.parsers.specific.MessageProcessorDefinitionParser;

public class SmooksTransformerDefinitionParser extends MessageProcessorDefinitionParser {

	public SmooksTransformerDefinitionParser() {
		super(SmooksTransformer.class);
	}

}
