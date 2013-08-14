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
package com.choicemaker.cm.io.blocking.automated.inmemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.io.blocking.automated.base.BlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.base.BlockingSet;
import com.choicemaker.cm.io.blocking.automated.base.BlockingValue;
import com.choicemaker.cm.io.blocking.automated.base.CountField;
import com.choicemaker.cm.io.blocking.automated.base.CountSource;
import com.choicemaker.cm.io.blocking.automated.base.DbField;
import com.choicemaker.cm.io.blocking.automated.cachecount.CacheCountSource;

/**
 * @author ajwinkel
 *
 */
public class InMemoryDataSource {

	protected BlockingConfiguration bc;

	protected int numBlockingFields;
	protected HashMap[] fieldMaps;
	protected List recordList;

	public InMemoryDataSource(BlockingConfiguration bc) {
		this.bc = bc;
	}

	//
	// Initialization
	//

	public void init(List records) {
		numBlockingFields = bc.blockingFields.length;
		
		fieldMaps = new HashMap[numBlockingFields];
		for (int i = 0; i < fieldMaps.length; i++) {
			fieldMaps[i] = new HashMap();
		}

		recordList = new ArrayList(records);
		int len = recordList.size();
		for (int recordIndex = 0; recordIndex < len; recordIndex++) {
			Record r = (Record)recordList.get(recordIndex);
			BlockingValue[] bvs = bc.createBlockingValues(r);
				
			for (int j = 0; j < bvs.length; j++) {
				String value = bvs[j].value;
				put(recordIndex, bvs[j].blockingField.dbField.number, value);
			}
		}					
	}

	private void put(int recordIndex, int blockingFieldNumber, String blockingValue) {
		IntArrayList bvList = (IntArrayList)fieldMaps[blockingFieldNumber].get(blockingValue);
		if (bvList == null) {
			bvList = new IntArrayList(1);
			bvList.add(recordIndex);
			fieldMaps[blockingFieldNumber].put(blockingValue, bvList);
		} else if (bvList.get(bvList.size() - 1) != recordIndex) {
			bvList.add(recordIndex);
		}
	}
	
	//
	// CountSource
	//
	
	public CountSource createCountSource() {
		int mainTableSize = recordList.size();
		
		int numFields = bc.dbFields.length;		
		CountField[] countFields = new CountField[numFields];
		for (int i = 0; i < numFields; i++) {
			countFields[i] = createCountField(i);
		}
		
		return new CacheCountSource(recordList.size(), countFields);
	}
	
	private CountField createCountField(int index) {
		DbField dbf = bc.dbFields[index];

		// get the  {field value} --> {set of record indices} map
		// for the corresponding field.
		int fieldNum = dbf.number;
		HashMap fieldMap = fieldMaps[fieldNum];

		// create a counts map for those values that occur more
		// than dbf.default times
		int defaultCount = dbf.defaultCount;
		HashMap biggerThanDefault = new HashMap();
		for (Iterator it = fieldMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			IntArrayList value = (IntArrayList)entry.getValue();
			int numRecords = value.size();
			if (numRecords > defaultCount) {
				biggerThanDefault.put(entry.getKey(), CountField.getInteger(numRecords));
			}
		}

		// create a CountField object to represent our results
		CountField cf = new CountField(
			2, // initial size of cf.m (which is replaced in the next line anyway anyway).
			defaultCount, 
			fieldMap.size(),
			dbf.name,
			dbf.table.name,
			dbf.table.uniqueId);
		cf.m = biggerThanDefault;
		
		return cf;
	}
	
	//
	// Query methods
	//

	/**
	 * Returns an iterator over the Records that blocked against the specified
	 * blocking set.
	 */
	public Iterator select(List blockingSets, int start) {
		IntIterator[] its = new IntIterator[blockingSets.size()];
		for (int i = 0; i < its.length; i++) {
			its[i] = createIndexIterator((BlockingSet)blockingSets.get(i), start);
		}
		
		IntIterator union = createUnionIntIterator(its);
		return new RecordSetIterator(recordList, union);
	}
	
	//
	// IntIterator stuff
	//
	
	private static final IntIterator[] ZERO_INT_ITERATOR = new IntIterator[0];

	private IntIterator createIndexIterator(BlockingSet bs, int start) {
		BlockingValue[] bvs = bs.getBlockingValues();

		List lists = new ArrayList(bvs.length);
		for (int i = 0; i < bvs.length; i++) {
			int fieldNum = bvs[i].blockingField.dbField.number;
			String fieldVal = bvs[i].value;
			
			IntArrayList ial = (IntArrayList) fieldMaps[fieldNum].get(fieldVal);
			if (ial != null) {
				lists.add(new IntArrayListIterator(ial, start));
			}
		}
		
		IntIterator[] its = (IntIterator[]) lists.toArray(ZERO_INT_ITERATOR);
		return createIntersectionIntIterator(its);
	}

	private static IntIterator createUnionIntIterator(IntIterator[] its) {
		return createUnionIntIterator(its, 0, its.length);
	}

