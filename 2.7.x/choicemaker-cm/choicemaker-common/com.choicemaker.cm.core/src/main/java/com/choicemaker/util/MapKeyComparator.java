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

import java.util.Comparator;
import java.util.Map;

class MapKeyComparator implements Comparator {

	protected Map map;

	public MapKeyComparator(Map map) {
		this.map = map;	
	}

	public int compare(Object obj1, Object obj2) {
		Object val1 = map.get(obj1);
		Object val2 = map.get(obj2);
		if (val1 == null && val2 == null) {
			return 0;
		} else if (val1 == null) {
			return 1;
		} else if (val2 == null) {
			return -1;	
		} else {
			return ((Comparable)val2).compareTo(val1);
		}
	}
}
