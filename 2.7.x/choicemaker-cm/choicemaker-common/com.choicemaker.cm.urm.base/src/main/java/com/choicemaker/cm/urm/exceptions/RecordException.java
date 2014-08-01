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
 * Signals that a problem accessing a record has occurred.
 * 
 * @author emoussikaev
 * @version Revision: 2.5  Date: Aug 23, 2005 2:15:07 PM
 * @see
 */
public class RecordException extends Exception {

	static final long serialVersionUID = -20778118706171015L;

	/**
	 * Constructs a <code>RecordException</code>
	 * <p> 
	 * 
	 */
	public RecordException() {
		super();
	}

	/**
	 * Constructs a <code>RecordException</code> with the specified detail message.
	 * <p> 
	 * @param message
	 */
	public RecordException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>RecordException</code> with the specified cause.
	 * <p> 
	 * @param cause
	 */
	public RecordException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a <code>RecordException</code> with the specified detail message and cause.
	 * <p> 
	 * @param message
	 * @param cause
	 */
	public RecordException(String message, Throwable cause) {
		super(message, cause);
	}
}
