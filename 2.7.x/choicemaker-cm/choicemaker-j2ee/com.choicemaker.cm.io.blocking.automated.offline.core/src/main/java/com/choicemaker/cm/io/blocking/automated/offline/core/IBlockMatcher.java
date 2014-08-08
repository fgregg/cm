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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.RecordSource;

/**
 * This interface defines how to match a block source.
 *   
 * @author pcheung
 *
 */
public interface IBlockMatcher {

	/** This method matches all the blocks in the block source.
	 * 
	 * @param bSource - block srouce
	 * @param model - probability model.  Need this to read records
	 * @param rs - record source
	 * @param mSink - match record sink
	 * @param append - true if you want to append to mSink.
	 * @param differ - differ threshold
	 * @param match - match threshold
	 * @param maxBlockSize - maximum block size
	 * @param validator - indicates if a pair is valid for comparison
	 */
	public void matchBlocks (IBlockSource bSource, IProbabilityModel model, 
		RecordSource rs, IMatchRecordSink mSink, boolean append, float differ, float match, 
		int maxBlockSize, IValidator validator) throws BlockingException;
		
	
	/** This returns the number of comparisons made.
	 * 
	 * @return int - number of comparisons made.
	 */
	public int getNumComparesMade ();
	
	/** This returns the number of matches and holds found.
	 * 
	 * @return int - number of matches found
	 */
	public int getNumMatches ();
	
	
	/** This returns the number of blocks processed.
	 * 
	 * @return int - number of blocks compared
	 */
	public int getNumBlocks ();
		
}
