package com.choicemaker.demo.oaba0;

import java.util.Date;

import com.choicemaker.cm.core.IControl;

public interface BatchJob extends IControl{

	public static enum STATUS {
		NEW(false), QUEUED(false), STARTED(false), COMPLETED(true),
		FAILED(true), ABORT_REQUESTED(false), ABORTED(true), CLEAR(true);
		public boolean isTerminal;
	
		private STATUS(boolean terminal) {
			this.isTerminal = terminal;
		}
	}

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

	long getTransactionId();

	void setTransactionId(long transactionId);

	String getType();

	String getDescription();

	void setDescription(String description);

	String getStatusAsString();

	void setStatusAsString(String status);

	int getPercentageComplete();

	void setPercentageComplete(int percentage);

	BatchJob.STATUS getStatus();

	void setStatus(BatchJob.STATUS currentStatus);

	Date getTimeStamp(BatchJob.STATUS status);

}