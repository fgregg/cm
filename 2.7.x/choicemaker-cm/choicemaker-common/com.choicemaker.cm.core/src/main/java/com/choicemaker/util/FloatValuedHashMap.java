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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * .
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:17:13 $
 */
public class FloatValuedHashMap extends HashMap {
	private static final long serialVersionUID = 1L;

	public FloatValuedHashMap() { }

	public FloatValuedHashMap(Map map) {
		super(map);
	}
		
	public FloatValuedHashMap(FloatValuedHashMap map) {
		super(map);	
	}
	
	public Object put(Object key, Object value) {
		if (!(value instanceof Float)) {
			throw new IllegalArgumentException();	
		}
	
		return super.put(key, value);	
	}
	
	public void putFloat(Object key, float value) {
		super.put(key, new Float(value));	
	}

	public float getFloat(Object key) {
		Float value = (Float)get(key);
		if (value != null)
			return value.floatValue();
		else
			return 0f;
	}
		
	public void increment(Object key) {
		add(key, 1f);	
	}
	
	public void add(Object key, float amount) {
		putFloat(key, getFloat(key) + amount);
	}
		
	public void multiply(Object key, float factor) {
		putFloat(key, getFloat(key) * factor);
	}

	public void multiply(float factor) {
		Iterator it = keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			putFloat(key, getFloat(key) * factor);
		}
	}

	public List sortedKeys() {
		List keys = new ArrayList(keySet());
		Collections.sort(keys, new MapKeyComparator(this));
		return keys;		
	}
}
