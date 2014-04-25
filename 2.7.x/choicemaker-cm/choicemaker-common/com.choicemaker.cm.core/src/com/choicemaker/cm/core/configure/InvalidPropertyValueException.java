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
package com.choicemaker.cm.core.configure;

/**
 * Thrown by a constructor or modifier if an invalid property
 * name is specified for an object.
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public class InvalidPropertyValueException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidPropertyValueException() {
		super();
	}

	public InvalidPropertyValueException(String message) {
		super(message);
	}

	public InvalidPropertyValueException(Throwable cause) {
		super(cause);
	}

	public InvalidPropertyValueException(String message, Throwable cause) {
		super(message, cause);
	}

}
