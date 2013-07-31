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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * .
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class IntValuedHashMap extends HashMap {
	public IntValuedHashMap() { }
		
	public IntValuedHashMap(IntValuedHashMap map) {
		super(map);	
	}
	
	public Object put(Object key, Object value) {
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException();	
		}
	
		return super.put(key, value);	
	}
	
	public void putInt(Object key, int value) {
		super.put(key, new Integer(value));	
	}

	public int getInt(Object key) {
		Integer value = (Integer)get(key);
		if (value != null)
			return value.intValue();
		else
			return 0;
	}
		
	public void increment(Object key) {
		putInt(key, getInt(key) + 1);	
	}
	
	public List sortedKeys() {
		List keys = new ArrayList(keySet());
		Collections.sort(keys, new MapKeyComparator(this));
		return keys;
	}

}
