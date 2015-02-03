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

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.BatchProcessingLogEntry;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingEvent;

/**
 * This is the EJB implementation of the OABA OabaProcessing interface.
 * 
 * @author pcheung
 *
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
public class OabaProcessingEventEntity extends BatchProcessingLogEntry
		implements OabaProcessingEvent {

	private static final long serialVersionUID = 271L;

	// -- Construction

	/** Required by JPA; do not invoke directly */
	protected OabaProcessingEventEntity() {
		super();
	}

	public OabaProcessingEventEntity(BatchJob job, OabaEvent status) {
		this(job, status, null);
	}

	public OabaProcessingEventEntity(BatchJob job, OabaEvent event, String info) {
		super(job.getId(), DISCRIMINATOR_VALUE, event.name(), event.eventId,
				event.percentComplete, info);
	}

	@Override
	public OabaEvent getOabaEvent() {
		OabaEvent retVal = OabaEvent.valueOf(this.getEventName());
		assert retVal != null;
		return retVal;
	}

}
