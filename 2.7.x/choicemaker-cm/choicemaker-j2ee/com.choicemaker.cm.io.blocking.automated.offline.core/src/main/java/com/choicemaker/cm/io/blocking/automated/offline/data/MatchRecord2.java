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

import java.io.Serializable;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;

/**
 * A MatchRecord has a pair of id's and a match probability on this pair.
 * 
 * Version 2 is more generalized, because it allows for Integer, Long, or String as record IDs.
 * 
 * @author pcheung
 *
 */
public class MatchRecord2<T extends Comparable<T>> implements
		Comparable<MatchRecord2<T>>, Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -3108962876276009775L;

	public static final char MATCH = 'M';
	public static final char DIFFER = 'D';
	public static final char HOLD = 'H';

	public static final char MASTER_SOURCE = 'D';
	public static final char STAGE_SOURCE = 'S';

	/** Probabilities that differ by less than this amount are considered equal */
	public static final float PRECISION = 0.0001f;

	private final T recordID1;
	private final T recordID2;
	private final float probability;
	private final char matchType;
	private final char record2Source;
	private final String notes;

	/** This method concats the notes into a single string delimited by Constants.DELIMITER.
	 * 
	 * @param notes
	 * @return
	 */
	public static String getNotesAsDelimitedString(String[] notes) {
		String retVal = null;
		if (notes != null && notes.length > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < notes.length; i++) {
				sb.append(notes[i]);
				sb.append(Constants.DELIMITER);
			}
			sb.deleteCharAt(sb.length() - 1);
			retVal = sb.toString();
		}
		return retVal;
	}

	/** This method concats the notes into a single string delimited by Constants.DELIMITER.
	 * 
	 * @param notes
	 * @return
	 */
	public static String getNotesAsDelimitedString(
		ActiveClues ac,
		ImmutableProbabilityModel model) {

		String[] notes = ac.getNotes(model);
		String retVal = getNotesAsDelimitedString(notes);
		return retVal;
	}

	/** This constructor takes in these key parameters.
	 * 
	 * @param i1 - id of the first record.  Can be Integer, Long, or String.
	 * @param i2 - id of the second record.  Can be Integer, Long, or String.
	 * @param source - indicates if the second record is from staging or master
	 * @param f - match probability
	 * @param type - Match or Hold or Differ
	 * @param ac - Active clue firings
	 * @param model - The model used to evaluate this pair
	 */
	public MatchRecord2(
		T i1,
		T i2,
		char source,
		float f,
		char type,
		ActiveClues ac,
		ImmutableProbabilityModel model) {
		this(i1, i2, source, f, type, getNotesAsDelimitedString(ac, model));
	}

	/** This constructor takes in these key parameters.
	 * 
	 * @param i1 - id of the first record.  Can be Integer, Long, or String.
	 * @param i2 - id of the second record.  Can be Integer, Long, or String.
	 * @param source - indicates if the second record is from staging or master
	 * @param f - match probability
	 * @param type - Match or Hold or Differ
	 * @param notes - delimited String representing any notes on clues fired by this pair.
	 */
	public MatchRecord2(
		T i1,
		T i2,
		char source,
		float f,
		char type,
		String notes) {
		recordID1 = i1;
		recordID2 = i2;
		record2Source = source;
		probability = f;
		matchType = type;
		this.notes = notes;
	}

	/** This is true if this MatchRecord has the same id pair as the input MatchRecord.
	 * 
	 * It checks that recordID1, recordID2, and record2 source are the same and
	 * probabiility equal to within {@link #PRECISION} 
	 * 
	 * @param mr
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	public boolean equals(MatchRecord2<T> mr) {
		boolean ret = false;
		if (this.recordID1.equals(mr.recordID1)
			&& this.recordID2.equals(mr.recordID2)
			&& this.record2Source == mr.record2Source
			&& Math.abs(this.probability - mr.probability) < PRECISION)
			ret = true;
		return ret;
	}

	public T getRecordID1() {
		return recordID1;
	}

	public T getRecordID2() {
		return recordID2;
	}

	public float getProbability() {
		return probability;
	}

	public char getMatchType() {
		return matchType;
	}

	public char getRecord2Source() {
		return record2Source;
	}

	public String getNotes() {
		return notes;
	}

	public int compareTo(MatchRecord2<T> mr) {
		int ret = 0;

		if (recordID1.compareTo(mr.recordID1) < 0)
			ret = -1;
		else if (recordID1.compareTo(mr.recordID1) > 0)
			ret = 1;
		else if (recordID1.compareTo(mr.recordID1) == 0) {
			if (recordID2.compareTo(mr.recordID2) < 0)
				ret = -1;
			else if (recordID2.compareTo(mr.recordID2) > 0)
				ret = 1;
			else if (recordID2.compareTo(mr.recordID2) == 0) {
				if (record2Source == mr.record2Source)
					ret = 0;
				else if (record2Source < mr.record2Source)
					ret = -1;
				else if (record2Source > mr.record2Source)
					ret = 1;
			}
		}
		return ret;
	}

//	/** This is true if this Object is a MatchRecord and has the same id pair as the input MatchRecord.
//	 * 
//	 * @param o
//	 * @return boolean - true if the ids from both MatchRecords match.
//	 */
//	public boolean equals(Object o) {
//		boolean ret = false;
//
//		if (o.getClass() == MatchRecord2.class) {
//			return equals((MatchRecord2) o);
//		}
//		return ret;
//	}

	public int hashCode() {
		int i1 = recordID1.hashCode();
		int i2 = recordID2.hashCode();

		return i1 ^ i2;
	}

}
