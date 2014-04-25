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
package com.choicemaker.cm.core.util;

/**
 *
 * @author    S. Yoakum-Stover
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class OperationFailedException extends ChainedException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a <code>OperationFailedException</code> with <code>s</code> as reason.
	 *
	 * @param   s  The reason the exception is thrown.
	 */
	public OperationFailedException(String s) {
		super(s);
	}

	/**
	 * Constructor from another exception.
	 *
	 * @param   ex  The exception ot be nested.
	 */
	public OperationFailedException(String s, Throwable ex) {
		super(s, ex);
	}
}
