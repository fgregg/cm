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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:30:46 $
 */
public class BlockingSet {
	private long mainTableSize;
	private ArrayList blockingValues;
	private ArrayList tables;
	private double expectedCount;

	public BlockingSet(long mainTableSize) {
		this.mainTableSize = mainTableSize;
		blockingValues = new ArrayList();
		tables = new ArrayList();
		expectedCount = 1;
	}

	public BlockingSet(BlockingSet base, BlockingValue bv) {
		this.mainTableSize = base.mainTableSize;
		this.blockingValues = (ArrayList) base.blockingValues.clone();
		this.tables = (ArrayList) base.tables.clone();
		this.expectedCount = base.expectedCount;
		add(bv);
	}

	private void add(BlockingValue bv) {
		bv = (BlockingValue)bv.clone();
		blockingValues.add(bv);
		BlockingField bf = bv.blockingField;
		String g = bf.group;
		DbTable bt = bf.dbField.table;
		int size = tables.size();
		int i = 0;
		while (i < size) {
			GroupTable gt = (GroupTable) tables.get(i);
			if (gt.table == bt && gt.group == g) {
				bv.groupTable = gt;
				break;
			}
			++i;
		}
		if (i == size) {
			GroupTable gt = new GroupTable(bt, g, size);
			bv.groupTable = gt;
			tables.add(gt);
			if (tables.size() > 1) {
				expectedCount /= mainTableSize;
			}
		} else {
			expectedCount /= bv.tableSize;
		}
		expectedCount *= bv.count;
	}

	public int numFields() {
		return blockingValues.size();
	}

	public long getExpectedCount() {
		return (long) expectedCount;
	}

	boolean returnsSupersetOf(BlockingSet bs) {
		for (int i = 0; i < blockingValues.size(); ++i) {
			BlockingValue bv = (BlockingValue) blockingValues.get(i);
			if (!bv.containsBase(bs)) {
				return false;
			}
		}
		return true;
	}

	boolean containsBlockingValue(BlockingValue bv) {
		int size = blockingValues.size();
		for (int i = 0; i < size; ++i) {
			if (bv.equals(blockingValues.get(i))) {
				return true;
			}
		}
		return false;
	}

	boolean containsField(Field f) {
		int size = blockingValues.size();
		for (int i = 0; i < size; ++i) {
			BlockingField b = ((BlockingValue) blockingValues.get(i)).blockingField;
			if (b == f || b.dbField == f || b.queryField == f) {
				return true;
			}
		}
		return false;
	}

	public GroupTable getGroupTable(BlockingField bf) {
		DbTable table = bf.dbField.table;
		int size = tables.size();
		for (int i = 0; i < size; ++i) {
			GroupTable gt = (GroupTable) tables.get(i);
			if (gt.table == table && bf.group.equals(gt.group)) {
				return gt;
			}
		}
		return null;
	}

	public static class GroupTable {
		public DbTable table;
		public String group;
		public int number;
		GroupTable(DbTable table, String group, int number) {
			this.table = table;
			this.group = group;
			this.number = number;
		}
		public boolean equals(Object o) {
			boolean retVal = false;
			if (o instanceof GroupTable) {
				retVal = true;
				GroupTable that = (GroupTable) o;
				if (this.number != that.number) {
					retVal = false;
				} else if (!this.group.equals(that.group)) {
					retVal = false;
				} else if (!this.table.equals(that.table)) {
					retVal = false;
				}
			}
			return retVal;
		}
		public int hashCode() {
			int retVal = table.hashCode() + group.hashCode() + number;
			return retVal;
		}
	}
	
	public BlockingValue[] getBlockingValues() {
		return (BlockingValue[]) blockingValues.toArray(new BlockingValue[blockingValues.size()]);
	}
	
	public BlockingValue getBlockingValue(int i) {
		return (BlockingValue)blockingValues.get(i);
	}
	
	public BlockingValue[] getBlockingValues(GroupTable gt) {
		List l = new ArrayList();
		for (Iterator iBlockingValues = blockingValues.iterator(); iBlockingValues.hasNext();) {
			BlockingValue bv = (BlockingValue) iBlockingValues.next();
			if(bv.groupTable == gt) {
				l.add(bv);
			}
		}
		return (BlockingValue[])l.toArray(new BlockingValue[l.size()]);
	}
	
	public GroupTable getTable(int i) {
		return (GroupTable) tables.get(i);
	}
	
	public GroupTable[] getTables() {
		return (GroupTable[]) tables.toArray(new GroupTable[tables.size()]);
	}
	
	public int getNumTables() {
		return tables.size();
	}
	
	public void sortValues(final boolean ascending) {
		Comparator comparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				int res;
				int cnt1 = ((BlockingValue)o1).count;
				int cnt2 = ((BlockingValue)o2).count;
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
	public void sortTables(final boolean likeValues, final boolean firstValueDecidesOrder) {
		if(tables.size() > 1) {
			Comparator comparator = new Comparator() {
				public int compare(Object o1, Object o2) {
					int p1 = firstValueDecidesOrder ? getFirstIndex((GroupTable)o1) : getLastIndex((GroupTable)o1);
					int p2 = firstValueDecidesOrder ? getFirstIndex((GroupTable)o2) : getLastIndex((GroupTable)o2);
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
				
				private int getFirstIndex(GroupTable gt) {
					int i = 0;
					while(getBlockingValue(i).groupTable != gt) {
						++i;
					}
					return i;
				}
				
				private int getLastIndex(GroupTable gt) {
					int i = numFields() - 1;
					while(getBlockingValue(i).groupTable != gt) {
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
			} else if (this.mainTableSize != that.mainTableSize) {
				retVal = false;
			} else if (this.getBlockingValues().length != that.getBlockingValues().length) {
				retVal = false;
			} else if (this.getTables().length != that.getTables().length) {
				retVal = false;
			}
			for (int i=0; retVal && i<this.getBlockingValues().length; i++) {
				BlockingValue thisBlockingValue = this.getBlockingValue(i);
				BlockingValue thatBlockingValue = that.getBlockingValue(i);
				if (!thisBlockingValue.equals(thatBlockingValue)) {
					retVal = false;
					break;
				}
			}
			for (int i=0; retVal && i<this.getTables().length; i++) {
				GroupTable thisGroupTable = this.getTable(i);
				GroupTable thatGroupTable = that.getTable(i);
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
		double d = expectedCount + mainTableSize + getTables().length + getBlockingValues().length + getNumTables();
		long bits = Double.doubleToLongBits(d);
		int retVal = (int)(bits ^ (bits >>> 32));
		return retVal;
	}
	
}

