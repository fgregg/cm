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
package com.choicemaker.cm.io.blocking.automated.offline.data;

/**
 * A MatchRecord has a pair of id's and a match probability on this pair.
 * 
 * @deprecated
 * 
 * @author pcheung
 *
 */
public class MatchRecord implements Comparable {
	
	public static final char MATCH = 'M';
	public static final char DIFFER = 'D';
	public static final char HOLD = 'H';
	
	long recordID1;
	long recordID2;
	float probability;
	char matchType;
	char record2Source;
	
	
	/** This constructor takes in these key parameters.
	 * 
	 * @param i1 - id of the first record
	 * @param i2 - id of the second record
	 * @param source - indicates if the second record is from staging or master
	 * @param f - match probability
	 * @param type - Match or Hold or Differ
	 */
	public MatchRecord (long i1, long i2, char source, float f, char type) {
		recordID1 = i1;
		recordID2 = i2;
		record2Source = source;
		probability = f;
		matchType = type;
	}
	
	
	/** This is true if this MatchRecord has the same id pair as the input MatchRecord.
	 * 
	 * @param mr
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	public boolean equals (MatchRecord mr) {
		boolean ret = false;
		if (this.recordID1 == mr.recordID1 && this.recordID2 == mr.recordID2 &&
			this.record2Source == mr.record2Source) ret = true;
		return ret;
	}
	
	public long getRecordID1 () {
		return recordID1;
	}

	public long getRecordID2 () {
		return recordID2;
	}
	
	public float getProbability () {
		return probability;
	}
	
	public char getMatchType () {
		return matchType;
	}
	
	public char getRecord2Source () {
		return record2Source;
	}

	public int compareTo(Object o) {
		int ret = 0;
		MatchRecord mr = (MatchRecord) o;
		
		if (recordID1 < mr.recordID1) ret = -1;
		else if (recordID1 > mr.recordID1) ret = 1;
		else if (recordID1 == mr.recordID1) {
			if (recordID2 < mr.recordID2) ret = -1;
			else if (recordID2 > mr.recordID2) ret = 1;
			else if (recordID2 == mr.recordID2) ret = 0; 
		}
		return ret;
	}
	
	
	/** This is true if this Object is a MatchRecord and has the same id pair as the input MatchRecord.
	 * 
	 * @param o
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	public boolean equals (Object o) {
		boolean ret = false;
		
		if (o.getClass() == MatchRecord.class) {
			MatchRecord p = (MatchRecord) o;
			if ((recordID1 == p.recordID1) && (recordID2 == p.recordID2)) ret = true;
		}
		return ret;
	}


	public int hashCode () {
		return (int)(recordID1 ^(recordID1>>>32) ^ recordID2 ^ (recordID2>>>32) );
	}

}
