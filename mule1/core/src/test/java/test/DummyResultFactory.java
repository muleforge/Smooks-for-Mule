package test;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.milyn.smooks.mule.ResultFactory;

public class DummyResultFactory implements ResultFactory {

	public Result createResult() {
		return new DOMResult();
	}

}
