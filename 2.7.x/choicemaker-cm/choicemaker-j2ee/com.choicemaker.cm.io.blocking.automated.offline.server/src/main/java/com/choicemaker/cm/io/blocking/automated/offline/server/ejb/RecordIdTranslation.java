package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.io.Serializable;

import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE;

public interface RecordIdTranslation<T extends Comparable<T>> extends
		Serializable, Comparable<RecordIdTranslation<T>> {

	/** Translated identifiers are non-negative */
	long INVALID_TRANSLATED_ID = -1;
	
	/** Persistence id */
	long getId();

	/** Job identifier */
	long getJobId();

	/** Translated id (job-specific) */
	long getTranslatedId();

	/** Record id */
	T getRecordId();

	/** Record source -- staging or master */
	RECORD_SOURCE getRecordSource();

	/** Record id type -- INTEGER, LONG or STRING */
	RECORD_ID_TYPE getRecordType();

}
