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
public abstract class AbstractScalarStatGroup implements FieldProfiler {

	public abstract void reset();
	public abstract void processRecord(Record r);

	public abstract int getScalarStatCount();
	public abstract String getScalarStatName(int index);
	public abstract Object getScalarStatValue(int index);
	public abstract boolean filterRecordForScalarStat(int index, Record r);

	public int getTabularStatCount() {
		return 0;
	}

	public String getTabularStatName(int index) {
		throw new UnsupportedOperationException();
	}

	public Object[] getTabularStatColumnHeaders(int index) {
		throw new UnsupportedOperationException();
	}

	public Object[][] getTabularStatTableData(int index) {
		throw new UnsupportedOperationException();
	}

	public boolean filterRecordForTableStat(int statIndex, Set values, Record r) {
		throw new UnsupportedOperationException();
	}

}
