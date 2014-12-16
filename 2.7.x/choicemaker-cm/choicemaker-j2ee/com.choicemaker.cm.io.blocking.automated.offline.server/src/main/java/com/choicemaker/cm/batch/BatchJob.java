package com.choicemaker.cm.batch;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import com.choicemaker.cm.core.IControl;

public interface BatchJob extends IControl, Serializable {

	/** Default id value for non-persistent batch jobs */
	long INVALID_ID = 0;

	/** Minimum valid value for fractionComplete (inclusive) */
	int MIN_PERCENTAGE_COMPLETED = 0;

	/** Maximum valid value for fractionComplete (inclusive) */
	int MAX_PERCENTAGE_COMPLETED = 100;

	BatchJobRigor DEFAULT_RIGOR = BatchJobRigor.COMPUTED;

	String STATUS_NEW = "NEW";
	String STATUS_QUEUED = "QUEUED";
	String STATUS_STARTED = "STARTED";
	String STATUS_COMPLETED = "COMPLETED";
	String STATUS_FAILED = "FAILED";
	String STATUS_ABORT_REQUESTED = "ABORT_REQUESTED";
	String STATUS_ABORTED = "ABORTED";
	String STATUS_CLEAR = "STATUS_CLEAR";

	long getId();

	long getBatchParentId();

	long getUrmId();

	long getTransactionId();

	String getExternalId();

	/**
	 * Indicates whether the results of a batch job have been (in the case of a
	 * completed job) or will be (in the case of a running job)
	 * {@link BatchJobRigor#ESTIMATED estimated} or
	 * {@link BatchJobRigor#COMPUTED computed}.
	 */
	BatchJobRigor getBatchJobRigor();

	String getDescription();

	/**
	 * Returns the working directory of this job. This directory is accessible
	 * from the server(s) on which this job is processed and in the case of
	 * multiple servers, the directory represents the same physical location
	 * across all the servers.
	 */
	File getWorkingDirectory();

	String getStatus();

	Date getTimeStamp(String status);

	int getFractionComplete();

	Date getRequested();

	Date getQueued();

	Date getStarted();

	Date getCompleted();

	Date getFailed();

	Date getAbortRequested();

	Date getAborted();

	void setDescription(String description);

	/** Use markAsXxx() methods instead */
	void setStatus(String status);

	void setFractionComplete(int i);

	void markAsQueued();

	void markAsStarted();

	/**
	 * This method is called when the job is restarted. This method doesn't
	 * check if the status is currently queued.
	 */
	void markAsReStarted();

	void markAsCompleted();

	void markAsFailed();

	void markAsAbortRequested();

	void markAsAborted();

	boolean shouldStop();

}