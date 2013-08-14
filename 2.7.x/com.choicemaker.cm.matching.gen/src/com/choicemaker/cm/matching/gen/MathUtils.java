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
package com.choicemaker.cm.matching.gen;

/**
 * @author Adam Winkel
 */
public class MathUtils {

	/**
	 * Returns the absolute difference between the integers represented by
	 * the arguments.
	 * 
	 * If either argument cannot be successfully converted to an int by
	 * Integer.parseInt(), this method returns Integer.MAX_VALUE.
	 * 
	 * @param s1 the String representation of an integer
	 * @param s2 the String representation of an second integer
	 * @return the absolute value of the difference between s1 and s2
	 */
	public static int intDiff(String s1, String s2) {
		try {
			return Math.abs(Integer.parseInt(s1) - Integer.parseInt(s2));
		} catch (NumberFormatException ex) { }
		return Integer.MAX_VALUE;	
	}

}
