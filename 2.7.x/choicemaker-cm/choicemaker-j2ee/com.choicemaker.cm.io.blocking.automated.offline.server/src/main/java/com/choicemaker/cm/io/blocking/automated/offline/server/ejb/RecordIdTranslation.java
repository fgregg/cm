package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.io.Serializable;

import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;

public interface RecordIdTranslation<T extends Comparable<T>> extends
		Serializable, Comparable<RecordIdTranslation<T>> {

	/** Translated identifiers are non-negative */
	int INVALID_TRANSLATED_ID = -1;

	/** Persistence id */
	long getId();

	/** Job identifier */
	long getJobId();

	/** Translated id (job-specific) */
	int getTranslatedId();

	/** Record id */
	T getRecordId();

	RECORD_SOURCE_ROLE getRecordSourceRole();

	RECORD_ID_TYPE getRecordIdType();

}
