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
import com.choicemaker.cm.core.util.IntArrayList;

/**
 * @author Adam Winkel
 */
public class DefaultIntProfiler implements FieldProfiler {

	private FieldAccessor fa;

	private int totalRecords;
	private int totalRows;
	private int numInvalid;
	
	private IntArrayList values = new IntArrayList();
	private boolean dirty = true;

	// These properties are computed from the values array above...
	private int numZeros, numNegative, numPositive;
	private int numDistinct, numUnique;
	
	private int minLength, maxLength;
	
	private int min, max;
	private float median;
	private int mode;
	private float mean;
	private float variance, stdDev;
	
	public DefaultIntProfiler(FieldAccessor fa) {
		this.fa = fa;
		if (fa == null) {
			throw new IllegalArgumentException("FieldAccessor cannot be null.");
		}
	}
	
	public void reset() {
		values = new IntArrayList();		
		totalRecords = 0;
		totalRows = 0;
		numInvalid = 0;

		dirty = true;
	}
	
	public void processRecord(Record r) {
		totalRecords++;
		
		int rows = fa.getRowCount(r);
		totalRows += rows;
		
		for (int row = 0; row < rows; row++) {
			Object obj = fa.getValue(r, row);
			int intVal = Integer.MIN_VALUE;
			if (obj instanceof Integer) {
				intVal = ((Integer)obj).intValue();
			} else if (obj instanceof String) {
				intVal = Integer.parseInt((String)obj);
			} else {
				throw new IllegalStateException("Unconvertible type: " + obj.getClass());
			}
			values.add(intVal);
			
			if (!fa.getValidity(r, row)) {
				numInvalid++;
			}
		}
		
		dirty = true;
	}
	
	private void clean() {
		if (dirty) {
			dirty = false;

			values.sort();
			int[] vals = values.toArray();
			if (vals.length != totalRows) {
				throw new IllegalStateException("Something's fishy here...");
			}

			// the default values
			min = 0;
			max = 0;
			median = 0;

			numZeros = 0;
			numNegative = 0;
			numPositive = 0;
			
			minLength = Integer.MAX_VALUE;
			maxLength = Integer.MIN_VALUE;
			
			mode = 0;
			
			numDistinct = 0;
			numUnique = 0;
			
			mean = 0;
			variance = 0;
			stdDev = 0;
			
			if (totalRows > 0) {				
				// min/max
				min = vals[0];
				max = vals[totalRows - 1];

				// median
				if (vals.length % 2 == 1) {
					int idx = (totalRows - 1) / 2;
					median = vals[idx];
				} else {
					int idx1 = totalRows / 2;
					int idx2 = idx1 + 1;
					median = (vals[idx1] + vals[idx2]) / 2.0f;
				}
				
				// total, firstZero, lastZero, mode, numUnique
				int sum = 0;
				int last = Integer.MIN_VALUE;
				int countOfLast = 0;
				int modeCount = 0;
				for (int i = 0; i < totalRows; i++) {
					int val = vals[i];

					// total
					sum += val;
					
					// numZeros, numNegative, numPositive
					if (val > 0) {
						numPositive++;
					} else if (val == 0) {
						numZeros++;
					} else {
						numNegative++;
					}

					// minLength, maxLength					
					int digits = 0;
					int tmp = val;
					while (tmp > 0) {
						digits++;
						tmp = tmp / 10;
					} 
					if (digits < minLength) {
						minLength = digits;
					}
					if (digits > maxLength) {
						maxLength = digits;
					}
					
					// mode
					if (val == last) {
						countOfLast++;
					}
					if (val != last || i + 1 == totalRows ) {
						if (countOfLast > modeCount) {
							mode = last;
							modeCount = countOfLast;
						}
						if (modeCount == 0 && i+1 == totalRows) {
							mode = val;
							modeCount = 1;
						}
					}
					
					// numDistinct
					if (val != last || countOfLast == 0)  { // the second piece is for the first value.
						numDistinct++;
					}
					
					// numUnique
					if (val != last && countOfLast == 1) {
						numUnique++;
					}
					if (val != last && i + 1 == totalRows) { // this is for the last item
						numUnique++;
					}
					
					// update last and countOfLast
					if (val != last) {
						last = val;
						countOfLast = 1;
					}
				}
				
				// mean
				mean = sum / (float)totalRows;
				
				// variance, stdDev
				float varianceTotal = 0;
				for (int i = 0; i < totalRows; i++) {
					int val = vals[i];
					float diff = val - mean;
					varianceTotal += diff*diff;
				}
				variance = varianceTotal / totalRows;
				stdDev = (float) Math.sqrt(variance);
			}
		}
	}
	
