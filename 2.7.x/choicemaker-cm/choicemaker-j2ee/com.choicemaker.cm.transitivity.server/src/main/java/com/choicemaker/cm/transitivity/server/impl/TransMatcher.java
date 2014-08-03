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
package com.choicemaker.cm.transitivity.server.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2Factory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.Matcher2;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * This is the Matcher for the Transitivity Engine.  It is called by TransMatchScheduler.
 * 
 * @author pcheung
 *
 */
public class TransMatcher extends Matcher2  {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TransMatcher.class);

	/** This method sends the message to the match result write bean.
	 * 
	 * @param data
	 * @throws NamingException
	 */
	protected void sendToMatchScheduler (MatchWriterData data) throws NamingException, JMSException{
		Queue queue = configuration.getTransMatchSchedulerMessageQueue();
		configuration.sendMessage(queue, data);
	} 


	/** This method returns the correct tree/array file for this chunk.
	 * 
	 * @param num - number of processors
	 * @param chunkId - chunk id
	 * @param treeId - tree id
	 * @return
	 */
	protected IComparisonSetSource getSource (int num, int maxBlockSize) throws BlockingException {
		if (data.ind < data.numRegularChunks) {
			//this should never happen
			throw new BlockingException ("Found regular chunks in Tranvitivity Engine");
			
		} else {
			//oversized
			int i = data.ind - data.numRegularChunks;
			ComparisonArrayGroupSinkSourceFactory factoryOS =
				oabaConfig.getComparisonArrayGroupFactoryOS(num);
			IComparisonArraySource sourceOS = factoryOS.getSource(i, data.treeInd);
			if (sourceOS.exists()) {
				IComparisonSetSource setSource = new ComparisonSetSource (sourceOS);
				return setSource;
			} else {
				throw new BlockingException ("Could not get source " + sourceOS.getInfo ());
			}
		}
	}


	protected void setHighLow () {
		//set low to 0.0 so we get the prob of non matches
		low = data.low;
		
		high = data.high;			
	}


	/** This method handles the comparisons of a IComparisonSet.  It returns an 
	 * ArrayList of MatchRecord2 produced by this IComparisonSet.  
	 * 
	 * @param cSet
	 * @param batchJob
	 * @param dataStore
	 * @param stageModel
	 * @return
	 * @throws RemoteException
	 * @throws BlockingException
	 */
	protected ArrayList handleComparisonSet (IComparisonSet cSet, BatchJob batchJob, 
		ChunkDataStore dataStore, IProbabilityModel stageModel) 
		throws RemoteException, BlockingException {
			
		boolean stop = batchJob.shouldStop();
		ComparisonPair p;
		Record q, m;
		MatchRecord2 match;
		
		ArrayList matches = new ArrayList ();
		
		while (cSet.hasNextPair() && !stop) {
			p = cSet.getNextPair();
			
			compares ++;

			stop = ControlChecker.checkStop (batchJob, compares, INTERVAL);

			q = getQ(dataStore, p);
			m = getM(dataStore, p);
							
			//major problem if this happens
			if (p.id1.equals(p.id2) && p.isStage) {
				log.error("id1 = id2: " + p.id1.toString());
				throw new BlockingException ("id1 = id2");								
			}

			match = compareRecords (q, m, p.isStage, stageModel);
			if (match != null) {
				matches.add(match);
			}

		}
		
		if (matches.size()==0) {
			log.error("No matches found for this comparison set.");
			log.error("Set: " + cSet.writeDebug());
			throw new IllegalStateException ("Invalid comparison set in TransMatcher");
		}
		
		return matches;
	}


	/** This method writes the matches of a IComparisonSet to thre file 
	 * corresponding to this matcher bean.
	 * 
	 * For TE, we have to write a separator MatchRecord2 to delimit the blocks, which will
	 * make building CompositeEntity a lot faster.
	 * 
	 * @param matches
	 * @param ind
	 * @param maxMatch
	 * @throws BlockingException
	 */
	protected void writeMatches (ArrayList matches) throws BlockingException {
		//first figure out the correct file for this processor
		IMatchRecord2Sink mSink = oabaConfig.getMatchChunkFactory().getSink(data.treeInd);
		IComparableSink sink =  new ComparableMRSink (mSink);
		
		//write matches to this file.
		sink.append();
		sink.writeComparables(matches.iterator());
		
		//write the separator
		MatchRecord2 mr = (MatchRecord2) matches.get(0);
		mr = MatchRecord2Factory.getSeparator(mr.getRecordID1());
		sink.writeComparable(mr);
		
		sink.close();
		
	}

}
