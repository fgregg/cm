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
package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;

/**
 * A BatchJobBean tracks the progress of a (long-running) offline blocking
 * process. A successful request goes through a sequence of states: NEW, QUEUED,
 * STARTED, and COMPLETED. A request may be aborted at any point, in which
 * case it goes through the ABORT_REQUESTED and the ABORT states.</p>
 *
 * A long-running process should provide some indication that it is making
 * progress. It can provide
 * this estimate as a fraction between 0 and 100 (inclusive) by updating
 * the getFractionComplete() field.</p>
 * 
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = "batchJobFindAll",
		query = "Select job from BatchJobBean job")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMT_OABA_BATCHJOB")
public class BatchJobBean implements IControl, Serializable, BatchJob {

	private static final long serialVersionUID = 271L;

	private static Logger log = Logger.getLogger(BatchJobBean.class.getName());

	public static final String TABLE_DISCRIMINATOR = "OABA";
	
	public static enum NamedQuery {
		FIND_ALL("batchJobFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	public static enum STATUS {
		NEW(false), QUEUED(false), STARTED(false), COMPLETED(true),
		FAILED(true), ABORT_REQUESTED(false), ABORTED(true), CLEAR(true);
		public boolean isTerminal;

		private STATUS(boolean terminal) {
			this.isTerminal = terminal;
		}
	}
	
	/** Default value for non-persistent batch jobs */
	private static final long INVALID_BATCHJOB_ID = 0;

	static boolean isInvalidBatchJobId(long id) {
		return id == INVALID_BATCHJOB_ID;
	}
	
	static boolean isNonPersistent(BatchJob batchJob) {
		boolean retVal = true;
		if (batchJob != null) {
			retVal = isInvalidBatchJobId(batchJob.getId());
		}
		return retVal;
	}

	@Id
	@Column(name = "ID")
	@TableGenerator(name = "OABA_BATCHJOB", table = "CMT_SEQUENCE",
			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
			pkColumnValue = "OABA_BATCHJOB")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "OABA_BATCHJOB")
	private long id;

	@Column(name = "EXTERNAL_ID")
	private String externalId;

	@Column(name = "TRANSACTION_ID")
	private long transactionId;

	@Column(name = "TYPE")
	private final String type = TABLE_DISCRIMINATOR;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "FRACTION_COMPLETE")
	private int percentageComplete;

	@Column(name = "STATUS")
	private STATUS status;

	@ElementCollection
	@MapKeyColumn(name = "STATUS")
	@Column(name = "TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	@CollectionTable(name = "CMT_OABA_BATCHJOB_TIMESTAMPS",
			joinColumns = @JoinColumn(name = "BATCHJOB_ID"))
	private Map<STATUS, Date> timestamps = new HashMap<>();

	// -- Construction

	protected BatchJobBean() {
		this(null);
	}

	public BatchJobBean(String externalId) {
		setExternalId(externalId);
		setStatus(STATUS.NEW);
	}

	// -- State machine

	private static Map<STATUS, EnumSet<STATUS>> allowedTransitions =
		new HashMap<>();
	static {
		allowedTransitions.put(STATUS.NEW, EnumSet.of(STATUS.QUEUED,
				STATUS.ABORT_REQUESTED, STATUS.ABORTED));
		allowedTransitions.put(STATUS.QUEUED, EnumSet.of(STATUS.STARTED,
				STATUS.ABORT_REQUESTED, STATUS.ABORTED));
		allowedTransitions.put(STATUS.STARTED, EnumSet.of(STATUS.STARTED,
				STATUS.COMPLETED, STATUS.FAILED, STATUS.ABORT_REQUESTED,
				STATUS.ABORTED));
		allowedTransitions.put(STATUS.ABORT_REQUESTED,
				EnumSet.of(STATUS.ABORTED));
		// Terminal transitions (unless re-queued/re-started)
		allowedTransitions.put(STATUS.COMPLETED, EnumSet.noneOf(STATUS.class));
		allowedTransitions.put(STATUS.FAILED, EnumSet.noneOf(STATUS.class));
		allowedTransitions.put(STATUS.ABORTED, EnumSet.noneOf(STATUS.class));
		allowedTransitions.put(STATUS.CLEAR, EnumSet.noneOf(STATUS.class));
	}

	public static boolean isAllowedTransition(STATUS current, STATUS next) {
		if (current == null || next == null) {
			throw new IllegalArgumentException("null status");
		}
		Set<STATUS> allowed = allowedTransitions.get(current);
		assert allowed != null;
		boolean retVal = allowed.contains(next);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#markAsQueued()
	 */
	@Override
	public void markAsQueued() {
		if (isAllowedTransition(getStatus(), STATUS.QUEUED)) {
			logTransition(STATUS.QUEUED);
			// REMOVE timestamps.put(STATUS.QUEUED, new Date());
			setStatus(STATUS.QUEUED);
		} else {
			logIgnoredTransition("markAsQueued");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#markAsStarted()
	 */
	@Override
	public void markAsStarted() {
		if (isAllowedTransition(getStatus(), STATUS.STARTED)) {
			logTransition(STATUS.QUEUED);
			// REMOVE timestamps.put(STATUS.STARTED, new Date());
			setStatus(STATUS.STARTED);
		} else {
			logIgnoredTransition("markAsStarted");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#markAsReStarted()
	 */
	@Override
	public void markAsReStarted() {
		// REMOVE timestamps.put(STATUS.QUEUED, new Date());
		setStatus(STATUS.QUEUED);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#markAsCompleted()
	 */
	@Override
	public void markAsCompleted() {
		if (isAllowedTransition(getStatus(), STATUS.COMPLETED)) {
			logTransition(STATUS.COMPLETED);
			setPercentageComplete(100);
			// REMOVE timestamps.put(STATUS.COMPLETED, new Date());
			setStatus(STATUS.COMPLETED);
		} else {
			logIgnoredTransition("markAsCompleted");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#markAsFailed()
	 */
	@Override
	public void markAsFailed() {
		if (isAllowedTransition(getStatus(), STATUS.FAILED)) {
			logTransition(STATUS.FAILED);
			// REMOVE timestamps.put(STATUS.FAILED, new Date());
			setStatus(STATUS.FAILED);
		} else {
			logIgnoredTransition("markAsFailed");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#markAsAbortRequested()
	 */
	@Override
	public void markAsAbortRequested() {
		if (isAllowedTransition(getStatus(), STATUS.ABORT_REQUESTED)) {
			logTransition(STATUS.ABORT_REQUESTED);
			// REMOVE timestamps.put(STATUS.ABORT_REQUESTED, new Date());
			setStatus(STATUS.ABORT_REQUESTED);
		} else {
			logIgnoredTransition("markAsAbortRequested");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#markAsAborted()
	 */
	@Override
	public void markAsAborted() {
		if (isAllowedTransition(getStatus(), STATUS.ABORTED)) {
			if (!STATUS.ABORT_REQUESTED.equals(getStatus())) {
				markAsAbortRequested();
			}
			logTransition(STATUS.ABORTED);
			// REMOVE timestamps.put(STATUS.ABORTED, new Date());
			setStatus(STATUS.ABORTED);
		} else {
			logIgnoredTransition("markAsAborted");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#updatePercentageCompleted(float)
	 */
	@Override
	public void updatePercentageCompleted(float percentageCompleted) {
		updatePercentageCompleted((int) percentageCompleted);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#updatePercentageCompleted(int)
	 */
	@Override
	public void updatePercentageCompleted(int percentageCompleted) {
		if (percentageCompleted < 0 || percentageCompleted > 100) {
			String msg =
				"invalid percentageCompleted == '" + percentageCompleted + "'";
			throw new IllegalArgumentException(msg);
		}
		if (isAllowedTransition(getStatus(), STATUS.STARTED)) {
			logTransition(STATUS.STARTED);
			// REMOVE timestamps.put(STATUS.STARTED, new Date());
			setStatus(STATUS.STARTED);
		} else {
			logIgnoredTransition("updatePercentageCompleted");
		}
	}

	private void logTransition(STATUS newStatus) {
		String msg =
			getId() + ", '" + getExternalId() + "': transitioning from "
					+ getStatusAsString() + " to " + newStatus;
		log.warning(msg);
	}

	private void logIgnoredTransition(String transition) {
		String msg =
			getId() + ", '" + getExternalId() + "': " + transition
					+ " ignored (status == '" + getStatusAsString() + "'";
		log.warning(msg);
	}

	// -- Job Control

	public boolean shouldStop() {
		if (getStatus().equals(STATUS.ABORT_REQUESTED)
				|| getStatus().equals(STATUS.ABORTED)) {
			return true;
		} else {
			return false;
		}
	}

	// -- Backwards compatibility

	// public void setRequested(Date date) {
	// this.timestamps.put(STATUS.NEW, date);
	// }

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getRequested()
	 */
	@Override
	public Date getRequested() {
		return this.timestamps.get(STATUS.NEW);
	}

	// public void setQueued(Date queued) {
	// this.timestamps.put(STATUS.QUEUED, queued);
	// }

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getQueued()
	 */
	@Override
	public Date getQueued() {
		return this.timestamps.get(STATUS.QUEUED);
	}

	// public void setStarted(Date started) {
	// this.timestamps.put(STATUS.STARTED, started);
	// }

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getStarted()
	 */
	@Override
	public Date getStarted() {
		return this.timestamps.get(STATUS.STARTED);
	}

	// public void setCompleted(Date completed) {
	// this.timestamps.put(STATUS.COMPLETED, completed);
	// }

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getCompleted()
	 */
	@Override
	public Date getCompleted() {
		return this.timestamps.get(STATUS.COMPLETED);
	}

	// public void setFailed(Date failed) {
	// this.timestamps.put(STATUS.FAILED, failed);
	// }

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getFailed()
	 */
	@Override
	public Date getFailed() {
		return this.timestamps.get(STATUS.FAILED);
	}

	// public void setAbortRequested(Date abortRequested) {
	// this.timestamps.put(STATUS.ABORT_REQUESTED, abortRequested);
	// }

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getAbortRequested()
	 */
	@Override
	public Date getAbortRequested() {
		return this.timestamps.get(STATUS.ABORT_REQUESTED);
	}

	// public void setAborted(Date aborted) {
	// this.timestamps.put(STATUS.ABORTED, aborted);
	// }

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getAborted()
	 */
	@Override
	public Date getAborted() {
		return this.timestamps.get(STATUS.ABORTED);
	}

	// -- Persistent fields

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getId()
	 */
	@Override
	public long getId() {
		return id;
	}

	protected void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getExternalId()
	 */
	@Override
	public String getExternalId() {
		return externalId;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#setExternalId(java.lang.String)
	 */
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getTransactionId()
	 */
	@Override
	public long getTransactionId() {
		return transactionId;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#setTransactionId(long)
	 */
	@Override
	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getStatusAsString()
	 */
	@Override
	public String getStatusAsString() {
		return status.name();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#setStatusAsString(java.lang.String)
	 */
	@Override
	public void setStatusAsString(String status) {
		setStatus(STATUS.valueOf(status));
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getPercentageComplete()
	 */
	@Override
	public int getPercentageComplete() {
		return percentageComplete;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#setPercentageComplete(int)
	 */
	@Override
	public void setPercentageComplete(int percentage) {
		if (percentage < MIN_PERCENTAGE_COMPLETED || percentage > MAX_PERCENTAGE_COMPLETED) {
			throw new IllegalArgumentException("invalid percentage: " + percentage);
		}
		this.percentageComplete = percentage;
		// Update the timestamp, indirectly
		setStatus(getStatus());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getStatus()
	 */
	@Override
	public STATUS getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#setStatus(com.choicemaker.cm.io.blocking.automated.offline.server.BatchJobBean.STATUS)
	 */
	@Override
	public void setStatus(STATUS currentStatus) {
		this.status = currentStatus;
		setTimeStamp(currentStatus, new Date());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob#getTimeStamp(com.choicemaker.cm.io.blocking.automated.offline.server.BatchJobBean.STATUS)
	 */
	@Override
	public Date getTimeStamp(STATUS status) {
		return this.timestamps.get(status);
	}

	// Should be invoked only by setStatus(STATUS)
	protected void setTimeStamp(STATUS status, Date date) {
		this.timestamps.put(status, date);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (id == 0) {
			result = hashCode0();
		} else {
			result = prime * result + (int) (id ^ (id >>> 32));
		}
		return result;
	}

	/**
	 * Hashcode for instances with id == 0
	 */
	protected int hashCode0() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result
					+ ((description == null) ? 0 : description.hashCode());
		result =
			prime * result + ((externalId == null) ? 0 : externalId.hashCode());
		result =
			prime * result + ((timestamps == null) ? 0 : timestamps.hashCode());
		result = prime * result + percentageComplete;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result =
			prime * result + (int) (transactionId ^ (transactionId >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BatchJobBean other = (BatchJobBean) obj;
		if (id != other.id) {
			return false;
		}
		if (id == 0) {
			return equals0(other);
		}
		return true;
	}

	/**
	 * Equality test for instances with id == 0
	 */
	protected boolean equals0(BatchJobBean other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (externalId == null) {
			if (other.externalId != null) {
				return false;
			}
		} else if (!externalId.equals(other.externalId)) {
			return false;
		}
		if (timestamps == null) {
			if (other.timestamps != null) {
				return false;
			}
		} else if (!timestamps.equals(other.timestamps)) {
			return false;
		}
		if (percentageComplete != other.percentageComplete) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (transactionId != other.transactionId) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
