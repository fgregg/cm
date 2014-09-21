package com.choicemaker.e2;

public class PluginDiscoveryException extends Error {

	private static final long serialVersionUID = 1L;

	public PluginDiscoveryException() {
	}

	public PluginDiscoveryException(String message) {
		super(message);
	}

	public PluginDiscoveryException(Throwable cause) {
		super(cause);
	}

	public PluginDiscoveryException(String message, Throwable cause) {
		super(message, cause);
	}

	public PluginDiscoveryException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
