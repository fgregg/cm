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
 * Thrown by a finder method if some specified instance id is not
 * unique in some context.
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public class NotUniqueException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotUniqueException() {
		super();
	}

	public NotUniqueException(String message) {
		super(message);
	}

	public NotUniqueException(Throwable cause) {
		super(cause);
	}

	public NotUniqueException(String message, Throwable cause) {
		super(message, cause);
	}

}
