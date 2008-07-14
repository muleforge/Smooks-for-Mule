package org.milyn.smooks.mule;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.milyn.container.plugin.SourceFactory;
import org.milyn.container.plugin.SourceResult;

public class GenericSourceResultFactory implements SourceResultFactory {

	private final ResultFactory resultFactory;

	public GenericSourceResultFactory(ResultFactory resultFactory) {
		this.resultFactory = resultFactory;
	}

	/* (non-Javadoc)
	 * @see org.milyn.smooks.mule.SourceResultFactory#createSourceResult(java.lang.Object)
	 */
	public SourceResult createSourceResult(Object payload) {

		Source source = SourceFactory.getInstance().createSource(payload);
		Result result = resultFactory.createResult();

		return new SourceResult(source, result);

	}

}
