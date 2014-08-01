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
package com.choicemaker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * .
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class DoubleValuedHashMap extends HashMap {
	private static final long serialVersionUID = 1L;

	public DoubleValuedHashMap() { }

	public DoubleValuedHashMap(Map map) {
		super(map);
	}
		
	public DoubleValuedHashMap(DoubleValuedHashMap map) {
		super(map);	
	}
	
	public Object put(Object key, Object value) {
		if (!(value instanceof Double)) {
			throw new IllegalArgumentException();	
		}
	
		return super.put(key, value);	
	}
	
	public void putDouble(Object key, double value) {
		super.put(key, new Double(value));	
	}

	public double getDouble(Object key) {
		Double value = (Double)get(key);
		if (value != null)
			return value.doubleValue();
		else
			return 0.0;
	}
		
	public void increment(Object key) {
		putDouble(key, getDouble(key) + 1);	
	}
	
	public List sortedKeys() {
		List keys = new ArrayList(keySet());
		Collections.sort(keys, new MapKeyComparator(this));
		return keys;		
	}
}
