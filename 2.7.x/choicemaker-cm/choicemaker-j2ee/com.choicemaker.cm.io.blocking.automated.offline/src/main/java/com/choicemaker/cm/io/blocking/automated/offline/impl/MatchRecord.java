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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import com.choicemaker.cm.io.blocking.automated.offline.data.IMatchRecord;

/**
 * A MatchRecord has a pair of id's and a match probability on this pair. This
 * class is used internally only by MatchRecordSource to create new instances of
 * IMatchRecord.
 * 
 * @author pcheung
 * @deprecated
 */
@Deprecated
class MatchRecord implements Comparable<IMatchRecord>, IMatchRecord {

	private final long recordID1;
	private final long recordID2;
	private final float probability;
	private final char matchType;
	private final char record2Source;

	/**
	 * This constructor takes in these key parameters.
	 * 
	 * @param i1
	 *            - id of the first record
	 * @param i2
	 *            - id of the second record
	 * @param source
	 *            - indicates if the second record is from staging or master
	 * @param f
	 *            - match probability
	 * @param type
	 *            - Match or Hold or Differ
	 */
	public MatchRecord(long i1, long i2, char source, float f, char type) {
		this.recordID1 = i1;
		this.recordID2 = i2;
		this.record2Source = source;
		this.probability = f;
		this.matchType = type;
	}

	/**
	 * This is true if this MatchRecord has the same id pair as the input
	 * MatchRecord.
	 * 
	 * @param mr
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	public boolean equals(IMatchRecord mr) {
		boolean ret = false;
		if (this.getRecordID1() == mr.getRecordID1()
				&& this.getRecordID2() == mr.getRecordID2()
				&& this.getRecord2Source() == mr.getRecord2Source())
			ret = true;
		return ret;
	}

	@Override
	public long getRecordID1() {
		return recordID1;
	}

	@Override
	public long getRecordID2() {
		return recordID2;
	}

	@Override
	public float getProbability() {
		return probability;
	}

	@Override
	public char getMatchType() {
		return matchType;
	}

	@Override
	public char getRecord2Source() {
		return record2Source;
	}

	@Override
	public int compareTo(IMatchRecord mr) {
		int ret = 0;
		if (getRecordID1() < mr.getRecordID1())
			ret = -1;
		else if (getRecordID1() > mr.getRecordID1())
			ret = 1;
		else if (getRecordID1() == mr.getRecordID1()) {
			if (getRecordID2() < mr.getRecordID2())
				ret = -1;
			else if (getRecordID2() > mr.getRecordID2())
				ret = 1;
			else if (getRecordID2() == mr.getRecordID2())
				ret = 0;
		}
		return ret;
	}

	/**
	 * This is true if this Object is a MatchRecord and has the same id pair as
	 * the input MatchRecord.
	 * 
	 * @param o
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	@Override
	public boolean equals(Object o) {
		boolean ret = false;

		if (o.getClass() == MatchRecord.class) {
			IMatchRecord p = (IMatchRecord) o;
			if ((getRecordID1() == p.getRecordID1())
					&& (getRecordID2() == p.getRecordID2()))
				ret = true;
		}
		return ret;
	}

	@Override
	public int hashCode() {
		return (int) (getRecordID1() ^ (getRecordID1() >>> 32) ^ getRecordID2() ^ (getRecordID2() >>> 32));
	}

}
