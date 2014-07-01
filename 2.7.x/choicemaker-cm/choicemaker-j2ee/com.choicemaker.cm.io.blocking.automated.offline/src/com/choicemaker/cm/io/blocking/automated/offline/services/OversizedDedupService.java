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
package com.choicemaker.cm.io.blocking.automated.offline.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.core.util.LongArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.utils.BlocksSpliterMap;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * @author pcheung
 *
 * This service dedups the oversized blocks.  It does the following:
 * 1. Break the oversized blocks into smaller files.  Each file contains blocks of a given size.
 * 2. Remove exact duplicate oversized blocks.
 * 3. Remove subset
 */
public class OversizedDedupService {

	private static final Logger log = Logger.getLogger(OversizedDedupService.class);

	// these two variables are used to stop the program in the middle
	private IControl control;
	private boolean stop;

	private IBlockSource osSource;

	private IBlockSink osSink;

	private BlockSinkSourceFactory osFactory;

	private IStatus status;

	//this splitter has 1 file for a range of block sizes
	private BlocksSpliterMap spliter;

	private int numBlocksIn = 0;
	private int numAfterExact = 0;
	private int numBlocksOut = 0;

	private long time; //this keeps track of time


	/** This constructor takes these parameters
	 *
	 * @param osSource - IBlockSource that contains dupliate oversized blocks
	 * @param osSink - IBlockSink that is going to store deduped oversized blocks
	 * @param osFactory - IBlockSinkSourceFactory to store temporary files
	 * @param status - status of the system.
	 */
	public OversizedDedupService (IBlockSource osSource, IBlockSink osSink,
		BlockSinkSourceFactory osFactory, IStatus status, IControl control) {

		this.osFactory = osFactory;
		this.osSink = osSink;
		this.osSource = osSource;
		this.status = status;

		this.control = control;
		this.stop = false;
	}


	public int getNumBlocksIn () { return numBlocksIn; }
	public int getNumAfterExact () { return numAfterExact; }
	public int getNumBlocksOut () { return numBlocksOut; }

	/** This method returns the time it takes to run the runService method.
	 *
	 * @return long - returns the time (in milliseconds) it took to run this service.
	 */
	public long getTimeElapsed () { return time; }


	public void runService () throws BlockingException {
		time = System.currentTimeMillis();

		if (status.getStatus() >= IStatus.DONE_DEDUP_OVERSIZED ) {
			//do nothing here

		} else if (status.getStatus() == IStatus.DONE_DEDUP_BLOCKS ) {
			//start from beginning
			splitOversized ();

			removeExact (0);

			removeSubsumed (0);

		} else if (status.getStatus() == IStatus.DEDUP_OVERSIZED_EXACT ) {
			//recovering dedup oversized exact
			String temp =  status.getAdditionalInfo();
			int ind = temp.indexOf( IStatus.DELIMIT);
			int s = Integer.parseInt( temp.substring(0,ind) );
			int startPoint = Integer.parseInt( temp.substring(ind + 1) );

			log.info("Recovering from remove exact " + s + " " + startPoint);
			recoverSpliter (s);

			removeExact (startPoint);

			removeSubsumed (0);

		} else if (status.getStatus() == IStatus.DONE_DEDUP_OVERSIZED_EXACT ) {
			//start from dedup subsumed
			int s = Integer.parseInt( status.getAdditionalInfo() );

			log.info("Recovering from remove subsumed " + s);
			recoverSpliter (s);

			removeSubsumed (0);

		} else if (status.getStatus() == IStatus.DEDUP_OVERSIZED ) {
			//recovering dedup subsumed oversized
			int s = Integer.parseInt( status.getAdditionalInfo() );

			log.info("Recovering from subsumed removal " + s );
			recoverSpliter (s);

			removeSubsumed (0);
		}
		time = System.currentTimeMillis() - time;
	}


	private void recoverSpliter (int s) throws BlockingException {
		spliter = new BlocksSpliterMap (osFactory);
		//this is not exact because we really don't have the os block size distribution.
		//we just need some ordering.
		for (int i=0; i<s; i++) {
			spliter.setSize(i,1);
		}
		spliter.recovery(s);
	}


	/** This method splits the oversized blocks file into smaller chunks.
	 * Each chunk contains oversized blocks with size in a certain range.
	 *
	 */
	private void splitOversized () throws BlockingException {
		//get a listing of all the oversized block sizes
		HashSet sizes = new HashSet ();
		osSource.open();
		while (osSource.hasNext() && !stop) {
			BlockSet bs = osSource.getNext();
			Integer I = new Integer (bs.getRecordIDs().size());

			if (!sizes.contains(I)) sizes.add(I);
		}
		osSource.close();

		//sort this set
		int [] sArray = new int [sizes.size()];
		Iterator it = sizes.iterator();
		int i=0;
		while (it.hasNext() && !stop) {
			Integer I = (Integer) it.next();
			sArray [i] = I.intValue();
			i ++;

			stop = ControlChecker.checkStop (control, i);
		}
		Arrays.sort (sArray);

		//use map implementation
		spliter = new BlocksSpliterMap (osFactory);
		for (i=0; i< sizes.size(); i++) {
			spliter.setSize(sArray[i], 1);
		}

		spliter.Initialize();

		osSource.open();

		while (osSource.hasNext() && !stop) {
			BlockSet bs = osSource.getNext();
			// 2014-04-24 rphall: Commented out unused local variable.
			// Any side effects?
			/* LongArrayList recordIds = */
			bs.getRecordIDs();
			spliter.writeToSink(bs);

			numBlocksIn ++;

			stop = ControlChecker.checkStop (control, numBlocksIn);
		}

		osSource.close();

	}


