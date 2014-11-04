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

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.*;

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

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaBatchJobProcessing;

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
public class OabaBatchJobProcessingBean implements OabaBatchJobProcessing, Serializable {

	private static final long serialVersionUID = 271L;
	
	public static final long INVALID_OABAPROCESSING_ID = 0;

	public static boolean isPersistent(OabaBatchJobProcessing p) {
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
	
	@Column(name = CN_STATUS_ID)
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
	protected OabaBatchJobProcessingBean() {
		this.jobId = BatchJobBean.INVALID_BATCHJOB_ID;
		this.type = OabaProcessingJPA.DISCRIMINATOR_VALUE;
		this.version = OabaBatchJobProcessing.VERSION;
	}

	public OabaBatchJobProcessingBean(long jobId) {
		this(OabaProcessingJPA.DISCRIMINATOR_VALUE, jobId, OabaProcessing.INIT, null, VERSION);
	}

	public OabaBatchJobProcessingBean(BatchJob job) {
		this(OabaProcessingJPA.DISCRIMINATOR_VALUE, job.getId(), OabaProcessing.INIT, null, VERSION);
	}

	public OabaBatchJobProcessingBean(BatchJob job, OabaProcessing status, String info) {
		this(OabaProcessingJPA.DISCRIMINATOR_VALUE, job.getId(), status.getCurrentProcessingEvent(), info, VERSION);
	}

	protected OabaBatchJobProcessingBean(String type, long jobId, int statusId, String info,
			long version) {
		if (type == null) {
			throw new IllegalArgumentException("null type");
		}
		type = type.trim();
		if (type.isEmpty()) {
			throw new IllegalArgumentException("blank type");
		}
		if (BatchJobBean.isInvalidBatchJobId(jobId)) {
			throw new IllegalArgumentException("non-persistent job id: " + jobId);
		}
		this.type = type;
		this.jobId = jobId;
		this.eventId = statusId;
		this.info = info;
		this.version = version;
		this.timestamp = new Date();
	}

	@Override
	public int getCurrentProcessingEvent() {
		return eventId;
	}

	@Override
	public void setCurrentProcessingEvent(int stat) {
		setCurrentProcessingEvent(stat,null);
	}

	@Override
	public void setCurrentProcessingEvent(int stat, String info) {
		this.eventId = stat;
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
