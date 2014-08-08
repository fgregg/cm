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
package com.choicemaker.cm.io.blocking.automated.offline.core;

/**
 * This contains constants used by OABA.
 * 
 * @author pcheung
 *
 */
public class Constants {

	// source/sink constants
	public static final int STRING = 1;
	public static final int BINARY = 2;

	public static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	/**
	 * This indicates the beginning of a suffix tree node.
	 * 
	 */
	public static final char OPEN_NODE = '[';

	/**
	 * This indicates the ending of a suffix tree node.
	 * 
	 */
	public static final char CLOSE_NODE = ']';

	/**
	 * This delimits the clues names in MatchResult2.getInfo ().
	 * 
	 */
	public static final char DELIMITER = '|';

}
