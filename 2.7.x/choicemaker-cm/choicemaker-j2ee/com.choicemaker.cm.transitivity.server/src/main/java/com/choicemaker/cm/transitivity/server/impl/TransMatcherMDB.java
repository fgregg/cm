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
import javax.jms.MessageListener;
import javax.jms.Queue;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractMatcher;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * This is the Matcher for the Transitivity Engine.  It is called by TransMatchSchedulerMDB.
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
public class TransMatcherMDB extends AbstractMatcher  implements MessageListener {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(TransMatcherMDB.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ TransMatcherMDB.class.getName());

	private static final int INTERVAL = 50000;

	// @EJB
	private OabaJobControllerBean jobController;

	// @EJB
	private OabaSettingsController oabaSettingsController;

	// @EJB
	private ServerConfigurationController serverController;

	// @EJB
	private OabaParametersControllerBean paramsController;
	
	// @EJB
	private OabaProcessingControllerBean processingController;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatchSchedulerQueue")
	private Queue transMatchSchedulerQueue;

	@Inject
	JMSContext jmsContext;

	@Override
	protected OabaJobControllerBean getJobController() {
		return jobController;
	}

	@Override
	protected OabaParametersControllerBean getParametersController() {
		return paramsController;
	}

	@Override
	protected OabaProcessingControllerBean getProcessingController() {
		return processingController;
	}

	@Override
	protected ServerConfigurationController getServerController() {
		return serverController; 
	}

	@Override
	protected OabaSettingsController getSettingsController() {
		return oabaSettingsController;
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
	protected List handleComparisonSet (IComparisonSet cSet, OabaJob oabaJob, 
		ChunkDataStore dataStore, IProbabilityModel stageModel, ImmutableThresholds t) 
		throws RemoteException, BlockingException {
			
		boolean stop = oabaJob.shouldStop();
		ComparisonPair p;
		Record q, m;
		MatchRecord2 match;
		
		ArrayList matches = new ArrayList ();
		
		while (cSet.hasNextPair() && !stop) {
			p = cSet.getNextPair();
			
			compares ++;

			stop = ControlChecker.checkStop (oabaJob, compares, INTERVAL);

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
			throw new IllegalStateException ("Invalid comparison set in TransMatcherMDB");
		}
		
		return matches;
	}

	@Override
	protected void writeMatches (OabaJobMessage data, List<MatchRecord2> matches) throws BlockingException {
		//first figure out the correct file for this processor
		throw new Error("not yet implemented");
//		OabaFileUtils oabaConfig = new OabaFileUtils(data.jobID);
//		IMatchRecord2Sink mSink = oabaConfig.getMatchChunkFactory().getSink(data.treeInd);
//		IComparableSink sink =  new ComparableMRSink (mSink);
//		
//		//write matches to this file.
//		sink.append();
//		sink.writeComparables(matches.iterator());
//		
//		//write the separator
//		MatchRecord2 mr = (MatchRecord2) matches.get(0);
//		mr = MatchRecord2Factory.getSeparator(mr.getRecordID1());
//		sink.writeComparable(mr);
//		
//		sink.close();
	}

	@Override
	protected void sendToScheduler(MatchWriterMessage data) {
		MessageBeanUtils.sendMatchWriterData(data, jmsContext,
				transMatchSchedulerQueue, log);
	}

}
