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
package com.choicemaker.cm.io.blocking.automated.offline.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.LongArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;

/**
 * This versions of BlockStatistics uses a HashMap, instead of array, to store the frequency.
 * 
 * @author pcheung
 *
 */
public class BlockStatisticsMap {

	private static final Logger log = Logger.getLogger(BlockStatisticsMap.class);

	private IBlockSource bs;
	private int totalBlocks = 0, totalElements = 0;
	private float avg;
	
	//total number of pairwaise comparisons
	private int totalComparisons;

	HashMap distribution = new HashMap ();
	
	
	
	/** This contructor creates a BlockStatistics object with these parameters.
	 * 
	 * @param blockSource - block source
	 * @param max - maximum number of records in a block
	 * @param intervals - the number of interval in the histogram.
	 */
	public BlockStatisticsMap (IBlockSource blockSource) {
		this.bs = blockSource;
	}
	

	/** This method write the block distribution data with log.info.
	 * 
	 * @param stat
	 */
	public void writeStat () throws BlockingException{
		compute();
		write ();
	}
	
	
	/** This method write the block distribution data with log.info.  This version also
	 * writes the blocks to another sink.
	 * 
	 * @param stat
	 */
	public void writeStat (IBlockSink sink)throws BlockingException {
		compute(sink);
		write ();
	}


	private void write () {
		log.info("Average block size " + avg);
		
		Set keys = distribution.keySet();
		int [] k = new int [keys.size()];
		Iterator it = keys.iterator();
		int i = 0;
		while (it.hasNext ()) {
			k[i] = ((Integer) it.next()).intValue();
			i++;
		}
		Arrays.sort (k);
		
		for (i=0; i<k.length; i++) {
			Integer count = (Integer) distribution.get(new Integer(k[i]));
			log.info(k[i] + " " + count.intValue ());
		}
		log.info("Total number of comparisons needed: " + totalComparisons);
	}


	
	/** Computes the average and distribution. */
	public void compute () throws BlockingException {
		bs.open();
		
		while (bs.hasNext()) {
			compute (bs.getNext());
		}
		
		avg = totalElements * 1.0f / totalBlocks;
		bs.close();
	}
	
	
	/** This variation computes the average and distribution, and also outputs the blocks to
	 *  another sink - i.e. text file.
	 * @param n
	 */
	private void compute (IBlockSink sink) throws BlockingException {
		
		bs.open();
		sink.open();
		
		while (bs.hasNext()) {
			BlockSet bSet = bs.getNext ();
			compute (bSet);
			sink.writeBlock(bSet);
		}
		
		avg = totalElements * 1.0f / totalBlocks;
		bs.close();
		sink.close();
	}	

	private void compute (BlockSet bSet) {
		totalBlocks ++;
		LongArrayList block = bSet.getRecordIDs();
		totalElements += block.size();
		putDistribution (block.size());
			
		totalComparisons += block.size()*(block.size()-1)/2;
	}


	/** This method puts the count into the correct bucket.
	 * 
	 * @param n is >= 2.
	 */
	private void putDistribution (int n) {
		Integer I = new Integer(n);
		Integer count = (Integer) distribution.get(I);
		
		if (count != null) {
			count = new Integer (count.intValue() + 1);
		} else {
			count = new Integer (1);
		}
		distribution.put(I, count);
		
	}
	
	public float getAverage () {
		return avg;
	}
	
}
