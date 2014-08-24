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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.log4j.Logger;

import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;

/**
 * @author rphall
 *
 */
@NamedQuery(name = "batchJobFindAll",
		query = "Select job from BatchJobBean job")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMT_OABA_BATCHJOB")
public class BatchJobBean implements IControl, Serializable {

	private static final long serialVersionUID = 271L;

	private static Logger log = Logger.getLogger(BatchJobBean.class.getName());

	public static enum NamedQuery {
		FIND_ALL("batchJobFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	public static final String TABLE_DISCRIMINATOR = "OABA";

	public static enum STATUS {
		NEW(false), QUEUED(false), STARTED(false), COMPLETED(true),
		FAILED(true), ABORT_REQUESTED(false), ABORTED(true), CLEAR(true);
		public boolean isTerminal;

		private STATUS(boolean terminal) {
			this.isTerminal = terminal;
		}
	}

	@Id
	@Column(name = "ID")
	@TableGenerator(name = "CMT_BATCHJOB_SEQUENCE", table = "CMT_SEQUENCE",
			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
			pkColumnValue = "OABA_BATCHJOB")
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = "CMT_BATCHJOB_SEQUENCE")
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
	private String sts;

	@OneToMany(mappedBy = "batchJob", cascade = CascadeType.ALL)
//	@JoinColumn(name = "BATCHJOB_ID")
	@MapKeyColumn(name = "status")
	private Map<String, BatchJobTimestamp> timestamps = new HashMap<>();

	// -- Construction

