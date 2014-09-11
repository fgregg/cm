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
package com.choicemaker.cm.persist0;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "JOB_TYPE")
@Table(/* schema = "CHOICEMAKER", */name = "CMP_BATCH_JOB")
public abstract class BatchJobBean implements IControl, Serializable, BatchJob {

	private static final long serialVersionUID = 271L;

	private static Logger log = Logger.getLogger(OfflineMatchingBean.class
			.getName());

	public static final String DEFAULT_TABLE_DISCRIMINATOR = "BATCH";

	public static enum NamedQuery {
		FIND_ALL("batchJobFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	/** Default value for non-persistent batch jobs */
	protected static final long INVALID_BATCHJOB_ID = 0;

	protected static boolean isInvalidBatchJobId(long id) {
		return id == INVALID_BATCHJOB_ID;
	}

	public static boolean isNonPersistent(BatchJob batchJob) {
		boolean retVal = true;
		if (batchJob != null) {
			retVal = isInvalidBatchJobId(batchJob.getId());
		}
		return retVal;
	}
	
	public static String randomTransactionId() {
		return UUID.randomUUID().toString().trim().toUpperCase();
	}
	
	// -- Instance data

	@Id
	@Column(name = "ID")
	@TableGenerator(name = "CMP_BATCHJOB", table = "CMT_SEQUENCE",
			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
			pkColumnValue = "CMP_BATCHJOB")
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = "CMP_BATCHJOB")
	protected long id;

	@Column(name = "EXTERNAL_ID")
	protected String externalId;

	@Column(name = "TRANSACTION_ID")
	protected String transactionId;

	@Column(name = "TYPE")
	protected final String type;

	@Column(name = "DESCRIPTION")
	protected String description;

	@Column(name = "FRACTION_COMPLETE")
	protected int percentageComplete;

	@Column(name = "BatchJobStatus")
	protected BatchJobStatus batchJobStatus;

	@OneToMany(cascade = CascadeType.ALL, mappedBy="job")
	protected List<OfflineMatchingAuditEvent> audit = new ArrayList<>();

	// -- Construction

	protected BatchJobBean() {
		this(null, randomTransactionId(), DEFAULT_TABLE_DISCRIMINATOR);
	}

