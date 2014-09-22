package com.choicemaker.cm.core;

public class ModelConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ModelConfigurationException() {
	}

	public ModelConfigurationException(String message) {
		super(message);
	}

	public ModelConfigurationException(Throwable cause) {
		super(cause);
	}

	public ModelConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelConfigurationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
