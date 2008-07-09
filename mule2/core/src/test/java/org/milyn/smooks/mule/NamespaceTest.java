package org.milyn.smooks.mule;

import org.mule.routing.outbound.DefaultOutboundRouterCollection;
import org.mule.tck.FunctionalTestCase;

public class NamespaceTest extends FunctionalTestCase {

	@Override
	protected String getConfigResources() {
		return "namespace-mule-config.xml";
	}

	public void testTransformerConfig() throws Exception {

		Transformer t = (Transformer) muleContext.getRegistry().lookupTransformer("smooksTransformer");

		assert t != null;
		assert "/transformer-smooks-config.xml".equals(t.getConfigFile());
		assert "target/smooks-report/report.html".equals(t.getReportPath());
		assert "smooks.executionContext".equals(t.getExecutionContextMessagePropertyKey());
		assert "JAVA".equals(t.getResultType());
		assert "a".equals(t.getJavaResultBeanId());
        assert true == t.isExecutionContextAsMessageProperty();
        assert false == t.isExcludeNonSerializables();

    }

	public void testRouterConfig() throws Exception {

		DefaultOutboundRouterCollection routers = (DefaultOutboundRouterCollection) muleContext.getRegistry().lookupService("TestUMO").getOutboundRouter();


		Router r = (Router) routers.getRouters().get(0);

		assert r != null;
		assert "/router-smooks-config.xml".equals(r.getConfigFile());
		assert "target/smooks-report/report.html".equals(r.getReportPath());
        assert "smooks.executionContext".equals(r.getExecutionContextMessagePropertyKey());
        assert true == r.isExecutionContextAsMessageProperty();
        assert false == r.isExcludeNonSerializables();
        assert true == r.isHonorSynchronicity();
	}

}
