package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;

public interface MutableRecordIdTranslatorLocal<T extends Comparable<T>>
		extends MutableRecordIdTranslator<T> {

	BatchJob getBatchJob();

}
