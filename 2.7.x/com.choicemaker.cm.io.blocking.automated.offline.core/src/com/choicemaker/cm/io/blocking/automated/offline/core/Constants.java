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

	//source/sink constants
	public static final int STRING = 1;
	public static final int BINARY = 2;

	//data file constants
//	public static final long NEWLINE = -1;
//	public static final int NEWLINE = -1;

	//Used by RecordValue	
	public static final int HASHMAP = 1;
	public static final int ARRAY = 2;
	
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");


	/** This indicates the beginning of a suffix tree node.
	 * 
	 */
	public static final char OPEN_NODE = '[';
	

	/** This indicates the ending of a suffix tree node.
	 * 
	 */
	public static final char CLOSE_NODE = ']';
	

	/* The following 3 variables indicates the type of the record IDs.
	 * 
	 */
	public static final int TYPE_INTEGER = 1;
	public static final int TYPE_LONG = 2;
	public static final int TYPE_STRING = 3;
	
	
	/** This delimits the clues names in MatchResult2.getInfo ().
	 * 
	 */
	public static final char DELIMITER = '|';
	
	
	
	/** This method checks if the object is an Integer, Long, or String.
	 * 
	 * @param o
	 * @return int - TYPE_INTEGER, or TYPE_LONG, or TYPE_STRING
	 */
	public static int checkType (Comparable o) {
		int ret = 0;

		if (o.getClass() == java.lang.Integer.class) {
			ret = TYPE_INTEGER;
		} else if (o.getClass() == java.lang.Long.class) {
			ret = TYPE_LONG;
		} else if (o.getClass() == java.lang.String.class) {
			ret = TYPE_STRING;
		}
		
		return ret;
	}

}
