package com.choicemaker.demo.persist0;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import com.choicemaker.demo.persist0.TransitivityJob.STATUS;

@NamedQuery(name = "transitivityJobAuditFindAll",
		query = "Select event from TransitivityJobAuditEvent event")
@Entity
@DiscriminatorValue(value = "TRANSITIVITY")
public class TransitivityJobAuditEvent extends CMP_AuditEvent {

	private static final long serialVersionUID = 271L;

	public static final String OBJECT_TYPE =
		TransitivityJobBean.TABLE_DISCRIMINATOR;

	public static final String standardize(STATUS status) {
		String retVal = null;
		if (status != null) {
			retVal = status.name();
			assert retVal.equals(retVal.trim().toUpperCase());
		}
		return retVal;
	}

	public static final STATUS toTransitivityJobStatus(String status) {
		STATUS retVal = null;
		if (status != null) {
			retVal = STATUS.valueOf(status.trim().toUpperCase());
		}
		return retVal;
	}

	protected TransitivityJobAuditEvent() {
	}

	public TransitivityJobAuditEvent(String transactionId, BatchJobBean batchJob) {
		super(transactionId, OBJECT_TYPE, batchJob, new Date(),
				CMP_WellKnownEventType.STATUS.toString(), BatchJobStatus
						.toString(batchJob.getStatus()), null);
	}

	public TransitivityJobAuditEvent(String transactionId,
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
		sb.append("TransitivityJobAuditEvent [eventId=").append(getId())
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
