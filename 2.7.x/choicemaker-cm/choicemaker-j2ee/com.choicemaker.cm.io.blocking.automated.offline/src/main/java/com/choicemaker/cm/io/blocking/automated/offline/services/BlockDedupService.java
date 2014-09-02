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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;
import com.choicemaker.util.IntArrayList;
import com.choicemaker.util.LongArrayList;

/**
 * @author pcheung
 *
 * This service object dedups a BlockGroup.
 * 
 * If this service processes first 10 files for dedup and then it is stopped, when it starts again for 
 * recovery, it won't reload the first 10 files.  It'll start with file 11.  If block A in file 1 is a subset
 * of block B in file 11, it would not know. 
 * 
 */
public class BlockDedupService {
	
	private static final Logger log = Logger.getLogger(BlockDedupService.class.getName());
	
	public final static float FULL = .6f;

	//BlockGroup that contains duplicate blocks
	private BlockGroup bGroup;
	
	//Output block sink that stores dedup-blocks
	private IBlockSink bSink;
	
	//maximum block size
//	private int maxBlockSize;
	
	private IStatus status;
	
	private int numBlocksIn = 0;
	private int numBlocksOut = 0;
	private int numReset = 0;
	
	private long time; //this keeps track of time
	
	
	/** This constructor takes these parameters
	 * 
	 * @param bGroup - input BlockGroup that contains dups
	 * @param bSink - output BlockSink containing deduped blocks
	 * @param maxBlockSize - maximum size of the blocks
	 */
	public BlockDedupService (BlockGroup bGroup, IBlockSink bSink, int maxBlockSize, IStatus status) {
		this.bGroup = bGroup;
		this.bSink = bSink;
//		this.maxBlockSize = maxBlockSize;
		this.status = status;
	}


	public int getNumBlocksIn () { return numBlocksIn; }

	public int getNumBlocksOut () { return numBlocksOut; }


	/** This method checks the current status and decides what to do next.
	 * 
	 * @throws IOException
	 */
	public void runService () throws BlockingException {
		
		time = System.currentTimeMillis();

		if (status.getStatus() >= IStatus.DONE_DEDUP_BLOCKS ) {
			//do nothing here
			
		} else if (status.getStatus() == IStatus.DONE_OVERSIZED_TRIMMING ) {
			//start deduping the blocks
			log.info("starting to dedup blocks");

			//open sink
			bSink.open();

			startDedup (0);
			
			bSink.close();
			
		} else if (status.getStatus() == IStatus.DEDUP_BLOCKS) {
			//the +1 is needed because we need to start with the next file
			int startPoint = Integer.parseInt( status.getAdditionalInfo() ) + 1;
			
			//start recovery
			log.info("starting dedup recovery, starting point: " + startPoint);
			
			//open sink
			bSink.append();

			startDedup (startPoint);
			
			bSink.close();
		}
		
		time = System.currentTimeMillis() - time;

	}
	
	
	/** This method returns the time it takes to run the runService method.
	 * 
	 * @return long - returns the time (in milliseconds) it took to run this service.
	 */
	public long getTimeElapsed () { return time; }
	
	
	/** This method starts the dedup from scratch
	 * 
	 * @throws IOException
	 */
	private void startDedup (int startPoint) throws BlockingException {
		IBlockSource [] parts = bGroup.getSources(); 
		
		//Initialize	
		SuffixTreeNode root = SuffixTreeNode.createRootNode();
		IntArrayList subsumedBlockSets = new IntArrayList();
		
		//read the files in size ascending order.
		int blockSetId = 0;
		
		ArrayList sources = new ArrayList ();
		
		for (int i=startPoint; i < parts.length ; i++) {
			IBlockSource source = parts[i];
			
			log.fine("deduping file: " + source.getInfo());
			
			if (source.exists()) {
				sources.add(source);
			
				source.open();
				
				while (source.hasNext()) {
					numBlocksIn ++;
					
					BlockSet blockSet = source.getNext();
					LongArrayList recordIds = blockSet.getRecordIDs();

					checkForSubsets(root, recordIds, blockSetId, 0, subsumedBlockSets);
					addBlockSet(root, recordIds, blockSetId);
				
					blockSetId ++;
				
				}
			
				source.close();

//				System.out.println (i + " Processing: " + source.getInfo() + " count: " + numBlocksIn);
			
				if (MemoryEstimator.isFull(FULL) || (i == parts.length  - 1)) {

					log.info ("Resetting " + subsumedBlockSets.size() + " file " + source.getInfo());
					numReset ++;
					
					//write out
					writeUnsubsumed4(subsumedBlockSets, bSink, sources);
					
					status.setStatus(IStatus.DEDUP_BLOCKS, Integer.toString(i) );
					
					
					//reset
					root = null;
					subsumedBlockSets = null;
					sources = null;
					
					blockSetId = 0;
					root = SuffixTreeNode.createRootNode();
					subsumedBlockSets = new IntArrayList();
					sources = new ArrayList ();
					
//					if (i >= 50 ) throw new RuntimeException ("test fail");
				} //end if
			} //end if

		}

		status.setStatus(IStatus.DONE_DEDUP_BLOCKS);
		log.info("Number of reset " + numReset);
		
		//clean up
		root = null;
		bGroup.remove();
	}


	private void writeUnsubsumed4(IntArrayList subsumedBlockSets, IBlockSink sink, ArrayList sources) 
		throws BlockingException {
			
		subsumedBlockSets.sort();
		
		int counter = 0; //counter for the blocks read
		int ind = 0; //current index on subsumedBlockSets
		
		for (int i = 0; i < sources.size(); i++) {

			IBlockSource srcI = (IBlockSource) sources.get(i);
			
			if (srcI.exists()) {
				srcI.open();
			
				while (srcI.hasNext()) {
					BlockSet bs = srcI.getNext();
				
					//don't write anything in subsumedBlocks
					if (ind < subsumedBlockSets.size() && counter == subsumedBlockSets.get(ind)) {
						ind ++;
					} else {
						sink.writeBlock(bs);
						numBlocksOut ++;
					}
				
					counter ++;
				}
			
				srcI.close();

			}
			
		} //end for
		
		//at the end, ind should be the size of the subsumedsubset
		if (ind != subsumedBlockSets.size()) throw new IllegalStateException
		 ("Done writeUnsubsumed4 ind " + ind + " size " + subsumedBlockSets.size ());
		
	}


	/** This method checks for subset recursively.
	 * 
	 * @param node - starting node of the suffix tree
	 * @param recordIds - records IDs to check
	 * @param blockSetId - Id of this block set
	 * @param fromIndex - which subsets to check
	 * @param subsumedSets - array of subsumed block set IDs
	 */
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
	
	

	/** This methods adds recordIds to the suffix tree.
	 * 
	 * @param root
	 * @param recordIds
	 * @param blockSetId
	 */
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


}
