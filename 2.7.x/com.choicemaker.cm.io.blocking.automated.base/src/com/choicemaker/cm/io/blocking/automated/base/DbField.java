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
package com.choicemaker.cm.io.blocking.automated.base;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:31:50 $
 */
public class DbField extends Field {
	public final int number;
	public final String name;
	public final String type;
	public final DbTable table;
	public final int defaultCount;

	public DbField(int number, String name, String type, DbTable table, int defaultCount) {
		this(number, name, type, table, defaultCount, NN_FIELD);
	}

	public DbField(
		int number,
		String name,
		String type,
		DbTable table,
		int defaultCount,
		Field[][] illegalCombinations) {
		super(illegalCombinations);
		this.number = number;
		this.name = name;
		this.type = type;
		this.table = table;
		this.defaultCount = defaultCount;
	}
	
	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof DbField) {
			DbField that = (DbField) o;
			retVal = this.number == that.number;
			if (retVal && this.name == null) {
				retVal = that.name == null;
			} else if (retVal) {
				retVal = this.name.equals(that.name);
			}
			if (retVal && this.type == null) {
				retVal = that.type == null;
			} else if (retVal) {
				retVal = this.type.equals(that.type);
			}
			if (retVal && this.table == null) {
				retVal = this.table == null;
			} else if (retVal) {
				retVal = this.table.equals(that.table);
			}
		}
		return retVal;
	}
	
	public int hashCode() {
		int retVal = number;
		if (name != null) {
			retVal += name.hashCode();
		}
		if (type != null) {
			retVal += type.hashCode();
		}
		if (table != null) {
			retVal += table.hashCode();
		}
		return retVal;
	}
	
}