	public BatchJobBean() {
		setStatus(STATUS.NEW);
		setTimeStamp(STATUS.NEW, new Date());
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

	public static boolean isAllowedTransition(String current, STATUS next) {
		if (current == null) {
			throw new IllegalArgumentException("null status");
		}
		return isAllowedTransition(STATUS.valueOf(current), next);
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

	public void markAsQueued() {
		if (isAllowedTransition(getStatus(), STATUS.QUEUED)) {
			logTransition(STATUS.QUEUED);
			setTimeStamp(STATUS.QUEUED, new Date());
			setStatus(STATUS.QUEUED);
		} else {
			logIgnoredTransition("markAsQueued");
		}
	}

	public void markAsStarted() {
		if (isAllowedTransition(getStatus(), STATUS.STARTED)) {
			logTransition(STATUS.QUEUED);
			setTimeStamp(STATUS.STARTED, new Date());
			setStatus(STATUS.STARTED);
		} else {
			logIgnoredTransition("markAsStarted");
		}
	}

	/**
	 * This method is misleadingly named. It is called when a job is re-queued,
	 * not when it is restarted. This method doesn't check the current state of
	 * the job before re-queuing it.
	 *
	 */
	public void markAsReStarted() {
		setTimeStamp(STATUS.QUEUED, new Date());
		setStatus(STATUS.QUEUED);
	}

	public void markAsCompleted() {
		if (isAllowedTransition(getStatus(), STATUS.COMPLETED)) {
			logTransition(STATUS.COMPLETED);
			setPercentageComplete(100);
			setTimeStamp(STATUS.COMPLETED, new Date());
			setStatus(STATUS.COMPLETED);
		} else {
			logIgnoredTransition("markAsCompleted");
		}
	}

	public void markAsFailed() {
		if (isAllowedTransition(getStatus(), STATUS.FAILED)) {
			logTransition(STATUS.FAILED);
			setTimeStamp(STATUS.FAILED, new Date());
			setStatus(STATUS.FAILED);
		} else {
			logIgnoredTransition("markAsFailed");
		}
	}

	public void markAsAbortRequested() {
		if (isAllowedTransition(getStatus(), STATUS.ABORT_REQUESTED)) {
			logTransition(STATUS.ABORT_REQUESTED);
			setTimeStamp(STATUS.ABORT_REQUESTED, new Date());
			setStatus(STATUS.ABORT_REQUESTED);
		} else {
			logIgnoredTransition("markAsAbortRequested");
		}
	}

	public void markAsAborted() {
		if (isAllowedTransition(getStatus(), STATUS.ABORTED)) {
			if (!STATUS.ABORT_REQUESTED.equals(getStatus())) {
				markAsAbortRequested();
			}
			logTransition(STATUS.ABORTED);
			setTimeStamp(STATUS.ABORTED, new Date());
			setStatus(STATUS.ABORTED);
		} else {
			logIgnoredTransition("markAsAborted");
		}
	}

	/**
	 * This operation has effect only if job status is STARTED.
	 * 
	 * @param percentageCompleted
	 *            a non-negative percentage in the range 0 to 100, inclusive.
	 */
	public void updatePercentageCompleted(float percentageCompleted) {
		updatePercentageCompleted((int) percentageCompleted);
	}

	/**
	 * This operation has effect only if job status is STARTED or QUEUED
	 * 
	 * @param percentageCompleted
	 *            a non-negative percentage in the range 0 to 100, inclusive.
	 */
	public void updatePercentageCompleted(int percentageCompleted) {
		if (percentageCompleted < 0 || percentageCompleted > 100) {
			String msg =
				"invalid percentageCompleted == '" + percentageCompleted + "'";
			throw new IllegalArgumentException(msg);
		}
		if (isAllowedTransition(getStatus(), STATUS.STARTED)) {
			logTransition(STATUS.STARTED);
			setTimeStamp(STATUS.STARTED, new Date());
			setStatus(STATUS.STARTED);
		} else {
			logIgnoredTransition("updatePercentageCompleted");
		}
	}

	private void logTransition(STATUS newStatus) {
		String msg =
			getId() + ", '" + getExternalId() + "': transitioning from "
					+ getStatus() + " to " + newStatus;
		log.warn(msg);
	}

	private void logIgnoredTransition(String transition) {
		String msg =
			getId() + ", '" + getExternalId() + "': " + transition
					+ " ignored (status == '" + getStatus() + "'";
		log.warn(msg);
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

	public void setRequested(Date date) {
		this.setTimeStamp(STATUS.NEW, date);
	}

	public Date getRequested() {
		return this.getTimeStamp(STATUS.NEW);
	}

	public void setQueued(Date queued) {
		this.setTimeStamp(STATUS.QUEUED, queued);
	}

	public Date getQueued() {
		return this.getTimeStamp(STATUS.QUEUED);
	}

	public void setStarted(Date started) {
		this.setTimeStamp(STATUS.STARTED, started);
	}

	public Date getStarted() {
		return this.getTimeStamp(STATUS.STARTED);
	}

	public void setUpdated(Date updated) {
		if (!STATUS.valueOf(getStatus()).isTerminal) {
			this.setTimeStamp(getStatus(), updated);
		} else {
			logIgnoredTransition("updated(" + updated + ")");
		}
	}

	public Date getUpdated() {
		return this.getTimeStamp(getStatus());
	}

	public void setCompleted(Date completed) {
		this.setTimeStamp(STATUS.COMPLETED, completed);
	}

	public Date getCompleted() {
		return this.getTimeStamp(STATUS.COMPLETED);
	}

	public void setFailed(Date failed) {
		this.setTimeStamp(STATUS.FAILED, failed);
	}

	public Date getFailed() {
		return this.getTimeStamp(STATUS.FAILED);
	}

	public void setAbortRequested(Date abortRequested) {
		this.setTimeStamp(STATUS.ABORT_REQUESTED, abortRequested);
	}

	public Date getAbortRequested() {
		return this.getTimeStamp(STATUS.ABORT_REQUESTED);
	}

	public void setAborted(Date aborted) {
		this.setTimeStamp(STATUS.ABORTED, aborted);
	}

	public Date getAborted() {
		return this.getTimeStamp(STATUS.ABORTED);
	}

	// -- Persistent fields

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPercentageComplete() {
		return percentageComplete;
	}

	public void setPercentageComplete(int fractionComplete) {
		this.percentageComplete = fractionComplete;
	}

	public String getStatus() {
		return sts;
	}

	public void setStatus(String s) {
		STATUS currentStatus = s == null ? null : STATUS.valueOf(s);
		setStatus(currentStatus);
	}

	public void setStatus(STATUS currentStatus) {
		this.sts = currentStatus.name();
		setTimeStamp(currentStatus, new Date());
	}

	public Date getTimeStamp(String status) {
		BatchJobTimestamp ts = this.timestamps.get(status);
		Date retVal = ts.getTimeStamp();
		return retVal;
	}

	public Date getTimeStamp(STATUS status) {
		BatchJobTimestamp ts = this.timestamps.get(status.name());
		Date retVal = ts.getTimeStamp();
		return retVal;
	}

	protected void setTimeStamp(String status, Date date) {
		BatchJobTimestamp timestamp = new BatchJobTimestamp(status, date);
		this.timestamps.put(status, timestamp);
	}

	protected void setTimeStamp(STATUS status, Date date) {
		BatchJobTimestamp timestamp = new BatchJobTimestamp(status, date);
		this.timestamps.put(status.name(), timestamp);
	}

	// -- Object overrides

	@Override
	public String toString() {
		return "BatchJobBean [id=" + id + ", externalId=" + externalId
				+ ", transactionId=" + transactionId + ", status=" + sts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (id == 0) {
			result = prime * result + (int) (id ^ (id >>> 32));
		} else {
			result = hashCode0();
		}
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
	 * Hashcode for instances with id == 0
	 * 
	 * @return
	 */
	public int hashCode0() {
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
		result = prime * result + ((sts == null) ? 0 : sts.hashCode());
		result =
			prime * result + (int) (transactionId ^ (transactionId >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * Equality test for instances with id == 0
	 */
	public boolean equals0(BatchJobBean other) {
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
		if (sts == null) {
			if (other.sts != null) {
				return false;
			}
		} else if (!sts.equals(other.sts)) {
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
