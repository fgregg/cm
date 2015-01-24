package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

@Local
public interface RecordIdController extends IRecordIdFactory {

	/**
	 * Saves the translations of a mutable translator to persistent storage (if
	 * they are not saved already) and returns an immutable translator that uses
	 * the saved translations. Translations can be saved only once, and once
	 * saved, they are effectively immutable.
	 * 
	 * @param job
	 *            the batch job to which this translator applies
	 * @param translator
	 *            a non-null mutable translator
	 * @return a non-null immutable translator whose translation are stored in
	 *         persistent storage.
	 * @throws BlockingException
	 *             if an immutable translator can not be created from the
	 *             specified translator.
	 */
	<T extends Comparable<T>> ImmutableRecordIdTranslatorLocal<T> save(
			BatchJob job, ImmutableRecordIdTranslator<T> translator)
			throws BlockingException;

	IRecordIdSinkSourceFactory getRecordIdSinkSourceFactory(BatchJob job);

	@Deprecated
	RECORD_ID_TYPE getTranslatorType(BatchJob job) throws BlockingException;

	MutableRecordIdTranslator<?> createMutableRecordIdTranslator(BatchJob job)
			throws BlockingException;

	<T extends Comparable<T>> List<RecordIdTranslation<T>> findAllRecordIdTranslations();

	<T extends Comparable<T>> ImmutableRecordIdTranslatorLocal<T> findRecordIdTranslator(
			BatchJob job) throws BlockingException;

	int deleteTranslationsByJob(BatchJob job);

}
