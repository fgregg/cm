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

import java.io.Serializable;

/**
 * This Interface specifies the important values of the OABA.
 * 
 * @author pcheung
 *
 */
public interface IOABAProperties extends Serializable {
	
	/** This returns the number of processors this server has.
	 * 
	 * @return int
	 */
	public int getNumProcessors ();
	
	
	/** This defines the root temporary directory in which to stores the OABA 
	 * processing files.  Each job is stored under $getTempDir()/job_id. 
	 * 
	 * @return String
	 */
	public String getTempDir ();
	
	
	/** This defines the maximum size of a regular block.
	 * 
	 * @return int
	 */
	public int getMaxBlockSize ();
	
	
	/** This defines which oversized blocks to keep.  If an oversized block has more elements
	 * than getMaxOversized, then it is thrown away.
	 * 
	 * @return int
	 */
	public int getMaxOversized ();
	
	
	/** This defines which oversized blocks to keep.  If an oversized block was created from
	 * N number of columns (or fields) and N is less than getMinFields, then it is
	 * thrown away.
	 * 
	 * @return int
	 */
	public int getMinFields ();
	
	
	/** This defines the number of block files to process at a time during block
	 * deduplication.
	 * 
	 * @return int
	 */
	public int getBlockDedupInterval ();
	
	
	/** This defines the maximum size of a chunk.
	 * 
	 * @return int
	 */
	public int getMaxChunkSize ();
	
	
	/** This returns the maximum number of chunk data files to create at once.  If there
	 * are 1000 chunk files and getMaxChunkFiles is 800, then OABA reads through the
	 * record source twice.  The pass creates 800 chunk data files and the second pass
	 * creates 200 chunk data files.
	 * 
	 * This is necessary because the OS has a limit on how many files can be opened at once.
	 * 
	 * @return int
	 */
	public int getMaxChunkFiles ();
	
	
	/** This defines the maximum number of MatchRecord2 object to hold in memory 
	 * during the match deduplication process. 
	 * 
	 * @return int
	 */
	public int getMaxMatchSize ();


}
