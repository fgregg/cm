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

import java.util.ArrayList;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;

/**
 * This object creates a list of IBlockSinks, one for each block size. It also
 * allows more than one sink per block size, if that block size bucket is huge.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class BlocksSpliter {

	private ArrayList sinks;

	// this keeps track of number of bucket files per block size.
	// numBuckets[i] contains the buckets for size i + 1.
	// numBuckets[0] is 0 because there is no single element block.
	private int[] numBuckets;

	// if there is more than 1 bucket file for an id, we need to rotate the
	// bucket files.
	// this variable stores the rotation info
	private int[] rotate;

	private int maxSize; // maximum size of a block set.

	private IBlockSinkSourceFactory bFactory;

	public BlocksSpliter(IBlockSinkSourceFactory bFactory, int maxSize) {
		this.maxSize = maxSize;
		this.bFactory = bFactory;

		// initialize numBuckets
		numBuckets = new int[maxSize];
		numBuckets[0] = 0;
		for (int i = 1; i < maxSize; i++) {
			numBuckets[i] = 1;
		}

		// initialize rotate
		rotate = new int[maxSize + 1];
		for (int i = 0; i < maxSize; i++) {
			rotate[i] = 0;
		}
	}

	/**
	 * This method sets the number of bucket files to create for the given block
	 * size. By default there is 1 bucket file per size, but you can increase
	 * that if there are a lot of blocks for a given size.
	 * 
	 * @param blockSize
	 * @param num
	 */
	public void setSize(int blockSize, int num) {
		numBuckets[blockSize - 1] = num;
	}

	/**
	 * This method initializes the sinks array list.
	 * 
	 * Call this method after you are done calling setSize (int, int).
	 *
	 */
	public void Initialize() throws BlockingException {
		int num = getSum(maxSize + 1);
		sinks = new ArrayList(num);

		for (int i = 0; i < num; i++) {
			IBlockSink sink = bFactory.getNextSink();
			sinks.add(sink);

			// remove old file
			// if (sink.exists()) bFactory.removeSink(sink);
		}
	}

	/**
	 * This method opens all the sinks.
	 * 
	 */
	public void openAll() throws BlockingException {
		for (int i = 0; i < sinks.size(); i++) {
			IBlockSink sink = (IBlockSink) sinks.get(i);
			sink.open();
		}
	}

	/**
	 * This method closes all the sinks.
	 * 
	 */
	public void closeAll() throws BlockingException {
		for (int i = 0; i < sinks.size(); i++) {
			IBlockSink sink = (IBlockSink) sinks.get(i);
			sink.close();
		}
	}

	/**
	 * This returns the array list of the bucket sinks. They are in the order of
	 * block size.
	 * 
	 * @return ArrayList of IBlockSink
	 */
	public ArrayList getSinks() {
		return sinks;
	}

	/**
	 * This method write the block set to the appropiate file by calculating the
	 * number of file with size less than it and take into consideration if
	 * there is more than 1 file for this size.
	 * 
	 * @param block
	 */
	public void writeToSink(BlockSet block) throws BlockingException {
		int size = block.getRecordIDs().size();

		int num = numBuckets[size - 1]; // number of buckets this size has
		int sum = getSum(size); // number of files before this size

		int ind = sum; // this indicates which file to write to.

		// need to rotate if there is more than 1 file
		if (num > 1) {
			ind = ind + (rotate[size - 1] % num);
			rotate[size - 1]++;
		}

		IBlockSink sink = (IBlockSink) sinks.get(ind);
		// sink.append();
		sink.writeBlock(block);
		// sink.close();

	}

	/**
	 * This method removes all files used by the sinks.
	 *
	 */
	public void removeAll() throws BlockingException {
		for (int i = 0; i < sinks.size(); i++) {
			bFactory.removeSink((IBlockSink) sinks.get(i));
		}
	}

	// this gets the number of files with size less than it.
	private int getSum(int max) {
		int total = 0;
		for (int i = 0; i < max - 1; i++) {
			total += numBuckets[i];
		}
		return total;
	}

}
