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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author    mbuechi (CM 2.3)
 * @author    rphall (CM 2.7 revision)
 */

public class CountField implements Serializable {
	
	private static final long serialVersionUID = 271;

	private static final int NUM_INTS = 1000;
	private static final Integer[] ints = new Integer[NUM_INTS];

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

	private final int defaultCount;
	private final int tableSize;
	private final Map/*<String,Integer*/ m;
	private final String column;
	private final String view;
	private final String uniqueId;

	public CountField(int mapSize, int defaultCount, int tableSize, String column, String view, String uniqueId) {
		if (mapSize > 1) {
			m = new HashMap(mapSize);
			
		} else {
			m = new HashMap();
		}
		this.defaultCount = defaultCount;
		this.tableSize = tableSize;
		this.column = column;
		this.view = view;
		this.uniqueId = uniqueId;
	}
	
	public CountField(int mapSize, int defaultCount, int tableSize) {
		this(mapSize, defaultCount, tableSize, null, null, null);
	}
	
	public void putValueCount(String value, Integer count) {
		if (value != null && count != null) {
			m.put(value, count);
		}
	}
	
	public void putAll(Map/*<String,Integer>*/ m) {
		if (m != null) {
			// FIXME replace this by m.putAll(..) when migrating to Java 1.5+
			for (Iterator itE = m.entrySet().iterator(); itE.hasNext(); ) {
				Entry entry = (Entry) itE.next();
				Object value = entry.getKey();
				Object count = entry.getValue(); 
				if (value != null && count != null) {
					if (!(value instanceof String) || !(count instanceof Integer)) {
						String msg = "Invalid entry: " + value + "/" + count;
						throw new IllegalArgumentException(msg);
					}
					m.put(value, count);
				}
			}
		}
	}

	public Integer getCountForValue(String value) {
		Integer retVal = null;
		if (value != null) {
			retVal = (Integer) m.get(value);
		}
		return retVal;
	}

	public int getDefaultCount() {
		return defaultCount;
	}

	public int getTableSize() {
		return tableSize;
	}

	public String getColumn() {
		return column;
	}

	public String getView() {
		return view;
	}

	public String getUniqueId() {
		return uniqueId;
	}

}
