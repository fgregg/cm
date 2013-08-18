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
import java.util.Comparator;
import java.util.List;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.core.util.LongArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;

/**
 * @author Adam Winkel
 */

public class SubsumedBlockRemover {

	public static final Comparator BLOCK_SET_SIZE_ORDER = new Comparator() {
		public int compare(Object a, Object b) {
			BlockSet bsA = (BlockSet)a;
			BlockSet bsB = (BlockSet)b;
			
			return bsA.getRecordIDs().size() - bsB.getRecordIDs().size();
		}
	};

	private static int INTERVAL = 10;

	private IBlockSource source;
	private IBlockSink sink;
	
	//these variables are for splitting the block source to avoid outofmemoryexception
	private IBlockSinkSourceFactory bFactory;
	private int maxBlockSize;

	
	BlocksSpliter spliter;
//	private BlocksSpliterMap spliter;
		
	private int numBlocksIn; //number of blocks before remove subsumed
	private int numBlocksOut; //number of blocks after remove subsumed

	public SubsumedBlockRemover(IBlockSource source, IBlockSink sink) {
		this.source = source;
		this.sink = sink;
	}


	/** This version is safer on the memory because it break the big block file into smaller files.  Each
	 * file contains blocks of the same size, so there are maxBlock - 1 file, since there is no 1 element 
	 * block.
	 * 
	 * @param source
	 * @param sink
	 * @param bFactory
	 * @param maxBlockSize
	 */
	public SubsumedBlockRemover(IBlockSource source, IBlockSink sink, IBlockSinkSourceFactory bFactory,
	 int maxBlockSize) {
		this.source = source;
		this.sink = sink;
		this.bFactory = bFactory;
		this.maxBlockSize = maxBlockSize;

		//set up spliter
		spliter = new BlocksSpliter (bFactory, maxBlockSize);
		spliter.setSize(2,4); //4 files for size = 2;
		spliter.setSize(3,3); //3 files for size = 3;
		spliter.setSize(4,2); //2 files for size = 4;
		spliter.setSize(5,2); //2 files for size = 5;


		//use map implementation
/*		spliter = new BlocksSpliterMap (bFactory);
		spliter.setSize(2,3); //3 files for size = 2;
		spliter.setSize(3,2); //2 files for size = 3;
		for (int i=4; i<= maxBlockSize; i++) {
			spliter.setSize(i,1);
		}
*/

	}

	

