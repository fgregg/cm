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
package com.choicemaker.cm.io.db.base;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:55 $
 */
public class Index {
	private String name;
	private String table;
	private String[] fields;
	
	public Index(String name, String table, String[] fields) {
		this.name = name;
		this.table = table;
		this.fields = fields;
	}
	
	public String[] getFields() {
		return fields;
	}

	public String getTable() {
		return table;
	}
	
	public int find(String field) {
		for(int i = 0; i < fields.length; ++i) {
			if(field.equals(fields[i])) {
				return i;
			}
		}
		return -1;
	}

	public String getName() {
		return name;
	}
}
