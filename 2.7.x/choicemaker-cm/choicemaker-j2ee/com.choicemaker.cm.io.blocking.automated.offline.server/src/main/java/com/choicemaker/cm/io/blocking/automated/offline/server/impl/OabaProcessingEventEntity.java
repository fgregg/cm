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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEventJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEventJPA.JPQL_OABAPROCESSING_DELETE_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEventJPA.JPQL_OABAPROCESSING_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEventJPA.JPQL_OABAPROCESSING_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEventJPA.QN_OABAPROCESSING_DELETE_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEventJPA.QN_OABAPROCESSING_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEventJPA.QN_OABAPROCESSING_FIND_BY_JOBID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.BatchProcessingEventEntity;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaBatchProcessingEvent;

/**
 * This is the EJB implementation of the OABA BatchProcessingEventEntity
 * interface.
 */
@NamedQueries({
		@NamedQuery(name = QN_OABAPROCESSING_FIND_ALL,
				query = JPQL_OABAPROCESSING_FIND_ALL),
		@NamedQuery(name = QN_OABAPROCESSING_FIND_BY_JOBID,
				query = JPQL_OABAPROCESSING_FIND_BY_JOBID),
		@NamedQuery(name = QN_OABAPROCESSING_DELETE_BY_JOBID,
				query = JPQL_OABAPROCESSING_DELETE_BY_JOBID) })
@Entity
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public class OabaProcessingEventEntity extends BatchProcessingEventEntity
		implements OabaBatchProcessingEvent {

	private static final long serialVersionUID = 271L;

	// -- Construction

	/** Required by JPA; do not invoke directly */
	protected OabaProcessingEventEntity() {
		super();
	}

	public OabaProcessingEventEntity(BatchJob job, ProcessingEvent status) {
		this(job, status, null);
	}

	public OabaProcessingEventEntity(BatchJob job, ProcessingEvent event,
			String info) {
		super(job.getId(), DISCRIMINATOR_VALUE, event.getEventName(), event
				.getEventId(), event.getPercentComplete(), info);
	}

	@Override
	public ProcessingEvent getProcessingEvent() {
		ProcessingEvent retVal =
			new OabaProcessingEvent(getEventName(), getEventSequenceNumber(),
					getFractionComplete());
		return retVal;
	}

}
