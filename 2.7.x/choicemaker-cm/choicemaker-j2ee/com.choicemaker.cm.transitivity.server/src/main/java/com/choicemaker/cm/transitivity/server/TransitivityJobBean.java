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
package com.choicemaker.cm.transitivity.server;

import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

import org.apache.log4j.Logger;

import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;


/**
 * A TransitivityJobBean tracks the progress of a (long-running) transitivity
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
@NamedQuery(name = "transitivityJobFindAll",
query = "Select job from TransitivityJobBean job")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMT_TRANSITIVITY_JOB")
public class TransitivityJobBean implements IControl, Serializable {

	private static final long serialVersionUID = 271L;

	private static Logger log = Logger.getLogger(TransitivityJobBean.class.getName());

	public static final int MIN_PERCENTAGE_COMPLETED = 0;

	public static final int MAX_PERCENTAGE_COMPLETED = 100;

	public static enum NamedQuery {
		FIND_ALL("transitivityJobFindAll");
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

	@Id
	@Column(name = "ID")
	@TableGenerator(name = "TRANSITIVITYJOB", table = "CMT_SEQUENCE",
			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
			pkColumnValue = "TRANSITIVITYJOB")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TRANSITIVITYJOB")
	private long id;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "STATUS")
	private STATUS status;

	@Column(name = "FRACTION_COMPLETE")
	private int percentageComplete;

	@Column(name = "MODEL")
	private String model;

	@Column(name = "MATCH")
	private float match;

	@Column(name = "DIFFER")
	private float differ;
	
	@ElementCollection
	@MapKeyColumn(name = "STATUS")
	@Column(name = "TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	@CollectionTable(name = "CMT_TRANSITIVITYJOB_TIMESTAMPS",
			joinColumns = @JoinColumn(name = "TRANSITIVITYJOB_ID"))
	private Map<STATUS, Date> timestamps = new HashMap<>();
	
	// -- Construction
	
	protected TransitivityJobBean() {
		setStatus(STATUS.NEW);
	}
	
	public TransitivityJobBean(String model) {
		setModel(model);
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

	public void markAsQueued() {
		if (isAllowedTransition(getStatus(), STATUS.QUEUED)) {
			logTransition(STATUS.QUEUED);
			// REMOVE timestamps.put(STATUS.QUEUED, new Date());
			setStatus(STATUS.QUEUED);
		} else {
			logIgnoredTransition("markAsQueued");
		}
	}

	public void markAsStarted() {
		if (isAllowedTransition(getStatus(), STATUS.STARTED)) {
			logTransition(STATUS.QUEUED);
			// REMOVE timestamps.put(STATUS.STARTED, new Date());
			setStatus(STATUS.STARTED);
		} else {
			logIgnoredTransition("markAsStarted");
		}
	}

	/**
	 * This method is misnamed. It is called when a job is re-queued, not when
	 * it is restarted. This method doesn't check the current state of the job
	 * before re-queuing it.
	 *
	 */
	public void markAsReStarted() {
		// REMOVE timestamps.put(STATUS.QUEUED, new Date());
		setStatus(STATUS.QUEUED);
	}

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

	public void markAsFailed() {
		if (isAllowedTransition(getStatus(), STATUS.FAILED)) {
			logTransition(STATUS.FAILED);
			// REMOVE timestamps.put(STATUS.FAILED, new Date());
			setStatus(STATUS.FAILED);
		} else {
			logIgnoredTransition("markAsFailed");
		}
	}

	public void markAsAbortRequested() {
		if (isAllowedTransition(getStatus(), STATUS.ABORT_REQUESTED)) {
			logTransition(STATUS.ABORT_REQUESTED);
			// REMOVE timestamps.put(STATUS.ABORT_REQUESTED, new Date());
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
			// REMOVE timestamps.put(STATUS.ABORTED, new Date());
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
			// REMOVE timestamps.put(STATUS.STARTED, new Date());
			setStatus(STATUS.STARTED);
		} else {
			logIgnoredTransition("updatePercentageCompleted");
		}
	}

	private void logTransition(STATUS newStatus) {
		String msg =
			getId() + ", '" + getModel() + "': transitioning from "
					+ getStatus() + " to " + newStatus;
		log.warn(msg);
	}

	private void logIgnoredTransition(String transition) {
		String msg =
			getId() + ", '" + getModel() + "': " + transition
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

	// public void setRequested(Date date) {
	// this.timestamps.put(STATUS.NEW, date);
	// }

	public Date getRequested() {
		return this.timestamps.get(STATUS.NEW);
	}

	// public void setQueued(Date queued) {
	// this.timestamps.put(STATUS.QUEUED, queued);
	// }

	public Date getQueued() {
		return this.timestamps.get(STATUS.QUEUED);
	}

	// public void setStarted(Date started) {
	// this.timestamps.put(STATUS.STARTED, started);
	// }

	public Date getStarted() {
		return this.timestamps.get(STATUS.STARTED);
	}

	// public void setCompleted(Date completed) {
	// this.timestamps.put(STATUS.COMPLETED, completed);
	// }

	public Date getCompleted() {
		return this.timestamps.get(STATUS.COMPLETED);
	}

	// public void setFailed(Date failed) {
	// this.timestamps.put(STATUS.FAILED, failed);
	// }

	public Date getFailed() {
		return this.timestamps.get(STATUS.FAILED);
	}

	// public void setAbortRequested(Date abortRequested) {
	// this.timestamps.put(STATUS.ABORT_REQUESTED, abortRequested);
	// }

	public Date getAbortRequested() {
		return this.timestamps.get(STATUS.ABORT_REQUESTED);
	}

	// public void setAborted(Date aborted) {
	// this.timestamps.put(STATUS.ABORTED, aborted);
	// }

	public Date getAborted() {
		return this.timestamps.get(STATUS.ABORTED);
	}

	// -- Persistent fields

	public long getId() {
		return id;
	}

	protected void setId(long id) {
		this.id = id;
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

	public void setPercentageComplete(int percentageComplete) {
		this.percentageComplete = percentageComplete;
	}

	public String getModel() {
		return model;
	}

	protected void setModel(String model) {
		if (model == "null") {
			throw new IllegalArgumentException("null model");
		}
		model = model.trim();
		if (model.isEmpty()) {
			throw new IllegalArgumentException("blank model");
		}
		this.model = model;
	}

	public float getMatch() {
		return match;
	}

	public void setMatch(float match) {
		this.match = match;
	}

	public float getDiffer() {
		return differ;
	}

	public void setDiffer(float differ) {
		this.differ = differ;
	}

	public STATUS getStatus() {
		return status;
	}

	public void setStatus(STATUS currentStatus) {
		this.status = currentStatus;
		setTimeStamp(currentStatus, new Date());
	}

	public String getStatusAsString() {
		return status.name();
	}

	public void setStatusAsString(String status) {
		setStatus(STATUS.valueOf(status));
	}

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
		result = prime * result + Float.floatToIntBits(differ);
		result = prime * result + Float.floatToIntBits(match);
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + percentageComplete;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result =
			prime * result + ((timestamps == null) ? 0 : timestamps.hashCode());
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
		TransitivityJobBean other = (TransitivityJobBean) obj;
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
	protected boolean equals0(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TransitivityJobBean other = (TransitivityJobBean) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (Float.floatToIntBits(differ) != Float.floatToIntBits(other.differ)) {
			return false;
		}
		if (Float.floatToIntBits(match) != Float.floatToIntBits(other.match)) {
			return false;
		}
		if (model == null) {
			if (other.model != null) {
				return false;
			}
		} else if (!model.equals(other.model)) {
			return false;
		}
		if (percentageComplete != other.percentageComplete) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (timestamps == null) {
			if (other.timestamps != null) {
				return false;
			}
		} else if (!timestamps.equals(other.timestamps)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "TransitivityJobBean [id=" + id + ", status=" + status
				+ ", model=" + model + "]";
	}

//	// -- Implementation
//
//	/** This method publishes the status to a topic queue.
//	 * 
//	 * @param status
//	 */
//	private void publishStatus(){
//		throw new Error("not yet implemented");
////		TopicConnection conn = null;
////		TopicSession session = null;
////		try {
////			conn = EJBConfiguration.getInstance().getTopicConnectionFactory().createTopicConnection();
////			session = conn.createTopicSession(false,  TopicSession.AUTO_ACKNOWLEDGE);
////			conn.start();
////			Topic topic = EJBConfiguration.getInstance().getTransStatusTopic();
////			TopicPublisher pub = session.createPublisher(topic);
////			ObjectMessage notifMsg = session.createObjectMessage(getId ());
////			pub.publish(notifMsg);
////			pub.close();
////		}
////		catch (Exception e) {
////			log.error(e.toString(),e);
////		} 
////		finally {
////			if (session != null) {
////				try {
////					session.close();
////				} catch (Exception e) {
////					log.error(e);
////				}
////			}
////			if (conn != null) {
////				try {
////					conn.close();
////				} catch (Exception e) {
////					log.error(e);
////				}
////			}
////		}
////		log.debug("...finished published status");
//	}

} // TransitivityJobBean

