package org.milyn.smooks.mule.config;

import org.milyn.smooks.mule.Router;
import org.milyn.smooks.mule.Transformer;
import org.mule.config.spring.parsers.generic.OrphanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

    public void init()
    {
        registerBeanDefinitionParser("transformer", new OrphanDefinitionParser(Transformer.class, true));
        registerBeanDefinitionParser("router", new OrphanDefinitionParser(Router.class, true));
    }
}
