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
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * @author pcheung
 *
 * This service handles the deduping of match record id pairs.  This version uses the MatchRecord2
 * object.
 * 
 * The deduping works as follows:
 * 1.	use a tree set to filter out dups
 * 2.	if there are too many pairs, write the hash set to file and empty the set
 * 3.	sort and merge all the files into 1. 
 * 
 */
public class MatchDedupService2 {
	
	private static final Logger log = Logger.getLogger(MatchDedupService2.class.getName());

	private IMatchRecord2Source mSource;
	private IMatchRecord2Sink mSink;
	private IMatchRecord2SinkSourceFactory mFactory;
	private IStatus status;
	private int max;
	private static int INTERVAL = 100;
	
	private ArrayList tempSinks = new ArrayList (); //list of temporary sinks
	
	private int numBefore = 0; //this counts the number of input matches
	private int numAfter = 0; //this counts the number of output matches
	
	private long time; //this keeps track of time

	
	public MatchDedupService2 (IMatchRecord2Source mSource, IMatchRecord2Sink mSink,
		IMatchRecord2SinkSourceFactory mFactory, int max, IStatus status) {
		
		this.mSource = mSource;
		this.mSink = mSink;
		this.mFactory = mFactory;
		this.status = status;
		this.max = max;
	}
	
	
	/** This method runs the service.
	 * 
	 * @throws IOException
	 */
	public void runService () throws BlockingException {
		time = System.currentTimeMillis();
		
		if (status.getStatus() >= IStatus.DONE_DEDUP_MATCHES ) {
			//do nothing
			
		} else if (status.getStatus() == IStatus.DONE_MATCHING_DATA) {
				
			//start writing out dedup
			log.info ("start writing to temp match files");
			writeToFiles (0);
			mergeFiles ();
			
		} else if (status.getStatus() == IStatus.OUTPUT_DEDUP_MATCHES) {
			//recover writing out dedup matches
			String temp =  status.getAdditionalInfo();
			int ind = temp.indexOf( IStatus.DELIMIT);
			int size = Integer.parseInt( temp.substring(0,ind) );
			int skip = Integer.parseInt( temp.substring(ind + 1));
			
			log.info ("match dedup recovery " + size + " " + skip);
			
			recover (size);
			writeToFiles (skip);
			mergeFiles ();
			
		} else if (status.getStatus() == IStatus.MERGE_DEDUP_MATCHES) {
			
			//merge the files into 1
			int size = Integer.parseInt( status.getAdditionalInfo() );

			log.info ("match dedup merge recovery " + size);

			recover (size);
			mergeFiles ();
		}
		
		time = System.currentTimeMillis() - time;
	}
	
	/** This method returns the time it takes to run the runService method.
	 * 
	 * @return long - returns the time (in milliseconds) it took to run this service.
	 */
	public long getTimeElapsed () { return time; }



	/** This returns the number of matches before the dedup.
	 * 
	 * @return
	 */
	public int getNumBefore () {
		return numBefore;
	}


	/** This returns the number of matches after the dedup.
	 * 
	 * @return
	 */
	public int getNumAfter () {
		return numAfter;
	}

	
	/** This method recovers previously written temp match record files
	 * 
	 * @param size - number of temp files
	 */
	private void recover (int size) throws BlockingException {
		for (int i=0; i<size; i++) {
			IMatchRecord2Sink tempSink = mFactory.getNextSink();
			tempSinks.add (tempSink);
		}
	}
	
	
	/** This method separates the match source into smaller ones that are distinct and sorted.
	 * 
	 * @param skip - number of match records to skip from the source
	 * @throws IOException
	 */
	private void writeToFiles (int skip) throws BlockingException {
		//if there is no match file, then do nothing.
		if (!mSource.exists()) return;
		
		TreeSet matches = new TreeSet ();
		
		IMatchRecord2Sink tempSink = mFactory.getNextSink();
		tempSinks.add (tempSink);
		tempSink.open();
		
		mSource.open();
		
		numBefore = skip;
		
		//skipping
		for (int i=0; i< skip; i++) {
			mSource.hasNext();
			mSource.getNext ();
		}

		while (mSource.hasNext()) {
			MatchRecord2 mr = mSource.getNext();
			numBefore ++;
			
			if (!matches.contains(mr)) {
				matches.add(mr);

				if (numBefore > 0 && numBefore % INTERVAL == 0 && isFull (matches.size(), max) ) {
					log.info ("writing out " + matches.size());
					MemoryEstimator.writeMem();

					tempSink.writeMatches( matches.iterator());
					tempSink.close();
					
					String temp = Integer.toString(tempSinks.size()) + IStatus.DELIMIT 
						+ Integer.toString(numBefore);
					status.setStatus( IStatus.OUTPUT_DEDUP_MATCHES, temp);
					
					tempSink = mFactory.getNextSink();
					tempSink.open();
					tempSinks.add(tempSink);
					matches = new TreeSet ();
					
//					if (true) throw new RuntimeException ("test fail");
				}
			} //if contains mr
		}
		
		mSource.close();
		
		//one last file
		if (matches.size() > 0) {
			log.info ("writing out " + matches.size());
			
			tempSink.writeMatches( matches.iterator());
			tempSink.close();
					
			String temp = Integer.toString(tempSinks.size()) + IStatus.DELIMIT 
				+ Integer.toString(numBefore);
			status.setStatus( IStatus.OUTPUT_DEDUP_MATCHES, temp);
		}
		
		status.setStatus( IStatus.MERGE_DEDUP_MATCHES, Integer.toString(tempSinks.size()));
		
		log.info ("total matches read " + numBefore);
		
	}
	
	
	
