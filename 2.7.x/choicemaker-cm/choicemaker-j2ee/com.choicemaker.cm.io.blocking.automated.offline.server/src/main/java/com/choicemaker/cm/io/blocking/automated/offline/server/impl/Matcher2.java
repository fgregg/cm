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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * This message bean compares the pairs given to it and sends a list of matches
 * to the match writer bean.
 *
 * In this version, there is only one chunk data in memory and different
 * processors work on different trees/arrays of this chunk.
 *
 * @author pcheung
 *
 * @param <T>
 *            the type of record identifier
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/matcherQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class Matcher2 extends AbstractMatcher {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(Matcher2.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ Matcher2.class.getName());

	private static final int INTERVAL = 50000;

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private SettingsController settingsController;

	@EJB
	private OabaParametersControllerBean paramsController;
	
	@EJB
	private OabaProcessingControllerBean processingController;

	@EJB
	private ServerConfigurationController serverController;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchSchedulerQueue")
	private Queue matchSchedulerQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

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
	protected SettingsController getSettingsController() {
		return settingsController;
	}

	@Override
	protected List<MatchRecord2> handleComparisonSet(IComparisonSet cSet,
			OabaJob oabaJob, ChunkDataStore dataStore,
			IProbabilityModel stageModel, ImmutableThresholds t)
			throws RemoteException, BlockingException {

		boolean stop = oabaJob.shouldStop();
		ComparisonPair p;
		Record q, m;
		MatchRecord2 match;

		List<MatchRecord2> matches = new ArrayList<>();

		while (cSet.hasNextPair() && !stop) {
			p = cSet.getNextPair();
			compares++;

			stop = ControlChecker.checkStop(oabaJob, compares, INTERVAL);

			q = getQ(dataStore, p);
			m = getM(dataStore, p);

			// Log severe problems
			boolean skipPair = false;
			if (p.getId1().equals(p.getId2()) && p.isStage) {
				// Should never happen
				skipPair = true;				
				String msg = "id1 = id2: " + p.getId1();
				getLogger().severe(msg);
			}

			
			// Skip a pair if a record is not
			// in this particular comparison set
			Level DETAILS = Level.FINER;
			boolean isLoggable = getLogger().isLoggable(DETAILS);
			if (q == null) {
				skipPair = true;
				if (isLoggable) {
					String msg = "Missing record: " + p.getId1();
					getLogger().log(DETAILS, msg);
				}
			}
			if (m == null) {
				skipPair = true;				
				if (isLoggable) {
					String msg = "Missing record: " + p.getId2();
					getLogger().log(DETAILS, msg);
				}
			}
			if (skipPair) {
				if (isLoggable) {
					String msg = "Skipped pair: " + p;
					getLogger().log(DETAILS, msg);
				}
				continue;
			}

			// If a pair isn't skipped, compute whether it is a MATCH or HOLD,
			// and if so, add it to the collections of matches. (DIFFER
			// decisions are returned as null.)
			match = compareRecords(q, m, p.isStage, stageModel, t);
			if (match != null) {
				matches.add(match);
			}
		}

		return matches;
	}

	/**
	 * This method writes the matches of a IComparisonSet to the file
	 * corresponding to this matcher bean.
	 *
	 * @param matches
	 */
	@Override
	protected void writeMatches(OabaJobMessage data, List<MatchRecord2> matches)
			throws BlockingException {

		// first figure out the correct file for this processor
		final long jobId = data.jobID;
		OabaJob oabaJob = getJobController().find(jobId);
		IMatchRecord2Sink mSink =
			OabaFileUtils.getMatchChunkFactory(oabaJob).getSink(data.treeInd);
		IComparableSink sink = new ComparableMRSink(mSink);

		// write matches to this file.
		sink.append();
		sink.writeComparables(matches.iterator());
		sink.close();
	}

	/**
	 * This returns an unique id for each instance of the object.
	 */
	public String getID() {
		String str = this.toString();
		int i = str.indexOf('@');
		return str.substring(i + 1);
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
	protected void sendToScheduler(MatchWriterMessage data) {
		MessageBeanUtils.sendMatchWriterData(data, jmsContext,
				matchSchedulerQueue, getLogger());
	}

}
