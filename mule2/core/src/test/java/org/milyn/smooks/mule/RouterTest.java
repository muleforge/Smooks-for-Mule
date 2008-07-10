package org.milyn.smooks.mule;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.tck.AbstractMuleTestCase;


public class RouterTest extends AbstractMuleTestCase {

	public void testInitWithoutSmooksConfigFile() throws InitialisationException
	{
		boolean thrown = false;
		try {
			Router router = new Router();
			router.setConfigFile( null );
			router.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

}
