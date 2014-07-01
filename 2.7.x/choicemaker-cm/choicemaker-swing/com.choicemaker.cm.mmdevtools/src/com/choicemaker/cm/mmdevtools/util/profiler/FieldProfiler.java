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
public interface FieldProfiler {

	public void reset();
	
	/**
	 * Process record <code>r</code>, including its
	 * data in the accumulated stats.
	 */
	public void processRecord(Record r);
	
	/**
	 * Returns the number of single-valued
	 * statistics that this profiler collects.
	 * For example, if we collect the min and max
	 * values for int fields (and nothing else),
	 * this method should return 2.
	 */
	public int getScalarStatCount();
	public String getScalarStatName(int index);
	public Object getScalarStatValue(int index);
	public boolean filterRecordForScalarStat(int statIndex, Record r);

	public int getTabularStatCount();
	public String getTabularStatName(int index);
	public Object[] getTabularStatColumnHeaders(int index);
	public Object[][] getTabularStatTableData(int index);
	public boolean filterRecordForTableStat(int statIndex, Set values, Record r);

}
