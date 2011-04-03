package org.milyn.smooks.mule.core;

public class AttachmentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AttachmentException(String message) {
		super(message);
	}

	public AttachmentException(String message, Throwable cause) {
		super(message, cause);
	}

}
