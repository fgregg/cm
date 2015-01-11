package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

@Local
public interface OabaPairResultController {

	void saveResults(OabaJob job, IMatchRecord2Source<?> results)
			throws BlockingException;

	int getResultCount(OabaJob job) throws BlockingException;

	RECORD_ID_TYPE getResultType(OabaJob job) throws BlockingException;

	void getResults(OabaJob job, IMatchRecord2Sink<?> results)
			throws BlockingException;

}
