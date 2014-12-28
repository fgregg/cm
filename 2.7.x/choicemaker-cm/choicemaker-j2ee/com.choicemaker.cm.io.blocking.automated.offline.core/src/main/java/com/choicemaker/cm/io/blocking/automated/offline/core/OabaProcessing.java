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
package com.choicemaker.cm.io.blocking.automated.offline.core;


/**
 * This interface the processing steps of the Offline Automated Blocking
 * Algorithm (OABA).
 * 
 * @author pcheung
 * @author rphall (renamed from IStatus to OabaProcessing)
 */
public interface OabaProcessing {

	// -- Ordered event ids used by OABA matching

	public static final int EVT_INIT = 0;
	public static final int EVT_CREATE_REC_VAL = 10;
	public static final int EVT_DONE_REC_VAL = 20;
	public static final int EVT_BLOCK_BY_ONE_COLUMN = 30;
	public static final int EVT_DONE_BLOCK_BY_ONE_COLUMN = 40;
	public static final int EVT_OVERSIZED_TRIMMING = 50;
	public static final int EVT_DONE_OVERSIZED_TRIMMING = 60;
	public static final int EVT_DEDUP_BLOCKS = 70;
	public static final int EVT_DONE_DEDUP_BLOCKS = 80;
	public static final int EVT_DEDUP_OVERSIZED_EXACT = 90;
	public static final int EVT_DONE_DEDUP_OVERSIZED_EXACT = 100;
	public static final int EVT_DEDUP_OVERSIZED = 110;
	public static final int EVT_DONE_DEDUP_OVERSIZED = 120;
	public static final int EVT_DONE_REVERSE_TRANSLATE_BLOCK = 130;
	public static final int EVT_DONE_REVERSE_TRANSLATE_OVERSIZED = 140;

	public static final int EVT_CREATE_CHUNK_IDS = 150;
	public static final int EVT_CREATE_CHUNK_OVERSIZED_IDS = 160;
	public static final int EVT_DONE_CREATE_CHUNK_IDS = 170;
	public static final int EVT_DONE_CREATE_CHUNK_DATA = 180;

	public static final int EVT_ALLOCATE_CHUNKS = 190;
	public static final int EVT_DONE_ALLOCATE_CHUNKS = 200;
	public static final int EVT_MATCHING_DATA = 210;
	public static final int EVT_DONE_MATCHING_DATA = 220;

	public static final int EVT_OUTPUT_DEDUP_MATCHES = 230;
	public static final int EVT_MERGE_DEDUP_MATCHES = 240;
	public static final int EVT_DONE_DEDUP_MATCHES = 250;
	public static final int EVT_DONE_OABA = 260;

	// -- Ordered events used by transitivity analysis

	public static final int EVT_DONE_TRANS_DEDUP_OVERSIZED =
		EVT_DONE_DEDUP_OVERSIZED;
	public static final int EVT_DONE_TRANSANALYSIS = EVT_DONE_OABA;

	// -- Estimates of the completion status of a job, 0 - 100 percent

	public static final int PCT_INIT = 0;
	public static final int PCT_CREATE_REC_VAL = 0;
	public static final int PCT_DONE_REC_VAL = 10;
	public static final int PCT_BLOCK_BY_ONE_COLUMN = 10;
	public static final int PCT_DONE_BLOCK_BY_ONE_COLUMN = 13;
	public static final int PCT_OVERSIZED_TRIMMING = 16;
	public static final int PCT_DONE_OVERSIZED_TRIMMING = 20;
	public static final int PCT_DEDUP_BLOCKS = 20;
	public static final int PCT_DONE_DEDUP_BLOCKS = 22;
	public static final int PCT_DEDUP_OVERSIZED_EXACT = 24;
	public static final int PCT_DONE_DEDUP_OVERSIZED_EXACT = 26;
	public static final int PCT_DEDUP_OVERSIZED = 28;
	public static final int PCT_DONE_DEDUP_OVERSIZED = 30;
	public static final int PCT_DONE_REVERSE_TRANSLATE_BLOCK = 35;
	public static final int PCT_DONE_REVERSE_TRANSLATE_OVERSIZED = 40;

	public static final int PCT_CREATE_CHUNK_IDS = 42;
	public static final int PCT_CREATE_CHUNK_OVERSIZED_IDS = 43;
	public static final int PCT_DONE_CREATE_CHUNK_IDS = 44;
	public static final int PCT_DONE_CREATE_CHUNK_DATA = 46;

	public static final int PCT_ALLOCATE_CHUNKS = 48;
	public static final int PCT_DONE_ALLOCATE_CHUNKS = 50;
	public static final int PCT_MATCHING_DATA = 60;
	public static final int PCT_DONE_MATCHING_DATA = 90;

	public static final int PCT_OUTPUT_DEDUP_MATCHES = 93;
	public static final int PCT_MERGE_DEDUP_MATCHES = 96;
	public static final int PCT_DONE_DEDUP_MATCHES = 99;
	public static final int PCT_DONE_OABA = 100;

	// -- Ordered events used by transitivity analysis

	public static final int PCT_DONE_TRANS_DEDUP_OVERSIZED =
		PCT_DONE_DEDUP_OVERSIZED;
	public static final int PCT_DONE_TRANSANALYSIS = PCT_DONE_OABA;

