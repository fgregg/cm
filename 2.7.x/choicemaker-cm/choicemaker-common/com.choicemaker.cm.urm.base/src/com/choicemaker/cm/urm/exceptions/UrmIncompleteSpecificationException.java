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
 * Thrown by a constructor or modifier if an object would be incompletely
 * specified after construction or modification. This exception class serves
 * an identical purpose to
 * {@link  com.choicemaker.cm.core.configure.IncompleteSpecificationException IncompleteSpecificationException}.
 * It is defined separately from <code>IncompleteSpecificationException</code>
 * in order to keep the URM interface independent of the core package.
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 * @see com.choicemaker.cm.core.configure.IncompleteSpecificationException
 */
public class UrmIncompleteSpecificationException extends Exception {

	static final long serialVersionUID = -7067762171425890077L;

	public UrmIncompleteSpecificationException() {
		super();
	}

	public UrmIncompleteSpecificationException(String message) {
		super(message);
	}

	public UrmIncompleteSpecificationException(Throwable cause) {
		super(cause);
	}

	public UrmIncompleteSpecificationException(
		String message,
		Throwable cause) {
		super(message, cause);
	}

}
