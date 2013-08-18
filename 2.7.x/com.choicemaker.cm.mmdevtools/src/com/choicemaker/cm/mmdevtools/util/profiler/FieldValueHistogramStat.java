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
package com.choicemaker.cm.mmdevtools.util.profiler;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.util.IntValuedHashMap;


/**
 * @author Owner
 *
 */
public class FieldValueHistogramStat implements FieldProfiler {
	
	private FieldAccessor fa;
	
	private boolean showLength;

	private IntValuedHashMap fieldCounts;
	private int totalRows;
	
	private boolean dirty;

	// computed in the clean() routine.
	private int numUniqueValues;

	public FieldValueHistogramStat(FieldAccessor fa) {
		this(fa, true);
	}
	
	public FieldValueHistogramStat(FieldAccessor fa, boolean showLength) {
		this.fa = fa;
		this.showLength = showLength;
		reset();
	}

	public void reset() {
		fieldCounts = new IntValuedHashMap();
		totalRows = 0;
		
		dirty = true;
	}

	public void processRecord(Record r) {
		int rows = fa.getRowCount(r);
		for (int row = 0; row < rows; row++) {
			Object val = fa.getValue(r, row);
			fieldCounts.increment(val);
		}
		
		totalRows += rows;
		
		dirty = true;
	}

	private void clean() {
		if (dirty) {
			dirty = false;
			
			numUniqueValues = 0;
			for (Iterator it = fieldCounts.entrySet().iterator(); it.hasNext(); ) {
				Entry e = (Entry)it.next();
				Integer count = (Integer)e.getValue();
				if (count.intValue() == 1) {
					numUniqueValues++;
				}
			}
		}
	}

	public int getScalarStatCount() {
		return 2;
	}

	public String getScalarStatName(int index) {
		switch (index) {
			case 0: return "Num Distinct Values";
			case 1: return "Num Unique Values";
		}
		throw new IndexOutOfBoundsException("Index: " + index);
	}

	public Object getScalarStatValue(int index) {
		switch (index) {
			case 0: return new Integer(fieldCounts.size());
			case 1:
				clean();
				return new Integer(numUniqueValues);
		}
		throw new IndexOutOfBoundsException("Index: " + index);
	}

	public boolean filterRecordForScalarStat(int index, Record r) {
		switch (index) {
			case 0:
			case 1:
				return false;
		}
		throw new IndexOutOfBoundsException("Index: " + index);
	}

	public int getTabularStatCount() {
		return 1;
	}

	public String getTabularStatName(int index) {
		if (index != 0) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		return "Field-Value Histogram";
	}

	public Object[] getTabularStatColumnHeaders(int index) {
		if (index != 0) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		if (showLength) {
			return new Object[] {"Field Value", "Count", "Percentage", "Length"};
		} else {
			return new Object[] {"Field Value", "Count", "Percentage"};
		}
	}

	public Object[][] getTabularStatTableData(int index) {
		if (index != 0) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		Object[][] data = null;
		if (showLength) {
			data = new Object[fieldCounts.size()][4];
		} else {
			data = new Object[fieldCounts.size()][3];
		}

		int idx = 0;
		for (Iterator it = fieldCounts.sortedKeys().iterator(); it.hasNext(); ) {
			Object key = it.next();
			Integer value = (Integer)fieldCounts.get(key);
			data[idx][0] = key;
			data[idx][1] = fieldCounts.get(key);
			data[idx][2] = new Float(value.intValue() /(float)totalRows);
			
			if (showLength) {
				data[idx][3] = key != null ? new Integer(key.toString().length()) : new Integer(0);
			}

			idx++;
		}

		return data;
	}

	public boolean filterRecordForTableStat(int index, Set values, Record r) {
		if (index != 0) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		int rows = fa.getRowCount(r);
		for (int row = 0; row < rows; row++) {
			Object val = fa.getValue(r, row);
			if (values.contains(val)) {
				return true;
			}
		}
		
		return false;
	}

}
