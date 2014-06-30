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

import com.choicemaker.cm.core.base.BlockingException;

/**
 * This interface defines how to store and get status information.
 * 
 * @author pcheung
 *
 */
public interface IStatus {

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
	public static final int DONE_PROGRAM = 260;
	
	public static final char DELIMIT = ':';


	/** This method sets the current status.
	 * 
	 * @param stat
	 * @throws BlockingException
	 */
	public void setStatus (int stat) throws BlockingException;
	
	
	/** This methods gets the current status.
	 * 
	 * @return int - gets the current status
	 * @throws BlockingException
	 */
	public int getStatus () throws BlockingException;
	
	
	/** This method sets the current status with additional info.
	 * 
	 * @param stat
	 * @param info
	 * @throws BlockingException
	 */
	public void setStatus (int stat, String info) throws BlockingException;
	
	
	/** This method gets the additional info assoicated with this status.
	 * 
	 * @return String - additional info.
	 * @throws BlockingException
	 */
	public String getAdditionalInfo () throws BlockingException;
}
