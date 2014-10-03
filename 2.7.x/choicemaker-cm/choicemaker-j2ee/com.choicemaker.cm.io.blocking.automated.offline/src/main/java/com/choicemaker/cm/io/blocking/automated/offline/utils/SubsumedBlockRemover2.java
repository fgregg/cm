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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.util.IntArrayList;
import com.choicemaker.util.LongArrayList;

/**
 * This version uses BlockGroup so it doesn't need to split up the blocks.
 * 
 * @author Adam Winkel
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SubsumedBlockRemover2 {

	public static final Comparator BLOCK_SET_SIZE_ORDER = new Comparator() {
		public int compare(Object a, Object b) {
			BlockSet bsA = (BlockSet)a;
			BlockSet bsB = (BlockSet)b;
			
			return bsA.getRecordIDs().size() - bsB.getRecordIDs().size();
		}
	};

	private BlockGroup bGroup;
	private IBlockSink sink;
	
//	private int maxBlockSize;

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
	public SubsumedBlockRemover2(BlockGroup bGroup, IBlockSink sink, int maxBlockSize) {
		this.bGroup = bGroup;
		this.sink = sink;
//		this.maxBlockSize = maxBlockSize;

	}


/**
 * Reads in the blockSets from the source, removed subsumed 
 * blockSets, and writes the unsubsumed blockSets to the sink.
 * 
 * This is the memory friendly version that splits the big block source into smaller ones
 * Version 2 writes to sinks when memory is getting full.  This potentially will miss some
 * subsets, but it's better than getting an OutOfMemoryException.
 * 
 */
public void removeSubsumedSafe2() throws BlockingException {

	IBlockSource [] parts = bGroup.getSources(); 
		
	//Initialize	
	SuffixTreeNode root = SuffixTreeNode.createRootNode();
	IntArrayList subsumedBlockSets = new IntArrayList();
	//read the files in size ascending order.
	int blockSetId = 0;
		
	//open sink
	sink.open();
	
	ArrayList sources = new ArrayList ();

	for (int i=0; i < parts.length ; i++) {
		IBlockSource source = parts[i];
			
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

//			System.out.println ("Processing: " + source.getInfo() + " count: " + numBlocksIn);
			
			if (MemoryEstimator.isFull(.60f) || (i == parts.length  - 1)) {
				System.out.println ("Resetting " + subsumedBlockSets.size());
				//write out
				writeUnsubsumed4(subsumedBlockSets, sink, sources);
						
				//reset
				root = SuffixTreeNode.createRootNode();
				subsumedBlockSets = new IntArrayList();
				blockSetId = 0;
				sources = new ArrayList ();
			} //end if
		} //end if

	}
		
	root = null;
		
	sink.close();

}
	
	
	
/** This is the memory friendly version that uses intermediate files.
 * It also bypasses the list of block sets by sorting the subsumed list first.
 * 
 * @param subsumedBlockSets - ids of the subsumed blocks
 * @param sink - output sink
 * @param parts - array of block sources
 * @throws FileNotFoundException
 * @throws IOException
 */
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
	 ("Done write ind " + ind + " size " + subsumedBlockSets.size ());
		
}
	
	
	
	
	
	public int getNumBlocksIn () {
		return numBlocksIn;
	}
	
	public int getNumBlocksOut () {
		return numBlocksOut;
	}
	

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



}