	/**
	 * Reads in the blockSets from the source, removed subsumed 
	 * blockSets, and writes the unsubsumed blockSets to the sink.
	 * 
	 * This is the memory friendly version that splits the big block source into smaller ones
	 * 
	 */
	public void removeSubsumedSafe() throws BlockingException {
		//splits the blocks first
		long t = System.currentTimeMillis();
		
		splitBlocks (source);
		
		t = System.currentTimeMillis() - t;
		System.out.println ("Done split " + t);
		
		ArrayList parts = spliter.getSinks();
		
		//Initialize	
		SuffixTreeNode root = SuffixTreeNode.createRootNode();
		IntArrayList subsumedBlockSets = new IntArrayList();
		
		//read the files in size ascending order.
		int blockSetId = 0;
		
		//count the number of block processed
		int count = 0;

		for (int i=0; i < parts.size() ; i++) {
			if (i% INTERVAL == 0) MemoryEstimator.writeMem();

			IBlockSource source = bFactory.getSource((IBlockSink) parts.get(i));
			
			if (source.exists()) {
				source.open();
				
//				System.out.println ("Processing: " + source.getInfo() + " count: " + count);
			
				while (source.hasNext()) {
					count ++;
					BlockSet blockSet = source.getNext();
				
					LongArrayList recordIds = blockSet.getRecordIDs();

					checkForSubsets(root, recordIds, blockSetId, 0, subsumedBlockSets);
					addBlockSet(root, recordIds, blockSetId);
				
					blockSetId ++;
				}
			
				source.close();
			}

		}
		
		root = null;
		
		// write toSink
		sink.open();
		writeUnsubsumed3(subsumedBlockSets, sink);
		sink.close();

		spliter.removeAll();		
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
	//splits the blocks first
	long t = System.currentTimeMillis();
		
	splitBlocks (source);
		
	t = System.currentTimeMillis() - t;
	System.out.println ("Done split " + t);
		
	ArrayList parts = spliter.getSinks();
		
	//Initialize	
	SuffixTreeNode root = SuffixTreeNode.createRootNode();
	IntArrayList subsumedBlockSets = new IntArrayList();
	//read the files in size ascending order.
	int blockSetId = 0;
		
	//count the number of block processed
	int count = 0;

	//open sink
	sink.open();
	
	ArrayList sources = new ArrayList ();

	for (int i=0; i < parts.size() ; i++) {
		IBlockSource source = bFactory.getSource((IBlockSink) parts.get(i));
			
		if (source.exists()) {
			sources.add(source);
			
			source.open();
				
			while (source.hasNext()) {
				count ++;
				BlockSet blockSet = source.getNext();
				
				LongArrayList recordIds = blockSet.getRecordIDs();

				checkForSubsets(root, recordIds, blockSetId, 0, subsumedBlockSets);
				addBlockSet(root, recordIds, blockSetId);
				
				blockSetId ++;
				
			}
			
			source.close();

//			System.out.println ("Processing: " + source.getInfo() + " count: " + count);
			
			if (MemoryEstimator.isFull(.60f) || (i == parts.size() - 1)) {
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

	spliter.removeAll();		
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
			
		subsumedBlockSets.sort();
		
		ArrayList parts = spliter.getSinks();

		int counter = 0; //counter for the blocks read
		int ind = 0; //current index on subsumedBlockSets
		
		for (int i = 0; i < parts.size(); i++) {
			IBlockSource srcI = bFactory.getSource((IBlockSink)parts.get(i));
			
			if (srcI.exists()) {
				srcI.open();
			
				while (srcI.hasNext()) {
					BlockSet bs = srcI.getNext();
				
//					if (subsumedBlockSets.size() > 0 && counter == subsumedBlockSets.get(ind)) {
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
	
	
	
	private List readBlocks (IBlockSource ibs) throws BlockingException{
		ArrayList list = new ArrayList ();
		ibs.open();
		
		while (ibs.hasNext()) {
			list.add(ibs.getNext());
			numBlocksIn ++;
		}
		
		ibs.close();
		return list;
	}
	
	
	
	/** This is the memory friendly version.  It splits the IBlockSource into small ones that can fit 
	 * into memory.  Each file contains block sets of the same size.  There are two for size = 2 because
	 * there are a lot of them.
	 * 
	 * @param ibs
	 */
	private void splitBlocks (IBlockSource ibs) throws BlockingException {
		spliter.Initialize();
		spliter.openAll();

		ibs.open();

		while (ibs.hasNext()) {
			numBlocksIn ++;
			BlockSet bs = ibs.getNext();
			int n = bs.getRecordIDs().size();
			
			LongArrayList recordIds = bs.getRecordIDs();
			recordIds.sort();

			spliter.writeToSink(bs);
		}


		ibs.close();
		
		spliter.closeAll();
	}
	
	

	/**
	 * This is subsumed
	 */
	public void writeUnsubsumed(List blockSets, IntArrayList indicesToRemove, IBlockSink sink) 
		throws BlockingException {
		// null out those blockingSets that were subsumed
		for (int i = 0, n = indicesToRemove.size(); i < n; i++) {
			int bsId = indicesToRemove.get(i);
			blockSets.set(bsId, null);
		}
		
		sink.open();
		for (int i = 0, n = blockSets.size(); i < n; i++) {
			BlockSet bs = (BlockSet)blockSets.get(i);
			if (bs != null) {
				sink.writeBlock(bs);
				blockSets.set(i, null); // null it out
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
