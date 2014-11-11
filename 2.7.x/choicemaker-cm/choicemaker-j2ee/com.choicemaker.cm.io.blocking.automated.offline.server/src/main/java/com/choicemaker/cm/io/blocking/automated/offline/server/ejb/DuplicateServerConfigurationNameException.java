package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

public class DuplicateServerConfigurationNameException extends Exception {

	private static final long serialVersionUID = 271L;

	public DuplicateServerConfigurationNameException() {
	}

	public DuplicateServerConfigurationNameException(String message) {
		super(message);
	}

	public DuplicateServerConfigurationNameException(Throwable cause) {
		super(cause);
	}

	public DuplicateServerConfigurationNameException(String message,
			Throwable cause) {
		super(message, cause);
	}

	public DuplicateServerConfigurationNameException(String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
