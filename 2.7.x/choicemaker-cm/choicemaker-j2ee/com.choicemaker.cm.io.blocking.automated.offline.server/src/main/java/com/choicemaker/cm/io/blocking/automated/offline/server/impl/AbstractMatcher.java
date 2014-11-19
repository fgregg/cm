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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonSetOSSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;

/**
 * Common functionality of {@link Matcher2} and {@link TransMatcher}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractMatcher implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	
	protected static final int INTERVAL = 50000;

	protected abstract Logger getLogger();
	
	protected abstract Logger getJMSTrace();
	
	protected abstract EntityManager getEntityManager();

	// These two tracker are set only in log debug mode
	private long inHMLookup;
	private long inCompare;

	// number of comparisons made
	protected int compares;

	public /* final */ void onMessage(Message inMessage) {
		getJMSTrace().info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		BatchJob batchJob = null;
		EJBConfiguration configuration = EJBConfiguration.getInstance();

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof StartData) {
					// start matching
					StartData data = ((StartData) o);
					final long jobId = data.jobID;

					getLogger().fine("Matcher2 In onMessage " + data.jobID + " "
							+ data.ind + " " + data.treeInd);

					batchJob =
						configuration.findBatchJobById(getEntityManager(), BatchJobBean.class,
								jobId);
					BatchParameters params =
						configuration.findBatchParamsByJobId(getEntityManager(), jobId);
					OABAConfiguration oabaConfig = new OABAConfiguration(jobId);
					OabaProcessing processingEntry =
						configuration.getProcessingLog(getEntityManager(), jobId);

					if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob
							.getStatus())) {
						MessageBeanUtils.stopJob(batchJob, processingEntry,
								oabaConfig);

					} else {
						handleMatching(data, batchJob, params);
					}

				} else {
					getLogger().warning("wrong type: " + inMessage.getClass().getName());
				}

			} else {
				getLogger().warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			getLogger().severe(e.toString());
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
//			mdc.setRollbackOnly();
		}
		getJMSTrace().info("Exiting onMessage for " + this.getClass().getName());
	}

	protected final void handleMatching(StartData data, final BatchJob batchJob,
			final BatchParameters params) throws BlockingException,
			RemoteException, NamingException, JMSException {

		final String modelConfigId = params.getModelConfigurationName();
		final IProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);
		final ImmutableThresholds t =
			new ImmutableThresholds(params.getLowThreshold(),
					params.getHighThreshold());

		// FIXME
//		String temp = (String) stageModel.properties().get("numProcessors");
//		int numProcessors = Integer.parseInt(temp);
		int numProcessors = -1;

//		temp = (String) stageModel.properties().get("maxMatchSize");
		// 2014-04-24 rphall: Commented out unused local variable.
		// int maxMatch = Integer.parseInt(temp);

		// FIXME
		// max block size
