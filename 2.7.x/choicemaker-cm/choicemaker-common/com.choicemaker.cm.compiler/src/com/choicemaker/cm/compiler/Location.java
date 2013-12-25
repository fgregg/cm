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
package com.choicemaker.cm.compiler;

/**
 * This class provides functions for encoding and decoding of source code
 * positions consisting of line and column number into a single int value
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public final class Location {

	/** source code positions are int values of the format:
	 *  line-number << LSHIFT + column-number
	 */
	private static final int LSHIFT = 10;
	private static final int CMASK = 1023;

	/** undefined position
	 */
	public static final int NOPOS = 0;

	/** first position in the source code
	 */
	public static final int FIRSTPOS = (1 << LSHIFT) + 1;

	/** encode a line and column number into a single int value
	 */
	public static int encode(int line, int column) {
		return (line << LSHIFT) + column;
	}

	/** extract the line number from an encoded position
	 */
	public static int line(int pos) {
		return pos >>> LSHIFT;
	}

	/** extract the column number from an encoded position
	 */
	public static int column(int pos) {
		return pos & CMASK;
	}
}
