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

import com.choicemaker.cm.io.blocking.automated.IBlockingField;
import com.choicemaker.cm.io.blocking.automated.IField;

/**
 * A pair comprised of a {@link QueryField query} and {@link DbField master}
 * which is used for blocking.
 * 
 * @author mbuechi
 * @version $Revision: 1.2 $ $Date: 2010/03/28 09:29:54 $
 */
public class BlockingField extends Field implements IBlockingField {
	
	private static final long serialVersionUID = 271;

	private final int number;
	private final QueryField queryField;
	private final DbField dbField;
	private final String group;

	public BlockingField(int number, QueryField queryField, DbField dbField, String group) {
		this(number, queryField, dbField, group, NN_FIELD);
	}

	public BlockingField(
		int number,
		QueryField queryField,
		DbField dbField,
		String group,
		IField[][] illegalCombinations) {
		super(illegalCombinations);
		this.number = number;
		this.queryField = queryField;
		this.dbField = dbField;
		this.group = group;
	}
	
	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public QueryField getQueryField() {
		return queryField;
	}

	@Override
	public DbField getDbField() {
		return dbField;
	}

	@Override
	public String getGroup() {
		return group;
	}

	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof BlockingField) {
			IBlockingField that = (IBlockingField) o;
			retVal = this.getNumber() == that.getNumber();
			if (retVal && this.getQueryField() == null) {
				retVal = that.getQueryField() == null;
			} else if (retVal) {
				retVal = this.getQueryField().equals(that.getQueryField());
			}
			if (retVal && this.getDbField() == null) {
				retVal = this.getDbField() == null;
			} else if (retVal) {
				retVal = this.getDbField().equals(that.getDbField());
			}
			if (retVal && this.getGroup() == null) {
				retVal = that.getGroup() == null;
			} else if (retVal) {
				retVal = this.getGroup().equals(that.getGroup());
			}
		}
		return retVal;
	}
	
	public int hashCode() {
		int retVal = this.getNumber();
		if (this.getQueryField() != null) {
			retVal += this.getQueryField().hashCode();
		}
		if (this.getDbField() != null) {
			retVal += this.getDbField().hashCode();
		}
		if (this.getGroup() != null) {
			retVal += this.getGroup().hashCode();
		}
		return retVal;
	}

}
