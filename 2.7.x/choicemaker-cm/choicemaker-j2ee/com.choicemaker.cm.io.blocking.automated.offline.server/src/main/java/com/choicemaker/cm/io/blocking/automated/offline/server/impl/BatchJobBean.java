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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

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

	private final static Set<String> validStatus = new HashSet<>();
	static {
		validStatus.add(STATUS_NEW);
		validStatus.add(STATUS_QUEUED);
		validStatus.add(STATUS_STARTED);
		validStatus.add(STATUS_COMPLETED);
		validStatus.add(STATUS_FAILED);
		validStatus.add(STATUS_ABORT_REQUESTED);
		validStatus.add(STATUS_ABORTED);
		validStatus.add(STATUS_CLEAR);
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
	private String status;

	@ElementCollection
	@MapKeyColumn(name = "TIMESTAMP")
	@MapKeyTemporal(TemporalType.TIMESTAMP)
	@Column(name = "STATUS")
	@CollectionTable(name = "CMT_OABA_BATCHJOB_AUDIT",
			joinColumns = @JoinColumn(name = "BATCHJOB_ID"))
	private Map<Date, String> audit = new HashMap<>();

	// -- Construction

	protected BatchJobBean() {
		this(null);
	}

	public BatchJobBean(String externalId) {
		setExternalId(externalId);
		setStatus(STATUS_NEW);
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
	public int getFractionComplete() {
		return percentageComplete;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public String getType() {
		return type;
	}

	public Date getTimeStamp(String status) {
		return this.mostRecentTimestamp(status);
	}

	/** Backwards compatibility */
	protected Date mostRecentTimestamp(String status) {
		// This could be replaced with a named, parameterized query
		Date retVal = null;
		if (status != null) {
			for (Map.Entry<Date, String> e : audit.entrySet()) {
				if (status.equals(e.getValue())) {
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
		return mostRecentTimestamp(STATUS_NEW);
	}

	@Override
	public Date getQueued() {
		return mostRecentTimestamp(STATUS_QUEUED);
	}

	@Override
	public Date getStarted() {
		return mostRecentTimestamp(STATUS_STARTED);
	}

	@Override
	public Date getCompleted() {
		return mostRecentTimestamp(STATUS_COMPLETED);
	}

	@Override
	public Date getFailed() {
		return mostRecentTimestamp(STATUS_FAILED);
	}

	@Override
	public Date getAbortRequested() {
		return mostRecentTimestamp(STATUS_ABORT_REQUESTED);
	}

	@Override
	public Date getAborted() {
		return mostRecentTimestamp(STATUS_ABORTED);
	}

	// -- Job Control

	public boolean shouldStop() {
		if (getStatus().equals(STATUS_ABORT_REQUESTED)
				|| getStatus().equals(STATUS_ABORTED))
			return true;
		else
			return false;
	}

	// -- Field modifiers

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

	public void setFractionComplete(int percentage) {
		if (percentage < MIN_PERCENTAGE_COMPLETED
				|| percentage > MAX_PERCENTAGE_COMPLETED) {
			throw new IllegalArgumentException("invalid percentage: "
					+ percentage);
		}
		this.percentageComplete = percentage;
		// Update the timestamp, indirectly
		setStatus(getStatus());
	}

	private void logTransition(String newStatus) {
		String msg =
			getId() + ", '" + getExternalId() + "': transitioning from "
					+ getStatus() + " to " + newStatus;
		log.warning(msg);
	}

	private void logIgnoredTransition(String transition) {
		String msg =
			getId() + ", '" + getExternalId() + "': " + transition
					+ " ignored (status == '" + getStatus() + "'";
		log.warning(msg);
	}

	@Override
	public void setStatus(String newStatus) {
		if (newStatus == null || !validStatus.contains(newStatus)) {
			throw new IllegalArgumentException("Invalid status: " + newStatus);
		}
		this.status = newStatus;
		setTimeStamp(newStatus, new Date());
	}

	// Should be invoked only by setStatus(STATUS)
	protected void setTimeStamp(String status, Date date) {
		this.audit.put(date, status);
	}

	// -- State machine

	private static Map<String, Set<String>> allowedTransitions =
		new HashMap<>();
	static {
		Set<String> allowed = new HashSet<>();
		allowed.add(STATUS_QUEUED);
		allowed.add(STATUS_ABORT_REQUESTED);
		allowed.add(STATUS_ABORTED);
		allowedTransitions.put(STATUS_NEW, allowed);
		allowed = new HashSet<>();
		allowed.add(STATUS_QUEUED);
		allowed.add(STATUS_ABORT_REQUESTED);
		allowed.add(STATUS_ABORTED);
		allowedTransitions.put(STATUS_NEW, allowed);
		allowed = new HashSet<>();
		allowed.add(STATUS_STARTED);
		allowed.add(STATUS_ABORT_REQUESTED);
		allowed.add(STATUS_ABORTED);
		allowedTransitions.put(STATUS_QUEUED, allowed);
		allowed = new HashSet<>();
		allowed.add(STATUS_STARTED);
		allowed.add(STATUS_COMPLETED);
		allowed.add(STATUS_FAILED);
		allowed.add(STATUS_ABORT_REQUESTED);
		allowed.add(STATUS_ABORTED);
		allowedTransitions.put(STATUS_STARTED, allowed);
		allowed = new HashSet<>();
		allowed.add(STATUS_ABORTED);
		allowedTransitions.put(STATUS_ABORT_REQUESTED, allowed);
		// Terminal transitions (unless re-queued/re-started)
		allowed = new HashSet<>();
		allowedTransitions.put(STATUS_COMPLETED, allowed);
		allowed = new HashSet<>();
		allowedTransitions.put(STATUS_FAILED, allowed);
		allowed = new HashSet<>();
		allowedTransitions.put(STATUS_ABORTED, allowed);
		allowed = new HashSet<>();
		allowedTransitions.put(STATUS_CLEAR, allowed);
	}

	public static boolean isAllowedTransition(String current, String next) {
		if (current == null || next == null) {
			throw new IllegalArgumentException("null status");
		}
		Set<String> allowed = allowedTransitions.get(current);
		assert allowed != null;
		boolean retVal = allowed.contains(next);
		return retVal;
	}

	@Override
	public void updateFractionCompleted(int percentageCompleted) {
		if (percentageCompleted < MIN_PERCENTAGE_COMPLETED || MAX_PERCENTAGE_COMPLETED > 100) {
			String msg =
				"invalid percentageCompleted == '" + percentageCompleted + "'";
			throw new IllegalArgumentException(msg);
		}
		if (isAllowedTransition(getStatus(), STATUS_STARTED)) {
			logTransition(STATUS_STARTED);
			setStatus(STATUS_STARTED);
		} else {
			logIgnoredTransition("updatePercentageCompleted");
		}
	}

	@Override
	public void markAsQueued() {
		if (isAllowedTransition(getStatus(), STATUS_QUEUED)) {
			logTransition(STATUS_QUEUED);
			setStatus(STATUS_QUEUED);
		} else {
			logIgnoredTransition("markAsQueued");
		}
	}

	@Override
	public void markAsStarted() {
		if (isAllowedTransition(getStatus(), STATUS_STARTED)) {
			logTransition(STATUS_QUEUED);
			setFractionComplete(MIN_PERCENTAGE_COMPLETED);
			setStatus(STATUS_STARTED);
		} else {
			logIgnoredTransition("markAsStarted");
		}
	}

	@Override
	public void markAsReStarted() {
		setFractionComplete(MIN_PERCENTAGE_COMPLETED);
		setStatus(STATUS_QUEUED);
	}

	@Override
	public void markAsCompleted() {
		if (isAllowedTransition(getStatus(), STATUS_COMPLETED)) {
			logTransition(STATUS_COMPLETED);
			setFractionComplete(MAX_PERCENTAGE_COMPLETED);
			setStatus(STATUS_COMPLETED);
		} else {
			logIgnoredTransition("markAsCompleted");
		}
	}

	@Override
	public void markAsFailed() {
		if (isAllowedTransition(getStatus(), STATUS_FAILED)) {
			logTransition(STATUS_FAILED);
			setStatus(STATUS_FAILED);
		} else {
			logIgnoredTransition("markAsFailed");
		}
	}

	@Override
	public void markAsAbortRequested() {
		if (isAllowedTransition(getStatus(), STATUS_ABORT_REQUESTED)) {
			logTransition(STATUS_ABORT_REQUESTED);
			setStatus(STATUS_ABORT_REQUESTED);
		} else {
			logIgnoredTransition("markAsAbortRequested");
		}
	}

	@Override
	public void markAsAborted() {
		if (isAllowedTransition(getStatus(), STATUS_ABORTED)) {
			if (!STATUS_ABORT_REQUESTED.equals(getStatus())) {
				markAsAbortRequested();
			}
			logTransition(STATUS_ABORTED);
			setStatus(STATUS_ABORTED);
		} else {
			logIgnoredTransition("markAsAborted");
		}
	}
	
} // BatchJobBean

