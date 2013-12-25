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
package com.choicemaker.cm.core.sort;

import java.io.Serializable;

/**
 * @author ajwinkel
 *
 */
public class SortCondition implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -1930085162004290238L;

	private String node;
	private String field;
	private boolean ascending;
	
	public SortCondition(String node, String field, boolean ascending) {
		this.node = node;
		this.field = field;
		this.ascending = ascending;
	}
	
	
	/**
	 * @return
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * @return
	 */
	public String getField() {
		return field;
	}

	/**
	 * @return
	 */
	public String getNode() {
		return node;
	}

}
