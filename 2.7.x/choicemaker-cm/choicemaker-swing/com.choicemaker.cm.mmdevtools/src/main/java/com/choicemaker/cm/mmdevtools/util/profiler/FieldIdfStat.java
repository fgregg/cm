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

import java.util.List;
import java.util.Set;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.matching.gen.tfidf.TfIdfCalculator;

/**
 * @author Adam Winkel
 */
public class FieldIdfStat extends AbstractTableStat {

	private FieldAccessor fa;

	private TfIdfCalculator calc;
//	private int totalRows;
//	private int nonNullRows;
	
	public FieldIdfStat(FieldAccessor fa) {
		this.fa = fa;
		reset();
	}	
	
	//
	// DataProfiler interface
	//

	public void reset() {
		calc = new TfIdfCalculator();
//		totalRows = 0;
//		nonNullRows = 0;
	}

	public void processRecord(Record r) {
		int numRows = fa.getRowCount(r);
//		totalRows += numRows;

		for (int i = 0; i < numRows; i++) {
			Object obj = fa.getValue(r, i);
			if (obj != null) {
//				nonNullRows++;
				String val = obj.toString();

				calc.chunkAndAdd(val);
			}
		}
	}

	public String getName() {
		return "Field IDF";
	}

	public Object[] getColumnHeaders() {
		return new Object[] {"Token", "Count", "IDF", "Length"};
	}

	public Object[][] getData() {
		List keys = calc.getSortedTokens();
		int len = keys.size();
				
		Object[][] data = new Object[len][4];
		for (int i= 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			data[i][0] = key;
			data[i][1] = new Integer(calc.getCount(key));
			data[i][2] = new Double(calc.getIdf(key));
			data[i][3] = new Integer(key.length());
		}

		return data;
	}

	/**
	 * NOTE: we don't filter by token...
	 */
	public boolean filterRecord(Set values, Record r) {
		return false;
	}

}
