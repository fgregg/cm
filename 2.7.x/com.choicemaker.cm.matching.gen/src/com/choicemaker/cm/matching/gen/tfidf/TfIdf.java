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
package com.choicemaker.cm.matching.gen.tfidf;

import java.util.Iterator;
import java.util.Map;

/**
 * @author ajwinkel
 *
 */
public class TfIdf implements WeightingFunction {

	protected Map values;
	protected float def;

	public TfIdf(Map values) {
		this(values, 0f);
		
		Iterator it = values.values().iterator();
		while (it.hasNext()) {
			float num = ((Number)it.next()).floatValue();
			if (num > def) {
				def = num;
			}
		}
	}
	
	public TfIdf(Map values, float def) {
		this.values = values;
		this.def = def;
		
		if (this.def < 0) {
			def = 0;
		}
	}

	public float weight(String s) {
		Number n = (Number) values.get(s);
		if (n != null) {
			return n.floatValue();
		} else {
			return def;
		}
	}	
	

}
