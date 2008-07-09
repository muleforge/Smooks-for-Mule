package org.milyn.smooks.mule;

import org.milyn.container.plugin.ResultType;
import org.mule.tck.FunctionalTestCase;

public class NamespaceTest extends FunctionalTestCase {

	@Override
	protected String getConfigResources() {
		return "namespace-mule-config.xml";
	}

	public void testConfig() throws Exception
    {
		Transformer t = (Transformer) muleContext.getRegistry().lookupTransformer("smooksTransformer");

		assertNotNull(t);
		assertEquals("/transformer-smooks-config.xml", t.getConfigFile());
        assertEquals("target/smooks-report/report.html", t.getReportPath());
        assertEquals("smooks.executionContext", t.getExecutionContextMessagePropertyKey());
        assertEquals(ResultType.JAVA, t.getResultType());
        assertEquals("a", t.getJavaResultBeanId());
        assertTrue(t.isExecutionContextAsMessageProperty());
        assertFalse(t.isExcludeNonSerializables());

    }

}
