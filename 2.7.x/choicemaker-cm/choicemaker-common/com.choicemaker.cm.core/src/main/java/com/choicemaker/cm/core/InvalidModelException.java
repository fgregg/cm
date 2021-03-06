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
package com.choicemaker.cm.core;

/**
 * Thrown when a Model is invalid, e.g., wrong XML representation.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:13 $
 */
public class InvalidModelException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an <code>InvalidModelException</code> with no detail message.
	 */
	public InvalidModelException() {
		super();
	}
	
	/**
	 * Constructs a <code>InvalidModelException</code> with the specified detail message.
	 * 
	 * @param   message  The detail message.
	 */
	public InvalidModelException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>InvalidModelException</code> with the specified detail message and cause.
	 * 
	 * @param   message  The detail message.
	 * @param   cause  The cause.
	 */
	public InvalidModelException(String message, Throwable cause) {
		super(message, cause);
	}
}
