package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEventEntity;

@Local
public interface OabaProcessingController {

	List<OabaProcessingEventEntity> findProcessingLogEntriesByJobId(long id);

	OabaEventLog getProcessingLog(OabaJob job);

	void updateStatusWithNotification(OabaJob job, OabaEvent event,
			Date timestamp, String info);

	OabaEvent getCurrentOabaEvent(OabaJob oabaJob);

}