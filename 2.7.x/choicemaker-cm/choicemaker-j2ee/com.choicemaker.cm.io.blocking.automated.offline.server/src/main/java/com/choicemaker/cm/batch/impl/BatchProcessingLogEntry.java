package com.choicemaker.cm.batch.impl;

import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.CN_EVENT_INFO;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.CN_EVENT_NAME;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.CN_EVENT_SEQNUM;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.CN_EVENT_TIMESTAMP;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.CN_EVENT_TYPE;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.CN_FRACTION_COMPLETE;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.CN_ID;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.CN_JOB_ID;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.batch.impl.BatchProcessingJPA.TABLE_NAME;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingEvent;

@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public class BatchProcessingLogEntry implements BatchProcessingEvent {

	private static final long serialVersionUID = 271L;

	public static boolean isPersistent(OabaProcessingEvent p) {
		boolean retVal = false;
		if (p != null && p.getId() != INVALID_ID) {
			retVal = true;
		}
		return retVal;
	}

	@Id
	@Column(name = CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	protected long id;

	@Column(name = CN_JOB_ID)
	protected final long jobId;

	@Column(name = CN_EVENT_TYPE)
	protected final String type;

	@Column(name = CN_EVENT_NAME)
	protected final String eventName;

	@Column(name = CN_EVENT_SEQNUM)
	protected final int eventSequenceNumber;

	@Column(name = CN_FRACTION_COMPLETE)
	protected final float fractionComplete;

	@Column(name = CN_EVENT_INFO)
	protected final String eventInfo;

	@Column(name = CN_EVENT_TIMESTAMP)
	@Temporal(TemporalType.TIMESTAMP)
	protected final Date eventTimestamp;

	protected BatchProcessingLogEntry() {
		this.jobId = BatchJob.INVALID_ID;
		this.type = DISCRIMINATOR_VALUE;
		this.eventName = INVALID_EVENT_NAME;
		this.eventSequenceNumber = INVALID_EVENT_SEQNUM;
		this.fractionComplete = DEFAULT_FRACTION_COMPLETE;
		this.eventInfo = DEFAULT_EVENT_INFO;
		this.eventTimestamp = INVALID_TIMESTAMP;
	}

	protected BatchProcessingLogEntry(long jobId, String type, String evtName,
			int eventSeqNum, float fractionComplete, String info) {
		if (jobId == BatchJob.INVALID_ID) {
			throw new IllegalArgumentException("invalid jobId: " + jobId);
		}
		if (type == null || !type.equals(type.trim()) || type.isEmpty()) {
			throw new IllegalArgumentException("invalid type: '" + type + "'");
		}
		if (evtName == null || !evtName.equals(evtName.trim())
				|| evtName.isEmpty()) {
			throw new IllegalArgumentException("invalid event name: '"
					+ evtName + "'");
		}
		if (eventSeqNum == INVALID_EVENT_SEQNUM) {
			throw new IllegalArgumentException("invalid eventSequenceNumber: "
					+ eventSeqNum);
		}
		if (Float.isNaN(fractionComplete)
				|| fractionComplete < MINIMUM_FRACTION_COMPLETE
				|| fractionComplete > MAXIMUM_FRACTION_COMPLETE) {
			throw new IllegalArgumentException("invalid fraction complete: "
					+ fractionComplete);
		}
		this.jobId = jobId;
		this.type = type;
		this.eventName = evtName;
		this.eventSequenceNumber = eventSeqNum;
		this.fractionComplete = fractionComplete;
		this.eventInfo = info;
		this.eventTimestamp = new Date();
	}

	/** Returns the persistence identifier for this entry */
	@Override
	public long getId() {
		return id;
	}

	/** Returns the identifier of the batch job to which this entry applies */
	@Override
	public long getJobId() {
		return jobId;
	}

	/** Returns the event type of this entry */
	@Override
	public String getEventType() {
		return type;
	}

	/** Returns the event name for this entry */
	@Override
	public String getEventName() {
		return eventName;
	}

	/** Returns the event sequence number for this entry */
	@Override
	public int getEventSequenceNumber() {
		return eventSequenceNumber;
	}

	/**
	 * Returns an estimate between {@link #MINIMUM_FRACTION_COMPLETE} and
	 * {@link #MAXIMUM_FRACTION_COMPLETE} inclusive
	 */
	@Override
	public float getFractionComplete() {
		return fractionComplete;
	}

	/** Returns optional, additional information about this event (may be null) */
	@Override
	public String getEventInfo() {
		return eventInfo;
	}

	/** Returns the event timestamp for this entry */
	@Override
	public Date getEventTimestamp() {
		return eventTimestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (id != INVALID_ID) {
			result = prime * result + (int) (id ^ (id >>> 32));
		} else {
			result = hashCode0();
		}
		return result;
	}

	protected int hashCode0() {
		assert this.id == INVALID_ID;

		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (jobId ^ (jobId >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result =
			prime * result + ((eventName == null) ? 0 : eventName.hashCode());
		result = prime * result + eventSequenceNumber;
		result = prime * result + Float.floatToIntBits(fractionComplete);
		result =
			prime
					* result
					+ ((eventTimestamp == null) ? 0 : eventTimestamp.hashCode());
		result =
			prime * result + ((eventInfo == null) ? 0 : eventInfo.hashCode());
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
		if (!(obj instanceof BatchProcessingLogEntry)) {
			return false;
		}
		BatchProcessingLogEntry other = (BatchProcessingLogEntry) obj;
		if (id != other.id) {
			return false;
		}
		if (id == INVALID_ID) {
			return equals0(other);
		}

		// Double check that DB values are unchanged
		assert equals0(other);

		return true;
	}

	protected boolean equals0(BatchProcessingLogEntry other) {
		assert this != other;
		assert other != null;
		assert this.id == other.id;

		if (jobId != other.jobId) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (eventName == null) {
			if (other.eventName != null) {
				return false;
			}
		} else if (!eventName.equals(other.eventName)) {
			return false;
		}
		if (eventSequenceNumber != other.eventSequenceNumber) {
			return false;
		}
		if (Float.floatToIntBits(fractionComplete) != Float
				.floatToIntBits(other.fractionComplete)) {
			return false;
		}
		if (eventTimestamp == null) {
			if (other.eventTimestamp != null) {
				return false;
			}
		} else if (!eventTimestamp.equals(other.eventTimestamp)) {
			return false;
		}
		if (eventInfo == null) {
			if (other.eventInfo != null) {
				return false;
			}
		} else if (!eventInfo.equals(other.eventInfo)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BatchProcessingLogEntry [id=" + id + ", jobId=" + jobId
				+ ", type=" + type + ", eventName=" + eventName
				+ ", eventSequenceNumber=" + eventSequenceNumber
				+ ", fractionComplete=" + fractionComplete + ", eventInfo="
				+ eventInfo + ", eventTimestamp=" + eventTimestamp + "]";
	}

}
