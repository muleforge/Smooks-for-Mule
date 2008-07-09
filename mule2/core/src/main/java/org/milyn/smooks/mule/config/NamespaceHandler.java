package org.milyn.smooks.mule.config;

import org.milyn.smooks.mule.Router;
import org.milyn.smooks.mule.Transformer;
import org.mule.config.spring.parsers.specific.RouterDefinitionParser;
import org.mule.config.spring.parsers.specific.TransformerDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

    public void init()
    {
        registerBeanDefinitionParser("transformer", new TransformerDefinitionParser(Transformer.class));
        registerBeanDefinitionParser("router", new RouterDefinitionParser(Router.class));
    }
}
