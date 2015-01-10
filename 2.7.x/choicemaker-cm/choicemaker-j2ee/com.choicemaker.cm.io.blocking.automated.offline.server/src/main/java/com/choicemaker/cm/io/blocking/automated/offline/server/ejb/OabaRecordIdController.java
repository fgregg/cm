package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

@Local
public interface OabaRecordIdController {

	void save(OabaJob job, IRecordIDTranslator2<?> translator)
			throws BlockingException;

	RECORD_ID_TYPE getTranslatorType(OabaJob job) throws BlockingException;

	IRecordIDTranslator2<?> restoreIRecordIDTranslator(OabaJob job)
			throws BlockingException;

}
