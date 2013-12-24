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

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.core.util.LongArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;

/**
 * This write out the block distributio diagram.
 *
 * This assumes that all the blocks are &lt;= max.
 * 
 * @author pcheung
 *
 */
public class BlockStatistics {
	
	private static final Logger log = Logger.getLogger(BlockStatistics.class);

	private IBlockSource bs;
	private int totalBlocks = 0, totalElements = 0;
	private float avg;
	
	private int intervals = 10;
	private int max;
	private int step;
	
	//total number of pairwaise comparisons
	private int totalComparisons;
	
	//this keep track of which blocking field combinations are most oftenly used.
	private HashMap useBlockingFields = new HashMap ();
	
	//this variable keeps track on block size distribution.
	// distribution[i] counts the number of blocks with size > i*step and <= (i+1)*step.
	private int [] distribution;
	
	
	/** This contructor creates a BlockStatistics object with these parameters.
	 * 
	 * @param blockSource - block source
	 * @param max - maximum number of records in a block
	 * @param intervals - the number of interval in the histogram.
	 */
	public BlockStatistics (IBlockSource blockSource, int max, int intervals) {
		this.bs = blockSource;
		this.max = max;
		this.intervals = intervals;
		
		this.step = max / intervals;
		
		distribution = new int [intervals]; 
	}
	

	/** This method write the block distribution data with log.info.
	 * 
	 * @param stat
	 */
	public void writeStat () throws BlockingException {
		compute();
		writeOut ();
	}

	
	/** This method write the block distribution data with log.info.  This version also
	 * writes the blocks to another sink.
	 * 
	 * @param stat
	 */
	public void writeStat (IBlockSink sink) throws BlockingException{
		compute(sink);
		writeOut ();
	}
	
	
	private void writeOut () {
		log.info("Average block size " + avg);
		
		for (int i=0; i< intervals; i++) {
			int min = i * step;
			int max = (i+1) * step;
			log.info(min + "-" + max + " " + distribution[i]);
		}
		
		log.info("Total number of comparisons needed: " + totalComparisons);
		
		log.info("Blocking Fields distribution");
		TreeSet ts = new TreeSet (this.useBlockingFields.keySet());
		Iterator it = ts.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Integer I = (Integer) useBlockingFields.get(key);
			log.info(key + " count: " + I.toString());
		}
		
	}

	
	/** Computes the average and distribution. */
	public void compute () throws BlockingException{
		bs.open();
		
		while (bs.hasNext()) {
			compute (bs.getNext());
		}
		
		avg = totalElements * 1.0f / totalBlocks;
		bs.close();
	}
	
	
	private void compute (BlockSet bSet) {
		totalBlocks ++;
		LongArrayList block = bSet.getRecordIDs();
		totalElements += block.size();
		putDistribution (block.size());
		
		//record the blocking fields
		recordBlockingFields (bSet);
			
		totalComparisons += block.size()*(block.size()-1)/2;
	}
	
	
	/** This variation computes the average and distribution, and also outputs the blocks to
	 *  another sink - i.e. text file.
	 * @param n
	 */
	private void compute (IBlockSink sink) throws BlockingException{
		
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
	
	
	/** This method records the blocking fields used in this block
	 * 
	 * @param bSet
	 */
	private void recordBlockingFields (BlockSet bSet) {
		IntArrayList columns = bSet.getColumns();
		StringBuffer key = new StringBuffer (20);
		key.append( Integer.toString(columns.size()));
		key.append (":");
		for (int i=0; i<columns.size(); i++) {
			key.append (" ");
			key.append( columns.get(i));
		}
		String temp = key.toString();
		
		Integer C = (Integer) useBlockingFields.get(temp);
		if (C == null) {
			C = new Integer(1);
			this.useBlockingFields.put(temp, C);
		} else {
			C = new Integer (C.intValue() + 1);
			this.useBlockingFields.put(temp, C);
		}
		
	}
	
	
	/** This method puts the count into the correct bucket.
	 * 
	 * @param n is size of the block, >= 2.
	 */
	private void putDistribution (int n) {
		int i = new Double( Math.floor ( (n - 1.0) / step) ).intValue() ;
		
		distribution [i] ++;
	}
	
	public float getAverage () {
		return avg;
	}
	
	/** This method returns the block size distribution.
	 * distribution[i] counts the number of blocks with size > i*10 and <= (i+1)*10.
	 * 
	 * @return int [] - returns the frequency distribution.
	 */
	public int [] getDistribution () {
		return distribution;
	}
	
	
	/** This method returns a hashmap containing blocking field combinations and the frequency they
	 * appear in the blocks.
	 * @return HashMap - returns the blocking field combination frequency.
	 */
	public HashMap getBlockingFieldsUsuage () {
		return useBlockingFields;
	}
	

}