	//
	// Stat accessors
	//

	public int getScalarStatCount() {
		return 23;
	}
	
	public String getScalarStatName(int i) {
		switch (i) {
			case 0: return "Total Records";
			case 1: return "Total Rows (" + fa.getRecordName() + ")";
			case 2: return "Num Distinct Values";
			case 3: return "Num Unique Values";
			case 4: return "Num Valid Values";
			case 5: return "    % Valid";
			case 6: return "Num Invalid Values";
			case 7: return "    % Invalid";
			case 8: return "Num Positive Values";
			case 9: return "    % Positive";
			case 10: return "Num Zero Values";
			case 11: return "    % Zero";
			case 12: return "Num Negative Values";
			case 13: return "    % Negative";
			case 14: return "Min Num Digits";
			case 15: return "Max Num Digits";
			case 16: return "Min Value";
			case 17: return "Max Value";
			case 18: return "Mean";
			case 19: return "Median";
			case 20: return "Mode";
			case 21: return "Variance";
			case 22: return "Standard Deviation";
		}
		throw new IllegalArgumentException("Index: " + i);
	}

	public Object getScalarStatValue(int i) {
		clean();
		switch (i) {
			case 0: return new Integer(totalRecords);
			case 1: return new Integer(totalRows);
			case 2: return new Integer(numDistinct);
			case 3: return new Integer(numUnique);
			case 4: return new Integer(totalRows - numInvalid);
			case 5: return FieldProfilerUtils.formatPercent((totalRows - numInvalid) / (float)totalRows);
			case 6: return new Integer(numInvalid);
			case 7: return FieldProfilerUtils.formatPercent(numInvalid / (float)totalRows);
			case 8: return new Integer(numPositive);
			case 9: return FieldProfilerUtils.formatPercent(numPositive / (float)totalRows);
			case 10: return new Integer(numZeros);
			case 11: return FieldProfilerUtils.formatPercent(numZeros / (float)totalRows);
			case 12: return new Integer(numNegative);
			case 13: return FieldProfilerUtils.formatPercent(numNegative / (float)totalRows);
			case 14: return new Integer(minLength);
			case 15: return new Integer(maxLength);
			case 16: return new Integer(min);
			case 17: return new Integer(max);
			case 18: return FieldProfilerUtils.formatDouble(mean);
			case 19: return new Float(median); // no need to format this, as it will always be integral or xxx.5
			case 20: return new Integer(mode);
			case 21: return FieldProfilerUtils.formatDouble(variance);
			case 22: return  FieldProfilerUtils.formatDouble(stdDev);
		}
		throw new IllegalArgumentException("Index: " + i);		
	}

	public boolean filterRecordForScalarStat(int i, Record r) {
		if (i < 0 || i >= getScalarStatCount()) {
			throw new IllegalArgumentException("Index: " + i);
		}
		return false;
	}

	public int getTabularStatCount() {
		return 0;
	}
	
	public String getTabularStatName(int i) {
		throw new IllegalArgumentException("Index: " + i);			
	}
	
	public Object[] getTabularStatColumnHeaders(int i) {
		throw new IllegalArgumentException("Index: " + i);			
	}
	
	public Object[][] getTabularStatTableData(int i) {
		throw new IllegalArgumentException("Index: " + i);			
	}

	public boolean filterRecordForTableStat(int statIndex, Set values, Record r) {
		throw new IllegalArgumentException("Index: " + statIndex);
	}

}
