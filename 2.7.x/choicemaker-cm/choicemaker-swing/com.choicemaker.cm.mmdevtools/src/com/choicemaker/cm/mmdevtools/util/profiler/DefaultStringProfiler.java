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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import com.choicemaker.cm.core.Record;

/**
 * @author Adam Winkel
 */
public class DefaultStringProfiler implements FieldProfiler {

	private FieldAccessor fa;

	private int totalRecords;
	private int totalRows;

	private StatAccumulator[] statAccumulators;
		
	private FieldValueHistogramStat fvhStat;
	private FieldProfileHistogramStat fphStat;
	private FieldLengthHistogramStat flhStat;
	private FieldIdfStat idfStat;

	public DefaultStringProfiler(FieldAccessor fa) {
		this.fa = fa;
		if (fa == null) {
			throw new IllegalArgumentException("FieldAccessor cannot be null.");
		}
		
		statAccumulators = new StatAccumulator[] {
			// min/max length
			new LengthBoundStatAccumulator(fa, true),
			new LengthBoundStatAccumulator(fa, false),
			
			// invalid and null
			new ValidityStatAccumulator(fa, false),
			new NullStatAccumulator(fa, true),
			
			// these are bucket stats.
			new BucketStatAccumulator(fa, "Num Empty", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == 0); } }),
			new BucketStatAccumulator(fa, "Num Whitespace Only", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == SPACE); } }),
			new BucketStatAccumulator(fa, "Num Punctuation (and maybe Whitespace)", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == OTHER || bucket == OTHER + SPACE); } }),
			new BucketStatAccumulator(fa, "Num Digits Only", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == DIGITS); } }),
			new BucketStatAccumulator(fa, "Num Digits and Other Stuff (but not Letters)", 
				new BucketTest() { public boolean testBucket(int bucket) { return ((bucket & DIGITS) == 1 && (bucket & LETTERS) == 0); } }),
			new BucketStatAccumulator(fa, "Num Letters Only", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == LETTERS); } }),
			new BucketStatAccumulator(fa, "Num Letters and Whitespace Only", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == LETTERS + SPACE); } }),
			new BucketStatAccumulator(fa, "Num Letters and Punctuation (but not Digits)", 
				new BucketTest() { public boolean testBucket(int bucket) { return ((bucket & LETTERS) != 0 && (bucket & OTHER) != 0 && (bucket & DIGITS) == 0); } }),
			new BucketStatAccumulator(fa, "Num Letters and Digits Only", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == LETTERS + DIGITS); } }),
			new BucketStatAccumulator(fa, "Num Letters, Digits, and Whitespace (but not Punctuation)", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == LETTERS + DIGITS + SPACE); } }),
			new BucketStatAccumulator(fa, "Num Letters, Digits, Whitespace, and Punctuation", 
				new BucketTest() { public boolean testBucket(int bucket) { return (bucket == LETTERS + DIGITS + SPACE + OTHER); } }),	
				
			// these are char set stats	
			new CharSetStatAccumulator(fa, UPPER),
			new CharSetStatAccumulator(fa, LOWER),
			new CharSetStatAccumulator(fa, NON_ROMAN),
			new CharSetStatAccumulator(fa, OTHER_UNICODE),
			
			// parsability stats
			new ParsabilityStatAccumulator(fa, INTEGER),
			new ParsabilityStatAccumulator(fa, REAL)
		};
				
		fvhStat = new FieldValueHistogramStat(fa);
		fphStat = new FieldProfileHistogramStat(fa);
		flhStat = new FieldLengthHistogramStat(fa);
		idfStat = new FieldIdfStat(fa);
	}
	
	public void reset() {
		totalRecords = 0;
		totalRows = 0;
				
		for (int i = 0; i < statAccumulators.length; i++) {
			statAccumulators[i].reset();
		}
		
		fvhStat.reset();
		fphStat.reset();
		flhStat.reset();
		idfStat.reset();
	}
	
	public void processRecord(Record r) {
		totalRecords++;
		
		int rows = fa.getRowCount(r);
		totalRows += rows;
				
		for (int i = 0; i < statAccumulators.length; i++) {
			statAccumulators[i].processRecord(r);
		}
				
		fvhStat.processRecord(r);				
		fphStat.processRecord(r);
		flhStat.processRecord(r);
		idfStat.processRecord(r);
	}
	
	//
	// Stat accessors
	//

	public int getScalarStatCount() {
		return 25;
	}
	
	public String getScalarStatName(int i) {
		switch (i) {
			case 0: return "Total Records";
			case 1: return "Total Rows (" + fa.getRecordName() + ")";
			case 2: return fvhStat.getScalarStatName(0);
			case 3: return fvhStat.getScalarStatName(1);
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
				return statAccumulators[i-4].getName();
		}
		throw new IllegalArgumentException("Index: " + i);
	}

	public Object getScalarStatValue(int i) {
		switch (i) {
			case 0: return new Integer(totalRecords);
			case 1: return new Integer(totalRows);
			case 2: return fvhStat.getScalarStatValue(0);
			case 3: return fvhStat.getScalarStatValue(1);
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
				return new Integer(statAccumulators[i-4].getCount());
		}
		throw new IllegalArgumentException("Index: " + i);		
	}

	public boolean filterRecordForScalarStat(int i, Record r) {
		switch (i) {
			case 0:
			case 1:
			case 2:
			case 3:
				return false;
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
				return statAccumulators[i-4].filterRecord(r);
		}
		throw new IllegalArgumentException("Index: " + i);
	}

	public int getTabularStatCount() {
		return 4;
	}
	
	public String getTabularStatName(int i) {
		switch (i) {
			case 0: return fvhStat.getTabularStatName(0);
			case 1: return fphStat.getName();
			case 2: return flhStat.getName();
			case 3: return idfStat.getName();
		}
		
		throw new IllegalArgumentException("Index: " + i);			
	}
	
	public Object[] getTabularStatColumnHeaders(int i) {
		switch (i) {
			case 0: return fvhStat.getTabularStatColumnHeaders(0);
			case 1: return fphStat.getColumnHeaders();
			case 2: return flhStat.getColumnHeaders();
			case 3: return idfStat.getColumnHeaders();
		}
		
		throw new IllegalArgumentException("Index: " + i);			
	}
	
	public Object[][] getTabularStatTableData(int i) {
		switch (i) {
			case 0: return fvhStat.getTabularStatTableData(0);
			case 1: return fphStat.getData();
			case 2: return flhStat.getData();
			case 3: return idfStat.getData();
		}
		throw new IllegalArgumentException("Index: " + i);			
	}

	public boolean filterRecordForTableStat(int statIndex, Set values, Record r) {
		switch (statIndex) {
			case 0: return fvhStat.filterRecordForTableStat(0, values, r);
			case 1: return fphStat.filterRecord(values, r);
			case 2: return flhStat.filterRecord(values, r);
			case 3: return false; // we don't filter by IDF
		}
		throw new IllegalArgumentException("Index: " + statIndex);
	}

	//
	// StatAccumulator def
	//
	
	private static interface StatAccumulator {
		public void reset();
		public void processRecord(Record r);
		public String getName();
		public int getCount();
		public boolean filterRecord(Record r);
	}
	
	private static abstract class DefaultStatAccumulator implements StatAccumulator {
		
		private FieldAccessor fa;
		private String name;
		private int count;

		public DefaultStatAccumulator(FieldAccessor fa, String name) {
			this.fa = fa;
			this.name = name;
			reset();
		}

		// only thing need to implement!
		protected abstract boolean testString(String s);
		
		public void reset() {
			count = 0;
		}
		
		public String getName() {
			return name;
		}
		
		public int getCount() {
			return count;
		}
		
		public void processRecord(Record r) {
			for (int i = 0, n = fa.getRowCount(r); i < n; i++) {
				Object obj = fa.getValue(r, i);
				if (obj != null && testString(obj.toString())) {
					count++;
				}
			}
		}
		
		public boolean filterRecord(Record r) {
			for (int i = 0, n = fa.getRowCount(r); i < n; i++) {
				Object obj = fa.getValue(r, i);
				if (obj != null && testString(obj.toString())) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	//
	// Min/Max Length
	//
	
//	private static final int MIN = -111;
//	private static final int MAX = -123;
	
	private static class LengthBoundStatAccumulator implements StatAccumulator {
		private FieldAccessor fa;
		private boolean isMin;
		private int val;
		public LengthBoundStatAccumulator(FieldAccessor fa, boolean isMin) {
			this.fa = fa;
			this.isMin = isMin;
			reset();
		}
		public void reset() {
			if (isMin) {
				val = Integer.MAX_VALUE;
			} else {
				val = Integer.MIN_VALUE;
			}
		}
		public void processRecord(Record r) {
			for (int i = 0, n = fa.getRowCount(r); i < n; i++) {
				int len = getLength(fa.getValue(r, i));
				if (isMin && len < val) {
					val = len;
				} else if (!isMin && len > val) {
					val = len;
				}
			}
		}
		protected int getLength(Object obj) {
			if (obj == null) {
				return 0;
			} else {
				return obj.toString().length();
			}
		}
		public String getName() {
			if (isMin) {
				return "Min Length";
			} else {
				return "Max Length";
			}
		}
		public int getCount() {
			return val;
		}
		public boolean filterRecord(Record r) {
			for (int i = 0, n = fa.getRowCount(r); i < n; i++) {
				int len = getLength(fa.getValue(r, i));
				if (len == getCount()) {
					return true;
				}
			}
			return false;
		}		
	}
	
	//
	// Invalid/Null
	//
	
	private static class ValidityStatAccumulator implements StatAccumulator {
		private FieldAccessor fa;
		private boolean validity;
		private int count;
		public ValidityStatAccumulator(FieldAccessor fa, boolean validity) {
			this.fa = fa;
			this.validity = validity;
			reset();
		}
		public void reset() {
			count = 0;
		}
		public void processRecord(Record r) {
			for (int i = 0, n = fa.getRowCount(r); i < n; i++) {
				if (fa.getValidity(r, i) == validity) {
					count++;
				}
			}
		}
		public String getName() {
			if (validity) {
				return "Num Valid Values";
			} else {
				return "Num Invalid Values";
			}
		}
		public int getCount() {
			return count;
		}
		public boolean filterRecord(Record r) {
			for (int i = 0, n = fa.getRowCount(r); i < n; i++) {
				if (fa.getValidity(r, i) == validity) {
					return true;
				}
			}
			return false;
		}
	}
	
	private static class NullStatAccumulator implements StatAccumulator {
		private FieldAccessor fa;
		private boolean nullness;
		private int count;
		public NullStatAccumulator(FieldAccessor fa, boolean nullness) {
			this.fa = fa;
			this.nullness = nullness;
		}
		public void reset() {
			count = 0;
		}
		public String getName() {
			if (nullness) {
				return "Num Null Values";
			} else {
				return "Num Non-Null Values";
			}
		}
		public void processRecord(Record r) {
			for (int i = 0, n = fa.getRowCount(r); i < n; i++) {
				Object obj = fa.getValue(r, i);
				if ((obj == null) == nullness) {
					count++;
				}
			}
		}
		public int getCount() {
			return count;
		}		
		public boolean filterRecord(Record r) {
			for (int i = 0, n = fa.getRowCount(r); i < n; i++) {
				Object obj = fa.getValue(r, i);
				if ((obj == null) == nullness) {
					return true;
				}
			}
			return false;
		}

	}
	
	//
	// Bucket Stat stuff
	//

	public interface BucketTest {
		public boolean testBucket(int bucket);
	}

	private static final int LETTERS = 8;
	private static final int DIGITS = 4;
	private static final int SPACE = 1;
	private static final int OTHER = 2;
	
	private static class BucketStatAccumulator extends DefaultStatAccumulator {
		private BucketTest test;
		public BucketStatAccumulator(FieldAccessor fa, String name, BucketTest test) {
			super(fa, name);
			this.test = test;
		}
		protected boolean testString(String s) {
			int bucket = bucketString(s);
			return test.testBucket(bucket);
		}
		private int bucketString(String s) {
			int ret = 0;
			for (int i = 0, n = s.length(); i < n; i++) {
				char c = s.charAt(i);
				if (Character.isLetter(c)) {
					ret |= LETTERS;
				} else if (Character.isDigit(c)) {
					ret |= DIGITS;
				} else if (Character.isWhitespace(c)) {
					ret |= SPACE;
				} else {
					ret |= OTHER;
				}			
			}
		
			return ret;
		}
	}
				
	private static final int UPPER = 8;
	private static final int LOWER = 4;
	private static final int NON_ROMAN = 2;
	private static final int OTHER_UNICODE = 1;

	public static class CharSetStatAccumulator extends DefaultStatAccumulator {
		private int test;
		public CharSetStatAccumulator(FieldAccessor fa, int test) {
			super(fa, getCharSetTestName(test));
			this.test = test;
		}
		protected boolean testString(String s) {
			int bucket = bucketString(s);
			return (bucket & test) != 0;
		}
		private static int bucketString(String s) {
			int ret = 0;
			for (int i = 0, n = s.length(); i < n; i++) {
				char c = s.charAt(i);
				if (Character.isLetter(c)) {
					if (Character.isUpperCase(c)) {
						ret |= UPPER;
					} else if (Character.isLowerCase(c)) {
						ret |= LOWER;
					}
					
					if (!('A' <= c && 'Z' >= c) && !('a' <= c && 'z' >= c)) {
						if (c < 256) {
							ret |= NON_ROMAN;
						} else {
							ret |= OTHER_UNICODE;
						}
					}
				}			
			}
		
			return ret;			
		}
		private static String getCharSetTestName(int test) {
			switch (test) {
				case UPPER: return "Contain Uppercase Alphas";
				case LOWER: return "Contain Lowercase Alphas";
				case NON_ROMAN: return "Contain Non-Roman (ANSI) Alphas";
				case OTHER_UNICODE: return "Contain Other (Unicode) Alphas";
			}
			throw new IllegalStateException();
		}
	}
	
	//
	// Parsable as Real/Integer
	//

	private static final int REAL = -23;
	private static final int INTEGER = -24;
	
	public static class ParsabilityStatAccumulator extends DefaultStatAccumulator {
		private int test;
		public ParsabilityStatAccumulator(FieldAccessor fa, int test) {
			super(fa, getTestName(test));
			this.test = test;
		}
		protected boolean testString(String s) {
			s = s.trim();
			if (test == INTEGER) {
				return canBeBigInteger(s);
			} else {
				return !canBeBigInteger(s) && canBeBigDecimal(s);
			}
		}
		private static boolean canBeBigDecimal(String sTrim) {
			try {
				new BigDecimal(sTrim);
				return true;
			} catch (NumberFormatException ex) {
				return false;
			}
		}
		private static boolean canBeBigInteger(String sTrim) {
			try {
				new BigInteger(sTrim);
				return true;
			} catch (NumberFormatException ex) {
				return false;
			}
		}
		private static String getTestName(int test) {
			switch (test) {
				case INTEGER: return "Num Parsable as Integer";
				case REAL: return "Num Parsable as Real (but not Integer)";
			}
			throw new IllegalStateException();
		}
	}

}
