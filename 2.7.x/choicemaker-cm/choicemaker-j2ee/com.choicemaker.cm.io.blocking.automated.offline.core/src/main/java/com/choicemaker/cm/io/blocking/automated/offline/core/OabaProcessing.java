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

import com.choicemaker.cm.args.BatchProcessing;

/**
 * This interface defines event ids and completion estimates used in Offline
 * Automated Blocking Algorithm (OABA) processing.
 * 
 * @author pcheung (implemented as IStatus)
 * @author rphall (refactored from IStatus to BatchProcessing)
 */
public interface OabaProcessing extends BatchProcessing {

	// -- Ordered event ids used by OABA processing

	int EVT_CREATE_REC_VAL = 10;
	int EVT_DONE_REC_VAL = 20;
	int EVT_BLOCK_BY_ONE_COLUMN = 30;
	int EVT_DONE_BLOCK_BY_ONE_COLUMN = 40;
	int EVT_OVERSIZED_TRIMMING = 50;
	int EVT_DONE_OVERSIZED_TRIMMING = 60;
	int EVT_DEDUP_BLOCKS = 70;
	int EVT_DONE_DEDUP_BLOCKS = 80;
	int EVT_DEDUP_OVERSIZED_EXACT = 90;
	int EVT_DONE_DEDUP_OVERSIZED_EXACT = 100;
	int EVT_DEDUP_OVERSIZED = 110;
	int EVT_DONE_DEDUP_OVERSIZED = 120;
	int EVT_DONE_REVERSE_TRANSLATE_BLOCK = 130;
	int EVT_DONE_REVERSE_TRANSLATE_OVERSIZED = 140;

	int EVT_CREATE_CHUNK_IDS = 150;
	int EVT_CREATE_CHUNK_OVERSIZED_IDS = 160;
	int EVT_DONE_CREATE_CHUNK_IDS = 170;
	int EVT_DONE_CREATE_CHUNK_DATA = 180;

	int EVT_ALLOCATE_CHUNKS = 190;
	int EVT_DONE_ALLOCATE_CHUNKS = 200;
	int EVT_MATCHING_DATA = 210;
	int EVT_DONE_MATCHING_DATA = 220;

	int EVT_OUTPUT_DEDUP_MATCHES = 230;
	int EVT_MERGE_DEDUP_MATCHES = 240;
	int EVT_DONE_DEDUP_MATCHES = 250;

	// -- Estimates of the completion status of a job, 0.0 - 1.00

	float PCT_CREATE_REC_VAL = 0.0f;
	float PCT_DONE_REC_VAL = 0.10f;
	float PCT_BLOCK_BY_ONE_COLUMN = 0.10f;
	float PCT_DONE_BLOCK_BY_ONE_COLUMN = 0.13f;
	float PCT_OVERSIZED_TRIMMING = 0.16f;
	float PCT_DONE_OVERSIZED_TRIMMING = 0.20f;
	float PCT_DEDUP_BLOCKS = 0.20f;
	float PCT_DONE_DEDUP_BLOCKS = 0.22f;
	float PCT_DEDUP_OVERSIZED_EXACT = 0.24f;
	float PCT_DONE_DEDUP_OVERSIZED_EXACT = 0.26f;
	float PCT_DEDUP_OVERSIZED = 0.28f;
	float PCT_DONE_DEDUP_OVERSIZED = 0.30f;
	float PCT_DONE_REVERSE_TRANSLATE_BLOCK = 0.35f;
	float PCT_DONE_REVERSE_TRANSLATE_OVERSIZED = 0.40f;

	float PCT_CREATE_CHUNK_IDS = 0.42f;
	float PCT_CREATE_CHUNK_OVERSIZED_IDS = 0.43f;
	float PCT_DONE_CREATE_CHUNK_IDS = 0.44f;
	float PCT_DONE_CREATE_CHUNK_DATA = 0.46f;

	float PCT_ALLOCATE_CHUNKS = 0.48f;
	float PCT_DONE_ALLOCATE_CHUNKS = 0.50f;
	float PCT_MATCHING_DATA = 0.60f;
	float PCT_DONE_MATCHING_DATA = 0.90f;

	float PCT_OUTPUT_DEDUP_MATCHES = 0.93f;
	float PCT_MERGE_DEDUP_MATCHES = 0.96f;
	float PCT_DONE_DEDUP_MATCHES = 0.99f;

}
