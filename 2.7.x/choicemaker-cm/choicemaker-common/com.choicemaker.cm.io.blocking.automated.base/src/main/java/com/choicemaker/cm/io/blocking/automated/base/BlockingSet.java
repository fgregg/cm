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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.choicemaker.cm.io.blocking.automated.IBlockingField;
import com.choicemaker.cm.io.blocking.automated.IBlockingSet;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;
import com.choicemaker.cm.io.blocking.automated.IDbTable;
import com.choicemaker.cm.io.blocking.automated.IField;
import com.choicemaker.cm.io.blocking.automated.IGroupTable;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:30:46 $
 */
public class BlockingSet implements Serializable, IBlockingSet {
	
	private static final long serialVersionUID = 271;

	private final long mainTableSize;
	private final ArrayList<IBlockingValue> blockingValues;
	private final ArrayList<IGroupTable> tables;
	private double expectedCount;

	public BlockingSet(long mainTableSize) {
		this.mainTableSize = mainTableSize;
		blockingValues = new ArrayList<>();
		tables = new ArrayList<>();
		expectedCount = 1;
	}

	@SuppressWarnings("unchecked")
	public BlockingSet(IBlockingSet currentSubset, IBlockingValue bv) {
		this.mainTableSize = currentSubset.getMainTableSize();
		this.blockingValues =
			(ArrayList<IBlockingValue>) new ArrayList<>(
					Arrays.asList(currentSubset.getBlockingValues())).clone();
		this.tables =
			(ArrayList<IGroupTable>) new ArrayList<>(
					Arrays.asList(currentSubset.getTables())).clone();
		this.expectedCount = currentSubset.getExpectedCount();
		add(bv);
	}

	private void add(IBlockingValue ibv) {
		BlockingValue bv = new BlockingValue((IBlockingValue) ibv.clone());
		blockingValues.add(bv);
		IBlockingField bf = bv.getBlockingField();
		String g = bf.getGroup();
		IDbTable bt = bf.getDbField().getTable();
		int size = tables.size();
		int i = 0;
		while (i < size) {
			IGroupTable gt = (IGroupTable) tables.get(i);
			if (gt.getTable() == bt && gt.getGroup() == g) {
				bv.setGroupTable(gt);
				break;
			}
			++i;
		}
		if (i == size) {
			GroupTable gt = new GroupTable(bt, g, size);
			bv.setGroupTable(gt);
			tables.add(gt);
			if (tables.size() > 1) {
				expectedCount /= getMainTableSize();
			}
		} else {
			expectedCount /= bv.getTableSize();
		}
		expectedCount *= bv.getCount();
	}

	@Override
	public int numFields() {
		return blockingValues.size();
	}

	@Override
	public long getExpectedCount() {
		return (long) expectedCount;
	}

	@Override
	public boolean returnsSupersetOf(IBlockingSet bs) {
		for (int i = 0; i < blockingValues.size(); ++i) {
			IBlockingValue bv = (IBlockingValue) blockingValues.get(i);
			if (!bv.containsBase(bs)) {
				return false;
			}
		}
		return true;
	}

	public boolean containsBlockingValue(IBlockingValue bv) {
		int size = blockingValues.size();
		for (int i = 0; i < size; ++i) {
			if (bv.equals(blockingValues.get(i))) {
				return true;
			}
		}
		return false;
	}

	public boolean containsField(IField f) {
		int size = blockingValues.size();
		for (int i = 0; i < size; ++i) {
			IBlockingField b = ((IBlockingValue) blockingValues.get(i)).getBlockingField();
			if (b == f || b.getDbField() == f || b.getQueryField() == f) {
				return true;
			}
		}
		return false;
	}

	public long getMainTableSize() {
		return mainTableSize;
	}

	@Override
	public IGroupTable getGroupTable(IBlockingField bf) {
		IDbTable table = bf.getDbField().getTable();
		int size = tables.size();
		for (int i = 0; i < size; ++i) {
			IGroupTable gt = (IGroupTable) tables.get(i);
			if (gt.getTable() == table && bf.getGroup().equals(gt.getGroup())) {
				return gt;
			}
		}
		return null;
	}

	public static class GroupTable implements Serializable, IGroupTable {
		private static final long serialVersionUID = 271;
		private final IDbTable table;
		private final String group;
		private final int number;
		GroupTable(IDbTable table, String group, int number) {
			this.table = table;
			this.group = group;
			this.number = number;
		}
		@Override
		public IDbTable getTable() {
			return table;
		}
		@Override
		public String getGroup() {
			return group;
		}
		@Override
		public int getNumber() {
			return number;
		}
		public boolean equals(Object o) {
			boolean retVal = false;
			if (o instanceof GroupTable) {
				retVal = true;
				IGroupTable that = (IGroupTable) o;
				if (this.getNumber() != that.getNumber()) {
					retVal = false;
				} else if (!this.getGroup().equals(that.getGroup())) {
					retVal = false;
				} else if (!this.getTable().equals(that.getTable())) {
					retVal = false;
				}
			}
			return retVal;
		}
		public int hashCode() {
			int retVal = getTable().hashCode() + getGroup().hashCode() + getNumber();
			return retVal;
		}
	}
	
