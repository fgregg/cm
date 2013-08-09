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
package com.choicemaker.cm.matching.cfg;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ajwinkel
 *
 */
public class InputData {

	private Map fields;
	
	public InputData() {
		fields = new HashMap();
	}
	
	public InputData(InputData copy) {
		fields = new HashMap(copy.fields);
	}

	public String put(String key, String value) {
		return (String) fields.put(key, value);
	}
	
	public boolean has(String key) {
		return fields.containsKey(key);
	}

	public String get(String key) {
		return (String) fields.get(key);
	}

}
