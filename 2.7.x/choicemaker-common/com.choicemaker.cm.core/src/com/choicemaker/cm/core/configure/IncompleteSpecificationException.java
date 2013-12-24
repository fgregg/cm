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
 * Thrown by a constructor or modifier if an object would be incompletely
 * specified after construction or modification.
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public class IncompleteSpecificationException extends RuntimeException {

	public IncompleteSpecificationException() {
		super();
	}

	public IncompleteSpecificationException(String message) {
		super(message);
	}

	public IncompleteSpecificationException(Throwable cause) {
		super(cause);
	}

	public IncompleteSpecificationException(String message, Throwable cause) {
		super(message, cause);
	}

}
