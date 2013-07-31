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
 * This is the generic OABA exception object.

 * @author pcheung
 *
 */
public class BlockingException extends Exception {
	
	public BlockingException () {
		super ();
	}

	public BlockingException (String message) {
		super (message);
	}

	public BlockingException (String message, Throwable t) {
		super (message, t);
	}

}
