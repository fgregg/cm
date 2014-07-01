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
package com.choicemaker.cm.io.blocking.automated.offline.result;

import java.util.TreeSet;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSource;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This class takes in a MatchRecord2Source, a record id file of the size two sets,
 * and a MatchRecord2Sink.  Its goal is to produce a MatchRecord2 sink 
 * containing only pairs from size 2 equivalence classes. 
 * 
 * @author pcheung
 *
 */
public class Size2MatchProducer {
	
	private IMatchRecord2Source originalSource;
	private IRecordIDSource idSource;
	private IMatchRecord2Sink mSink;
	
	
	/**
	 * This class takes in a MatchRecord2Source, a record id file of the size two sets,
 	 * and a MatchRecord2Sink.
	 * 
	 * @param originalSource - The MatchRecord2 source produced by the OABA.
	 * @param idSource - The id file with record id of size 2 sets.
	 * @param idFactory - RecordIDSinkSourceFactory to produce record 
	 * @param mSink
	 */
	public Size2MatchProducer (IMatchRecord2Source originalSource,
		IRecordIDSource idSource, IMatchRecord2Sink mSink) {
			
		this.originalSource = originalSource;
		this.idSource = idSource;
		this.mSink = mSink;
	}
	
	
	/** This method produces a MatchRecord2Sink that has only the size 2 equivalence classes.
	 * It returns the number of MatchRecord2 written.
	 * 
	 * @return int - number of MatchRecord2 written.
	 * @throws BlockingException
	 */
	public int process () throws BlockingException {
		TreeSet ids = loadIDs ();
		return writeOut (ids);
	}
	
	
	/** This method loads the ids from size 2 equivalence classes into a TreeSet 
	 * and returns it.
	 * 
	 * @return TreeSet - ids in the size 2 equivalence classes.
	 * @throws BlockingException
	 */
	private TreeSet loadIDs () throws BlockingException {
		TreeSet ids = new TreeSet ();
		idSource.open();
		
		while (idSource.hasNext()) {
			Comparable C = idSource.getNextID();
			ids.add(C);
		}
		
		idSource.close();
		return ids;
	}
	
	
	
	/** This method writes all MatchRecord2 source that are in the ids TreeSet out to the
	 * MatchRecord2 sink.  It returns the number of MatchRecord2 written out.
	 * 
	 * @param ids - TreeSet containing record ids of size 2 equivalence classes. 
	 * @return int - numbers of MatchRecord2 written out.
	 * @throws BlockingException
	 */
	private int writeOut (TreeSet ids) throws BlockingException {
		originalSource.open();
		mSink.open();
		
		int count = 0;
		
		while (originalSource.hasNext()) {
			MatchRecord2 mr = originalSource.getNext();
			
			if (ids.contains(mr.getRecordID1())) {
				mSink.writeMatch(mr);
				count ++;
			} 
		}
		
		mSink.close();
		originalSource.close();
		
		return count;
	}
	

}
