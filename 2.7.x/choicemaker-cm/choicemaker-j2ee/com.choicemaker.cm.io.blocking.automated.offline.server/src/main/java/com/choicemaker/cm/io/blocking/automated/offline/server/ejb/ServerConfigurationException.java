package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

public class ServerConfigurationException extends Exception {

	private static final long serialVersionUID = 271L;

	public ServerConfigurationException() {
	}

	public ServerConfigurationException(String message) {
		super(message);
	}

	public ServerConfigurationException(Throwable cause) {
		super(cause);
	}

	public ServerConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerConfigurationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