//		temp = (String) stageModel.properties().get("maxBlockSize");
//		int maxBlock = Integer.parseInt(temp);
		int maxBlock = -1;

		// get the data store
		ChunkDataStore dataStore = ChunkDataStore.getInstance();

		// get the right source
		IComparisonSetSource source = getSource(data, numProcessors, maxBlock);

		getLogger().info(getID() + " matching " + source.getInfo());

		compares = 0;

		inHMLookup = 0;
		inCompare = 0;

		int sets = 0;
		int numMatches = 0;
		try {
			source.open();
			while (source.hasNext()) {
				sets++;
				IComparisonSet cSet = source.getNextSet();
				List<MatchRecord2> matches =
					handleComparisonSet(cSet, batchJob, dataStore, stageModel, t);
				numMatches += matches.size();
				writeMatches(data, matches);
			}
		} finally {
			source.close();
		}

		getLogger().info("Chunk: " + data.ind + "_" + data.treeInd + ", sets: " + sets
				+ ", compares: " + compares + ", matches: " + numMatches);

		MatchWriterData mwd = new MatchWriterData(data);
		mwd.numCompares = compares;
		// FIXME
		mwd.timeWriting = 0;
		// END FIXME
		mwd.inCompare = inCompare;
		mwd.inLookup = inHMLookup;
		mwd.numMatches = numMatches;

		sendToScheduler(mwd);
	}

	/**
	 * This method handles the comparisons of a IComparisonSet. It returns an
	 * ArrayList of MatchRecord2 produced by this IComparisonSet.
	 */
	protected abstract List<MatchRecord2> handleComparisonSet(IComparisonSet cSet,
			BatchJob batchJob, ChunkDataStore dataStore,
			IProbabilityModel stageModel, ImmutableThresholds t)
			throws RemoteException, BlockingException;

	protected final Record getQ(ChunkDataStore dataStore, ComparisonPair p) {
		long t = 0;
		if (getLogger().isLoggable(Level.FINE))
			t = System.currentTimeMillis();

		Record r = (Record) dataStore.getStage(p.getId1());

		if (getLogger().isLoggable(Level.FINE)) {
			t = System.currentTimeMillis() - t;
			inHMLookup += t;
		}

		return r;
	}

	protected final  Record getM(ChunkDataStore dataStore, ComparisonPair p) {
		long t = 0;
		if (getLogger().isLoggable(Level.FINE))
			t = System.currentTimeMillis();

		Record r = null;
		if (p.isStage)
			r = (Record) dataStore.getStage(p.getId2());
		else
			r = (Record) dataStore.getMaster(p.getId2());

		if (getLogger().isLoggable(Level.FINE)) {
			t = System.currentTimeMillis() - t;
			inHMLookup += t;
		}

		return r;
	}

	/**
	 * This method returns the correct tree/array file for this chunk.
	 *
	 * @param num
	 *            - number of processors
	 */
	protected final IComparisonSetSource getSource(StartData data, int num,
			int maxBlockSize) throws BlockingException {
		OABAConfiguration oabaConfig = new OABAConfiguration(data.jobID);
		if (data.ind < data.numRegularChunks) {
			// regular
			ComparisonTreeGroupSinkSourceFactory factory =
				oabaConfig.getComparisonTreeGroupFactory(data.stageType, num);
			IComparisonTreeSource source =
				factory.getSource(data.ind, data.treeInd);
			if (source.exists()) {
				IComparisonSetSource setSource =
					new ComparisonTreeSetSource(source);
				return setSource;
			} else {
				throw new BlockingException("Could not get source "
						+ source.getInfo());
			}
		} else {
			// oversized
			int i = data.ind - data.numRegularChunks;
			ComparisonArrayGroupSinkSourceFactory factoryOS =
				oabaConfig.getComparisonArrayGroupFactoryOS(num);
			IComparisonArraySource sourceOS =
				factoryOS.getSource(i, data.treeInd);
			if (sourceOS.exists()) {
				IComparisonSetSource setSource =
					new ComparisonSetOSSource(sourceOS, maxBlockSize);
				return setSource;
			} else {
				throw new BlockingException("Could not get source "
						+ sourceOS.getInfo());
			}
		}
	}

	/**
	 * This method writes the matches of a IComparisonSet to the file
	 * corresponding to this matcher bean.
	 *
	 * @param matches
	 */
	protected abstract void writeMatches(StartData data, List<MatchRecord2> matches)
			throws BlockingException;

	/**
	 * This method compares two records and returns a MatchRecord2 object.
	 * @param q
	 *            - first record
	 * @param m
	 *            - second record
	 * @param isStage
	 *            - indicates if the second record is staging or master
	 */
	@SuppressWarnings("unchecked")
	protected final MatchRecord2 compareRecords(Record q, Record m, boolean isStage,
			ImmutableProbabilityModel model, ImmutableThresholds t) {
		long startTime = 0;
		if (getLogger().isLoggable(Level.FINE))
			startTime = System.currentTimeMillis();

		Evaluator evaluator = model.getEvaluator();
		MatchRecord2 mr = null;
		if ((q != null) && (m != null)) {
			Match match =
				evaluator.getMatch(q, m, t.getDifferThreshold(),
						t.getMatchThreshold());

			// no match
			if (match == null)
				return null;
			Decision decision = match.decision;
			float matchProbability = match.probability;

			// char source = 'D';
			char source = MatchRecord2.MASTER_SOURCE;

			Comparable i1 = q.getId();
			Comparable i2 = m.getId();

			if (isStage) {
				// source = 'S';
				source = MatchRecord2.STAGE_SOURCE;

				// make sure the smaller id is first
				if (i1.compareTo(i2) > 0) {
					Comparable i3 = i1;
					i1 = i2;
					i2 = i3;
				}
			}

			String noteInfo =
				MatchRecord2.getNotesAsDelimitedString(match.ac, model);
			if (decision == Decision.MATCH) {
				mr =
					new MatchRecord2(i1, i2, source, matchProbability,
							MatchRecord2.MATCH, noteInfo);
			} else if (decision == Decision.DIFFER) {
			} else if (decision == Decision.HOLD) {
				mr =
					new MatchRecord2(i1, i2, source, matchProbability,
							MatchRecord2.HOLD, noteInfo);
			}

		}

		if (getLogger().isLoggable(Level.FINE)) {
			startTime = System.currentTimeMillis() - startTime;
			inCompare += startTime;
		}

		return mr;
	}

	/**
	 * This returns an unique id for each instance of the object.
	 */
	public String getID() {
		String str = this.toString();
		int i = str.indexOf('@');
		return str.substring(i + 1);
	}

	protected abstract void sendToScheduler(MatchWriterData data);

}