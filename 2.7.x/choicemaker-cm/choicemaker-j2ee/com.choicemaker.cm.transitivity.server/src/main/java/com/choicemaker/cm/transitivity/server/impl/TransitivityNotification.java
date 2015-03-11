/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.transitivity.server.impl;

import java.util.Date;

import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchProcessingNotification;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityBatchProcessingEvent;

/**
 * This is the data object that gets passed to the UpdateStatusMDB message bean.
 * 
 * @author pcheung (original version)
 * @rphall rewrote as subclass of BatchProcessingNotification
 *
 */
public class TransitivityNotification extends BatchProcessingNotification {

	static final long serialVersionUID = 271;

	public TransitivityNotification(BatchJob job, ProcessingEvent event,
			Date timestamp) {
		this(job, event, timestamp, null);
	}

	public TransitivityNotification(BatchJob job, ProcessingEvent event,
			Date timestamp, String info) {
		super(job.getId(), TransitivityJobJPA.DISCRIMINATOR_VALUE, event
				.getPercentComplete(), event.getEventId(),
				TransitivityProcessingEventJPA.DISCRIMINATOR_VALUE, event
						.getEventName(), timestamp, info);
	}

	public TransitivityNotification(TransitivityBatchProcessingEvent ope) {
		super(ope.getJobId(), TransitivityJobJPA.DISCRIMINATOR_VALUE, ope
				.getFractionComplete(), ope.getEventSequenceNumber(),
				TransitivityProcessingEventJPA.DISCRIMINATOR_VALUE, ope
						.getEventName(), ope.getEventTimestamp(), ope
						.getEventInfo());
	}

}
