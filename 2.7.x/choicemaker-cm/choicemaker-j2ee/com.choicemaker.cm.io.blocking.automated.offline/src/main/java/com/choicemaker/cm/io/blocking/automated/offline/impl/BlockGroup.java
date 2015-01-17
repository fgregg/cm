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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;

/**
 * This object uses a group of IBlockSinks to store the blocks. It uses at least
 * one sink for blocks of a given size.
 * 
 * This objects makes it faster to run blocks dedup, because it doesn't have to
 * split the blocks.
 * 
 * @author pcheung
 *
 */
public class BlockGroup implements IBlockSink {

	private List<IBlockSink> sinks;

	// this keeps track of number of bucket files per block size.
	// numBuckets[i] contains the buckets for size i + 1.
	// numBuckets[0] is 0 because there is no single element block.
	private int[] numBuckets;

	// if there is more than 1 bucket file for an id, we need to rotate the
	// bucket files.
	// this variable stores the rotation info
	private int[] rotate;

	// private int maxSize; //maximum size of a block set.

	private IBlockSinkSourceFactory bFactory;

	private int count = 0;

	/**
	 * This constructor takes these paramters.
	 * 
	 * @param bFactory
	 *            - IBlockSinkSourceFactory that provides sinks for different
	 *            block size.
	 * @param maxSize
	 *            - maximum size of a block.
	 * @throws BlockingException
	 */
	public BlockGroup(IBlockSinkSourceFactory bFactory, int maxSize)
			throws BlockingException {
		// this.maxSize = maxSize;
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

		setSize(2, 4); // 4 files for size = 2;
		setSize(3, 3); // 3 files for size = 3;
		setSize(4, 2); // 2 files for size = 4;
		setSize(5, 2); // 2 files for size = 5;

		int num = getSum(maxSize + 1);
		sinks = new ArrayList<>(num);

		for (int i = 0; i < num; i++) {
			IBlockSink sink = bFactory.getNextSink();
			sinks.add(sink);
		}

	}

	/**
	 * This returns an array of IBlockSource built from the IBlockSinks. The
	 * ordering is the file containing the smallest blocks to the file with the
	 * largest blocks.
	 * 
	 * @return IBlockSource [] - an array of IBlockSource. All the blocks in a
	 *         source all have the same size.
	 * @throws IOException
	 */
	public IBlockSource[] getSources() throws BlockingException {
		IBlockSource[] sources = new IBlockSource[sinks.size()];
		for (int i = 0; i < sinks.size(); i++) {
			sources[i] = bFactory.getSource(sinks.get(i));
		}

		return sources;
	}

	/**
	 * This returns an array of IBlockSource built from the IBlockSinks. The
	 * ordering is the file containing the largest blocks to the file with the
	 * smallest blocks.
	 * 
	 * @return IBlockSource [] - an array of IBlockSource. All the blocks in a
	 *         source all have the same size.
	 * @throws IOException
	 */
	public IBlockSource[] getSourcesReverse() throws BlockingException {
		IBlockSource[] sources = new IBlockSource[sinks.size()];
		int j = sinks.size() - 1;
		for (int i = 0; i < sinks.size(); i++) {
			sources[i] = bFactory.getSource(sinks.get(j));
			j--;
		}

		return sources;
	}

	@Override
	public void remove() throws BlockingException {
		for (int i = 0; i < sinks.size(); i++) {
			bFactory.removeSink(sinks.get(i));
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
	private void setSize(int blockSize, int num) {
		numBuckets[blockSize - 1] = num;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink#writeBlock
	 * (com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet)
	 */
	@Override
	public void writeBlock(BlockSet bs) throws BlockingException {
		int size = bs.getRecordIDs().size();

		int num = numBuckets[size - 1]; // number of buckets this size has
		int sum = getSum(size); // number of files before this size

		int ind = sum; // this indicates which file to write to.

		// need to rotate if there is more than 1 file
		if (num > 1) {
			ind = ind + (rotate[size - 1] % num);
			rotate[size - 1]++;
		}

		IBlockSink sink = sinks.get(ind);
		sink.writeBlock(bs);
		count++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#exists()
	 */
	@Override
	public boolean exists() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#open()
	 */
	@Override
	public void open() throws BlockingException {
		for (int i = 0; i < sinks.size(); i++) {
			IBlockSink sink = sinks.get(i);
			sink.open();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#append()
	 */
	@Override
	public void append() throws BlockingException {
		for (int i = 0; i < sinks.size(); i++) {
			IBlockSink sink = sinks.get(i);
			sink.append();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#close()
	 */
	@Override
	public void close() throws BlockingException {
		for (int i = 0; i < sinks.size(); i++) {
			IBlockSink sink = sinks.get(i);
			sink.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISink#getCount()
	 */
	@Override
	public int getCount() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISink#getInfo()
	 */
	@Override
	public String getInfo() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#flush()
	 */
	@Override
	public void flush() throws BlockingException {
	}

}
