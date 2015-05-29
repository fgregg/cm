package com.choicemaker.cm.batch.impl;

import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.CN_EVENT_INFO;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.CN_EVENT_NAME;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.CN_EVENT_SEQNUM;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.CN_EVENT_TIMESTAMP;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.CN_EVENT_TYPE;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.CN_FRACTION_COMPLETE;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.CN_ID;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.CN_JOB_ID;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.batch.impl.BatchProcessingEventJPA.TABLE_NAME;

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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.choicemaker.cm.args.BatchProcessing;
import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.PersistentObject;
import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.batch.BatchJobProcessingEvent;

@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public class BatchProcessingEventEntity extends AbstractPersistentObject
		implements BatchProcessing, BatchJobProcessingEvent, Serializable {

	private static final long serialVersionUID = 271L;

	/** Default id value for non-persistent batch jobs */
	public static final long INVALID_ID = 0;

	/** Default event name */
	public static final String INVALID_EVENT_NAME = null;

	/** Default event sequence number */
	public static final int INVALID_EVENT_SEQNUM = -1;

	/** Default event information */
	public static final String DEFAULT_EVENT_INFO = null;

	/** Default event timestamp */
	public static final Date INVALID_TIMESTAMP = new Date(0L);

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

	protected BatchProcessingEventEntity() {
		this.jobId = PersistentObject.NONPERSISTENT_ID;
		this.type = DISCRIMINATOR_VALUE;
		this.eventName = INVALID_EVENT_NAME;
		this.eventSequenceNumber = INVALID_EVENT_SEQNUM;
		this.fractionComplete = DEFAULT_FRACTION_COMPLETE;
		this.eventInfo = DEFAULT_EVENT_INFO;
		this.eventTimestamp = INVALID_TIMESTAMP;
	}

	protected BatchProcessingEventEntity(long jobId, String type, String evtName,
			int eventSeqNum, float fractionComplete, String info) {
		if (jobId == PersistentObject.NONPERSISTENT_ID) {
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
	public ProcessingEvent getProcessingEvent() {
		ProcessingEvent retVal =
			new BatchProcessingEvent(getEventName(), getEventSequenceNumber(),
					getFractionComplete());
		return retVal;
	}

	@Override
	public String toString() {
		return "BatchProcessingEventEntity [id=" + id + ", jobId=" + jobId
				+ ", type=" + type + ", eventName=" + eventName
				+ ", eventSequenceNumber=" + eventSequenceNumber
				+ ", fractionComplete=" + fractionComplete + ", eventInfo="
				+ eventInfo + ", eventTimestamp=" + eventTimestamp + "]";
	}

}
