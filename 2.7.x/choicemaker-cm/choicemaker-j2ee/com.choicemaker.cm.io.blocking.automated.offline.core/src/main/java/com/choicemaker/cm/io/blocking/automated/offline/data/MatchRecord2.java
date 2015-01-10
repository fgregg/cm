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

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;

/**
 * A MatchRecord has a pair of id's and a match probability on this pair.
 * 
 * @author pcheung
 *
 */
public class MatchRecord2<T extends Comparable<T>> implements
		Comparable<MatchRecord2<T>>, Serializable {

	static final long serialVersionUID = 271;

	public static final char MATCH = Decision.MATCH.toSingleChar();
	public static final char DIFFER = Decision.DIFFER.toSingleChar();
	public static final char HOLD = Decision.HOLD.toSingleChar();

	public static final char ROLE_MASTER = RECORD_SOURCE_ROLE.MASTER.getCharSymbol();
	public static final char ROLE_STAGING = RECORD_SOURCE_ROLE.STAGING.getCharSymbol();

	private final T recordID1;
	private final T recordID2;
	private final float probability;
	private final char matchType;
	private final char record2Source;
	private final String notes;

	/**
	 * This method concatenates the notes into a single string delimited by
	 * Constants.EXPORT_NOTE_SEPARATOR.
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
				sb.append(Constants.EXPORT_NOTE_SEPARATOR);
			}
			sb.deleteCharAt(sb.length() - 1);
			retVal = sb.toString();
		}
		return retVal;
	}

	/**
	 * This method concatenates the notes into a single string delimited by
	 * Constants.EXPORT_NOTE_SEPARATOR.
	 * 
	 * @param notes
	 * @return
	 */
	public static String getNotesAsDelimitedString(ActiveClues ac,
			ImmutableProbabilityModel model) {

		String[] notes = ac.getNotes(model);
		String retVal = getNotesAsDelimitedString(notes);
		return retVal;
	}

// // FIXME REMOVEME UNUSED
//	/**
//	 * This constructor takes in these key parameters.
//	 * 
//	 * @param i1
//	 *            - id of the first record. Can be Integer, Long, or String.
//	 * @param i2
//	 *            - id of the second record. Can be Integer, Long, or String.
//	 * @param source
//	 *            - indicates if the second record is from staging or master
//	 * @param f
//	 *            - match probability
//	 * @param type
//	 *            - Match or Hold or Differ
//	 * @param ac
//	 *            - Active clue firings
//	 * @param model
//	 *            - The model used to evaluate this pair
//	 */
//	public MatchRecord2(T i1, T i2, char source, float f, char type,
//			ActiveClues ac, ImmutableProbabilityModel model) {
//		this(i1, i2, source, f, type, getNotesAsDelimitedString(ac, model));
//	}

	/**
	 * This constructor takes in these key parameters.
	 * 
	 * @param i1
	 *            - id of the first record. Can be Integer, Long, or String.
	 * @param i2
	 *            - id of the second record. Can be Integer, Long, or String.
	 * @param source
	 *            - indicates if the second record is from staging or master
	 * @param f
	 *            - match probability
	 * @param type
	 *            - Match or Hold or Differ
	 * @param notes
	 *            - delimited String representing any notes on clues fired by
	 *            this pair.
	 */
	public MatchRecord2(T i1, T i2, char source, float f, char type,
			String notes) {
		recordID1 = i1;
		recordID2 = i2;
		record2Source = source;
		probability = f;
		matchType = type;
		this.notes = notes;
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

	public char getRecord2Role() {
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
		else {
			assert recordID1.compareTo(mr.recordID1) == 0;
			assert ret == 0;

			if (recordID2.compareTo(mr.recordID2) < 0)
				ret = -1;
			else if (recordID2.compareTo(mr.recordID2) > 0)
				ret = 1;
			else {
				assert recordID2.compareTo(mr.recordID2) == 0;
				assert ret == 0;

				if (record2Source < mr.record2Source)
					ret = -1;
				else if (record2Source > mr.record2Source)
					ret = 1;
				else {
					assert record2Source == mr.record2Source;
					assert ret == 0;

					if (probability < mr.probability)
						ret = -1;
					else if (probability > mr.probability)
						ret = 1;
					else {
						assert probability == mr.probability;
						assert ret == 0;

						if (matchType < mr.matchType)
							ret = -1;
						else if (matchType > mr.matchType)
							ret = 1;
						else {
							assert matchType == mr.matchType;
							assert ret == 0;

							if (notes == null && mr.notes == null)
								ret = 0;
							else if (notes == null && mr.notes != null)
								ret = -1;
							else if (notes != null && mr.notes == null)
								ret = 1;
							else if (notes.compareTo(mr.notes) < 0)
								ret = -1;
							else if (notes.compareTo(mr.notes) > 0)
								ret = 1;
							else {
								assert notes.compareTo(mr.notes) == 0;
								assert ret == 0;
							}
						}
					}
				}
			}
		}
		assert (ret == 0 && this.equals(mr)) || (ret != 0 && !this.equals(mr));
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + matchType;
		result = prime * result + ((notes == null) ? 0 : notes.hashCode());
		result = prime * result + Float.floatToIntBits(probability);
		result = prime * result + record2Source;
		result =
			prime * result + ((recordID1 == null) ? 0 : recordID1.hashCode());
		result =
			prime * result + ((recordID2 == null) ? 0 : recordID2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		MatchRecord2<T> other = (MatchRecord2<T>) obj;
		if (matchType != other.matchType) {
			return false;
		}
		if (notes == null) {
			if (other.notes != null) {
				return false;
			}
		} else if (!notes.equals(other.notes)) {
			return false;
		}
		if (Float.floatToIntBits(probability) != Float
				.floatToIntBits(other.probability)) {
			return false;
		}
		if (record2Source != other.record2Source) {
			return false;
		}
		if (recordID1 == null) {
			if (other.recordID1 != null) {
				return false;
			}
		} else if (!recordID1.equals(other.recordID1)) {
			return false;
		}
		if (recordID2 == null) {
			if (other.recordID2 != null) {
				return false;
			}
		} else if (!recordID2.equals(other.recordID2)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MatchRecord2 [recordID1=" + recordID1 + ", recordID2="
				+ recordID2 + ", probability=" + probability + ", matchType="
				+ matchType + "]";
	}

}
