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

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.CN_EVENT_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.CN_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.CN_INFO;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.CN_JOB_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.CN_TIMESTAMP;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.CN_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.CN_VERSION;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.JPQL_OABAPROCESSING_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.JPQL_OABAPROCESSING_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.QN_OABAPROCESSING_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.QN_OABAPROCESSING_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.TABLE_NAME;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.choicemaker.cm.batch.BatchJob;
//import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;


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
				query = JPQL_OABAPROCESSING_FIND_BY_JOBID) })
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public class OabaProcessingEntity implements OabaJobProcessing, Serializable {

	private static final long serialVersionUID = 271L;
	
	public static final long INVALID_OABAPROCESSING_ID = 0;

	public static boolean isPersistent(OabaJobProcessing p) {
		boolean retVal = false;
		if (p != null && p.getId() != INVALID_OABAPROCESSING_ID) {
			retVal = true;
		}
		return retVal;
	}

	// -- Instance data

	@Id
	@Column(name = CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	private long id;

	@Column(name = CN_TYPE)
	private final String type;
	
	@Column(name = CN_JOB_ID)
	private final long jobId;
	
	@Column(name = CN_EVENT_ID)
	private int eventId;
	
	@Column(name = CN_INFO)
	private String info;
	
	@Column(name = CN_VERSION)
	private final long version;
	
	@Column(name = CN_TIMESTAMP)
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	// -- Construction
	
	/** Required by JPA; do not invoke directly */
	protected OabaProcessingEntity() {
		this.jobId = OabaJobEntity.INVALID_ID;
		this.type = OabaProcessingJPA.DISCRIMINATOR_VALUE;
		this.version = OabaJobProcessing.VERSION;
	}

	public OabaProcessingEntity(long jobId) {
		this(OabaProcessingJPA.DISCRIMINATOR_VALUE, jobId,
				OabaEvent.INIT, null, VERSION);
	}

	public OabaProcessingEntity(BatchJob job) {
		this(OabaProcessingJPA.DISCRIMINATOR_VALUE, job.getId(), OabaEvent.INIT, null, VERSION);
	}

	public OabaProcessingEntity(BatchJob job, OabaEvent status, String info) {
		this(OabaProcessingJPA.DISCRIMINATOR_VALUE, job.getId(), status, info, VERSION);
	}

	protected OabaProcessingEntity(String type, long jobId, OabaEvent event, String info,
			long version) {
		if (type == null) {
			throw new IllegalArgumentException("null type");
		}
		type = type.trim();
		if (type.isEmpty()) {
			throw new IllegalArgumentException("blank type");
		}
		if (BatchJobEntity.isInvalidBatchJobId(jobId)) {
			throw new IllegalArgumentException("non-persistent job id: " + jobId);
		}
		if (event == null) {
			throw new IllegalArgumentException("null event");
		}

		this.type = type;
		this.jobId = jobId;
		this.eventId = event.eventId;
		this.info = info;
		this.version = version;
		this.timestamp = new Date();
	}

	@Override
	public int getCurrentProcessingEventId() {
		return eventId;
	}

	@Override
	public OabaEvent getCurrentProcessingEventObject() {
		OabaEvent retVal = OabaProcessingUtil.getOabaEvent(this.eventId);
		if (retVal == null) {
			throw new IllegalStateException("invalid OABA processing event id: " + this.eventId);
		}
		return retVal;
	}

	@Override
	public void setCurrentProcessingEvent(OabaEvent stat) {
		setCurrentProcessingEvent(stat,null);
	}

	@Override
	public void setCurrentProcessingEvent(OabaEvent stat, String info) {
		if (stat == null) {
			throw new IllegalArgumentException("null event");
		}
		this.eventId = stat.eventId;
		this.info = info;
		this.timestamp = new Date();
	}

	@Override
	public String getAdditionalInfo() {
		return info;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getJobId() {
		return jobId;
	}

	@Override
	public long getVersion() {
		return version;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

}
