package com.choicemaker.cm.core;

public class ModelTrainingException extends Exception {

	private static final long serialVersionUID = 1L;

	public ModelTrainingException() {
	}

	public ModelTrainingException(String message) {
		super(message);
	}

	public ModelTrainingException(Throwable cause) {
		super(cause);
	}

	public ModelTrainingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelTrainingException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
