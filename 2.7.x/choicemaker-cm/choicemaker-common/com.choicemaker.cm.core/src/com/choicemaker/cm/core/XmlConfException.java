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
package com.choicemaker.cm.core;



/**
 * XML configuration exception.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:01 $
 */
public class XmlConfException extends Exception {
	private static final long serialVersionUID = 2L;

	/**
	 * Constructs a <code>XmlConfException</code> with <code>s</code> as reason.
	 *
	 * @param   s  The reason the exception is thrown.
	 */
	public XmlConfException(String s) {
		super(s);
	}

	/**
	 * Constructor from another exception.
	 *
	 * @param   ex  The exception to be nested.
	 */
	public XmlConfException(String s, Throwable ex) {
		super(s, ex);
	}

}
