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

import com.choicemaker.cm.io.blocking.automated.base.BlockingSet.GroupTable;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:31:09 $
 */
public class BlockingValue implements Comparable, Cloneable {
	public static BlockingValue[][] NULL_NULL_BV = new BlockingValue[0][0];

	public BlockingField blockingField;
	public BlockingValue[][] base; // not reflexive or transitive
	public String value;
	public String group;
	public int count;
	public int tableSize;
	public GroupTable groupTable;

	public BlockingValue(BlockingField blockingField, String value, BlockingValue[][] base) {
		this.blockingField = blockingField;
		this.value = value;
		this.base = base;
	}

	public BlockingValue(BlockingField blockingField, String value) {
		this(blockingField, value, NULL_NULL_BV);
	}

	boolean isBaseOf(BlockingValue b) {
		for (int i = 0; i < b.base.length; ++i) {
			int len = b.base[i].length;
			for (int j = 0; j < len; ++j) {
				if (b.base[i][j] == this) {
					return true;
				}
			}
		}
		return false;
	}

	boolean containsBase(BlockingSet bs) {
		if (bs.containsBlockingValue(this)) {
			return true;
		}
		boolean found = false;
		for (int j = 0; j < base.length && !found; ++j) {
			BlockingValue[] cand = base[j];
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

	// not consistent with equals
	public int compareTo(Object o) {
		BlockingValue obv = (BlockingValue) o;
		if (count < obv.count) {
			return -1;
		} else if (count > obv.count) {
			return +1;
		}
		// 	if(isBaseOf(obv)) {
		// 	    return -1;
		// 	} else if(obv.isBaseOf(this)) {
		// 	    return +1;
		// 	}
		return 0;
	}

	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof BlockingValue) {
			BlockingValue b = (BlockingValue)o;
			retVal = b.blockingField == blockingField && b.value == value;
		}
		return retVal;
	}
	
	public int hashCode() {
		int retVal = 0;
		if (this.blockingField != null) {
			retVal += this.blockingField.hashCode();
		}
		if (this.value != null) {
			retVal += this.value.hashCode();
		}
		return retVal;
	}

	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
