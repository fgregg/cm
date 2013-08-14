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
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:29:54 $
 */
public class BlockingField extends Field {
	public final int number;
	public final QueryField queryField;
	public final DbField dbField;
	public final String group;

	public BlockingField(int number, QueryField queryField, DbField dbField, String group) {
		this(number, queryField, dbField, group, NN_FIELD);
	}

	public BlockingField(
		int number,
		QueryField queryField,
		DbField dbField,
		String group,
		Field[][] illegalCombinations) {
		super(illegalCombinations);
		this.number = number;
		this.queryField = queryField;
		this.dbField = dbField;
		this.group = group;
	}
	
	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof BlockingField) {
			BlockingField that = (BlockingField) o;
			retVal = this.number == that.number;
			if (retVal && this.queryField == null) {
				retVal = that.queryField == null;
			} else if (retVal) {
				retVal = this.queryField.equals(that.queryField);
			}
			if (retVal && this.dbField == null) {
				retVal = this.dbField == null;
			} else if (retVal) {
				retVal = this.dbField.equals(that.dbField);
			}
			if (retVal && this.group == null) {
				retVal = that.group == null;
			} else if (retVal) {
				retVal = this.group.equals(that.group);
			}
		}
		return retVal;
	}
	
	public int hashCode() {
		int retVal = this.number;
		if (this.queryField != null) {
			retVal += this.queryField.hashCode();
		}
		if (this.dbField != null) {
			retVal += this.dbField.hashCode();
		}
		if (this.group != null) {
			retVal += this.group.hashCode();
		}
		return retVal;
	}

}
