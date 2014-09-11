package com.choicemaker.cm.persist0;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

@NamedQuery(name = "batchJobAuditFindAll",
query = "Select event from TransitivityJobAuditEvent event")
@Entity
@DiscriminatorValue(value="OFFLINEMATCH")
public class OfflineMatchingAuditEvent extends CM_AuditEvent {

	private static final long serialVersionUID = 271L;

	public static final String OBJECT_TYPE = OfflineMatchingBean.TABLE_DISCRIMINATOR;

	public static final String EVENT_TYPE = "STATUS";

	protected OfflineMatchingAuditEvent() {
	}

	public OfflineMatchingAuditEvent(String transactionId, BatchJobBean batchJob) {
		super(transactionId, OBJECT_TYPE, batchJob, new Date(),
				EVENT_TYPE, BatchJobStatus.toString(batchJob.getStatus()), null);
	}

	public OfflineMatchingAuditEvent(String transactionId, BatchJobBean batchJob,
			Date timestamp, BatchJobStatus batchJobStatus) {
		super(transactionId, OBJECT_TYPE, batchJob, timestamp,
				EVENT_TYPE, BatchJobStatus.toString(batchJobStatus), null);
	}
	
	public BatchJobStatus getStatus() {
		String s = this.getEvent();
		BatchJobStatus retVal = BatchJobStatus.fromString(s);
		return retVal;
	}

}
