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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Persistent information about a probability model. This information is not
 * sufficient to reproduce a matching model, but it is sufficient to check
 * whether model is the same as some previously saved model.
 * 
 * @author rphall
 *
 */
@NamedQuery(name = "auditFindAll",
		query = "Select event from CMP_AuditEvent event")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMP_AUDIT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "JOB_TYPE")
public abstract class CMP_AuditEvent implements Serializable {

	private static final long serialVersionUID = 271L;

	// private static final Logger log = Logger.getLogger(CMP_AuditEvent.class
	// .getName());

	/** Default value for non-persistent audit events */
	public static final int INVALID_AUDIT_ID = 0;

	/** Object identifiers must be positive */
	public static final int OBJECT_ID_LOWER_BOUND = 1;

	public static final String DATE_FORMAT_SPEC = "yyyy-MM-dd HH:mm";

	public static enum NamedQuery {
		FIND_ALL("auditFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	/** Well-known event types */

	static boolean isInvalidAuditId(long id) {
		return id == INVALID_AUDIT_ID;
	}

	public static boolean isNonPersistent(CMP_AuditEvent e) {
		boolean retVal = true;
		if (e != null) {
			retVal = isInvalidAuditId(e.getId());
		}
		return retVal;
	}

	public static String standardize(String s) {
		if (s != null) {
			s = s.trim().toUpperCase();
			if (s.isEmpty()) {
				s = null;
			}
		}
		return s;
	}

	// -- Instance data

	@Id
	@Column(name = "ID")
	@TableGenerator(name = "CMT_AUDIT", table = "CMT_SEQUENCE",
			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
			pkColumnValue = "CMT_AUDIT")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "CMT_AUDIT")
	private long id;

	@Column(name = "TRANSACTION_ID")
	private String transactionId;

	@Column(name = "JOB_TYPE")
	private String jobType;

	@JoinColumn(name = "JOB_ID")
	private BatchJobBean job;

	@Column(name = "TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@Column(name = "EVENT_TYPE")
	private String eventType;

	@Column(name = "EVENT")
	private String event;

	@Column(name = "EVENT_DETAILS")
	private String eventDetails;

	// -- Construction

	protected CMP_AuditEvent() {
	}

	/**
	 * Creates an entry for the audit table
	 * 
	 * @param transactionId
	 *            non-null and non-blank. Will be trimmed and converted to upper
	 *            case.
	 * @param jobType
	 *            non-null and non-blank. Will be trimmed and converted to upper
	 *            case.
	 * @param job
	 *            Must be equal to or great than {@link #OBJECT_ID_LOWER_BOUND
	 *            the lower bound} for (persistent) job identifiers
	 * @param timestamp
	 *            non-null
	 * @param eventType
	 *            non-null and non-blank. Will be trimmed and converted to upper
	 *            case.
	 * @param event
	 *            non-null and non-blank. Will be trimmed and converted to upper
	 *            case.
	 * @param eventDetails
	 *            optional. May be null. Will not be trimmed nor converted to
	 *            upper case.
	 */
	public CMP_AuditEvent(String transactionId, String objectType,
			BatchJobBean batchJob, Date timestamp, String eventType,
			String event, String eventDetails) {
		this.setTransactionId(transactionId);
		this.setObjectType(objectType);
		this.setObject(batchJob);
		this.setTimestamp(timestamp);
		this.setEventType(eventType);
		this.setEvent(event);
		this.setEventDetail(eventDetails);
	}

	// -- Accessors

	public long getId() {
		return id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public String getObjectType() {
		return jobType;
	}

	public BatchJob getObject() {
		return job;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getEventType() {
		return eventType;
	}

	public String getEvent() {
		return event;
	}

	public String getEventDetail() {
		return eventDetails;
	}

	// -- Modifiers

	protected void setTransactionId(String tid) {
		this.transactionId = standardize(tid);
	}

	protected void setObjectType(String ot) {
		this.jobType = standardize(ot);
	}

	protected void setObject(BatchJobBean o) {
		// if (oid < OBJECT_ID_LOWER_BOUND) {
		// throw new IllegalArgumentException("Invalid job id: " + oid);
		// }
		this.job = o;
	}

	protected void setTimestamp(Date ts) {
		// if (ts == null) {
		// throw new IllegalArgumentException("null timestamp");
		// }
		this.timestamp = ts;
	}

	protected void setEventType(String et) {
		this.eventType = standardize(et);
	}

	protected void setEvent(String event) {
		this.event = standardize(event);
	}

	protected void setEventDetail(String eventDetails) {
		this.eventDetails = eventDetails;
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

	public int hashCode0() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result =
			prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((job == null) ? 0 : job.hashCode());
		result = prime * result + ((jobType == null) ? 0 : jobType.hashCode());
		result =
			prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result =
			prime * result
					+ ((transactionId == null) ? 0 : transactionId.hashCode());
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
		CMP_AuditEvent other = (CMP_AuditEvent) obj;
		if (id != other.id) {
			return false;
		}
		if (id == 0) {
			return equals0(other);
		}
		return true;
	}

	public boolean equals0(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CMP_AuditEvent other = (CMP_AuditEvent) obj;
		if (event == null) {
			if (other.event != null) {
				return false;
			}
		} else if (!event.equals(other.event)) {
			return false;
		}
		if (eventType == null) {
			if (other.eventType != null) {
				return false;
			}
		} else if (!eventType.equals(other.eventType)) {
			return false;
		}
		if (job != other.job) {
			return false;
		}
		if (jobType == null) {
			if (other.jobType != null) {
				return false;
			}
		} else if (!jobType.equals(other.jobType)) {
			return false;
		}
		if (timestamp == null) {
			if (other.timestamp != null) {
				return false;
			}
		} else if (!timestamp.equals(other.timestamp)) {
			return false;
		}
		if (transactionId == null) {
			if (other.transactionId != null) {
				return false;
			}
		} else if (!transactionId.equals(other.transactionId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "CMP_AuditEvent [id=" + id + ", transactionId=" + transactionId
				+ ", jobType=" + jobType + ", job=" + job + ", timestamp="
				+ timestamp + ", eventType=" + eventType + ", event=" + event
				+ "]";
	}

}
