package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.io.Serializable;

import com.choicemaker.cm.core.Decision;

public interface OabaPairResult<T extends Comparable<T>> extends Serializable {

	char EXPORT_FIELD_SEPARATOR = ' ';

	char EXPORT_NOTE_SEPARATOR = '|';

	enum RecordSource {
		STAGING('S'), MASTER('D');
		public final char symbol;

		RecordSource(char c) {
			this.symbol = c;
		}

		public static RecordSource getRecordSource(char c) {
			RecordSource retVal;
			if (c == 'S' || c == 's') {
				retVal = STAGING;
			} else if (c == 'D' || c == 'd') {
				retVal = MASTER;
			} else {
				throw new IllegalArgumentException("invalid symbol: " + c);
			}
			return retVal;
		}
	}

	/** Default id value for non-persistent pair results */
	long INVALID_ID = 0;

	long getId();

	long getJobId();

	Class<T> getRecordIdType();

	T getRecord1Id();

	T getRecord2Id();

	RecordSource getRecord2Source();

	float getProbability();

	Decision getDecision();

	String[] getNotes();

	String export();

//	/** TransitivityPairResult */
//	int getEquivalenceClass();

}