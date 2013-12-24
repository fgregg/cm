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
 * Utilities useful for debuging a clue set.
 * @author rphall
 */
public class ClueDebugUtils {

	private ClueDebugUtils() {}
	
	/**
	 * Prints a message to standard out. Always returns true, so that this
	 * method may be 'and-ed" into the body of a clue.
	 * @author rphall
	 */
	public static boolean println(String msg) {
		System.out.println(msg);
		return true;
	}

}

 
