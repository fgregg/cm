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
package com.choicemaker.cm.module.namedstate;

import com.choicemaker.cm.module.INamedState;


/**
 * The default controller only has one state.
 * @author rphall
 */
public class NamedState
	implements INamedState {
		
	public static final String DEFAULT_NAMED_STATE = "ST.DEFAULT_NAMED_STATE";

	private String name;
	
	public NamedState() {
		this.name = DEFAULT_NAMED_STATE;
	}
	
	protected NamedState(String name) {
		this.name = name;
		// Fail fast
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("null or blank name");
		}
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
		
}

