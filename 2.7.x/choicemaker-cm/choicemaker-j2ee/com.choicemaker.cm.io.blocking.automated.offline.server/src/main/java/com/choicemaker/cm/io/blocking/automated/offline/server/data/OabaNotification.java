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
package com.choicemaker.cm.io.blocking.automated.offline.server.data;

import java.util.Date;

import com.choicemaker.cm.batch.BatchProcessingNotification;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA;

/**
 * This is the data object that gets passed to the UpdateStatusMDB message bean.
 * 
 * @author pcheung (original version)
 * @rphall rewrote as subclass of BatchProcessingNotification
 *
 */
public class OabaNotification extends BatchProcessingNotification {

	static final long serialVersionUID = 271;

	public OabaNotification(OabaJob job, OabaEvent event, Date timestamp) {
		this(job, event, timestamp, null);
	}

	public OabaNotification(OabaJob job, OabaEvent event, Date timestamp,
			String info) {
		super(job.getId(), OabaJobJPA.DISCRIMINATOR_VALUE,
				event.percentComplete, event.eventId,
				OabaProcessingJPA.DISCRIMINATOR_VALUE, event.name(), timestamp,
				info);
	}

	public OabaNotification(OabaProcessingEvent ope) {
		super(ope.getJobId(), OabaJobJPA.DISCRIMINATOR_VALUE, ope
				.getFractionComplete(), ope.getEventSequenceNumber(),
				OabaProcessingJPA.DISCRIMINATOR_VALUE, ope.getEventName(), ope
						.getEventTimestamp(), ope.getEventInfo());
	}

}
