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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;

/**
 * This version of the block splitter uses a hashmap to keep track of the file.  It is useful in cases where
 * block sizes are just few discrete values.  If block sizes are just 20 and 100, there is not need to create 
 * 100 files for 100 possible sizes.
 * 
 * @author pcheung
 *
 * 
 */
public class BlocksSpliterMap {

	//this stores all the block sinks 
	ArrayList sinks;
	
	//maps block size to the file info
	HashMap map = new HashMap () ;

	int current = 0;
		
	IBlockSinkSourceFactory bFactory;
	
	public BlocksSpliterMap (IBlockSinkSourceFactory bFactory) {
		this.bFactory = bFactory;
	}


	/** This method sets the number of bucket files to create for the given block size.
	 * By default there is 1 bucket file per size, but you can increase that if there are
	 * a lot of blocks for a given size.
	 * 
	 * @param blockSize
	 * @param num
	 */
	public void setSize (int blockSize, int num) {
		Integer I = new Integer(blockSize);
		if (! map.containsKey(I)) {
			FileInfo fInfo = new FileInfo ();
			fInfo.numBuckets = num;
			fInfo.rotate = 0;
			fInfo.fileID = current;
			current += num;
			
			map.put(I, fInfo);

		}
	}
	

	/** This method initializes the sinks array list.
	 * 
	 * Call this method after you are done calling setSize (int, int).
	 *
	 */
	public void Initialize () throws BlockingException {
		sinks = new ArrayList (current);
		
		for (int i=0; i< current; i++) {
			IBlockSink sink = bFactory.getNextSink();
			sinks.add(sink);
			
			//clean up old files
			if (sink.exists()) bFactory.removeSink(sink);
		}
		
//		System.out.println ("opening " + current + " sinks");
	}


	/** This method is used to recover all the sink files.  The sink files are all sequential starting
	 * from 1 to size.
	 * 
	 * @param size - the number of sink files.  
	 * @throws IOException
	 */
	public void recovery (int size) throws BlockingException {
		sinks = new ArrayList (size);
		for (int i=0; i< size; i++) {
			IBlockSink sink = bFactory.getNextSink();
			sinks.add(sink);
		}
	}


	/** This method opens all the sinks.
	 * 
	 */
/*
	public void openAll () throws IOException {
		for (int i=0; i< sinks.size(); i++) {
			IBlockSink sink = (IBlockSink) sinks.get(i);
			sink.open();
		}
	}
*/
	
	/** This method closes all the sinks.
	 * 
	 */
/*
	public void closeAll () throws IOException {
		for (int i=0; i< sinks.size(); i++) {
			IBlockSink sink = (IBlockSink) sinks.get(i);
			sink.close();
		}
	}
*/
	
	
	/** This returns the array list of the bucket sinks.  They are in the order of block size.
	 * 
	 * @return ArrayList of IBlockSink
	 */
	public ArrayList getSinks () {
		Set keys = map.keySet();
		int [] kArray = new int [keys.size ()];
		Iterator it = keys.iterator();
		int i = 0;
		while (it.hasNext()) {
			kArray[i] = ((Integer) it.next()).intValue();
			i ++;
		}
		
		Arrays.sort (kArray);
		
		ArrayList list = new ArrayList (keys.size());
		for (int j=0; j< i; j++) {
			FileInfo fInfo = (FileInfo) map.get(new Integer (kArray[j]));
			
			for (int k=0; k< fInfo.numBuckets; k++) {
				list.add( sinks.get(fInfo.fileID + k));
			}
			
		}
		
		return list;
	}



	/** This method returns an array of IBlockSource created from the sinks.
	 * The order is not sorted.
	 * 
	 * @return IBlockSource [] - returns an array of IBlockSource
	 */
	public IBlockSource [] getSources () throws BlockingException {
		IBlockSource [] sources = new IBlockSource [sinks.size()];
		for (int i=0; i< sinks.size(); i++) {
			sources[i] = bFactory.getSource((IBlockSink) sinks.get(i));
		}
		
		return sources;
	}
	

	/** This method removes all files used by the sinks.
	 *
	 */
	public void removeAll () throws BlockingException {
		for (int i=0; i< sinks.size(); i++) {
			bFactory.removeSink((IBlockSink) sinks.get(i));
		}
	}


	/** This method write the block set to the appropiate file by calculating the number of file 
	 * with size less than it and take into consideration if there is more than 1 file for this size.
	 * 
	 * @param block
	 */
	public void writeToSink (BlockSet block) throws BlockingException {
		int size = block.getRecordIDs().size();
		
		FileInfo fInfo = (FileInfo) map.get(new Integer (size));
		
		int num = fInfo.numBuckets; //number of buckets this size has
		
		int ind = fInfo.fileID; //this indicates which file to write to.
		
		//need to rotate if there is more than 1 file
		if (num > 1) {
			ind = ind + (fInfo.rotate % num);
			fInfo.rotate ++;
		}
		
//		System.out.println ("writing to size: " + size + " ind: " + ind);

		IBlockSink sink = (IBlockSink) sinks.get(ind);
		sink.append();
		sink.writeBlock(block);
		sink.close();

	}
	

	
	
	private static class FileInfo {
		public int numBuckets;
		public int rotate;
		public int fileID;
	}

}
