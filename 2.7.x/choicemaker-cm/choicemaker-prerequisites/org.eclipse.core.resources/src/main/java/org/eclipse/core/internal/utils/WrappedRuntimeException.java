/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.utils;

public class WrappedRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private Throwable target;

	public WrappedRuntimeException(Throwable target) {
		super();
		this.target = target;
	}

	public Throwable getTargetException() {
		return this.target;
	}

	public String getMessage() {
		return target.getMessage();
	}
}
