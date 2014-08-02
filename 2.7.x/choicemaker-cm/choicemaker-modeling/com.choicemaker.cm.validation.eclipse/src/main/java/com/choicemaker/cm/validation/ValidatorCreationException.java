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
package com.choicemaker.cm.validation;

/**
 * Throw if a validator can not be created.
 */
public class ValidatorCreationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ValidatorCreationException() {}

	public ValidatorCreationException(String message) {
		super(message);
	}

	public ValidatorCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidatorCreationException(Throwable cause) {
		super(cause);
	}

}

