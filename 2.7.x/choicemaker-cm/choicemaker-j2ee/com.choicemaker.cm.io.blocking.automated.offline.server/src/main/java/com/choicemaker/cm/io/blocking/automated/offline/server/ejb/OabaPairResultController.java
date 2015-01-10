package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

@Local
public interface OabaPairResultController {

	void saveResults(OabaJob job, IMatchRecord2Source<?> results)
			throws BlockingException;

	RECORD_ID_TYPE getResultType(OabaJob job) throws BlockingException;

	IMatchRecord2Source<?> restoreResults(OabaJob job)
			throws BlockingException;

}
