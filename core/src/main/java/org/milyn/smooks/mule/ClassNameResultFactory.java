package org.milyn.smooks.mule;

import javax.xml.transform.Result;

import org.apache.commons.lang.ClassUtils;

public class ClassNameResultFactory implements ResultFactory {

	private final Class<Result> resultClass;



	public ClassNameResultFactory(Class<Result> resultClass) {
		this.resultClass = resultClass;
	}

	@SuppressWarnings("unchecked")
	public ClassNameResultFactory(String className) throws ClassNotFoundException {
		this.resultClass = ClassUtils.getClass(this.getClass().getClassLoader(), className);

		if(!Result.class.isAssignableFrom(resultClass)) {
			throw new IllegalArgumentException("The class '" + className + "' does not implement the 'javax.xml.transform.Result' interface.");
		}
	}

	public Result createResult(){

		try {
			return resultClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Couldn't create an instance of '" + resultClass.getName() + "'" , e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Couldn't create an instance of '" + resultClass.getName() + "'" , e);
		}
	}

}