	/** This method merges all the smaller sorted distinct files.
	 * 
	 * All the smaller files are all sorted.
	 *
	 */
	private void mergeFiles () throws BlockingException {
		//if there is no source, do nothing
		if (!mSource.exists()) return;

		mSink.open();
		
		int numFiles = tempSinks.size();

		//for just one small file, just do a file move
		if (numFiles == 1) {
			mSink.close();
			
			IMatchRecord2Sink snk = (IMatchRecord2Sink) tempSinks.get(0);
			mFactory.move (snk, mSink);
			status.setStatus( IStatus.DONE_DEDUP_MATCHES );

			//clean up none dedup file
			mSource.remove();

			return;
		} 
		
		//merge the smaller files
		//initialize the arrays
		IMatchRecord2Source [] sources = new IMatchRecord2Source [numFiles];
		MatchRecord2 [] records = new MatchRecord2 [numFiles];

		for (int i=0; i < numFiles; i++) {
			sources[i] = mFactory.getSource((IMatchRecord2Sink) tempSinks.get(i));
			sources[i].open();
			
			if (sources[i].hasNext()) {
				records[i] = sources[i].getNext();
			} else {
				records[i] = null;
			}
		}
		
		
		boolean stop = false;
		while (!stop) {
			int idx = findMin (records);
			
			if (idx == -1) stop = true;
			else {
				mSink.writeMatch( records[idx] );
				numAfter ++;
				
				//remove the same match from other sources
				for (int i=0; i < numFiles; i ++) {
					if (i != idx) {
						if (records[i] != null) {
							if (records[i].equals (records[idx]) ) {
								if (sources[i].hasNext()) records[i] = sources[i].getNext();
								else records[i] = null;
							}
						}
					}
				} //end for

				//get the next record			
				if (sources[idx].hasNext()) records[idx] = sources[idx].getNext();
				else records[idx] = null;
			}
		} //end while
		
		mSink.close();
		log.info ("total matches written " + numAfter);

		status.setStatus( IStatus.DONE_DEDUP_MATCHES );

		
		//close all files
		for (int i=0; i< numFiles; i++) {
			sources[i].close();
			sources[i].remove();
		}
		
		//remove source
		mSource.remove();
		
	}



	/** This method checks to see if the system is running out of memory
	 * 
	 * @param size - size of the hash set
	 * @param max - maximum allowable size of the hash set, or if 0, check to see if 70% of the
	 * 	system memory is being used.
	 * @return
	 */
	private static boolean isFull (int size, int max) {
		boolean ret = false;
		if (max > 0) {
			if (size > max) ret = true;
		} else {
			ret = MemoryEstimator.isFull(.70f);
		}
		return ret;
	}
	

	/** This method finds the index of the array with the smallest element.
	 * This compares both id1 and id2 of the MatchRecord.
	 * 
	 * @param records
	 * @return
	 */
	private static int findMin (MatchRecord2 [] records) {
		Comparable min1 = null;
		Comparable min2 = null;
		
		int minIdx = -1;
		
		for (int i=0; i<records.length; i++) {
			if ((records[i] != null)) {
				Comparable id1 = records[i].getRecordID1();
				Comparable id2 = records[i].getRecordID2();
				
				if (min1 == null) {
					min1 = id1;
					min2 = id2;
					minIdx = i;
				} else {
					if (id1.compareTo(min1) < 0) {
						min1 = id1;
						min2 = id2;
						minIdx = i;
					} else if (id1.compareTo(min1) == 0) {
						if (id2.compareTo(min2) < 0) {
							min2 = id2;
							minIdx = i;
						}
					}
				}
			} //end record != null
		}
		return minIdx;
	}

}
