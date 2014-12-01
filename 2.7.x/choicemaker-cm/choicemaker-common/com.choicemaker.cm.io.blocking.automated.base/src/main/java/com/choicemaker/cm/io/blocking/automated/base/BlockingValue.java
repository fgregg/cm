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

import java.io.Serializable;

import com.choicemaker.cm.io.blocking.automated.IBlockingField;
import com.choicemaker.cm.io.blocking.automated.IBlockingSet;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;
import com.choicemaker.cm.io.blocking.automated.IGroupTable;

/**
 *
 * @author    mbuechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:31:09 $
 */
public class BlockingValue implements Comparable<IBlockingValue>, Cloneable,
		Serializable, IBlockingValue {
	
	private static final long serialVersionUID = 271;

	public final static BlockingValue[][] NULL_NULL_BV = new BlockingValue[0][0];

	private IBlockingField blockingField;
	private IBlockingValue[][] base; // neither reflexive nor transitive
	private String value;
	private String group;
	private int count;
	private int tableSize;
	private IGroupTable groupTable;

	public BlockingValue(IBlockingField blockingField, String value, IBlockingValue[][] base) {
		this.blockingField = blockingField;
		this.value = value;
		this.setBase(base);
	}

	public BlockingValue(IBlockingField blockingField, String value) {
		this(blockingField, value, NULL_NULL_BV);
	}
	
	public BlockingValue(IBlockingValue ibv) {
		this.blockingField = ibv.getBlockingField();
		this.base = ibv.getBase();
		this.value = ibv.getValue();
		this.group = ibv.getGroup();
		this.count = ibv.getTableSize();
		this.groupTable = ibv.getGroupTable();
	}

	@Override
	public IBlockingField getBlockingField() {
		return blockingField;
	}

	@Override
	public IBlockingValue[][] getBase() {
		return base;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public int getTableSize() {
		return tableSize;
	}

	@Override
	public IGroupTable getGroupTable() {
		return groupTable;
	}

	void setGroupTable(IGroupTable groupTable) {
		this.groupTable = groupTable;
	}

	@Override
	public void setTableSize(int tableSize) {
		this.tableSize = tableSize;
	}

	@Override
	public void setCount(int count) {
		this.count = count;
	}

	void setGroup(String group) {
		this.group = group;
	}

	void setBase(IBlockingValue[][] base) {
		this.base = base;
	}

	boolean isBaseOf(IBlockingValue b) {
		for (int i = 0; i < b.getBase().length; ++i) {
			int len = b.getBase()[i].length;
			for (int j = 0; j < len; ++j) {
				if (b.getBase()[i][j] == this) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsBase(IBlockingSet bs) {
		if (bs.containsBlockingValue(this)) {
			return true;
		}
		boolean found = false;
		for (int j = 0; j < getBase().length && !found; ++j) {
			IBlockingValue[] cand = getBase()[j];
			int k = 0;
			while (k < cand.length && cand[k].containsBase(bs)) {
				++k;
			}
			if (k == cand.length) {
				found = true;
			}
		}
		return found;
	}

	// BUG? FIXME ? not consistent with equals
	@Override
	public int compareTo(IBlockingValue obv) {
		if (obv == null) {
			throw new IllegalArgumentException("null blocking value");
		}
		if (getCount() < obv.getCount()) {
			return -1;
		} else if (getCount() > obv.getCount()) {
			return +1;
		}
		// 	if(isBaseOf(obv)) {
		// 	    return -1;
		// 	} else if(obv.isBaseOf(this)) {
		// 	    return +1;
		// 	}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof BlockingValue) {
			IBlockingValue b = (IBlockingValue)o;
			retVal = b.getBlockingField() == getBlockingField() && b.getValue() == getValue();
		}
		return retVal;
	}
	
	@Override
	public int hashCode() {
		int retVal = 0;
		if (this.getBlockingField() != null) {
			retVal += this.getBlockingField().hashCode();
		}
		if (this.getValue() != null) {
			retVal += this.getValue().hashCode();
		}
		return retVal;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return "BlockingValue [blockingField=" + blockingField + ", value="
				+ value + "]";
	}

}
