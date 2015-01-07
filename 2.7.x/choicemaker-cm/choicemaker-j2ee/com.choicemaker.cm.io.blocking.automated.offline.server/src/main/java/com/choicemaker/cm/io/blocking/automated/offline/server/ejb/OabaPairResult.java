package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.io.Serializable;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE;

public interface OabaPairResult<T extends Comparable<T>> extends
	Serializable {

	/** Default id value for non-persistent pair results */
	long INVALID_ID = 0;

	long getId();

	long getJobId();

	Class<T> getRecordIdType();

	T getRecord1Id();

	T getRecord2Id();

	RECORD_SOURCE getRecord2Source();

	float getProbability();

	Decision getDecision();

	String[] getNotes();

	String getPairSHA1();

//	/** TransitivityPairResult */
//	String getEquivalenceClassSHA1();

	String exportToString();

}