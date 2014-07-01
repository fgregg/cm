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

import com.choicemaker.cm.core.Record;
import com.choicemaker.util.IntValuedHashMap;

/**
 * @author Adam Winkel
 */
public class FieldLengthHistogramStat extends AbstractTableStat {

	private FieldAccessor fa;
	
	private IntValuedHashMap lengthCounts = new IntValuedHashMap();
	private int totalRows = 0;
	
	public FieldLengthHistogramStat(FieldAccessor fa) {
		this.fa = fa;
	}
		
	//
	// DataProfiler interface
	//

	public void reset() {
		lengthCounts = new IntValuedHashMap();
		totalRows = 0;
	}

	public void processRecord(Record r) {
		int numRows = fa.getRowCount(r);
		totalRows += numRows;

		for (int i = 0; i < numRows; i++) {
			Object obj = fa.getValue(r, i);
			
			int len = 0;
			if (obj != null) {
				len = obj.toString().length();
			}
			
			lengthCounts.increment(new Integer(len));
		}		
	}

	public String getName() {
		return "Length Histogram";
	}

	public Object[] getColumnHeaders() {
		return new Object[] {"Length", "Count", "Percentage"};
	}

	public Object[][] getData() {
		int len = lengthCounts.size();

		Object[][] data = new Object[len][3];
		int idx = 0;
		for (Iterator it = lengthCounts.sortedKeys().iterator(); it.hasNext(); ) {
			Integer length = (Integer)it.next();
			Integer count = (Integer)lengthCounts.get(length);
			data[idx][0] = length;
			data[idx][1] = count;
			data[idx][2] = new Float(count.intValue()/(float)totalRows);
			
			idx++;
		}

		return data;
	}

	public boolean filterRecord(Set values, Record r) {
		int numRows = fa.getRowCount(r);
		for (int i = 0; i < numRows; i++) {
			Object obj = fa.getValue(r, i);
			
			Integer length = new Integer(0);
			if (obj != null) {
				length = new Integer(obj.toString().length());
			}
				
			if (values.contains(length)) {
				return true;
			}
		}

		return false;
	}

	//
	// helpers...
	//
	
	private static StringBuffer buff = new StringBuffer();
	
	private static synchronized String profile(String s, boolean collapseWhitespace, boolean convertAlphaToUpper, boolean convertPuncToSpace) {
		int len = s.length();
		boolean lastIsSpace = false;
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			
			if (Character.isLetter(c)) {
				if (convertAlphaToUpper || Character.isUpperCase(c)) {
					buff.append('A');
				} else {
					buff.append('a');
				}
				lastIsSpace = false;
			} else if (Character.isDigit(c)) {
				buff.append('9');
				lastIsSpace = false;
			} else if (convertPuncToSpace || Character.isWhitespace(c)) {
				if (!collapseWhitespace || !lastIsSpace) {
					buff.append(c);
				}
				lastIsSpace = true;
			} else {
				buff.append(c);
				lastIsSpace = false;
			}
		}
		
		String ret = buff.toString();
		buff.setLength(0);
		
		if (collapseWhitespace) {
			ret = ret.trim();
		}

		return ret;
	}

}
