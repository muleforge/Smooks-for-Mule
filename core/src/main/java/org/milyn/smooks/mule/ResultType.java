package org.milyn.smooks.mule;

public enum ResultType {
	STRING(org.milyn.container.plugin.ResultType.STRING),
    BYTES(org.milyn.container.plugin.ResultType.BYTES),
    JAVA(org.milyn.container.plugin.ResultType.JAVA),
    RESULT(null),
    NORESULT(org.milyn.container.plugin.ResultType.NORESULT);

    private org.milyn.container.plugin.ResultType smooksResultType;

    public org.milyn.container.plugin.ResultType getSmooksResultType() {
		return smooksResultType;
	}

	private ResultType(org.milyn.container.plugin.ResultType smooksResultType) {
    	this.smooksResultType = smooksResultType;
	}
}
