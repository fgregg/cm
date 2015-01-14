package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

@Local
public interface RecordIdController {

	void save(BatchJob job, MutableRecordIdTranslator<?> translator)
			throws BlockingException;

	IRecordIdSinkSourceFactory getRecordIdSinkSourceFactory(BatchJob job);

	@Deprecated
	RECORD_ID_TYPE getTranslatorType(BatchJob job) throws BlockingException;

	ImmutableRecordIdTranslator<?> getImmutableRecordIdTranslator(BatchJob job)
			throws BlockingException;

	MutableRecordIdTranslator<?> createMutableRecordIdTranslator(BatchJob job)
			throws BlockingException;

}
