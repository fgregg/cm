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

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobJPA.*;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
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
import com.choicemaker.cm.io.blocking.automated.offline.server.data.BatchJobStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

/**
 * This class tracks the progress of a (long-running) offline matching process.
 * It also serves as the base class of other types of long-running jobs.
 * <p>
 * A successful batch job goes through a sequence of states: NEW, QUEUED,
 * STARTED, and COMPLETED. If processing fails in one of these stages, the job
 * state is marked as FAILED. A request may be aborted at any point, in which
 * case it goes through the ABORT_REQUESTED and the ABORT states.
 * </p>
 * <p>
 * A long-running process should provide some indication that it is making
 * progress. This class provides this estimate as a fraction between 0 and 100
 * (inclusive) by updating the {@link #setFractionComplete(int) fraction
 * complete} field.
 * </p>
 * 
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = QN_BATCHJOB_FIND_ALL,
		query = EQL_BATCHJOB_FIND_ALL)
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public class BatchJobBean implements IControl, Serializable, BatchJob {

	private static final long serialVersionUID = 271L;

	private static Logger log = Logger.getLogger(BatchJobBean.class.getName());

	protected final static Set<String> validStatus = new HashSet<>();
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

	protected static boolean isInvalidBatchJobId(long id) {
		return id == BatchJob.INVALID_BATCHJOB_ID;
	}

	public static boolean isPersistent(BatchJob batchJob) {
		boolean retVal = false;
		if (batchJob != null) {
			retVal = !isInvalidBatchJobId(batchJob.getId());
		}
		return retVal;
	}

	public static boolean isTopLevelJob(BatchJob batchJob) {
		boolean retVal = true;
		if (batchJob != null) {
			retVal = isInvalidBatchJobId(batchJob.getBatchParentId());
		}
		return retVal;
	}

	public static long randomTransactionId() {
		String uuid = UUID.randomUUID().toString();
		return uuid.hashCode();
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
	
	/**
	 * {@link BatchJob#INVALID_BATCHJOB_ID} or references the id of some other
	 * BatchJobBean
	 */
	@Column(name = CN_BPARENT_ID)
	private final long bparentId;

	/**
	 * {@link BatchJob#INVALID_BATCHJOB_ID} or references the id of some
	 * UrmJobBean
	 */
	@Column(name = CN_URM_ID)
	private final long urmId;

	@Column(name = CN_TRANSACTION_ID)
	private final long transactionId;

	@Column(name = CN_EXTERNAL_ID)
	private String externalId;

	@Column(name = CN_DESCRIPTION)
	private String description;

	@Column(name = CN_FRACTION_COMPLETE)
	private int percentageComplete;

	@Column(name = CN_STATUS)
	private String status;

	@ElementCollection
	@MapKeyColumn(name = CN_TIMESTAMP)
	@MapKeyTemporal(TemporalType.TIMESTAMP)
	@Column(name = CN_STATUS)
	@CollectionTable(name = AUDIT_TABLE_NAME,
			joinColumns = @JoinColumn(name = CN_AUDIT_JOIN))
	private Map<Date, String> audit = new HashMap<>();

	// -- Construction

	protected BatchJobBean() {
		this(null, randomTransactionId(), INVALID_BATCHJOB_ID, INVALID_URMJOB_ID);
	}

	public BatchJobBean(String externalId) {
		this(externalId, randomTransactionId(), INVALID_BATCHJOB_ID, INVALID_URMJOB_ID);
	}

	public BatchJobBean(BatchJob job) {
		this(job.getExternalId(), job.getTransactionId(), job.getBatchParentId(), job.getUrmId());
		this.setDescription(job.getDescription());
		this.setFractionComplete(job.getFractionComplete());
		this.setStatus(job.getStatus());
		Date ts = job.getTimeStamp(job.getStatus());
		this.setTimeStamp(job.getStatus(), ts);
	}

	protected BatchJobBean(String externalId, long tid, long bpid, long urmid) {
		this.transactionId = tid;
		this.bparentId = bpid;
		this.urmId = urmid;
		setExternalId(externalId);
		setStatus(STATUS_NEW);
	}

	// -- Accessors
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getBatchParentId() {
		return this.bparentId;
	}

	@Override
	public long getUrmId() {
		return this.urmId;
	}

	@Override
	public long getTransactionId() {
		return transactionId;
	}

	@Override
	public String getExternalId() {
		return externalId;
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
	public void setDescription(String description) {
		this.description = description;
	}

	public void setFractionComplete(int percentage) {
		if (percentage < MIN_PERCENTAGE_COMPLETED
				|| percentage > MAX_PERCENTAGE_COMPLETED) {
			String msg = "invalid percentage: " + percentage;
			throw new IllegalArgumentException(msg);
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
		publishStatus();
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
	
	// -- Messaging

	/**
	 * This method publishes the status of a job to a topic queue.
	 */
	protected void publishStatus(){
		TopicConnection conn = null;
		TopicSession session = null;
		try {
			conn = EJBConfiguration.getInstance().getTopicConnectionFactory().createTopicConnection();
			session = conn.createTopicSession(false,  TopicSession.AUTO_ACKNOWLEDGE);
			conn.start();
			Topic topic = EJBConfiguration.getInstance().getTransStatusTopic();
			TopicPublisher pub = session.createPublisher(topic);
			BatchJobStatus jobStatus = new BatchJobStatus(this);
			ObjectMessage notifMsg = session.createObjectMessage(jobStatus);
			pub.publish(notifMsg);
			pub.close();
		}
		catch (Exception e) {
			log.severe(e.toString());
		} 
		finally {
			if (session != null) {
				try {
					session.close();
				} catch (Exception e) {
					log.severe(e.toString());
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					log.severe(e.toString());
				}
			}
		}
		log.fine("...finished published status");
	}

	// -- Identity

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (isInvalidBatchJobId(id)) {
			result = hashCode0();
		} else {
			result = prime * result + (int) (id ^ (id >>> 32));
		}
		return result;
	}

	/**
	 * Hashcode for instances with id == 0 (non-persistent/invalid id)
	 */
	protected int hashCode0() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result + ((externalId == null) ? 0 : externalId.hashCode());
		result = prime * result + percentageComplete;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result =
				prime * result + (int) (bparentId ^ (bparentId >>> 32));
		result =
			prime * result + (int) (transactionId ^ (transactionId >>> 32));
		result =
			prime * result + DISCRIMINATOR_VALUE.hashCode();
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
		// Implicitly checks Discriminator type
		if (getClass() != obj.getClass()) {
			return false;
		}
		BatchJobBean other = (BatchJobBean) obj;
		if (id != other.id) {
			return false;
		}
		if (isInvalidBatchJobId(id)) {
			return equals0(other);
		}
		return true;
	}

	/**
	 * Equality test for instances with id == 0 (non-persistent/invalid id)
	 */
	protected boolean equals0(BatchJobBean other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		/*
		 * if (description == null) { if (other.description != null) { return
		 * false; } } else if (!description.equals(other.description)) { return
		 * false; }
		 */
		if (externalId == null) {
			if (other.externalId != null) {
				return false;
			}
		} else if (!externalId.equals(other.externalId)) {
			return false;
		}
		/*
		 * if (audit == null) { if (other.audit != null) { return false; } }
		 * else if (!audit.equals(other.audit)) { return false; }
		 */
		if (percentageComplete != other.percentageComplete) {
			return false;
		}
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		} else if (!status.equals(other.status)) {
			return false;
		}
		if (bparentId != other.bparentId) {
			return false;
		}
		if (transactionId != other.transactionId) {
			return false;
		}
		// Discriminator type is implicitly checked by comparing
		// this.getClass() and other.getClass()
		return true;
	}

	@Override
	public String toString() {
		return "BatchJobBean [" + id + "/" + externalId + "/" + status
				+ "]";
	}

} // BatchJobBean

