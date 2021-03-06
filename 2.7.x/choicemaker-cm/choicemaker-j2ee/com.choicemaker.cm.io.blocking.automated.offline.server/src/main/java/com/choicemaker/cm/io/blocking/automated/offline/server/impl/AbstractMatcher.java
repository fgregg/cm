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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_CURRENT_CHUNK_INDEX;
import static com.choicemaker.cm.args.OperationalPropertyNames.PN_RECORD_ID_TYPE;
import static com.choicemaker.cm.args.OperationalPropertyNames.PN_REGULAR_CHUNK_FILE_COUNT;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecordUtils;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonSetOSSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.LoggingUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * Common functionality of {@link MatcherMDB} and {@link TransMatcher}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractMatcher implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;

	protected static final int INTERVAL = 50000;

	protected abstract Logger getLogger();

	protected abstract Logger getJMSTrace();

	// This instance data must be replaced by Entity properties
	private long inHMLookup;
	protected long inCompare;
	protected int compares;

	// -- Abstract call-back methods

	/** Writes matches to an implicit, on-disk cache */
	protected abstract void writeMatches(OabaJobMessage data,
			List<MatchRecord2> matches) throws BlockingException;

	/** Reports completion to the scheduler that uses this matcher */
	protected abstract void sendToScheduler(MatchWriterMessage data);

	protected abstract OabaJobController getJobController();

	protected abstract OabaParametersController getOabaParametersController();

	protected abstract ProcessingController getProcessingController();

	protected abstract ServerConfigurationController getServerController();

	protected abstract OabaSettingsController getSettingsController();

	protected abstract OperationalPropertyController getPropertyController();

	protected abstract JMSContext getJMSContext();

	// -- Template methods

	@Override
	public void onMessage(Message inMessage) {
		getJMSTrace().info(
				"Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		BatchJob batchJob = null;

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof OabaJobMessage) {
					// start matching
					OabaJobMessage data = ((OabaJobMessage) o);
					final long jobId = data.jobID;

					batchJob = getJobController().findBatchJob(jobId);
					final OabaParameters params =
						getOabaParametersController().findOabaParametersByBatchJobId(
								jobId);
					final ProcessingEventLog processingLog =
						getProcessingController().getProcessingLog(batchJob);
					final OabaSettings oabaSettings =
						getSettingsController().findOabaSettingsByJobId(jobId);
					final ServerConfiguration serverConfig =
						getServerController().findServerConfigurationByJobId(
								jobId);

					if (batchJob == null || params == null
							|| oabaSettings == null || serverConfig == null) {
						String s0 = "Null configuration info for job " + jobId;
						String s =
							LoggingUtils.buildDiagnostic(s0, batchJob, params,
									oabaSettings, serverConfig);
						getLogger().severe(s);
						throw new IllegalStateException(s);
					}

					final String modelConfigId =
						params.getModelConfigurationName();
					ImmutableProbabilityModel model =
						PMManager.getModelInstance(modelConfigId);
					if (model == null) {
						String s =
							"No modelId corresponding to '" + modelConfigId
									+ "'";
						getLogger().severe(s);
						throw new IllegalArgumentException(s);
					}

					final String _currentChunk =
						getPropertyController().getJobProperty(batchJob,
								PN_CURRENT_CHUNK_INDEX);
					final int currentChunk = Integer.valueOf(_currentChunk);
					getLogger().fine(
							"MatcherMDB In onMessage " + data.jobID + " "
									+ currentChunk + " " + data.treeIndex);

					if (BatchJobStatus.ABORT_REQUESTED == batchJob.getStatus()) {
						MessageBeanUtils.stopJob(batchJob,
								getPropertyController(), processingLog);

					} else {
						handleMatching(data, batchJob, params, oabaSettings,
								serverConfig, currentChunk);
					}

				} else {
					getLogger().warning(
							"wrong type: " + inMessage.getClass().getName());
				}

			} else {
				getLogger().warning(
						"wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			String msg0 = throwableToString(e);
			getLogger().severe(msg0);
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
		}
		getJMSTrace()
				.info("Exiting onMessage for " + this.getClass().getName());
	}

	protected String throwableToString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(throwable.toString());
		throwable.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}

	protected final void handleMatching(OabaJobMessage data,
			final BatchJob batchJob, final OabaParameters params,
			final OabaSettings settings,
			final ServerConfiguration serverConfig, final int currentChunk)
			throws BlockingException, RemoteException, NamingException,
			JMSException {

		final String modelConfigId = params.getModelConfigurationName();
		final ImmutableProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);
		final ImmutableThresholds t =
			new ImmutableThresholds(params.getLowThreshold(),
					params.getHighThreshold());
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();
		final int maxBlock = settings.getMaxBlockSize();

		// get the data store
		ChunkDataStore dataStore = ChunkDataStore.getInstance();

		// get the right source
		IComparisonSetSource source =
			getSource(data, numProcessors, maxBlock, currentChunk);

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
					handleComparisonSet(cSet, batchJob, dataStore, stageModel, t);
				numMatches += matches.size();
				writeMatches(data, matches);
			}
		} finally {
			source.close();
		}

		getLogger().info(
				"Chunk: " + currentChunk + "_" + data.treeIndex + ", sets: "
						+ sets + ", compares: " + compares + ", matches: "
						+ numMatches);

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
	 * This method handles the comparisons of a IComparisonSet. It returns a(n
	 * Array)List of MatchRecord2 produced by this IComparisonSet.
	 */
	protected final List<MatchRecord2> handleComparisonSet(IComparisonSet cSet,
			BatchJob batchJob, ChunkDataStore dataStore,
			ImmutableProbabilityModel stageModel, ImmutableThresholds t)
			throws RemoteException, BlockingException {

		boolean stop = batchJob.shouldStop();
		ComparisonPair p;
		Record q, m;
		MatchRecord2 match;

		List<MatchRecord2> matches = new ArrayList<>();

		while (cSet.hasNextPair() && !stop) {
			p = cSet.getNextPair();
			compares++;

			stop = ControlChecker.checkStop(batchJob, compares, INTERVAL);

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
			// Conditionally compute the time spent doing this comparison.
			long startTime = 0;
			if (getLogger().isLoggable(Level.FINE)) {
				startTime = System.currentTimeMillis();
			}
			match = compareRecords(q, m, p.isStage, stageModel, t);
			if (match != null) {
				matches.add(match);
			}
			if (getLogger().isLoggable(Level.FINE)) {
				startTime = System.currentTimeMillis() - startTime;
				inCompare += startTime;
			}

		}

		return matches;
	}

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

	protected final Record getM(ChunkDataStore dataStore, ComparisonPair p) {
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
	 */
	protected final IComparisonSetSource getSource(OabaJobMessage data,
			final int numProcessors, final int maxBlockSize,
			final int currentChunk) throws BlockingException {

		BatchJob job = getJobController().findBatchJob(data.jobID);

		final String _numRegularChunks =
			getPropertyController().getJobProperty(job,
					PN_REGULAR_CHUNK_FILE_COUNT);
		final int numRegularChunks = Integer.valueOf(_numRegularChunks);

		if (currentChunk < numRegularChunks) {
			// regular chunks
			final String _recordIdType =
				getPropertyController().getJobProperty(job, PN_RECORD_ID_TYPE);
			final RECORD_ID_TYPE recordIdType =
				RECORD_ID_TYPE.valueOf(_recordIdType);
			ComparisonTreeGroupSinkSourceFactory factory =
				OabaFileUtils.getComparisonTreeGroupFactory(job, recordIdType,
						numProcessors);
			IComparisonTreeSource source =
				factory.getSource(currentChunk, data.treeIndex);
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
			// over-sized chunks
			int i = currentChunk - numRegularChunks;
			ComparisonArrayGroupSinkSourceFactory factoryOS =
				OabaFileUtils.getComparisonArrayGroupFactoryOS(job,
						numProcessors);
			IComparisonArraySource sourceOS =
				factoryOS.getSource(i, data.treeIndex);
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
	 * This method compares two records and returns a MatchRecord2 object.
	 *
	 * @param q
	 *            - first record
	 * @param m
	 *            - second record
	 * @param isStage
	 *            - indicates if the second record is staging or master
	 */
	protected final MatchRecord2 compareRecords(Record q, Record m,
			boolean isStage, ImmutableProbabilityModel model,
			ImmutableThresholds t) {

		final ClueSet clueSet = model.getClueSet();
		final boolean[] enabledClues = model.getCluesToEvaluate();
		final float low = t.getDifferThreshold();
		final float high = t.getMatchThreshold();
		return MatchRecordUtils.compareRecords(clueSet, enabledClues, model, q,
				m, isStage, low, high);
	}

	/**
	 * This returns an unique id for each instance of the object.
	 */
	public String getID() {
		String str = this.toString();
		int i = str.indexOf('@');
		return str.substring(i + 1);
	}

}
