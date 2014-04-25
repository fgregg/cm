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
import java.util.HashMap;
import java.util.Iterator;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.LongArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;

/**
 * This version does not use a suffix tree, because suffix tree takes up too much memeory and the
 * data in this case contains many identical blocks.
 *
 * @author pcheung
 *
 */
public class OversizedRemover {

	private IBlockSource source;
	private IBlockSink sink;

	//these variables are for splitting the block source to avoid outofmemoryexception
	private IBlockSinkSourceFactory bFactory;

//	private BlocksSpliterMap spliter;
	private BlocksSpliter2 spliter;

	private int numBlocksIn; //number of blocks before remove subsumed
	private int numBlocksOut; //number of blocks after remove subsumed


	/** This version is safer on the memory because it break the big block file into smaller files.  Each
	 * file contains blocks of the same size, so there are maxBlock - 1 file, since there is no 1 element
	 * block.
	 *
	 * @param source
	 * @param sink
	 * @param bFactory
	 * @param maxBlockSize
	 */
	public OversizedRemover (IBlockSource source, IBlockSink sink, IBlockSinkSourceFactory bFactory)
		throws BlockingException {

		this.source = source;
		this.sink = sink;
		this.bFactory = bFactory;

/*
		HashSet sizes = new HashSet ();
		source.open();
		int min;
		int max

		while (source.hasNext()) {
			BlockSet bs = source.getNext();
			Integer I = new Integer (bs.getRecordIDs().size());

			if (!sizes.contains(I)) sizes.add(I);
		}
		source.close();

		//use map implementation
		spliter = new BlocksSpliterMap (bFactory);
		Iterator it = sizes.iterator();
		while (it.hasNext ()) {
			Integer I = (Integer) it.next();
			spliter.setSize(I.intValue(), 1);

			System.out.println ("oversized size: " + I.intValue());
		}
*/
		source.open();
		int min=1000;
		int max=0;

		while (source.hasNext()) {
			BlockSet bs = source.getNext();
			int i = bs.getRecordIDs().size();

			if (i> max) max = i;
			if (i < min)  min = i;
		}
		source.close();

		//use map implementation
//		spliter = new BlocksSpliterMap (bFactory);
		spliter = new BlocksSpliter2 (bFactory, min, max, 100);

	}


	/**
	 * Reads in the blockSets from the source, removed identical block sets.
	 * Each file contains blocks of the same size.
	 *
	 */
	public void removeSubsumedSafe() throws BlockingException {
		//splits the blocks first
		long t = System.currentTimeMillis();
		splitBlocks (source);
		t = System.currentTimeMillis() - t;
		System.out.println ("Done split " + t);

		ArrayList parts = spliter.getSinks();

		//hashmap containing sum and block
		HashMap sumMap = new HashMap ();

		//for each file
		for (int i=0; i < parts.size() ; i++) {
			IBlockSource source = bFactory.getSource((IBlockSink) parts.get(i));

			if (source.exists()) {
				source.open();

				while (source.hasNext()) {

					BlockSet blockSet = source.getNext();
					LongArrayList recordIds = blockSet.getRecordIDs();

					Long L = new Long (getSum(recordIds));

					ArrayList blockList = (ArrayList) sumMap.get(L);
					if (blockList == null) {
						blockList = new ArrayList ();
						blockList.add (blockSet);
						sumMap.put (L, blockList);
					} else {
						if (!contain (blockList, recordIds)) {
							blockList.add(blockSet);
						}
					}

				}

				source.close();
			}

		}

		// write toSink
		writeUnsubsumed3(sumMap, sink);

		spliter.removeAll();
	}


	private long getSum (LongArrayList list) {
		long sum = 0;
		for (int i=0; i<list.size(); i++) {
			sum += list.get(i);
		}
		return sum;
	}



	private boolean contain (ArrayList blockList, LongArrayList ids) {
		boolean found = false;

		int i =0;

		while (!found && i < blockList.size()) {
			BlockSet bs = (BlockSet) blockList.get(i);

			if (!differ (bs.getRecordIDs(), ids)) found = true;
			else i ++;
		}

		return found;
	}


	private boolean differ (LongArrayList ids1, LongArrayList ids2) {
		boolean diff = false;

		int i = 0;

		if (ids1.size() != ids2.size()) diff = true;
		else {
			while (!diff && i < ids1.size()) {
				if (ids1.get(i) != ids2.get(i)) diff = true;
				else i ++;
			}
		}

		return diff;
	}


	/** This is the memory friendly version.  It splits the IBlockSource into small ones that can fit
	 * into memory.  Each file contains block sets of the same size.  There are two for size = 2 because
	 * there are a lot of them.
	 *
	 * @param ibs
	 */
	private void splitBlocks (IBlockSource ibs) throws BlockingException {

		spliter.Initialize();

		ibs.open();

		while (ibs.hasNext()) {
			BlockSet bs = ibs.getNext();
			// 2014-04-24 rphall: Commented out unused local variable.
//			int n = bs.getRecordIDs().size();

			LongArrayList recordIds = bs.getRecordIDs();
			recordIds.sort();

			spliter.writeToSink(bs);

			numBlocksIn ++;
		}

		ibs.close();
	}


	/** This writes out the distinct blocks.
	 */
	private void writeUnsubsumed3(HashMap sumMap, IBlockSink sink)
		throws BlockingException {

		sink.open();

		Iterator it = sumMap.values().iterator();

		while (it.hasNext()) {
			ArrayList blockList = (ArrayList) it.next();

			for (int i=0; i<blockList.size(); i++) {
				BlockSet bs = (BlockSet) blockList.get(i);
				sink.writeBlock(bs);
				numBlocksOut ++;
			}
		}

		sink.close();
	}


	public int getNumBlocksIn () {
		return numBlocksIn;
	}

	public int getNumBlocksOut () {
		return numBlocksOut;
	}

/*
	public static void checkForSubsets(SuffixTreeNode node, LongArrayList recordIds, int blockSetId,
		int fromIndex, IntArrayList subsumedSets) {

		for (int i = fromIndex, n = recordIds.size(); i < n; i++) {
			long recordId = recordIds.get(i);
			SuffixTreeNode kid = node.getChild(recordId);
			if (kid != null) {
				if (kid.hasBlockingSetId()) {  // the kid represents an (as yet) unsubsumed blocking set.
					subsumedSets.add(kid.getBlockingSetId());
					kid.removeFromParentRecursive();
				} else {
					checkForSubsets(kid, recordIds, blockSetId, i+1, subsumedSets);
				}
			}
		}
	}

	public static void addBlockSet(SuffixTreeNode root, LongArrayList recordIds, int blockSetId) {
		SuffixTreeNode cur = root;

		int last = recordIds.size () - 1;
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
*/

}
