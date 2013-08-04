/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.urm.exceptions;

/**
 * Signals that a problem caused by an incorrect configuration has occurred.
 * @author emoussikaev
 * @version Revision: 2.5  Date: Sep 30, 2005 12:59:45 PM
 * @see
 */
public class ConfigException extends Exception {

	static final long serialVersionUID = 8998394233674389406L;

	/**
	 * Constructs a <code>ConfigException</code>
	 * <p> 
	 * 
	 */
	public ConfigException() {
		super();
	}

	/**
	 * Constructs a <code>ConfigException</code> with the specified detail message.
	 * <p> 
	 * @param message
	 */
	public ConfigException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>ConfigException</code> with the specified cause.
	 * <p> 
	 * @param cause
	 */
	public ConfigException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a <code>ConfigException</code> with the specified detail message and cause.
	 * <p> 
	 * @param message
	 * @param cause
	 */
	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
