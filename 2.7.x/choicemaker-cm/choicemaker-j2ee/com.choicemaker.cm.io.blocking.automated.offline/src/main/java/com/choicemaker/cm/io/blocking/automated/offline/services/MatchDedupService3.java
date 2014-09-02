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

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSource;

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
 * This version calls the GenericDedupService to handle the work. 
 * 
 */
public class MatchDedupService3 {
	
	private static final Logger log = Logger.getLogger(MatchDedupService3.class.getName());

	private IMatchRecord2Source mSource;
	private IMatchRecord2Sink mSink;
	private IMatchRecord2SinkSourceFactory mFactory;
	private IStatus status;
//	private int max;
	
	private int numBefore = 0; //this counts the number of input matches
	private int numAfter = 0; //this counts the number of output matches
	
	private long time; //this keeps track of time

	// these two variables are used to stop the program in the middle 
	private IControl control;
//	private boolean stop;

	
	public MatchDedupService3 (IMatchRecord2Source mSource, IMatchRecord2Sink mSink,
		IMatchRecord2SinkSourceFactory mFactory, int max, IStatus status, IControl control) {
		
		this.mSource = mSource;
		this.mSink = mSink;
		this.mFactory = mFactory;
		this.status = status;
//		this.max = max;
		this.control = control;
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
			
			IComparableSource source = new ComparableMRSource (mSource);
			IComparableSink sink = new ComparableMRSink (mSink);
			ComparableMRSinkSourceFactory factory = new ComparableMRSinkSourceFactory (mFactory);
			
			GenericDedupService service = new GenericDedupService (source, sink, factory, 500000, control);
			service.runDedup();
			numBefore = service.getNumBefore();
			numAfter = service.getNumAfter();
			
			log.info ("total matches before " + numBefore);
			log.info ("total matches after " + numAfter);

			status.setStatus( IStatus.DONE_DEDUP_MATCHES );
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



}
