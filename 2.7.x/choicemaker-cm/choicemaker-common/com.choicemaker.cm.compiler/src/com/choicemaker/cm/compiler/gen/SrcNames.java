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
package com.choicemaker.cm.compiler.gen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.choicemaker.cm.core.Constants;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
class SrcNames {
	private int c;
	private Map m;

	SrcNames() {
		c = 0;
		m = new HashMap();
	}

	int getId(String name) {
		if(name == null) {
			name = "all";
		}
		Object o = m.get(name);
		if (o != null) {
			return ((Integer) o).intValue();
		} else {
			m.put(name, new Integer(++c));
			return c;
		}
	}

	String getDeclarations() {
		StringBuffer b = new StringBuffer();
		Iterator i = m.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			b.append(
				"private static DerivedSource __src"
					+ e.getValue()
					+ " = DerivedSource.valueOf(\""
					+ e.getKey()
					+ "\");" + Constants.LINE_SEPARATOR);
		}
		return b.toString();
	}
}