	@Override
	public IBlockingValue[] getBlockingValues() {
		return (IBlockingValue[]) blockingValues.toArray(new IBlockingValue[blockingValues.size()]);
	}
	
	@Override
	public IBlockingValue getBlockingValue(int i) {
		return (IBlockingValue)blockingValues.get(i);
	}
	
	@Override
	public IBlockingValue[] getBlockingValues(IGroupTable gt) {
		List<IBlockingValue> l = new ArrayList<>();
		for (IBlockingValue bv : blockingValues) {
			if(bv.getGroupTable() == gt) {
				l.add(bv);
			}
		}
		return (IBlockingValue[])l.toArray(new IBlockingValue[l.size()]);
	}
	
	@Override
	public IGroupTable getTable(int i) {
		return (IGroupTable) tables.get(i);
	}
	
	@Override
	public IGroupTable[] getTables() {
		return (IGroupTable[]) tables.toArray(new GroupTable[tables.size()]);
	}
	
	@Override
	public int getNumTables() {
		return tables.size();
	}
	
	@Override
	public void sortValues(final boolean ascending) {
		Comparator<IBlockingValue> comparator = new Comparator<IBlockingValue>() {
			public int compare(IBlockingValue o1, IBlockingValue o2) {
				int res;
				int cnt1 = o1.getCount();
				int cnt2 = o2.getCount();
				if(cnt1 < cnt2) {
					res = -1;
				} else if(cnt1 > cnt2) {
					res = 1;
				} else {
					res = 0;
				}
				if(!ascending) {
					res = -res;
				}
				return res;
			}
		};
		Collections.sort(blockingValues, comparator);
	}
	
	// last element decides
	// sorts like values
	@Override
	public void sortTables(final boolean likeValues, final boolean firstValueDecidesOrder) {
		if(tables.size() > 1) {
			Comparator<IGroupTable> comparator = new Comparator<IGroupTable>() {
				public int compare(IGroupTable o1, IGroupTable o2) {
					int p1 = firstValueDecidesOrder ? getFirstIndex(o1) : getLastIndex(o1);
					int p2 = firstValueDecidesOrder ? getFirstIndex(o2) : getLastIndex(o2);
					int res;
					if(p1 < p2) {
						res = -1;
					} else if(p1 > p2) {
						res = +1;
					} else {
						res = 0;
					}
					if(!likeValues) {
						res = -res;
					}
					return res;
				}
				
				private int getFirstIndex(IGroupTable gt) {
					int i = 0;
					while(getBlockingValue(i).getGroupTable() != gt) {
						++i;
					}
					return i;
				}
				
				private int getLastIndex(IGroupTable gt) {
					int i = numFields() - 1;
					while(getBlockingValue(i).getGroupTable() != gt) {
						--i;
					}
					return i;
				}
			};
			Collections.sort(tables, comparator);
		}
	}
	
	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof BlockingSet) {
			BlockingSet that = (BlockingSet) o;
			retVal = true;
			if (this.expectedCount != that.expectedCount) {
				retVal = false;
			} else if (this.getMainTableSize() != that.getMainTableSize()) {
				retVal = false;
			} else if (this.getBlockingValues().length != that.getBlockingValues().length) {
				retVal = false;
			} else if (this.getTables().length != that.getTables().length) {
				retVal = false;
			}
			for (int i=0; retVal && i<this.getBlockingValues().length; i++) {
				IBlockingValue thisBlockingValue = this.getBlockingValue(i);
				IBlockingValue thatBlockingValue = that.getBlockingValue(i);
				if (!thisBlockingValue.equals(thatBlockingValue)) {
					retVal = false;
					break;
				}
			}
			for (int i=0; retVal && i<this.getTables().length; i++) {
				IGroupTable thisGroupTable = this.getTable(i);
				IGroupTable thatGroupTable = that.getTable(i);
				if (!thisGroupTable.equals(thatGroupTable)) {
					retVal = false;
					break;
				}
			}
		}
		return retVal;
	}
	
	public int hashCode() {
		// With apologies to the Double source code...
		double d = expectedCount + getMainTableSize() + getTables().length + getBlockingValues().length + getNumTables();
		long bits = Double.doubleToLongBits(d);
		int retVal = (int)(bits ^ (bits >>> 32));
		return retVal;
	}

}

