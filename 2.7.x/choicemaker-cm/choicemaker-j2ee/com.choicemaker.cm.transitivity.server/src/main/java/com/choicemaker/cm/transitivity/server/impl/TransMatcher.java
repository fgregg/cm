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
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2Factory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractMatcher;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * This is the Matcher for the Transitivity Engine.  It is called by TransMatchScheduler.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/transMatcherQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class TransMatcher extends AbstractMatcher  {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(TransMatcher.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ TransMatcher.class.getName());

	private static final int INTERVAL = 50000;

	@PersistenceContext (unitName = "oaba")
	private EntityManager em;

//	@Resource
//	private MessageDrivenContext mdc;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatchSchedulerQueue")
	private Queue transMatchSchedulerQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

//	@Resource(lookup = "java:/choicemaker/urm/jms/yyyQueue")
//	private Queue yyyQueue;

	@Inject
	JMSContext jmsContext;

	@Override
	protected List handleComparisonSet (IComparisonSet cSet, BatchJob batchJob, 
		ChunkDataStore dataStore, IProbabilityModel stageModel, ImmutableThresholds t) 
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
			if (p.getId1().equals(p.getId2()) && p.isStage) {
				log.severe("id1 = id2: " + p.getId1().toString());
				throw new BlockingException ("id1 = id2");								
			}

			match = compareRecords (q, m, p.isStage, stageModel, t);
			if (match != null) {
				matches.add(match);
			}

		}
		
		if (matches.size()==0) {
			log.severe("No matches found for this comparison set.");
			log.severe("Set: " + cSet.writeDebug());
			throw new IllegalStateException ("Invalid comparison set in TransMatcher");
		}
		
		return matches;
	}

	@Override
	protected void writeMatches (StartData data, List<MatchRecord2> matches) throws BlockingException {
		//first figure out the correct file for this processor
		OABAConfiguration oabaConfig = new OABAConfiguration(data.jobID);
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

	@Override
	protected void sendToScheduler(MatchWriterData data) {
		MessageBeanUtils.sendMatchWriterData(data, jmsContext,
				transMatchSchedulerQueue, log);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJMSTrace() {
		return jmsTrace;
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

}
