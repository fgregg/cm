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
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;

/**
 * A MatchRecord has a pair of id's and a match probability on this pair.
 * 
 * @author pcheung
 *
 */
public class MatchRecord2<T extends Comparable<T>> implements
		Comparable<MatchRecord2<T>>, Serializable {

	private static final long serialVersionUID = 271;

	public static final char MATCH = Decision.MATCH.toSingleChar();
	public static final char DIFFER = Decision.DIFFER.toSingleChar();
	public static final char HOLD = Decision.HOLD.toSingleChar();

	// -- Instance data

	private final T recordID1;
	private final T recordID2;
	private final float probability;
	private final char matchType;
	private final char record2Source;
	private final String notes;

	// -- Constructor

	/**
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
	public MatchRecord2(T i1, T i2, RECORD_SOURCE_ROLE source, float f,
			Decision type, String notes) {
		if (i1 == null || i2 == null || source == null
				|| !MatchRecordUtils.isValidProbability(f) || type == null) {
			throw new IllegalArgumentException("invalid argument");
		}
		recordID1 = i1;
		recordID2 = i2;
		record2Source = source.getCharSymbol();
		probability = f;
		matchType = type.toSingleChar();
		this.notes = notes;
	}

	// -- Accessors

	public Decision getMatchType() {
		return Decision.valueOf(matchType);
	}

	public String[] getNotes() {
		return MatchRecordUtils.notesFromDelimitedString(notes);
	}

	public String getNotesAsDelimitedString() {
		return notes;
	}

	public float getProbability() {
		return probability;
	}

	public RECORD_SOURCE_ROLE getRecord2Role() {
		return RECORD_SOURCE_ROLE.fromSymbol(record2Source);
	}

	public T getRecordID1() {
		return recordID1;
	}

	public T getRecordID2() {
		return recordID2;
	}

	// -- Identity

	public int compareTo(MatchRecord2<T> mr) {
		final int GREATER_THAN = 1;
		final int LESS_THAN = -1;
		final int EQUALS = 0;

		int ret = EQUALS;
		if (recordID1.compareTo(mr.recordID1) < EQUALS)
			ret = LESS_THAN;
		else if (recordID1.compareTo(mr.recordID1) > EQUALS)
			ret = GREATER_THAN;
		else {
			assert recordID1.compareTo(mr.recordID1) == EQUALS;
			assert ret == EQUALS;

			if (recordID2.compareTo(mr.recordID2) < EQUALS)
				ret = LESS_THAN;
			else if (recordID2.compareTo(mr.recordID2) > EQUALS)
				ret = GREATER_THAN;
			else {
				assert recordID2.compareTo(mr.recordID2) == EQUALS;
				assert ret == EQUALS;

				if (record2Source < mr.record2Source)
					ret = LESS_THAN;
				else if (record2Source > mr.record2Source)
					ret = GREATER_THAN;
				else {
					assert record2Source == mr.record2Source;
					assert ret == EQUALS;

					if (probability < mr.probability)
						ret = LESS_THAN;
					else if (probability > mr.probability)
						ret = GREATER_THAN;
					else {
						assert probability == mr.probability;
						assert ret == EQUALS;

						if (matchType < mr.matchType)
							ret = LESS_THAN;
						else if (matchType > mr.matchType)
							ret = GREATER_THAN;
						else {
							assert matchType == mr.matchType;
							assert ret == EQUALS;

							if (notes == null && mr.notes == null)
								ret = EQUALS;
							else if (notes == null && mr.notes != null)
								ret = LESS_THAN;
							else if (notes != null && mr.notes == null)
								ret = GREATER_THAN;
							else if (notes.compareTo(mr.notes) < EQUALS)
								ret = LESS_THAN;
							else if (notes.compareTo(mr.notes) > EQUALS)
								ret = GREATER_THAN;
							else {
								assert notes.compareTo(mr.notes) == EQUALS;
								assert ret == EQUALS;
							}
						}
					}
				}
			}
		}
		assert (ret == EQUALS && this.equals(mr))
				|| (ret != EQUALS && !this.equals(mr));
		return ret;
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
	public String toString() {
		return "MatchRecord2 [recordID1=" + recordID1 + ", recordID2="
				+ recordID2 + ", probability=" + probability + ", matchType="
				+ matchType + "]";
	}

}
