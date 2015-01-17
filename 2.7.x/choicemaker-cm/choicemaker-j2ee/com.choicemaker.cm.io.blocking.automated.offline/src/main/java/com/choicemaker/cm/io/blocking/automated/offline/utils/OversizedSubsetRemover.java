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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;
import com.choicemaker.util.IntArrayList;
import com.choicemaker.util.LongArrayList;

/**
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class OversizedSubsetRemover {

	public static final Comparator BLOCK_SET_SIZE_ORDER = new Comparator() {
		@Override
		public int compare(Object a, Object b) {
			BlockSet bsA = (BlockSet) a;
			BlockSet bsB = (BlockSet) b;

			return bsA.getRecordIDs().size() - bsB.getRecordIDs().size();
		}
	};

	private IBlockSource source;
	private IBlockSink sink;

	private static int INTERVAL = 10;

	// these variables are for splitting the block source to avoid
	// outofmemoryexception
	private IBlockSinkSourceFactory bFactory;

	private BlocksSpliterMap spliter;

	private int numBlocksIn; // number of blocks before remove subsumed
	private int numBlocksOut; // number of blocks after remove subsumed

	/**
	 * This version is safer on the memory because it break the big block file
	 * into smaller files. Each file contains blocks of the same size, so there
	 * are maxBlock - 1 file, since there is no 1 element block.
	 *
	 * @param source
	 * @param sink
	 * @param bFactory
	 * @param maxBlockSize
	 */
	public OversizedSubsetRemover(IBlockSource source, IBlockSink sink,
			IBlockSinkSourceFactory bFactory) throws BlockingException {

		this.source = source;
		this.sink = sink;
		this.bFactory = bFactory;

		HashSet sizes = new HashSet();
		source.open();
		while (source.hasNext()) {
			BlockSet bs = source.next();
			Integer I = new Integer(bs.getRecordIDs().size());

			if (!sizes.contains(I))
				sizes.add(I);
		}
		source.close();

		// use map implementation
		spliter = new BlocksSpliterMap(bFactory);
		Iterator it = sizes.iterator();
		while (it.hasNext()) {
			Integer I = (Integer) it.next();
			spliter.setSize(I.intValue(), 1);
		}

	}

	/**
	 * Reads in the blockSets from the source, removed subsumed blockSets, and
	 * writes the unsubsumed blockSets to the sink.
	 *
	 * This is the memory friendly version that splits the big block source into
	 * smaller ones
	 *
	 */
	public void removeSubsumedSafe() throws BlockingException {
		// splits the blocks first
		long t = System.currentTimeMillis();
		splitBlocks(source);
		t = System.currentTimeMillis() - t;
		System.out.println("Done split " + t);

		ArrayList parts = spliter.getSinks();

		// Initialize
		SuffixTreeNode root = SuffixTreeNode.createRootNode();
		IntArrayList subsumedBlockSets = new IntArrayList();

		// read the files in size ascending order.
		int blockSetId = 0;

		for (int i = 0; i < parts.size(); i++) {
			if (i % INTERVAL == 0)
				MemoryEstimator.writeMem();

			IBlockSource source = bFactory.getSource((IBlockSink) parts.get(i));

			if (source.exists()) {
				source.open();

				while (source.hasNext()) {
					BlockSet blockSet = source.next();

					LongArrayList recordIds = blockSet.getRecordIDs();

					checkForSubsets(root, recordIds, blockSetId, 0,
							subsumedBlockSets);
					addBlockSet(root, recordIds, blockSetId);

					blockSetId++;
				}

				source.close();
			}

		}

		System.out.println("Done putting on suffix tree");

		root = null;

		// write toSink
		writeUnsubsumed3(subsumedBlockSets, sink);

		spliter.removeAll();
	}

	/**
	 * This is the memory friendly version. It splits the IBlockSource into
	 * small ones that can fit into memory. Each file contains block sets of the
	 * same size. There are two for size = 2 because there are a lot of them.
	 *
	 * @param ibs
	 */
	private void splitBlocks(IBlockSource ibs) throws BlockingException {

		spliter.Initialize();
		// spliter.openAll();

		ibs.open();

		while (ibs.hasNext()) {
			BlockSet bs = ibs.next();
			// 2014-04-24 rphall: Commented out unused local variable.
			// int n = bs.getRecordIDs().size();

			LongArrayList recordIds = bs.getRecordIDs();
			recordIds.sort();

			spliter.writeToSink(bs);

			numBlocksIn++;
		}

		// spliter.closeAll();

		ibs.close();
	}

	/**
	 * This is the memory friendly version that uses intermediate files. It also
	 * bypasses the list of block sets by sorting the subsumed list first.
	 *
	 * @param subsumedBlockSets
	 *            - ids of the subsumed blocks
	 * @param sink
	 *            - output sink
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void writeUnsubsumed3(IntArrayList subsumedBlockSets,
			IBlockSink sink) throws BlockingException {

		sink.open();

		subsumedBlockSets.sort();

		ArrayList parts = spliter.getSinks();

		int counter = 0; // counter for the blocks read
		int ind = 0; // current index on subsumedBlockSets

		for (int i = 0; i < parts.size(); i++) {
			IBlockSource srcI = bFactory.getSource((IBlockSink) parts.get(i));
			srcI.open();

			while (srcI.hasNext()) {
				BlockSet bs = srcI.next();

				// if (subsumedBlockSets.size() > 0 && counter ==
				// subsumedBlockSets.get(ind)) {
				if (ind < subsumedBlockSets.size()
						&& counter == subsumedBlockSets.get(ind)) {
					ind++;
				} else {
					sink.writeBlock(bs);
					numBlocksOut++;
				}

				counter++;
			}

			srcI.close();

		} // end for

		// at the end, ind should be the size of the subsumedsubset
		if (ind != subsumedBlockSets.size())
			throw new IllegalStateException("Done write ind " + ind + " size "
					+ subsumedBlockSets.size());

		sink.close();
	}

	public int getNumBlocksIn() {
		return numBlocksIn;
	}

	public int getNumBlocksOut() {
		return numBlocksOut;
	}

	public static void checkForSubsets(SuffixTreeNode node,
			LongArrayList recordIds, int blockSetId, int fromIndex,
			IntArrayList subsumedSets) {

		for (int i = fromIndex, n = recordIds.size(); i < n; i++) {
			long recordId = recordIds.get(i);
			SuffixTreeNode kid = node.getChild(recordId);
			if (kid != null) {
				if (kid.hasBlockingSetId()) { // the kid represents an (as yet)
												// unsubsumed blocking set.
					subsumedSets.add(kid.getBlockingSetId());
					kid.removeFromParentRecursive();
				} else {
					checkForSubsets(kid, recordIds, blockSetId, i + 1,
							subsumedSets);
				}
			}
		}
	}

	public static void addBlockSet(SuffixTreeNode root,
			LongArrayList recordIds, int blockSetId) {
		SuffixTreeNode cur = root;

		int last = recordIds.size() - 1;
		for (int i = 0; i < last; i++) {
			long recordId = recordIds.get(i);
			SuffixTreeNode child = cur.getChild(recordId);
			if (child == null) {
				child = cur.putChild(recordId);
			}

			cur = child;
		}

		// the leaf node.
		cur.putChild(recordIds.get(last), blockSetId);
	}

}
