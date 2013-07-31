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
package com.choicemaker.cm.core.gen;

import com.choicemaker.cm.core.util.ChainedException;


/**
 * Generator exception.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:06 $
 */
public class GenException extends ChainedException {
	/**
	 * Constructs a <code>GenException</code> with <code>s</code> as reason.
	 *
	 * @param   s  The reason the exception is thrown.
	 */
	public GenException(String s) {
		super(s);
	}

	/**
	 * Constructor from another exception.
	 *
	 * @param   ex  The exception ot be nested.
	 */
	public GenException(String s, Throwable ex) {
		super(s, ex);
	}
}
