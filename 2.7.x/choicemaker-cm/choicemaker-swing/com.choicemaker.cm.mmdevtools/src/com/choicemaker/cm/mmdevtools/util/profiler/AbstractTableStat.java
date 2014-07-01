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

import java.util.Set;

import com.choicemaker.cm.core.Record;

/**
 * @author Owner
 *
 */
public abstract class AbstractTableStat implements FieldProfiler {

	public abstract void reset();
	public abstract void processRecord(Record r);
	
	public abstract String getName();
	public abstract Object[] getColumnHeaders();
	public abstract Object[][] getData();
	public abstract boolean filterRecord(Set values, Record r);
		
	public final int getScalarStatCount() {
		return 0;
	}
	
	public final String getScalarStatName(int index) {
		throw new UnsupportedOperationException();
	}
	
	public final Object getScalarStatValue(int index) {
		throw new UnsupportedOperationException();
	}
	
	public final boolean filterRecordForScalarStat(int index, Record r) {
		throw new UnsupportedOperationException();
	}
	
	public final int getTabularStatCount() {
		return 1;
	}

	public final String getTabularStatName(int index) {
		if (index != 0) {
			throw new IllegalArgumentException("Index: " + index);
		} else {
			return getName();
		}
	}

	public final Object[] getTabularStatColumnHeaders(int index) {
		if (index != 0) {
			throw new IllegalArgumentException("Index: " + index);
		} else {
			return getColumnHeaders();
		}
	}

	public final Object[][] getTabularStatTableData(int index) {
		if (index != 0) {
			throw new IllegalArgumentException("Index: " + index);
		} else {
			return getData();
		}
	}
	
	public final boolean filterRecordForTableStat(int index, Set values, Record r) {
		if (index != 0) {
			throw new IllegalArgumentException("Index: " + index);
		} else {
			return filterRecord(values, r);
		}
	}

}
