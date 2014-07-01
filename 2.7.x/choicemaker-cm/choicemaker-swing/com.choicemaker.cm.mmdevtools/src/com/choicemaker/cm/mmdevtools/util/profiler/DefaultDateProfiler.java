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

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.util.LongArrayList;

/**
 * @author Adam Winkel
 */
public class DefaultDateProfiler implements FieldProfiler {

	private FieldAccessor fa;

	private int totalRecords;
	private int numNull;
	private int numInvalid;
	
	private LongArrayList values = new LongArrayList();
	private boolean dirty = true;

	// These properties are computed from the values array above...
	private int totalRows;
	private int numZeros, numNegative, numPositive;
	
	private long min, max;
	private double median;
	private long mode;
	private double mean;
	private double variance, stdDev;
	
	private FieldValueHistogramStat fvhStat;
	
	public DefaultDateProfiler(FieldAccessor fa) {
		this.fa = fa;
		if (fa == null) {
			throw new IllegalArgumentException("FieldAccessor cannot be null.");
		}
		
		fvhStat = new FieldValueHistogramStat(fa, false);
	}
	
	public void reset() {
		values = new LongArrayList();

		totalRecords = 0;
		numNull = 0;
		numInvalid = 0;
		
		fvhStat.reset();

		dirty = true;
	}
	
	public void processRecord(Record r) {
		totalRecords++;
		
		int rows = fa.getRowCount(r);
		totalRows += rows;
			
		for (int row = 0; row < rows; row++) {
			Object obj = fa.getValue(r, row);
			
			if (obj != null) {
				values.add(((Date)obj).getTime());
			} else {
				numNull++;
			}
			
			if (!fa.getValidity(r, row)) {
				numInvalid++;
			}
		}
		
		fvhStat.processRecord(r);
		
		dirty = true;
	}
	
	private void clean() {
		if (dirty) {
			dirty = false;

			// the default values
			numZeros = 0;
			numNegative = 0;
			numPositive = 0;
			min = 0;
			max = 0;
			median = 0;
			mode = 0;
			mean = 0;
			stdDev = 0;
			
			long[] values = this.values.toArray();
			Arrays.sort(values);
			
			int nonNull = values.length;
			
			if (nonNull > 0) {
				// min/max
				min = values[0];
				max = values[nonNull - 1];

				// median
				if (totalRows % 2 == 1) {
					int idx = (nonNull - 1) / 2;
					median = values[idx];
				} else {
					int idx1 = nonNull / 2;
					int idx2 = idx1 + 1;
					median = (values[idx1] + values[idx2]) / 2.0f;
				}
				
				// total, firstZero, lastZero, mode, numUnique
				long total = 0;
				long last = Integer.MIN_VALUE;
				int countOfLast = 0;
				int modeCount = 0;
				for (int i = 0; i < nonNull; i++) {
					long val = values[i];

					// total
					total += val;
					
					// numZeros, numNegative, numPositive
					if (val > 0) {
						numPositive++;
					} else if (val == 0) {
						numZeros++;
					} else {
						numNegative++;
					}
					
					// mode
					if (val == last) {
						countOfLast++;
					}
					if (val != last || i + 1 == nonNull ) {
						if (countOfLast > modeCount) {
							mode = last;
							modeCount = countOfLast;
						}
						if (modeCount == 0 && i+1 == nonNull) {
							mode = val;
							modeCount = 1;
						}
					}
										
					// update last and countOfLast
					if (val != last) {
						last = val;
						countOfLast = 1;
					}
				}
				
				// mean
				mean = total / (float)nonNull;
				
				// variance, stdDev
				float varianceTotal = 0;
				for (int i = 0; i < nonNull; i++) {
					long val = values[i];
					double diff = val - mean;
					varianceTotal += diff*diff;
				}
				variance = varianceTotal / nonNull;
				stdDev = (float) Math.sqrt(variance);
			}
		}
	}
	
	//
	// Stat accessors
	//

	public int getScalarStatCount() {
		return 14;
	}
	
	public String getScalarStatName(int i) {
		switch (i) {
			case 0: return "Total Records";
			case 1: return "Total Rows (" + fa.getRecordName() + ")";
			case 2: return fvhStat.getScalarStatName(0);
			case 3: return fvhStat.getScalarStatName(1);
			case 4: return "Num Valid Values";
			case 5: return "    % Valid";
			case 6: return "Num Invalid Values";
			case 7: return "    % Invalid";
			case 8: return "Min Value";
			case 9: return "Max Value";
			case 10: return "Mean";
			case 11: return "Median";
			case 12: return "Mode";
			case 13: return "Standard Deviation";
		}
		throw new IllegalArgumentException("Index: " + i);
	}

	public Object getScalarStatValue(int i) {
		clean();
		switch (i) {
			case 0: return new Integer(totalRecords);
			case 1: return new Integer(totalRows);
			case 2: return fvhStat.getScalarStatValue(0);
			case 3: return fvhStat.getScalarStatValue(1);
			case 4: return new Integer(totalRows - numInvalid);
			case 5: return FieldProfilerUtils.formatPercent((totalRows - numInvalid) / (float)totalRows);
			case 6: return new Integer(numInvalid);
			case 7: return FieldProfilerUtils.formatPercent(numInvalid / (float)totalRows);
			case 8: return DateFormat.getDateInstance().format(new Date(min));
			case 9: return DateFormat.getDateInstance().format(new Date(max));
			case 10: return DateFormat.getDateInstance().format(new Date((long)mean));
			case 11: return DateFormat.getDateInstance().format(new Date((long)median));
			case 12: return DateFormat.getDateInstance().format(new Date(mode));
			case 13: return formatStdDev();
		}
		throw new IllegalArgumentException("Index: " + i);		
	}
	
	private String formatStdDev() {
		if (stdDev <= 0) {
			return "0";
		} else {
			float stdDevInDays = (float) stdDev / (1000 * 60 * 60 * 24);
			int yrs = (int) (stdDevInDays / 365);
			float days = stdDevInDays - (yrs * 365);

			StringBuffer buff = new StringBuffer();
			if (yrs == 1) {
				buff.append("1 year, ");
			} else if (yrs > 1) {
				buff.append(yrs);
				buff.append(" years, ");
			}
			buff.append(FieldProfilerUtils.formatDouble(days));
			buff.append(" days");
			
			return buff.toString();
		}
	}
	
	public boolean filterRecordForScalarStat(int i, Record r) {
		clean();
		return false;
	}

	public int getTabularStatCount() {
		return 1;
	}
	
	public String getTabularStatName(int i) {
		if (i == 0) {
			return fvhStat.getTabularStatName(i);
		}
		throw new IllegalArgumentException("Index: " + i);			
	}
	
	public Object[] getTabularStatColumnHeaders(int i) {
		if (i == 0) {
			return fvhStat.getTabularStatColumnHeaders(i);
		}
		throw new IllegalArgumentException("Index: " + i);			
	}
	
	public Object[][] getTabularStatTableData(int i) {
		if (i == 0) {
			return fvhStat.getTabularStatTableData(i);
		}
		throw new IllegalArgumentException("Index: " + i);			
	}

	public boolean filterRecordForTableStat(int statIndex, Set values, Record r) {
		if (statIndex == 0) {
			return fvhStat.filterRecordForTableStat(statIndex, values, r);
		}
		throw new IllegalArgumentException("Index: " + statIndex);
	}

}