	// -- Enumeration of events and completion estimates

	public static enum OabaEvent {
		INIT(EVT_INIT, PCT_INIT),
		CREATE_REC_VAL(EVT_CREATE_REC_VAL, PCT_CREATE_REC_VAL),
		DONE_REC_VAL(EVT_DONE_REC_VAL, PCT_DONE_REC_VAL),
		BLOCK_BY_ONE_COLUMN(EVT_BLOCK_BY_ONE_COLUMN, PCT_BLOCK_BY_ONE_COLUMN),
		DONE_BLOCK_BY_ONE_COLUMN(EVT_DONE_BLOCK_BY_ONE_COLUMN,
				PCT_DONE_BLOCK_BY_ONE_COLUMN),
		OVERSIZED_TRIMMING(EVT_OVERSIZED_TRIMMING, PCT_OVERSIZED_TRIMMING),
		DONE_OVERSIZED_TRIMMING(EVT_DONE_OVERSIZED_TRIMMING,
				PCT_DONE_OVERSIZED_TRIMMING),
		DEDUP_BLOCKS(EVT_DEDUP_BLOCKS, PCT_DEDUP_BLOCKS),
		DONE_DEDUP_BLOCKS(EVT_DONE_DEDUP_BLOCKS, PCT_DONE_DEDUP_BLOCKS),
		DEDUP_OVERSIZED_EXACT(EVT_DEDUP_OVERSIZED_EXACT,
				PCT_DEDUP_OVERSIZED_EXACT),
		DONE_DEDUP_OVERSIZED_EXACT(EVT_DONE_DEDUP_OVERSIZED_EXACT,
				PCT_DONE_DEDUP_OVERSIZED_EXACT),
		DEDUP_OVERSIZED(EVT_DEDUP_OVERSIZED, PCT_DEDUP_OVERSIZED),
		DONE_DEDUP_OVERSIZED(EVT_DONE_DEDUP_OVERSIZED, PCT_DONE_DEDUP_OVERSIZED),
		DONE_REVERSE_TRANSLATE_BLOCK(EVT_DONE_REVERSE_TRANSLATE_BLOCK,
				PCT_DONE_REVERSE_TRANSLATE_BLOCK),
		DONE_REVERSE_TRANSLATE_OVERSIZED(EVT_DONE_REVERSE_TRANSLATE_OVERSIZED,
				PCT_DONE_REVERSE_TRANSLATE_OVERSIZED),

		CREATE_CHUNK_IDS(EVT_CREATE_CHUNK_IDS, PCT_CREATE_CHUNK_IDS),
		CREATE_CHUNK_OVERSIZED_IDS(EVT_CREATE_CHUNK_OVERSIZED_IDS,
				PCT_CREATE_CHUNK_OVERSIZED_IDS),
		DONE_CREATE_CHUNK_IDS(EVT_DONE_CREATE_CHUNK_IDS,
				PCT_DONE_CREATE_CHUNK_IDS),
		DONE_CREATE_CHUNK_DATA(EVT_DONE_CREATE_CHUNK_DATA,
				PCT_DONE_CREATE_CHUNK_DATA),

		ALLOCATE_CHUNKS(EVT_ALLOCATE_CHUNKS, PCT_ALLOCATE_CHUNKS),
		DONE_ALLOCATE_CHUNKS(EVT_DONE_ALLOCATE_CHUNKS, PCT_DONE_ALLOCATE_CHUNKS),
		MATCHING_DATA(EVT_MATCHING_DATA, PCT_MATCHING_DATA),
		DONE_MATCHING_DATA(EVT_DONE_MATCHING_DATA, PCT_DONE_MATCHING_DATA),

		OUTPUT_DEDUP_MATCHES(EVT_OUTPUT_DEDUP_MATCHES, PCT_OUTPUT_DEDUP_MATCHES),
		MERGE_DEDUP_MATCHES(EVT_MERGE_DEDUP_MATCHES, PCT_MERGE_DEDUP_MATCHES),
		DONE_DEDUP_MATCHES(EVT_DONE_DEDUP_MATCHES, PCT_DONE_DEDUP_MATCHES),
		DONE_OABA(EVT_DONE_OABA, PCT_DONE_OABA);

		public final int eventId;
		public final int percentComplete;

		OabaEvent(int evtId, int pct) {
			if (pct < 0 || pct > 100) {
				throw new IllegalArgumentException("invalid percentage: " + pct);
			}
			this.eventId = evtId;
			this.percentComplete = pct;
		}

	};

	// -- Other manifest constants

	public static final char DELIMIT = ':';

	/** This methods gets the most recent processing event */
	OabaEvent getCurrentProcessingEventObject();

	/** This methods gets the id of the most recent processing event */
//	@Deprecated
	public int getCurrentProcessingEventId();

	/**
	 * This method sets the current processing event with null additional info.
	 */
	void setCurrentProcessingEvent(OabaEvent event);

	/**
	 * This method sets the current processing event with additional info.
	 */
	public void setCurrentProcessingEvent(OabaEvent stat, String info);

	/**
	 * This method gets the additional info associated with the most recent
	 * processing event.
	 */
	public String getAdditionalInfo();

}
