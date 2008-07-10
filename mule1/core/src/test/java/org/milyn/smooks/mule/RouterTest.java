package org.milyn.smooks.mule;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.lifecycle.InitialisationException;


public class RouterTest extends AbstractMuleTestCase {

	public void testInitWithoutSmooksConfigFile()
	{
		boolean thrown = false;
		try {
			Router router = new Router();
			router.setConfigFile( null );
		} catch (IllegalStateException e) {
			if(e.getCause() instanceof InitialisationException) {
				thrown = true;
			}
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

}