	/** This method removes the exact blocks
	 *
	 */
	private void removeExact (int startPoint) throws BlockingException {

		IBlockSource [] sources = spliter.getSources();
		int s = sources.length;

		for (int i=startPoint; i<s && !stop; i++) {
			stop = ControlChecker.checkStop (control, ControlChecker.CONTROL_INTERVAL);

			//hashmap containing sum and block
			HashMap sumMap = new HashMap ();

//			System.out.println (i + " " + sources[i].getInfo());

			String temp = Integer.toString(s) + IStatus.DELIMIT + Integer.toString(i);
			status.setStatus(IStatus.DEDUP_OVERSIZED_EXACT, temp);

			if (sources[i].exists()) {
				sources[i].open();

				while (sources[i].hasNext() && !stop) {

					BlockSet blockSet = sources[i].getNext();
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

				sources[i].close();

				//now write the distinct ones back to the file.
				IBlockSink sink = osFactory.getSink(sources[i]);
				sink.open();
				Iterator it = sumMap.values().iterator();

				while (it.hasNext()) {
					ArrayList blockList = (ArrayList) it.next();

					for (int j=0; j<blockList.size(); j++) {
						BlockSet bs = (BlockSet) blockList.get(j);
						sink.writeBlock(bs);
						numAfterExact ++;
					}
				}
				sink.close();

			} //end if

		} //end for i

		status.setStatus( IStatus.DONE_DEDUP_OVERSIZED_EXACT, Integer.toString(s) );

		status.setStatus( IStatus.DEDUP_OVERSIZED, Integer.toString(s) );

//		if (true) throw new RuntimeException ("test fail");

	}



	/** This method removes the subsumed oversized blocks
	 *
	 */
	private void removeSubsumed (int startPoint) throws BlockingException {
		//get the split sinks in order of block size
		IBlockSource [] sources = spliter.getSources();
		int s = sources.length;

		//Initialize
		SuffixTreeNode root = SuffixTreeNode.createRootNode();
		IntArrayList subsumedBlockSets = new IntArrayList();
		int blockSetId = 0;

		for (int i = startPoint; i< s && !stop; i++) {

			if (sources[i].exists()) {
				sources[i].open();

				while (sources[i].hasNext() && !stop) {
					BlockSet blockSet = sources[i].getNext();

					LongArrayList recordIds = blockSet.getRecordIDs();

					checkForSubsets(root, recordIds, blockSetId, 0, subsumedBlockSets);
					addBlockSet(root, recordIds, blockSetId);

					blockSetId ++;

					stop = ControlChecker.checkStop (control, blockSetId);
				}

				sources[i].close();
			} //end if
		} //end for

		root = null;

		// write toSink
		writeUnsubsumed3(subsumedBlockSets, osSink);

		spliter.removeAll();
		osSource.remove();

		status.setStatus( IStatus.DONE_DEDUP_OVERSIZED);
	}


	/** This is the memory friendly version that uses intermediate files.
	 * It also bypasses the list of block sets by sorting the subsumed list first.
	 *
	 * @param subsumedBlockSets - ids of the subsumed blocks
	 * @param sink - output sink
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void writeUnsubsumed3(IntArrayList subsumedBlockSets, IBlockSink sink)
		throws BlockingException {

		sink.open();

		subsumedBlockSets.sort();

		ArrayList parts = spliter.getSinks();

		int counter = 0; //counter for the blocks read
		int ind = 0; //current index on subsumedBlockSets

		for (int i = 0; i < parts.size() && !stop; i++) {
			IBlockSource srcI = osFactory.getSource((IBlockSink)parts.get(i));
			srcI.open();

//			System.out.println (i + " " + srcI.getInfo() + " " + numBlocksOut);

			while (srcI.hasNext()) {
				BlockSet bs = srcI.getNext();

				if (ind < subsumedBlockSets.size() && counter == subsumedBlockSets.get(ind)) {
					ind ++;
				} else {
					sink.writeBlock(bs);
					numBlocksOut ++;
				}

				counter ++;

				stop = ControlChecker.checkStop (control, counter);
			}

			srcI.close();

		} //end for

		//at the end, ind should be the size of the subsumedsubset
		if (ind != subsumedBlockSets.size()) throw new IllegalStateException
		 ("Done write ind " + ind + " size " + subsumedBlockSets.size ());

		sink.close();
	}


	/** This method calculates the sum of an array of IDs
	 *
	 * @param list
	 * @return
	 */
	private long getSum (LongArrayList list) {
		long sum = 0;
		for (int i=0; i<list.size(); i++) {
			sum += list.get(i);
		}
		return sum;
	}


	/** This checks to see if ids is in the ArrayList.
	 *
	 * @param blockList
	 * @param ids
	 * @return true if ids is in blockList.
	 */
	private static boolean contain (ArrayList blockList, LongArrayList ids) {
		boolean found = false;

		int i =0;

		while (!found && i < blockList.size()) {
			BlockSet bs = (BlockSet) blockList.get(i);

			if (!differ (bs.getRecordIDs(), ids)) found = true;
			else i ++;
		}

		return found;
	}


	/** This returns true if the content of two LongArrayList are different.
	 *
	 * @param ids1
	 * @param ids2
	 * @return
	 */
	private static boolean differ (LongArrayList ids1, LongArrayList ids2) {
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


	private static void checkForSubsets(SuffixTreeNode node, LongArrayList recordIds, int blockSetId,
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

	private static void addBlockSet(SuffixTreeNode root, LongArrayList recordIds, int blockSetId) {
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


}
