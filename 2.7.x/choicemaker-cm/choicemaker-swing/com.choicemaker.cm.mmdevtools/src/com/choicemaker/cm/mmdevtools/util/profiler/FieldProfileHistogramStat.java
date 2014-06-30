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

import com.choicemaker.cm.core.base.Record;
import com.choicemaker.cm.core.util.IntValuedHashMap;

/**
 * @author Adam Winkel
 */
public class FieldProfileHistogramStat extends AbstractTableStat {

	private FieldAccessor fa;

	private boolean collapseWhitespace;
	private boolean convertAlphaToUpper;
	private boolean convertPuncToSpace;
	
	private IntValuedHashMap profileCounts;
	private int totalRows;
	
	public FieldProfileHistogramStat(FieldAccessor fa) {
		this(fa, true);
	}
	
	public FieldProfileHistogramStat(FieldAccessor fa, boolean collapseWhitespace) {
		this(fa, collapseWhitespace, false, false);
	}
	
	public FieldProfileHistogramStat(FieldAccessor fa,
									 boolean collapseWhitespace,
									 boolean convertAlphaToUpper,
									 boolean convertPuncToSpace) {
		this.fa = fa;
		this.collapseWhitespace = collapseWhitespace;
		this.convertAlphaToUpper = convertAlphaToUpper;
		this.convertPuncToSpace = convertPuncToSpace;
		
		reset();
	}
	
	//
	// DataProfiler interface
	//

	public void reset() {
		profileCounts = new IntValuedHashMap();
		totalRows = 0;
	}

	public void processRecord(Record r) {
		int numRows = fa.getRowCount(r);
		totalRows += numRows;

		for (int i = 0; i < numRows; i++) {
			Object obj = fa.getValue(r, i);
			String val = "";
			if (obj != null) {
				val = obj.toString();
			}
			
			String profile = profile(val, collapseWhitespace, convertAlphaToUpper, convertPuncToSpace);
			profileCounts.increment(profile);
		}		
	}

	public String getName() {
		return "Pattern Histogram";
	}

	public Object[] getColumnHeaders() {
		return new Object[] {"Pattern", "Count", "Percentage", "Length"};
	}

	public Object[][] getData() {
		int len = profileCounts.size();

		Object[][] data = new Object[len][4];
		int idx = 0;
		for (Iterator it = profileCounts.sortedKeys().iterator(); it.hasNext(); ) {
			String profile = (String)it.next();
			Integer count = (Integer)profileCounts.get(profile);
			data[idx][0] = profile;
			data[idx][1] = count;
			data[idx][2] = new Float(count.intValue() / (float)totalRows);
			data[idx][3] = new Integer(profile.length());
			
			idx++;
		}

		return data;
	}

	public boolean filterRecord(Set values, Record r) {
		int numRows = fa.getRowCount(r);
		for (int i = 0; i < numRows; i++) {
			Object obj = fa.getValue(r, i);
			String val = "";
			if (obj != null) {
				val = obj.toString();
			}
				
			String profile = profile(val, collapseWhitespace, convertAlphaToUpper, convertPuncToSpace);
			if (values.contains(profile)) {
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
