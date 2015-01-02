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

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJobStatus;
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
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonSetOSSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;

/**
 * Common functionality of {@link MatcherMDB} and {@link TransMatcher}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractMatcher implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	
	protected static final int INTERVAL = 50000;

	protected abstract Logger getLogger();
	
	protected abstract Logger getJMSTrace();
	
	protected abstract OabaJobControllerBean getJobController();
	
	protected abstract OabaParametersControllerBean getParametersController();
	
	protected abstract OabaProcessingControllerBean getProcessingController();

	protected abstract ServerConfigurationController getServerController();
	
	protected abstract OabaSettingsController getSettingsController();

	// These two tracker are set only in log debug mode
	private long inHMLookup;
	private long inCompare;

	// number of comparisons made
	protected int compares;

	public /* final */ void onMessage(Message inMessage) {
		getJMSTrace().info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJob oabaJob = null;

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof OabaJobMessage) {
					// start matching
					OabaJobMessage data = ((OabaJobMessage) o);
					final long jobId = data.jobID;

					oabaJob = getJobController().findOabaJob(jobId);
					final OabaParameters params =
						getParametersController().findBatchParamsByJobId(jobId);
					final OabaEventLog processingLog =
							getProcessingController().getProcessingLog(oabaJob);
					final OabaSettings oabaSettings =
							getSettingsController().findOabaSettingsByJobId(jobId);
					final ServerConfiguration serverConfig = getServerController().findServerConfigurationByJobId(jobId);
					if (oabaJob == null || params == null || oabaSettings == null || serverConfig == null) {
						String s = "Unable to find a job, parameters, settings or server configuration for " + jobId;
						getLogger().severe(s);
						throw new IllegalArgumentException(s);
					}
					final String modelConfigId = params.getModelConfigurationName();
					ImmutableProbabilityModel model =
						PMManager.getModelInstance(modelConfigId);
					if (model == null) {
						String s =
							"No modelId corresponding to '" + modelConfigId + "'";
						getLogger().severe(s);
						throw new IllegalArgumentException(s);
					}
					
					getLogger().fine("MatcherMDB In onMessage " + data.jobID + " "
							+ data.ind + " " + data.treeInd);

					if (BatchJobStatus.ABORT_REQUESTED == oabaJob
							.getStatus()) {
						MessageBeanUtils.stopJob(oabaJob, processingLog);

					} else {
						handleMatching(data, oabaJob, params, oabaSettings, serverConfig);
					}

				} else {
					getLogger().warning("wrong type: " + inMessage.getClass().getName());
				}

			} else {
				getLogger().warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			getLogger().severe(e.toString());
			if (oabaJob != null) {
				oabaJob.markAsFailed();
			}
//			mdc.setRollbackOnly();
		}
		getJMSTrace().info("Exiting onMessage for " + this.getClass().getName());
	}

	protected final void handleMatching(OabaJobMessage data,
			final OabaJob oabaJob, final OabaParameters params,
			OabaSettings settings, ServerConfiguration serverConfig)
			throws BlockingException, RemoteException, NamingException,
			JMSException {

		final String modelConfigId = params.getModelConfigurationName();
		final IProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);
		final ImmutableThresholds t =
			new ImmutableThresholds(params.getLowThreshold(),
					params.getHighThreshold());
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();
		final int maxBlock = settings.getMaxBlockSize();

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
				IComparisonSet cSet = (IComparisonSet) source.next();
				List<MatchRecord2> matches =
					handleComparisonSet(cSet, oabaJob, dataStore, stageModel, t);
				numMatches += matches.size();
				writeMatches(data, matches);
			}
		} finally {
			source.close();
		}

		getLogger().info("Chunk: " + data.ind + "_" + data.treeInd + ", sets: " + sets
				+ ", compares: " + compares + ", matches: " + numMatches);

		MatchWriterMessage mwd = new MatchWriterMessage(data);
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
			OabaJob oabaJob, ChunkDataStore dataStore,
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
	protected final IComparisonSetSource getSource(OabaJobMessage data,
			int num, int maxBlockSize) throws BlockingException {
		OabaJob job = getJobController().findOabaJob(data.jobID);
		if (data.ind < data.numRegularChunks) {
			// regular
			ComparisonTreeGroupSinkSourceFactory factory =
				OabaFileUtils.getComparisonTreeGroupFactory(job,
						data.stageType, num);
			IComparisonTreeSource source =
				factory.getSource(data.ind, data.treeInd);
			if (source.exists()) {
				@SuppressWarnings("unchecked")
				IComparisonSetSource setSource =
					new ComparisonTreeSetSource(source);
				return setSource;
			} else {
				throw new BlockingException("Could not get regular source "
						+ source.getInfo());
			}
		} else {
			// oversized
			int i = data.ind - data.numRegularChunks;
			ComparisonArrayGroupSinkSourceFactory factoryOS =
				OabaFileUtils.getComparisonArrayGroupFactoryOS(job, num);
			IComparisonArraySource sourceOS =
				factoryOS.getSource(i, data.treeInd);
			if (sourceOS.exists()) {
				@SuppressWarnings("unchecked")
				IComparisonSetSource setSource =
					new ComparisonSetOSSource(sourceOS, maxBlockSize);
				return setSource;
			} else {
				throw new BlockingException("Could not get oversized source "
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
	protected abstract void writeMatches(OabaJobMessage data, List<MatchRecord2> matches)
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

	protected abstract void sendToScheduler(MatchWriterMessage data);

}