	/**
	 * from is inclusive, to is not
	 */
	private static IntIterator createUnionIntIterator(IntIterator[] its, int from, int to) {
		int len = to - from;
		if (len < 0) {
			throw new IllegalStateException();
		} else if (len == 0) {
			return EMPTY_ITERATOR;
		} else if (len == 1) {
			return its[from];
		} else { // len >= 2
			int pivot = (from + to) / 2;
			IntIterator it1 = createUnionIntIterator(its, from, pivot);
			IntIterator it2 = createUnionIntIterator(its, pivot, to);
			
			return new UnionIntIterator(it1, it2);
		}		
	}

	private static IntIterator createIntersectionIntIterator(IntIterator[] its) {
		return createIntersectionIntIterator(its, 0, its.length);
	}

	/**
	 * from is inclusive, to is not
	 */
	private static IntIterator createIntersectionIntIterator(IntIterator[] its, int from, int to) {
		int len = to - from;
		if (len < 0) {
			throw new IllegalStateException();
		} else if (len == 0) {
			return EMPTY_ITERATOR;
		} else if (len == 1) {
			return its[from];
		} else { // len >= 2
			int pivot = (from + to) / 2;
			IntIterator it1 = createIntersectionIntIterator(its, from, pivot);
			IntIterator it2 = createIntersectionIntIterator(its, pivot, to);
			
			return new IntersectionIntIterator(it1, it2);
		}
	}

	public static interface IntIterator {
		public boolean hasNext();
		public int next();	
	}
	
	private static final IntIterator EMPTY_ITERATOR = new IntIterator() {
		public boolean hasNext() { return false; }
		public int next() { throw new NoSuchElementException(); };	
	};

	public static class IntArrayListIterator implements IntIterator {
		private IntArrayList list;
		private int size;
		private int nextIndex;
		public IntArrayListIterator(IntArrayList list) {
			this.list = list;
			this.size = list.size();
			nextIndex = 0;
		}
		public IntArrayListIterator(IntArrayList list, int start) {
			this(list);
			fastForwardTo(start);
		}
		public boolean hasNext() {
			return nextIndex < size;
		}
		public int next() {
			return list.get(nextIndex++);
		}
		private void fastForwardTo(int start) {
			while (nextIndex < size && list.get(nextIndex) < start) {
				nextIndex++;
			}
		}
	}
	
	public static class UnionIntIterator implements IntIterator {
		private static final int NEXT_IS_I1 = 1;
		private static final int NEXT_IS_I2 = 2;
		private static final int NEXT_IS_NULL = -1;
		
		private IntIterator it1, it2;
		
		private int i1, i2;
		private boolean i1Valid, i2Valid;
		
		public UnionIntIterator(IntIterator it1, IntIterator it2) {
			this.it1 = it1;
			this.it2 = it2;	
			
			i1Valid = i2Valid = true;
			advance();
		}
		public boolean hasNext() {
			return i1Valid || i2Valid;
		}
		public int next() {
			if (hasNext()) {
				int nextInt = nextImpl();
				advance();
				return nextInt;
			} else {
				throw new NoSuchElementException();
			}
		}
		private int nextImpl() {
			if (i1Valid && i2Valid) {
				return i1 < i2 ? i1 : i2;
			} else if (i1Valid) {
				return i1;
			} else {
				return i2;
			}
		}
		private void advance() {
			if (i1Valid && i2Valid) {
				if (i1 < i2) {
					i1Valid = it1.hasNext();
					if (i1Valid) {
						i1 = it1.next();
					}
				} else if (i2 < i1) {
					i2Valid = it2.hasNext();
					if (i2Valid) {
						i2 = it2.next();
					}
				} else {
					i1Valid = it1.hasNext();
					i2Valid = it2.hasNext();
					if (i1Valid) {
						i1 = it1.next();
					}
					if (i2Valid) {
						i2 = it2.next();
					}					
				}
			} else if (i1Valid) {
				i1Valid = it1.hasNext();
				if (i1Valid) {
					i1 = it1.next();
				}
			} else if (i2Valid) {
				i2Valid = it2.hasNext();
				if (i2Valid) {
					i2 = it2.next();
				}
			}
		}
	}
	
	public static class IntersectionIntIterator implements IntIterator {
		private IntIterator it1, it2;
		
		private int next;
		private boolean nextIsValid;		
		
		public IntersectionIntIterator(IntIterator it1, IntIterator it2) {
			this.it1 = it1;
			this.it2 = it2;
			
			advance();
		}
		public boolean hasNext() {
			return nextIsValid;
		}
		public int next() {
			if (hasNext()) {
				int nextInt = this.next;
				advance();
				return nextInt;
			} else {
				throw new NoSuchElementException();				
			}
		}
		private void advance() {
			nextIsValid = false;
			if (!it1.hasNext() || !it2.hasNext()) {
				return;
			}

			int i1 = it1.next();
			int i2 = it2.next();
			while (true) {
				if (i1 == i2) {
					next = i1;
					nextIsValid = true;
					break;
				} else if (i1 < i2 && it1.hasNext()) {
					i1 = it1.next();
				} else if (i2 < i1 && it2.hasNext()) {
					i2 = it2.next();
				} else {
					break;
				}
			}
		}
	}
	
	private static class RecordSetIterator implements Iterator {
		
		private List recordList;
		private IntIterator indices;
		
		private RecordSetIterator(List recordList, IntIterator indices) {
			this.recordList = recordList;
			this.indices = indices;
		}

		public boolean hasNext() {
			return indices.hasNext();
		}
		
		public Object next() {
			return recordList.get(indices.next());
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}		
		
	}

}
