package com.choicemaker.demo.persist0;

import java.util.Date;
import java.util.List;

import com.choicemaker.cm.core.IControl;

public interface BatchJob extends IControl {

	int MIN_PERCENTAGE_COMPLETED = 0;

	int MAX_PERCENTAGE_COMPLETED = 100;

	void markAsQueued();

	void markAsStarted();

	/**
	 * This method is misnamed. It is called when a job is re-queued, not when
	 * it is restarted. This method doesn't check the current state of the job
	 * before re-queuing it.
	 *
	 */
	void markAsReStarted();

	void markAsCompleted();

	void markAsFailed();

	void markAsAbortRequested();

	void markAsAborted();

	/**
	 * This operation has effect only if job status is STARTED.
	 * 
	 * @param percentageCompleted
	 *            a non-negative percentage in the range 0 to 100, inclusive.
	 */
	void updatePercentageCompleted(float percentageCompleted);

	/**
	 * This operation has effect only if job status is STARTED or QUEUED
	 * 
	 * @param percentageCompleted
	 *            a non-negative percentage in the range 0 to 100, inclusive.
	 */
	void updatePercentageCompleted(int percentageCompleted);

	Date getRequested();

	Date getQueued();

	Date getStarted();

	Date getCompleted();

	Date getFailed();

	Date getAbortRequested();

	Date getAborted();

	long getId();

	String getExternalId();

	void setExternalId(String externalId);

	String getTransactionId();

	String getType();

	String getDescription();

	void setDescription(String description);

	int getPercentageComplete();

	void setPercentageComplete(int percentage);

	Date getTimeStamp(BatchJobStatus batchJobStatus);

	List<CMP_AuditEvent> getTimeStamps();

	BatchJobStatus getStatus();

	void setStatus(BatchJobStatus currentStatus);

	void setStatus(BatchJobStatus batchJobStatus, String detail);

}
