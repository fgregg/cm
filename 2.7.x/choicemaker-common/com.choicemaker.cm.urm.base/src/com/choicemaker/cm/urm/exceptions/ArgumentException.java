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
 * Signals that an invaid argument was passed to an API method. 
 * 
 * @author emoussikaev
 * @version Revision: 2.5  Date: Sep 30, 2005 1:12:46 PM
 * @see
 */
public class ArgumentException extends Exception {

	static final long serialVersionUID = -543795871235164177L;

	/**
	 * Constructs an <code>ArgumentException</code>
	 * <p> 
	 * 
	 */
	public ArgumentException() {
		super();
	}

	/**
	 * Constructs a <code>ArgumentException</code> with the specified detail message.
	 * <p> 
	 * @param message
	 */
	public ArgumentException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>ArgumentException</code> with the specified cause.
	 * <p> 
	 * @param cause
	 */
	public ArgumentException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a <code>ArgumentException</code> with the specified detail message and cause.
	 * <p> 
	 * @param message
	 * @param cause
	 */
	public ArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

}
