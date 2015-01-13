package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

@Local
public interface RecordIdController {

	void save(BatchJob job, IRecordIdTranslator2<?> translator)
			throws BlockingException;

	IRecordIdSinkSourceFactory getRecordIdSinkSourceFactory(BatchJob job);

	RECORD_ID_TYPE getTranslatorType(BatchJob job) throws BlockingException;

	IRecordIdTranslator2<?> getRecordIdTranslator(BatchJob oabaJob)
			throws BlockingException;

	IRecordIdTranslator2<?> restoreIRecordIdTranslator(BatchJob job)
			throws BlockingException;

}
