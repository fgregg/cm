package com.choicemaker.cm.batch.impl;

public interface BatchProcessingEventJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OABA_BATCH_PROCESSING";

	/** Name of the column used to distinguish between batch jobs and sub-types */
	String DISCRIMINATOR_COLUMN = "EVENT_TYPE";

	/**
	 * Value of the discriminator column used to mark abstract BatchJob types.
	 * None should be marked if sub-classes are set up correctly with their own
	 * discriminator values.
	 */
	String DISCRIMINATOR_VALUE = "BATCH";

	/**
	 * Generated id column.
	 * 
	 * @see #ID_GENERATOR_NAME
	 */
	String CN_ID = "ID";

	/** Required link to a persistent batch job */
	String CN_JOB_ID = "JOB_ID";

	/**
	 * Discriminator column
	 * 
	 * @see #DISCRIMINATOR_COLUMN
	 */
	String CN_EVENT_TYPE = DISCRIMINATOR_COLUMN;

	/** The name of an event */
	String CN_EVENT_NAME = "EVENT_NAME";

	/** An ordinal indicating the order of an event with respect to other events */
	String CN_EVENT_SEQNUM = "EVENT_SEQNUM";

	/**
	 * Estimate of the amount of processing already complete, between 0.0 and
	 * 1.0 (inclusive)
	 */
	String CN_FRACTION_COMPLETE = "FRACTION_COMPLETE";

	/** Internally defined transaction id that links several related batch jobs */
	String CN_EVENT_INFO = "EVENT_INFO";

	/** Timestamp column of the audit table */
	String CN_EVENT_TIMESTAMP = "TIMESTAMP";

	String ID_GENERATOR_NAME = "BATCHPROCESSING";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "BATCHPROCESSING";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

}