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
package com.choicemaker.cm.io.blocking.automated.base;

import java.util.HashMap;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:47 $
 */

public class CountField {
	private static final int NUM_INTS = 1000;
	private static final Integer[] ints = new Integer[NUM_INTS];

	public int defaultCount;
	public int tableSize;
	public HashMap m;
	public String column;
	public String view;
	public String uniqueId;

	public CountField(int mapSize, int defaultCount, int tableSize, String column, String view, String uniqueId) {
		m = new HashMap(mapSize);
		this.defaultCount = defaultCount;
		this.tableSize = tableSize;
		this.column = column;
		this.view = view;
		this.uniqueId = uniqueId;
	}

	public CountField(int mapSize, int defaultCount, int tableSize) {
		this(mapSize, defaultCount, tableSize, null, null, null);
	}

	public static Integer getInteger(int value) {
		if (value < NUM_INTS) {
			Integer i = ints[value];
			if (i != null) {
				return i;
			} else {
				return (ints[value] = new Integer(value));
			}
		} else {
			return new Integer(value);
		}
	}
}
