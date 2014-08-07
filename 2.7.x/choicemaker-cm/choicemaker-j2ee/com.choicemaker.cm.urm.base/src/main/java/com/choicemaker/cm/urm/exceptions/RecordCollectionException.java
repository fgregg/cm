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
 * Signals that a problem accessing a record collection has occurred.  
 * 
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jul 21, 2005 12:32:32 PM
 * @see
 */
public class RecordCollectionException extends Exception {

	static final long serialVersionUID = -6552824819208291204L;

	/**
	 * Constructs a <code>RecordCollectionException</code>
	 * <p> 
	 * 
	 */
	public RecordCollectionException() {
		super();
	}

	/**
	 * Constructs a <code>RecordCollectionException</code> with the specified detail message.
	 * <p> 
	 * @param message
	 */
	public RecordCollectionException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>RecordCollectionException</code> with the specified cause.
	 * <p> 
	 * @param cause
	 */
	public RecordCollectionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a <code>RecordCollectionException</code> with the specified detail message and cause.
	 * <p> 
	 * @param message
	 * @param cause
	 */
	public RecordCollectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
