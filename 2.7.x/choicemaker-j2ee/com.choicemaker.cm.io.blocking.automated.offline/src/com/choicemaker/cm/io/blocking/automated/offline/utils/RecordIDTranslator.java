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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.LongArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource;

/**
 *	This object handles translating input record ids into internal record id.  The internal record ids are all
 * sequential enabling us to use an array instead of a hashmap.  
 *
 * It writes the information out to 1 or 2 files depending if there are 1 or 2 input record sources. 
 * 
 * This object assumes that the input record ids are pre sorted numerically.
 * 
 * @author pcheung
 *
 */
public class RecordIDTranslator {
	
	private IChunkRecordIDSinkSourceFactory cFactory;
	
	//List of files to store the mapping of input record id to internal record id.
	private IChunkRecordIDSink sink1;
	private IChunkRecordIDSink sink2;
	
	private LongArrayList list1;
	private LongArrayList list2;
	
	/** This contains the range of the record ID in sink1.  
	 * range1[0] is min and range1[1] is max.
	 */
	private long [] range1 = new long [2];
	
	/** This contains the range of the record ID in sink2.  
	 * range2[0] is min and range2[1] is max.
	 */
	private long [] range2 = new long [2];

	/**
	 * This contains the mapping from input record id I to internal record id J.
	 * mapping[J] = I.  J starts from 0.
	 */
	private int currentIndex;
	
	/**
	 * This is the point at which the second record source record ids start.  if this is 0, it means
	 * there is only 1 record source.
	 */
	private int splitIndex;
	
	private boolean initialized = false; //indicates whether initReverseTranslation has happened.
	
	
	
	public RecordIDTranslator (IChunkRecordIDSinkSourceFactory cFactory) throws BlockingException {
		this.cFactory = cFactory;
		
		sink1 = cFactory.getNextSink();
		sink2 = cFactory.getNextSink();

		range1[0] = Long.MAX_VALUE;
		range1[1] = Long.MIN_VALUE;

		range2[0] = Long.MAX_VALUE;
		range2[1] = Long.MIN_VALUE;
	}
	
	
	public long [] getRange1 () { return range1; }
	public long [] getRange2 () { return range2; }
	public int getSplitIndex () { return splitIndex; }
	
	
	/** This method perform initialization
	 * 
	 * @throws IOException
	 */
	public void open () throws BlockingException {
		currentIndex = -1;
		
		sink1.open();
		
		splitIndex = 0;		
	}
	
	
	/** This method attemps to recover the data from a previous run
	 * 
	 * @throws IOException
	 */
	public void recover () throws BlockingException {
		IChunkRecordIDSource source = cFactory.getSource(sink1);
		currentIndex = -1;
		splitIndex = 0;		
		if (source.exists()) {
			source.open();
			while (source.hasNext()) {
				currentIndex ++;
				
				long id = source.getNext();
				if (id < range1[0]) range1[0] = id;
				if (id > range1[1]) range1[1] = id;
			}
			source.close();
			sink1.append();
		}
		
		source = cFactory.getSource(sink2);
		if (source.exists()) {
			sink1.close();
			splitIndex = currentIndex + 1;
			source.open();
			while (source.hasNext()) {
				currentIndex ++;

				long id = source.getNext();
				if (id < range2[0]) range2[0] = id;
				if (id > range2[1]) range2[1] = id;
			}
			source.close();
			sink2.append();
		}
	}
	
	
	/** This method tells the objects that source1 is done and it sets the split index at where source 2
	 * begins.
	 * 
	 */
	public void split () throws BlockingException {
		splitIndex = currentIndex + 1;
		
		sink1.close();
		
		sink2.open();
	}
	
	
	public void close () throws BlockingException {
		if (splitIndex == 0) sink1.close();
		else sink2.close();
	}
	
	
	/** This method translates input record id to an internal id.
	 * 
	 * @param id - input record id
	 * @return int - internal id for this input record id.
	 */
	public int translate (long id) throws BlockingException {
		currentIndex ++;
		
//		System.out.println ("translate " + id + " " + currentIndex);
		
		if (splitIndex == 0) {
			sink1.writeRecordID(id);
			
			if (id < range1[0]) range1[0] = id;
			if (id > range1[1]) range1[1] = id;
			
		} else {
			sink2.writeRecordID(id);

			if (id < range2[0]) range2[0] = id;
			if (id > range2[1]) range2[1] = id;
			
		} 
		
		return currentIndex;
	}
	
	
	/**
	 * This method initializes for reverse translation - going from internal id to input record id.
	 * 
	 */
	public void initReverseTranslation () throws BlockingException {
		if (!initialized) {
			if (splitIndex == 0) list1 = new LongArrayList (currentIndex);
			else list1 = new LongArrayList (splitIndex);
		
			IChunkRecordIDSource source1 = cFactory.getSource(sink1);
			source1.open();
		
			while (source1.hasNext()) {
				list1.add(source1.getNext());
			}
		
			source1.close();
		
			//Read the second source if there is one
			if (splitIndex > 0) {
				list2 = new LongArrayList (currentIndex - splitIndex + 1);
				IChunkRecordIDSource source2 = cFactory.getSource(sink2);
				source2.open();
		
				while (source2.hasNext()) {
					list2.add(source2.getNext());
				}
		
				source2.close();
		
			}
		
			initialized = true;
		}
	}
	
	
	
	/** This method returns the input record id of the given internal id.
	 * 
	 * @param index - internal id, starts from 0
	 * @return long - the original input record id.
	 */
	public long reverseLookup (int index) {
		long ret = -1;
		
		if (splitIndex == 0) ret = list1.get(index);
		else {
			if (index < splitIndex) ret = list1.get(index);
			else {
				ret = list2.get(index - splitIndex);
			}
		}
		
		return ret;
	}
	
	
	/**
	 * This method performs the reverse id translation so that bSink contains the records ids used in the
	 * input file.
	 *
	 * @param bSource - block source file containing the internal id
	 * @param bSink - output block sink to contain the input record id.
	 */
	public void reverseTranslate (IBlockSource bSource, IBlockSink bSink) throws BlockingException {
		initReverseTranslation ();
		
		bSource.open();
		bSink.open();
		
		while (bSource.hasNext ()) {
			BlockSet bs = bSource.getNext();
			LongArrayList list = bs.getRecordIDs();
			for (int i=0; i<list.size(); i++) {
				list.set(i, reverseLookup((int) list.get(i)) );
			}
			
			bSink.writeBlock(bs);
		}
		
		bSource.close();
		bSink.close();
	}


	/** This method cleans up by removing the temporary files it used.
	 * 
	 *
	 */
	public void cleanUp () throws BlockingException {
		list1 = null;
		list2 = null;
		
		sink1.remove();
		if (splitIndex > 0) sink2.remove();
	}

}
