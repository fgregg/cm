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
package com.choicemaker.demo.oaba0;

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
import javax.persistence.MapKeyTemporal;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.TemporalType;

import com.choicemaker.cm.core.IControl;

/**
 * A BatchJobBean tracks the progress of a (long-running) offline blocking
 * process. A successful request goes through a sequence of states: NEW, QUEUED,
 * STARTED, and COMPLETED. A request may be aborted at any point, in which case
 * it goes through the ABORT_REQUESTED and the ABORT states.</p>
 *
 * A long-running process should provide some indication that it is making
 * progress. It can provide this estimate as a fraction between 0 and 100
 * (inclusive) by updating the getFractionComplete() field.</p>
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
	
	// -- Instance data

	@Id
	@Column(name = "ID")
	@TableGenerator(name = "OABA_BATCHJOB", table = "CMT_SEQUENCE",
			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
			pkColumnValue = "OABA_BATCHJOB")
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = "OABA_BATCHJOB")
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
	private BatchJob.STATUS status;

	@ElementCollection
	@MapKeyColumn(name = "TIMESTAMP")
	@MapKeyTemporal(TemporalType.TIMESTAMP)
	@Column(name = "STATUS")
	@CollectionTable(name = "CMT_OABA_BATCHJOB_AUDIT",
			joinColumns = @JoinColumn(name = "BATCHJOB_ID"))
	private Map<Date, BatchJob.STATUS> audit = new HashMap<>();

	// -- Construction

	protected BatchJobBean() {
		this(null);
	}

	public BatchJobBean(String externalId) {
		setExternalId(externalId);
		setStatus(BatchJob.STATUS.NEW);
	}
	
	// -- Accessors

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public long getTransactionId() {
		return transactionId;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getPercentageComplete() {
		return percentageComplete;
	}

	@Override
	public BatchJob.STATUS getStatus() {
		return status;
	}

	@Override
	public String getStatusAsString() {
		return status.name();
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Date getTimeStamp(BatchJob.STATUS status) {
		return this.mostRecentTimestamp(status);
	}

	/** Backwards compatibility */
	protected Date mostRecentTimestamp(BatchJob.STATUS status) {
		// This could be replaced with a named, parameterized query
		Date retVal = null;
		if (status != null) {
			for (Map.Entry<Date, BatchJob.STATUS> e : audit.entrySet()) {
				if (status == e.getValue()) {
					if (retVal == null || retVal.compareTo(e.getKey()) < 0) {
						retVal = e.getKey();
					}
				}
			}
		}
		return retVal;
	}

	@Override
	public Date getRequested() {
		return mostRecentTimestamp(BatchJob.STATUS.NEW);
	}

	@Override
	public Date getQueued() {
		return mostRecentTimestamp(BatchJob.STATUS.QUEUED);
	}

	@Override
	public Date getStarted() {
		return mostRecentTimestamp(BatchJob.STATUS.STARTED);
	}

	@Override
	public Date getCompleted() {
		return mostRecentTimestamp(BatchJob.STATUS.COMPLETED);
	}

	@Override
	public Date getFailed() {
		return mostRecentTimestamp(BatchJob.STATUS.FAILED);
	}

	@Override
	public Date getAbortRequested() {
		return mostRecentTimestamp(BatchJob.STATUS.ABORT_REQUESTED);
	}

	@Override
	public Date getAborted() {
		return mostRecentTimestamp(BatchJob.STATUS.ABORTED);
	}

	// -- Job Control

	public boolean shouldStop() {
		if (getStatus().equals(BatchJob.STATUS.ABORT_REQUESTED)
				|| getStatus().equals(BatchJob.STATUS.ABORTED)) {
			return true;
		} else {
			return false;
		}
	}

	// -- State machine

	private static Map<BatchJob.STATUS, EnumSet<BatchJob.STATUS>> allowedTransitions =
		new HashMap<>();
	static {
		allowedTransitions.put(BatchJob.STATUS.NEW, EnumSet.of(BatchJob.STATUS.QUEUED,
				BatchJob.STATUS.ABORT_REQUESTED, BatchJob.STATUS.ABORTED));
		allowedTransitions.put(BatchJob.STATUS.QUEUED, EnumSet.of(BatchJob.STATUS.STARTED,
				BatchJob.STATUS.ABORT_REQUESTED, BatchJob.STATUS.ABORTED));
		allowedTransitions.put(BatchJob.STATUS.STARTED, EnumSet.of(BatchJob.STATUS.STARTED,
				BatchJob.STATUS.COMPLETED, BatchJob.STATUS.FAILED, BatchJob.STATUS.ABORT_REQUESTED,
				BatchJob.STATUS.ABORTED));
		allowedTransitions.put(BatchJob.STATUS.ABORT_REQUESTED,
				EnumSet.of(BatchJob.STATUS.ABORTED));
		// Terminal transitions (unless re-queued/re-started)
		allowedTransitions.put(BatchJob.STATUS.COMPLETED, EnumSet.noneOf(BatchJob.STATUS.class));
		allowedTransitions.put(BatchJob.STATUS.FAILED, EnumSet.noneOf(BatchJob.STATUS.class));
		allowedTransitions.put(BatchJob.STATUS.ABORTED, EnumSet.noneOf(BatchJob.STATUS.class));
		allowedTransitions.put(BatchJob.STATUS.CLEAR, EnumSet.noneOf(BatchJob.STATUS.class));
	}

	public static boolean isAllowedTransition(BatchJob.STATUS current, BatchJob.STATUS next) {
		if (current == null || next == null) {
			throw new IllegalArgumentException("null status");
		}
		Set<BatchJob.STATUS> allowed = allowedTransitions.get(current);
		assert allowed != null;
		boolean retVal = allowed.contains(next);
		return retVal;
	}

	@Override
	public void markAsQueued() {
		if (isAllowedTransition(getStatus(), BatchJob.STATUS.QUEUED)) {
			logTransition(BatchJob.STATUS.QUEUED);
			// REMOVE timestamps.put(STATUS.QUEUED, new Date());
			setStatus(BatchJob.STATUS.QUEUED);
		} else {
			logIgnoredTransition("markAsQueued");
		}
	}

	@Override
	public void markAsStarted() {
		if (isAllowedTransition(getStatus(), BatchJob.STATUS.STARTED)) {
			logTransition(BatchJob.STATUS.QUEUED);
			// REMOVE timestamps.put(STATUS.STARTED, new Date());
			setStatus(BatchJob.STATUS.STARTED);
		} else {
			logIgnoredTransition("markAsStarted");
		}
	}

	@Override
	public void markAsReStarted() {
		// REMOVE timestamps.put(STATUS.QUEUED, new Date());
		setStatus(BatchJob.STATUS.QUEUED);
	}

	@Override
	public void markAsCompleted() {
		if (isAllowedTransition(getStatus(), BatchJob.STATUS.COMPLETED)) {
			logTransition(BatchJob.STATUS.COMPLETED);
			setPercentageComplete(100);
			// REMOVE timestamps.put(STATUS.COMPLETED, new Date());
			setStatus(BatchJob.STATUS.COMPLETED);
		} else {
			logIgnoredTransition("markAsCompleted");
		}
	}

	@Override
	public void markAsFailed() {
		if (isAllowedTransition(getStatus(), BatchJob.STATUS.FAILED)) {
			logTransition(BatchJob.STATUS.FAILED);
			// REMOVE timestamps.put(STATUS.FAILED, new Date());
			setStatus(BatchJob.STATUS.FAILED);
		} else {
			logIgnoredTransition("markAsFailed");
		}
	}

	@Override
	public void markAsAbortRequested() {
		if (isAllowedTransition(getStatus(), BatchJob.STATUS.ABORT_REQUESTED)) {
			logTransition(BatchJob.STATUS.ABORT_REQUESTED);
			// REMOVE timestamps.put(STATUS.ABORT_REQUESTED, new Date());
			setStatus(BatchJob.STATUS.ABORT_REQUESTED);
		} else {
			logIgnoredTransition("markAsAbortRequested");
		}
	}

	@Override
	public void markAsAborted() {
		if (isAllowedTransition(getStatus(), BatchJob.STATUS.ABORTED)) {
			if (!BatchJob.STATUS.ABORT_REQUESTED.equals(getStatus())) {
				markAsAbortRequested();
			}
			logTransition(BatchJob.STATUS.ABORTED);
			// REMOVE timestamps.put(STATUS.ABORTED, new Date());
			setStatus(BatchJob.STATUS.ABORTED);
		} else {
			logIgnoredTransition("markAsAborted");
		}
	}
	
	// -- Other modifiers

	@Override
	public void updatePercentageCompleted(float percentageCompleted) {
		updatePercentageCompleted((int) percentageCompleted);
	}

	@Override
	public void updatePercentageCompleted(int percentageCompleted) {
		if (percentageCompleted < 0 || percentageCompleted > 100) {
			String msg =
				"invalid percentageCompleted == '" + percentageCompleted + "'";
			throw new IllegalArgumentException(msg);
		}
		if (isAllowedTransition(getStatus(), BatchJob.STATUS.STARTED)) {
			logTransition(BatchJob.STATUS.STARTED);
			// REMOVE timestamps.put(STATUS.STARTED, new Date());
			setStatus(BatchJob.STATUS.STARTED);
		} else {
			logIgnoredTransition("updatePercentageCompleted");
		}
	}

	private void logTransition(BatchJob.STATUS newStatus) {
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

	protected void setId(long id) {
		this.id = id;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setStatusAsString(String status) {
		setStatus(BatchJob.STATUS.valueOf(status));
	}

	@Override
	public void setPercentageComplete(int percentage) {
		if (percentage < MIN_PERCENTAGE_COMPLETED
				|| percentage > MAX_PERCENTAGE_COMPLETED) {
			throw new IllegalArgumentException("invalid percentage: "
					+ percentage);
		}
		this.percentageComplete = percentage;
		// Update the timestamp, indirectly
		setStatus(getStatus());
	}

	@Override
	public void setStatus(BatchJob.STATUS currentStatus) {
		this.status = currentStatus;
		setTimeStamp(currentStatus, new Date());
	}

	// Should be invoked only by setStatus(STATUS)
	protected void setTimeStamp(BatchJob.STATUS status, Date date) {
		this.audit.put(date, status);
	}

	// -- Identity

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
		result = prime * result + ((audit == null) ? 0 : audit.hashCode());
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
		if (audit == null) {
			if (other.audit != null) {
				return false;
			}
		} else if (!audit.equals(other.audit)) {
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

	@Override
	public String toString() {
		return "BatchJobBean [" + id + "/" + externalId + "/" + status + "]";
	}

}