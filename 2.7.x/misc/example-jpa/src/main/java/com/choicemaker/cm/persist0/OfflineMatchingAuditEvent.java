package com.choicemaker.cm.persist0;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

@NamedQuery(name = "batchJobAuditFindAll",
		query = "Select event from TransitivityJobAuditEvent event")
@Entity
@DiscriminatorValue(value = "OFFLINEMATCH")
public class OfflineMatchingAuditEvent extends CMP_AuditEvent {

	private static final long serialVersionUID = 271L;

	public static final String OBJECT_TYPE =
		OfflineMatchingBean.TABLE_DISCRIMINATOR;

	protected OfflineMatchingAuditEvent() {
	}

	public OfflineMatchingAuditEvent(String transactionId, BatchJobBean batchJob) {
		super(transactionId, OBJECT_TYPE, batchJob, new Date(),
				CMP_WellKnownEventType.STATUS.toString(), BatchJobStatus
						.toString(batchJob.getStatus()), null);
	}

	public OfflineMatchingAuditEvent(String transactionId,
			BatchJobBean batchJob, Date timestamp, BatchJobStatus batchJobStatus) {
		super(transactionId, OBJECT_TYPE, batchJob, timestamp,
				CMP_WellKnownEventType.STATUS.toString(), BatchJobStatus
						.toString(batchJobStatus), null);
	}

	public BatchJobStatus getStatus() {
		String s = this.getEvent();
		BatchJobStatus retVal = BatchJobStatus.fromString(s);
		return retVal;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OfflineMatchingAuditEvent [eventId=").append(getId())
				.append(", transactionId=").append(getTransactionId())
				.append(", job=").append(getObject().getId())
				.append(", timestamp=").append(getTimestamp())
				.append(", event=").append(getEvent());
		if (getEventDetail() != null) {
			sb.append(", detail=" + getEventDetail());
		}
		sb.append("]");
		return sb.toString();
	}

}
