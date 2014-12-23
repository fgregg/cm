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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IControl;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * This object takes in a source that gets Comparable object and dedups that source.
 * It does the following:
 * 
 * 1.	Put Comparables on a TreeSet to filter out dupes.
 * 2.	If the tree gets full, write to a temp file.  The content of the temp file is sorted.
 * 3.	Merge all the temp files by opening all of them and walk through them. 
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GenericDedupService {
	
	private static final Logger log = Logger.getLogger(GenericDedupService.class.getName());
	
	private static final int INTERVAL = 1000;

	private IComparableSource cSource;
	private IComparableSink cSink;
	private IComparableSinkSourceFactory cFactory;
	private int maxMatches;
	
	private ArrayList tempSinks = new ArrayList (); //list of temporary sinks
	
	private int numBefore; //this counts the number of input matches
	private int numAfter; //this counts the number of output matches
	
	private long time; //this keeps track of time

	// these two variables are used to stop the program in the middle 
	private IControl control;
	private boolean stop;


	
	/** This constructor takes in the source with dups, a sink to store the dedup Comparables,
	 * and a factory to generate temp sinks.  maxMatches is the maximum size of the TreeSet.
	 * 
	 * @param mSource
	 * @param mSink
	 * @param mFactory
	 * @param maxMatches
	 * @param status
	 */
	public GenericDedupService (IComparableSource cSource, IComparableSink cSink,
	IComparableSinkSourceFactory cFactory, int max, IControl control) {
		
		this.cSource = cSource;
		this.cSink = cSink;
		this.cFactory = cFactory;
		this.maxMatches = max;
		this.control = control;
		stop = false;
	}
	
	public int getNumBefore () {return numBefore;}
	public int getNumAfter () {return numAfter;}
	public long getTime () {return time;}
	
	/**
	 * This performs the deduping of cSource.
	 */
	public void runDedup () throws BlockingException {
		time = System.currentTimeMillis();
		writeToFiles ();
		
		//special case of nothing to do
		if (numBefore == 0) {
			int countSinks = tempSinks.size();

			//remove the temporary sinks
			for (int i = 0; i<countSinks; i++) {
				IComparableSink tempSink = (IComparableSink) tempSinks.get(i);
				log.fine("removing " + tempSink.getInfo());
				tempSink.remove();
			}
			
			log.fine("removing " + cSource.getInfo());
			cSource.delete();
			
			//create a blank output file
			log.fine("creating " + cSink.getInfo());
			cSink.open();
			cSink.close();
			
		} else {
			int i = mergeFiles (tempSinks, cSink, cFactory, true);
			if (i > 0) numAfter = i;
			
			//remove the source
			cSource.delete();
		}
		
		time = System.currentTimeMillis() - time;
	}
	
	
	/** This reads from cSource and put Comparables on a TreeSet and when the set gets too big,
	 * it dumps it to a temp file.
	 * 
	 *
	 */
	private void writeToFiles () throws BlockingException {
		
		TreeSet matches = new TreeSet ();
		
		IComparableSink tempSink = cFactory.getNextSink();
		tempSinks.add (tempSink);
		tempSink.open();
		
		cSource.open();

		log.fine("Reading from " + cSource.getInfo() + " writing to " + tempSink.getInfo());

		while (cSource.hasNext() && !stop) {
			Comparable c = cSource.getNext();
			numBefore ++;
			
			stop = ControlChecker.checkStop (control, numBefore);
			
			if (!matches.contains(c)) {
				matches.add(c);

				if (numBefore % INTERVAL == 0 && isFull (matches.size(), maxMatches) ) {
					log.fine ("writing out " + matches.size());
					MemoryEstimator.writeMem();

					tempSink.writeComparables( matches.iterator());
					tempSink.close();
					
					tempSink = cFactory.getNextSink();
					tempSink.open();
					tempSinks.add(tempSink);
					
					matches = new TreeSet ();
				}
			} //if contains mr
		}
		
		cSource.close();

		//one last file
		if (matches.size() > 0) {
			log.fine ("final writing out " + matches.size());
			numAfter = matches.size();
			
			//This iterator returns comparables in ascending order.
			tempSink.writeComparables( matches.iterator());
		}
		tempSink.close();

		log.info ("total comparables read " + numBefore);
	}
	
	
	/** 
	 * This method merges all the temp files into one.
	 * 
	 * All the temp files are sorted in the same order.
	 * 
	 * @param tempSinks - ArrayList of temp files
	 * @param cSink - the output
	 * @param cFactory - IComparableSinkSourceFactory for moving other utility functions.
	 * @param delete - indicates if the tempSinks should be deleted.
	 * @return
	 * @throws BlockingException
	 */
	public static int mergeFiles (List tempSinks, 
		IComparableSink cSink, IComparableSinkSourceFactory cFactory, 
		boolean delete) throws BlockingException {
			
		int numFiles = tempSinks.size();

		//for just one small file, just do a file move
		if (numFiles == 1) {
			IComparableSink snk = (IComparableSink) tempSinks.get(0);
			int i = count (snk, cFactory);
			cFactory.move (snk, cSink);

			return i;
		} 
		
		//merge the smaller files
		//initialize the arrays
		IComparableSource [] sources = new IComparableSource [numFiles];
		Comparable [] comps = new Comparable [numFiles];

		for (int i=0; i < numFiles; i++) {
			sources[i] = cFactory.getSource((IComparableSink) tempSinks.get(i));
			sources[i].open();
			
			if (sources[i].hasNext()) {
				comps[i] = sources[i].getNext();
			} else {
				comps[i] = null;
			}
		} //end for
		
		cSink.open();

		int num = 0;
		boolean stop = false;
		while (!stop) {
			int idx = findMin (comps, numFiles);
			
			if (idx == -1) stop = true;
			else {
				cSink.writeComparable( comps[idx] );
				num ++;
				
				//remove the same match from other sources
				for (int i=0; i < numFiles; i ++) {
					if (i != idx) {
						if (comps[i] != null) {
							if (comps[i].equals (comps[idx]) ) {
								if (sources[i].hasNext()) comps[i] = sources[i].getNext();
								else comps[i] = null;
							}
						}
					}
				} //end for

				//get the next record			
				if (sources[idx].hasNext()) comps[idx] = sources[idx].getNext();
				else comps[idx] = null;
			}
		} //end while

		cSink.close();

		log.fine ("total matches written " + num);

		//close and remove all files
		for (int i=0; i< numFiles; i++) {
			sources[i].close();
			if (delete) sources[i].delete();
		}
		
		return num;
	}
	
	
	private static int count (IComparableSink snk, IComparableSinkSourceFactory cFactory) 
		throws BlockingException {
			
		int i = 0;
		IComparableSource source = cFactory.getSource(snk);
		source.open();
		while (source.hasNext()) {
			source.getNext();
			i ++;
		}
		source.close();
		return i;
	}
	
	
	/** This method compares source to dups and write all rows in source that do not exist
	 * in dups to cSink.
	 * 
	 * It returns the number of rows written to cSink.
	 * 
	 * This assumes that source and dups are both sorted in the same order.
	 * 
	 * @param source
	 * @param dups
	 * @param cSink
	 * @return
	 */
	public static int nonOverlap (IComparableSource source, IComparableSource dups,
		IComparableSink cSink) throws BlockingException {
			
		int ret = 0;
		
		source.open();
		dups.open();
		cSink.open();
		
		Comparable nextComp = getNextComp (dups);
		
		while (source.hasNext()) {
			Comparable c = source.getNext();
			
			if (nextComp != null) {
				//get the next dup that is at least as great as the current object.
				if (c.compareTo(nextComp) == 1) {
					nextComp = getNextCompBiggerThan (dups, c);
				}
				
				if (c.compareTo(nextComp) == 0) {
					nextComp = getNextComp (dups);
				} else if (c.compareTo(nextComp) == -1){
					cSink.writeComparable(c);
					ret ++;
				} else {
					throw new BlockingException ("error: " + c + " " + nextComp);
				}
				
			} else {
				cSink.writeComparable(c);
				ret ++;
			}
		}
		
		source.close();
		dups.close();
		cSink.close();
		
		return ret;
	}
	
	
	/* This gets the next Comparable from the source.  If no more, then it returns null.
	 * It assumes that source is already open.
	 * 
	 */
	private static Comparable getNextComp (IComparableSource dups) throws BlockingException {
		Comparable nextComp = null;
		if (dups.hasNext()) {
			nextComp = dups.getNext();
		}
		return nextComp;
	}
	
	
	/* This method gets the next value in the source that is >= c.
	 * 
	 */
	private static Comparable getNextCompBiggerThan (IComparableSource dups, Comparable c) throws BlockingException {
		Comparable nextComp = null;
		boolean stop = false;
		while (!stop && dups.hasNext()) {
			nextComp = dups.getNext();
			if (c.compareTo(nextComp) != 1) stop = true;
		}
		return nextComp;
	}
	
	

	/** This method finds the index of the array with the smallest element.
	 * This compares both id1 and id2 of the MatchRecord.
	 * 
	 * @param records
	 * @return
	 */
	private static int findMin (Comparable [] records, int size) {
		Comparable min = null;
		
		int minIdx = -1;
		
		for (int i=0; i<size; i++) {
			if ((records[i] != null)) {
				if (min == null) {
					min = records[i];
					minIdx = i;
				} else {
					if (records[i].compareTo(min) < 0) {
						min = records[i];
						minIdx = i;
					}
				}
			} //end record != null
		}
		return minIdx;
	}
	

	/** This method checks to see if the system is running out of memory
	 * 
	 * @param size - size of the hash set
	 * @param maxMatches - maximum allowable size of the hash set, or if 0, check to see if 70% of the
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


}
