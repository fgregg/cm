package com.choicemaker.cm.transitivity.server.ejb;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.transitivity.core.TransitivityEvent;
import com.choicemaker.cm.transitivity.core.TransitivityEventLog;
import com.choicemaker.cm.transitivity.server.impl.TransitivityProcessingEventEntity;

@Local
public interface TransitivityProcessingController {

	List<TransitivityProcessingEventEntity> findAllTransitivityProcessingEvents();

	List<TransitivityProcessingEventEntity> findTransitivityProcessingEventsByJobId(long id);

	/** Returns a count of the number of events deleted */
	int deleteTransitivityProcessingEventsByJobId(long id);

	TransitivityEventLog getProcessingLog(TransitivityJob job);

	void updateStatusWithNotification(TransitivityJob job, TransitivityEvent event,
			Date timestamp, String info);

	TransitivityEvent getCurrentTransitivityEvent(TransitivityJob oabaJob);

}