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

import org.milyn.smooks.mule.SmooksRouter;
import org.milyn.smooks.mule.SmooksTransformer;
import org.mule.routing.outbound.DefaultOutboundRouterCollection;
import org.mule.tck.FunctionalTestCase;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class NamespaceTest extends FunctionalTestCase {

	@Override
	protected String getConfigResources() {
		return "namespace-mule-config.xml";
	}

	public void testTransformerConfig() throws Exception {

		SmooksTransformer t = (SmooksTransformer) muleContext.getRegistry().lookupTransformer("smooksTransformer");

		assert t != null;
		assert "/transformer-smooks-config.xml".equals(t.getConfigFile());
		assert "someProfile".equals(t.getProfile());
		assert "smooksProfile".equals(t.getProfileMessagePropertyKey());
		assert "smooks.executionContext".equals(t.getExecutionContextMessagePropertyKey());
		assert "javax.xml.transform.dom.DOMResult".equals(t.getResultClass());
		assert "test.DummyResultFactory".equals(t.getResultFactoryClass());
		assert "JAVA".equals(t.getResultType());
		assert "a".equals(t.getJavaResultBeanId());
        assert true == t.isExecutionContextAsMessageProperty();
        assert false == t.isExcludeNonSerializables();
        assert "target/smooks-report/report.html".equals(t.getReportPath());
    }

	public void testRouterConfig() throws Exception {

		DefaultOutboundRouterCollection routers = (DefaultOutboundRouterCollection) muleContext.getRegistry().lookupService("TestUMO").getOutboundMessageProcessor();


		SmooksRouter r = (SmooksRouter) routers.getRoutes().get(0);

		assert r != null;
		assert "someProfile".equals(r.getProfile());
		assert "smooksProfile".equals(r.getProfileMessagePropertyKey());
		assert "target/smooks-report/report.html".equals(r.getReportPath());
        assert "smooks.executionContext".equals(r.getExecutionContextMessagePropertyKey());
        assert true == r.isExecutionContextAsMessageProperty();
        assert false == r.isExcludeNonSerializables();
        assert "/router-smooks-config.xml".equals(r.getConfigFile());
	}

}
