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
package com.choicemaker.cm.core.configure.xml;

/**
 * Thrown by a constructor or modifier if an invalid property
 * value is specified for an object.
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public class InvalidPropertyNameException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidPropertyNameException() {
		super();
	}

	public InvalidPropertyNameException(String message) {
		super(message);
	}

	public InvalidPropertyNameException(Throwable cause) {
		super(cause);
	}

	public InvalidPropertyNameException(String message, Throwable cause) {
		super(message, cause);
	}

}
