package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;

public interface ImmutableRecordIdTranslatorLocal<T extends Comparable<T>> extends
		ImmutableRecordIdTranslator<T> {
	
	BatchJob getBatchJob();

}
