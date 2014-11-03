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

	public static final int INIT = 0;
	public static final int CREATE_REC_VAL = 10;
	public static final int DONE_REC_VAL = 20;
	public static final int BLOCK_BY_ONE_COLUMN = 30;
	public static final int DONE_BLOCK_BY_ONE_COLUMN = 40;
	public static final int OVERSIZED_TRIMMING = 50;
	public static final int DONE_OVERSIZED_TRIMMING = 60;
	public static final int DEDUP_BLOCKS = 70;
	public static final int DONE_DEDUP_BLOCKS = 80;
	public static final int DEDUP_OVERSIZED_EXACT = 90;
	public static final int DONE_DEDUP_OVERSIZED_EXACT = 100;
	public static final int DEDUP_OVERSIZED = 110;
	public static final int DONE_DEDUP_OVERSIZED = 120;
	public static final int DONE_REVERSE_TRANSLATE_BLOCK = 130;
	public static final int DONE_REVERSE_TRANSLATE_OVERSIZED = 140;

	public static final int CREATE_CHUNK_IDS = 150;
	public static final int CREATE_CHUNK_OVERSIZED_IDS = 160;
	public static final int DONE_CREATE_CHUNK_IDS = 170;
	public static final int DONE_CREATE_CHUNK_DATA = 180;

	public static final int ALLOCATE_CHUNKS = 190;
	public static final int DONE_ALLOCATE_CHUNKS = 200;
	public static final int MATCHING_DATA = 210;
	public static final int DONE_MATCHING_DATA = 220;

	public static final int OUTPUT_DEDUP_MATCHES = 230;
	public static final int MERGE_DEDUP_MATCHES = 240;
	public static final int DONE_DEDUP_MATCHES = 250;
	public static final int DONE_OABA = 260;
	
	// -- Ordered events used by transitivity analysis
	
	public static final int DONE_TRANS_DEDUP_OVERSIZED = DONE_DEDUP_OVERSIZED;
	public static final int DONE_TRANSANALYSIS = DONE_OABA;
	
	
	// -- Other manifest constants

	public static final char DELIMIT = ':';

	/**
	 * This methods gets the current processing step
	 */
	public int getCurrentProcessingEvent();

	/**
	 * This method sets the current processing step with null additional info.
	 */
	public void setCurrentProcessingEvent(int stat);

	/**
	 * This method sets the current processing step with additional info.
	 */
	public void setCurrentProcessingEvent(int stat, String info);

	/**
	 * This method gets the additional info associated with this processing
	 * step.
	 */
	public String getAdditionalInfo();
}