	protected BatchJobBean(String externalId, String transactionId, String type) {
		setExternalId(externalId);
		setTransactionId(transactionId);
		this.type = type;
		setStatus(BatchJobStatus.NEW);
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
	public String getTransactionId() {
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
	public BatchJobStatus getStatus() {
		return batchJobStatus;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Date getTimeStamp(BatchJobStatus batchJobStatus) {
		return this.mostRecentTimestamp(batchJobStatus);
	}

	/** Backwards compatibility */
	protected Date mostRecentTimestamp(BatchJobStatus batchJobStatus) {
		// This could (should?) be replaced with a named, parameterized query
		Date retVal = null;
		if (batchJobStatus != null) {
			for (OfflineMatchingAuditEvent e : audit) {
				if (batchJobStatus == e.getStatus()) {
					Date ts = e.getTimestamp();
					if (retVal == null || retVal.compareTo(ts) < 0) {
						retVal = ts;
					}
				}
			}
		}
		return retVal;
	}

	@Override
	public Date getRequested() {
		return mostRecentTimestamp(BatchJobStatus.NEW);
	}

	@Override
	public Date getQueued() {
		return mostRecentTimestamp(BatchJobStatus.QUEUED);
	}

	@Override
	public Date getStarted() {
		return mostRecentTimestamp(BatchJobStatus.STARTED);
	}

	@Override
	public Date getCompleted() {
		return mostRecentTimestamp(BatchJobStatus.COMPLETED);
	}

	@Override
	public Date getFailed() {
		return mostRecentTimestamp(BatchJobStatus.FAILED);
	}

	@Override
	public Date getAbortRequested() {
		return mostRecentTimestamp(BatchJobStatus.ABORT_REQUESTED);
	}

	@Override
	public Date getAborted() {
		return mostRecentTimestamp(BatchJobStatus.ABORTED);
	}

	// -- Job Control

	public boolean shouldStop() {
		if (getStatus().equals(BatchJobStatus.ABORT_REQUESTED)
				|| getStatus().equals(BatchJobStatus.ABORTED)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void markAsQueued() {
		if (BatchJobStatus.isAllowedTransition(getStatus(), BatchJobStatus.QUEUED)) {
			logTransition(BatchJobStatus.QUEUED);
			// REMOVE timestamps.put(BatchJobStatus.QUEUED, new Date());
			setStatus(BatchJobStatus.QUEUED);
		} else {
			logIgnoredTransition("markAsQueued");
		}
	}

	@Override
	public void markAsStarted() {
		if (BatchJobStatus.isAllowedTransition(getStatus(), BatchJobStatus.STARTED)) {
			logTransition(BatchJobStatus.QUEUED);
			// REMOVE timestamps.put(BatchJobStatus.STARTED, new Date());
			setStatus(BatchJobStatus.STARTED);
		} else {
			logIgnoredTransition("markAsStarted");
		}
	}

	@Override
	public void markAsReStarted() {
		// REMOVE timestamps.put(BatchJobStatus.QUEUED, new Date());
		setStatus(BatchJobStatus.QUEUED);
	}

	@Override
	public void markAsCompleted() {
		if (BatchJobStatus.isAllowedTransition(getStatus(), BatchJobStatus.COMPLETED)) {
			logTransition(BatchJobStatus.COMPLETED);
			setPercentageComplete(100);
			// REMOVE timestamps.put(BatchJobStatus.COMPLETED, new Date());
			setStatus(BatchJobStatus.COMPLETED);
		} else {
			logIgnoredTransition("markAsCompleted");
		}
	}

	@Override
	public void markAsFailed() {
		if (BatchJobStatus.isAllowedTransition(getStatus(), BatchJobStatus.FAILED)) {
			logTransition(BatchJobStatus.FAILED);
			// REMOVE timestamps.put(BatchJobStatus.FAILED, new Date());
			setStatus(BatchJobStatus.FAILED);
		} else {
			logIgnoredTransition("markAsFailed");
		}
	}

	@Override
	public void markAsAbortRequested() {
		if (BatchJobStatus.isAllowedTransition(getStatus(), BatchJobStatus.ABORT_REQUESTED)) {
			logTransition(BatchJobStatus.ABORT_REQUESTED);
			// REMOVE timestamps.put(BatchJobStatus.ABORT_REQUESTED, new Date());
			setStatus(BatchJobStatus.ABORT_REQUESTED);
		} else {
			logIgnoredTransition("markAsAbortRequested");
		}
	}

	@Override
	public void markAsAborted() {
		if (BatchJobStatus.isAllowedTransition(getStatus(), BatchJobStatus.ABORTED)) {
			if (!BatchJobStatus.ABORT_REQUESTED.equals(getStatus())) {
				markAsAbortRequested();
			}
			logTransition(BatchJobStatus.ABORTED);
			// REMOVE timestamps.put(BatchJobStatus.ABORTED, new Date());
			setStatus(BatchJobStatus.ABORTED);
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
		if (BatchJobStatus.isAllowedTransition(getStatus(), BatchJobStatus.STARTED)) {
			logTransition(BatchJobStatus.STARTED);
			// REMOVE timestamps.put(BatchJobStatus.STARTED, new Date());
			setStatus(BatchJobStatus.STARTED);
		} else {
			logIgnoredTransition("updatePercentageCompleted");
		}
	}

	protected void logTransition(BatchJobStatus newStatus) {
		String msg =
			getId() + ", '" + getExternalId() + "': transitioning from "
					+ getStatus() + " to " + newStatus;
		log.warning(msg);
	}

	protected void logIgnoredTransition(String transition) {
		String msg =
			getId() + ", '" + getExternalId() + "': " + transition
					+ " ignored (batchJobStatus == '" + getStatus() + "'";
		log.warning(msg);
	}

	protected void setId(long id) {
		this.id = id;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	protected void setTransactionId(String transactionId) {
		if (transactionId != null) {
			transactionId = transactionId.trim().toUpperCase();
			if (transactionId.isEmpty()) {
				transactionId = null;
			}
		}
		this.transactionId = transactionId;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
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
	public void setStatus(BatchJobStatus currentStatus) {
		this.batchJobStatus = currentStatus;
		setTimeStamp(currentStatus, new Date());
	}

	// Should be invoked only by setStatus(BatchJobStatus)
	protected void setTimeStamp(BatchJobStatus batchJobStatus, Date date) {
		OfflineMatchingAuditEvent e = new OfflineMatchingAuditEvent(
				this.getTransactionId(),
				this,
				date,
				batchJobStatus
				);
		this.audit.add(e);
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
//		result =
//			prime * result
//					+ ((description == null) ? 0 : description.hashCode());
		result =
			prime * result + ((externalId == null) ? 0 : externalId.hashCode());
//		result = prime * result + ((audit == null) ? 0 : audit.hashCode());
		result = prime * result + percentageComplete;
		result = prime * result + ((batchJobStatus == null) ? 0 : batchJobStatus.hashCode());
		result =
			prime * result
					+ ((transactionId == null) ? 0 : transactionId.hashCode());
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
		/*
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		*/
		if (externalId == null) {
			if (other.externalId != null) {
				return false;
			}
		} else if (!externalId.equals(other.externalId)) {
			return false;
		}
		/*
		if (audit == null) {
			if (other.audit != null) {
				return false;
			}
		} else if (!audit.equals(other.audit)) {
			return false;
		}
		*/
		if (percentageComplete != other.percentageComplete) {
			return false;
		}
		if (batchJobStatus != other.batchJobStatus) {
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
		return "BatchJobBean [" + id + "/" + externalId + "/" + batchJobStatus + "]";
	}

}
