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
 * Thrown by finder methods if some specified instance id is not
 * unique in some context. This exception class serves an identical purpose to
 * {@link  com.choicemaker.cm.core.configure.NotUniqueException NotUniqueException}.
 * It is defined separately from <code>NotUniqueException</code>
 * in order to keep the URM interface independent of the core package.
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 * @see com.choicemaker.cm.core.configure.NotUniqueException
 */
public class UrmNotUniqueException extends Exception {

	static final long serialVersionUID = -5464434789265823422L;

	public UrmNotUniqueException() {
		super();
	}

	public UrmNotUniqueException(String message) {
		super(message);
	}

	public UrmNotUniqueException(Throwable cause) {
		super(cause);
	}

	public UrmNotUniqueException(String message, Throwable cause) {
		super(message, cause);
	}

}
