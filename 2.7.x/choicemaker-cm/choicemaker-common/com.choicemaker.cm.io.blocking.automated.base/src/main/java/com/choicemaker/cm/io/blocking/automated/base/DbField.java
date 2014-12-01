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

import com.choicemaker.cm.io.blocking.automated.IDbField;
import com.choicemaker.cm.io.blocking.automated.IDbTable;
import com.choicemaker.cm.io.blocking.automated.IField;

/**
 * A field on a master record, against which query record are compared.
 * 
 * @author    mbuechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:31:50 $
 */
public class DbField extends Field implements IDbField {
	
	private static final long serialVersionUID = 271;

	private final int number;
	private final String name;
	private final String type;
	private final IDbTable table;
	private final int defaultCount;

	public DbField(int number, String name, String type, IDbTable table, int defaultCount) {
		this(number, name, type, table, defaultCount, NN_FIELD);
	}

	public DbField(
		int number,
		String name,
		String type,
		IDbTable table,
		int defaultCount,
		IField[][] illegalCombinations) {
		super(illegalCombinations);
		this.number = number;
		this.name = name;
		this.type = type;
		this.table = table;
		this.defaultCount = defaultCount;
	}
	
	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public IDbTable getTable() {
		return table;
	}

	@Override
	public int getDefaultCount() {
		return defaultCount;
	}

	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof DbField) {
			IDbField that = (IDbField) o;
			retVal = this.getNumber() == that.getNumber();
			if (retVal && this.getName() == null) {
				retVal = that.getName() == null;
			} else if (retVal) {
				retVal = this.getName().equals(that.getName());
			}
			if (retVal && this.getType() == null) {
				retVal = that.getType() == null;
			} else if (retVal) {
				retVal = this.getType().equals(that.getType());
			}
			if (retVal && this.getTable() == null) {
				retVal = this.getTable() == null;
			} else if (retVal) {
				retVal = this.getTable().equals(that.getTable());
			}
		}
		return retVal;
	}
	
	public int hashCode() {
		int retVal = getNumber();
		if (getName() != null) {
			retVal += getName().hashCode();
		}
		if (getType() != null) {
			retVal += getType().hashCode();
		}
		if (getTable() != null) {
			retVal += getTable().hashCode();
		}
		return retVal;
	}

	@Override
	public String toString() {
		return "DbField [number=" + number + ", name=" + name + ", type="
				+ type + ", table=" + table + ", defaultCount=" + defaultCount
				+ "]";
	}
	
}
