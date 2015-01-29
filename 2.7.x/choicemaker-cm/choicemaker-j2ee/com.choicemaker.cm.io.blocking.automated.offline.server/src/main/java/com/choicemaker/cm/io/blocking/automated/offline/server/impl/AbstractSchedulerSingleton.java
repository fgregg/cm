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

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_CURRENT_CHUNK_INDEX;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_REGULAR_CHUNK_FILE_COUNT;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.FinderException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * Common functionality of {@link MatcherScheduler2} and
 * {@link TransMatchScheduler}. This class is implemented as a Singleton EJB,
 * not an MDB, because it must retain data in memory between invocations of
 * <code>onMessage</code>.
 */
public abstract class AbstractSchedulerSingleton implements Serializable {

	private static final long serialVersionUID = 271L;

	// FIXME REMOVEME (after operational properties are completed)
	protected static final String DELIM = "|";

	// -- Injected data

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OperationalPropertyController propertyController;

	@EJB
	private OabaProcessingController processingController;

	// -- Session data

	protected RecordSource[] stageRS = null;

	protected RecordSource[] masterRS = null;

	// This counts the number of messages sent to matcher and number of done
	// messages got back.
	protected int countMessages;

	// this indicates which chunks is currently being processed.
	protected int currentChunk = -1;

	protected long numCompares;

	protected long numMatches;

	protected long currentJobID = -1;

	// time trackers
	protected long timeStart;
	protected long timeReadData;
	protected long timegc;

	// array size = number of processors
	// these time tracker are active only in getLogger() debug
	protected long[] timeWriting;
	protected long[] inHMLookUp;
	protected long[] inCompare;

	// number of processors to use
	protected int numProcessors;

	// max chunk
	protected int maxChunkSize;

	// -- Callbacks

	protected abstract Logger getLogger();

	protected abstract Logger getJMSTrace();

	protected abstract void cleanUp(OabaJob job, OabaJobMessage sd)
			throws BlockingException;

	protected abstract void sendToMatcher(OabaJobMessage sd);

	protected abstract void sendToUpdateStatus(OabaJob job, OabaEvent event,
			Date timestamp, String info);

	protected abstract void sendToMatchDebup(OabaJob job, OabaJobMessage sd);

	// -- Accessors

	protected OabaJobControllerBean getJobController() {
		return jobController;
	}

	protected OabaParametersControllerBean getParametersController() {
		return paramsController;
	}

	protected ServerConfigurationController getServerController() {
		return serverController;
	}

	protected OabaSettingsController getSettingsController() {
		return oabaSettingsController;
	}

	protected OperationalPropertyController getPropertyController() {
		return propertyController;
	}

	protected OabaProcessingController getProcessingController() {
		return processingController;
	}

	// -- Message processing

	public void onMessage(Message inMessage) {
		getJMSTrace().info(
				"Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJob oabaJob = null;

		getLogger().fine("MatchSchedulerMDB In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof OabaJobMessage) {
					final OabaJobMessage sd = (OabaJobMessage) o;
					final long jobId = sd.jobID;
					oabaJob = getJobController().findOabaJob(jobId);
					OabaParameters params =
						getParametersController().findBatchParamsByJobId(jobId);
					OabaSettings oabaSettings =
						getSettingsController().findOabaSettingsByJobId(jobId);
					ServerConfiguration serverConfig =
						getServerController().findServerConfigurationByJobId(
								jobId);
					if (oabaJob == null || params == null
							|| oabaSettings == null || serverConfig == null) {
						String s =
							"Unable to find a job, parameters, settings or server configuration for "
									+ jobId;
						getLogger().severe(s);
						throw new IllegalArgumentException(s);
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

					countMessages = 0;
					maxChunkSize = oabaSettings.getMaxChunkSize();
					numProcessors = serverConfig.getMaxChoiceMakerThreads();
					getLogger().info("Maximum chunk size: " + maxChunkSize);
					getLogger().info("Number of processors: " + numProcessors);

					OabaEventLog processingLog =
						getProcessingController().getProcessingLog(oabaJob);
					if (processingLog.getCurrentOabaEventId() >= OabaProcessing.EVT_DONE_MATCHING_DATA) {
						// matching is already done, so go on to the next step.
						nextSteps(oabaJob, sd);
					} else {
						if (sd.jobID != currentJobID) {
							// reset counters
							numCompares = 0;
							numMatches = 0;
							currentChunk = -1;
							currentJobID = sd.jobID;
							timeStart = System.currentTimeMillis();
							timeReadData = 0;
							timegc = 0;

							timeWriting = new long[numProcessors];
							inCompare = new long[numProcessors];
							inHMLookUp = new long[numProcessors];
						}

						// start matching
						startMatch(sd);
					}

				} else if (o instanceof MatchWriterMessage) {
					final MatchWriterMessage mwd = (MatchWriterMessage) o;
					handleNextChunk(mwd);
				}

			} else {
				getLogger().warning(
						"wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			getLogger().severe(e.toString());
			if (oabaJob != null) {
				oabaJob.markAsFailed();
			}
			// mdc.setRollbackOnly();
		}
		getJMSTrace()
				.info("Exiting onMessage for " + this.getClass().getName());
	}

	/**
	 * This method is called when a chunk is done and the system is ready for
	 * the next chunk.
	 * 
	 * It tabulates the statistics from the chunk that just finished and it
	 * starts the next available chunk.
	 * 
	 * @param mwd
	 *            - the message data from the chunk that just finished.
	 * @throws RemoteException
	 * @throws FinderException
	 * @throws XmlConfException
	 * @throws BlockingException
	 * @throws NamingException
	 * @throws JMSException
	 */
	protected final void handleNextChunk(MatchWriterMessage mwd)
			throws BlockingException {

		final long jobId = mwd.jobID;
		OabaJob oabaJob = getJobController().findOabaJob(jobId);
		OabaJobMessage sd = new OabaJobMessage(mwd);
		OabaEventLog status =
			getProcessingController().getProcessingLog(oabaJob);

		// keeping track of messages sent and received.
		countMessages--;
		getLogger().info("outstanding messages: " + countMessages);

		if (BatchJobStatus.ABORT_REQUESTED == oabaJob.getStatus()) {
			MessageBeanUtils.stopJob(oabaJob, getPropertyController(), status);

		} else if (BatchJobStatus.ABORTED != oabaJob.getStatus()) {
			// if there are multiple processors, we have don't do anything for
			// STATUS_ABORTED.

			// getting information that a segment is done
			numCompares += mwd.numCompares;
			numMatches += mwd.numMatches;

			// update time trackers
			if (getLogger().isLoggable(Level.FINE)) {
				timeWriting[mwd.treeIndex - 1] += mwd.timeWriting;
				inHMLookUp[mwd.treeIndex - 1] += mwd.inLookup;
				inCompare[mwd.treeIndex - 1] += mwd.inCompare;
			}

			final String _latestChunkProcessed =
				getPropertyController().getJobProperty(oabaJob,
						PN_CHUNK_FILE_COUNT);
			final int latestChunkProcessed =
				Integer.valueOf(_latestChunkProcessed);
			getLogger().info(
					"Chunk " + latestChunkProcessed + " tree " + mwd.treeIndex
							+ " is done.");
			assert latestChunkProcessed == currentChunk;

			// Go on to the next chunk
			if (countMessages == 0) {
				final String _numChunks =
					getPropertyController().getJobProperty(oabaJob,
							PN_CHUNK_FILE_COUNT);
				final int numChunks = Integer.valueOf(_numChunks);

				final String _numRegularChunks =
					getPropertyController().getJobProperty(oabaJob,
							PN_REGULAR_CHUNK_FILE_COUNT);
				final int numRegularChunks = Integer.valueOf(_numRegularChunks);

				String temp =
					Integer.toString(numChunks) + DELIM
							+ Integer.toString(numRegularChunks) + DELIM
							+ Integer.toString(currentChunk);
				status.setCurrentOabaEvent(OabaEvent.MATCHING_DATA, temp);

				getLogger().info("Chunk " + latestChunkProcessed + " is done.");

				currentChunk++;

				if (currentChunk < numChunks) {
					startChunk(sd, currentChunk);
				} else {
					// all the chunks are done
					status.setCurrentOabaEvent(OabaEvent.DONE_MATCHING_DATA);

					getLogger().info(
							"total comparisons: " + numCompares
									+ " total matches: " + numMatches);
					timeStart = System.currentTimeMillis() - timeStart;
					getLogger().info("total matching time: " + timeStart);
					getLogger()
							.info("total reading data time: " + timeReadData);
					getLogger()
							.info("total garbage collection time: " + timegc);

					// writing out time break downs
					if (getLogger().isLoggable(Level.FINE)) {
						for (int i = 0; i < numProcessors; i++) {
							getLogger().fine(
									"Processor " + i + " writing time: "
											+ timeWriting[i] + " lookup time: "
											+ inHMLookUp[i] + " compare time: "
											+ inCompare[i]);
						}
					}

					nextSteps(oabaJob, sd);
				}
			} // end countMessages == 0
		} // end if abort requested
	}

	/**
	 * This method is called when all the chunks are done.
	 */
	protected final void nextSteps(final OabaJob job, OabaJobMessage sd)
			throws BlockingException {
		cleanUp(job, sd);
		sendToUpdateStatus(job, OabaEvent.DONE_MATCHING_DATA, new Date(), null);
		sendToMatchDebup(job, sd);
	}

	/**
	 * This method sends the different chunks to different beans.
	 */
	protected final void startMatch(final OabaJobMessage sd)
			throws RemoteException, FinderException, BlockingException,
			NamingException, JMSException, XmlConfException {

		// init values
		final long jobId = sd.jobID;
		OabaJob oabaJob = getJobController().findOabaJob(jobId);
		OabaEventLog processingLog =
			getProcessingController().getProcessingLog(oabaJob);

		if (BatchJobStatus.ABORT_REQUESTED == oabaJob.getStatus()) {
			MessageBeanUtils.stopJob(oabaJob, getPropertyController(),
					processingLog);

		} else {
			currentChunk = 0;
			if (processingLog.getCurrentOabaEventId() == OabaProcessing.EVT_MATCHING_DATA) {
				currentChunk = recover(oabaJob, sd, processingLog) + 1;
				getLogger().info("recovering from " + currentChunk);
			}

			// set up the record source arrays.
			OabaParameters oabaParams =
				getParametersController().findBatchParamsByJobId(jobId);
			String modelName = oabaParams.getModelConfigurationName();
			ImmutableProbabilityModel ipm =
				PMManager.getImmutableModelInstance(modelName);
			IChunkDataSinkSourceFactory stageFactory =
				OabaFileUtils.getStageDataFactory(oabaJob, ipm);
			IChunkDataSinkSourceFactory masterFactory =
				OabaFileUtils.getMasterDataFactory(oabaJob, ipm);

			final String _numChunks =
				getPropertyController().getJobProperty(oabaJob,
						PN_CHUNK_FILE_COUNT);
			final int numChunks = Integer.valueOf(_numChunks);

			stageRS = new RecordSource[numChunks];
			masterRS = new RecordSource[numChunks];

			for (int i = 0; i < numChunks; i++) {
				stageRS[i] = stageFactory.getNextSource();
				masterRS[i] = masterFactory.getNextSource();
			}

			if (numChunks > 0) {
				startChunk(sd, currentChunk);
			} else {
				// special case of nothing to do, except to clean up
				getLogger().info("No matching chunk found.");
				noChunk(sd);
			}
		}
	}

	protected final int recover(OabaJob oabaJob, OabaJobMessage sd,
			OabaEventLog status) throws BlockingException {

		StringTokenizer stk =
			new StringTokenizer(status.getCurrentOabaEventInfo(), DELIM);

		final int numChunks = Integer.parseInt(stk.nextToken());
		getLogger().info("Number of chunks " + numChunks);
		getPropertyController().setJobProperty(oabaJob, PN_CHUNK_FILE_COUNT,
				String.valueOf(numChunks));

		final int numRegularChunks = Integer.parseInt(stk.nextToken());
		getLogger().info("Number of regular chunks " + numChunks);
		getPropertyController().setJobProperty(oabaJob,
				PN_REGULAR_CHUNK_FILE_COUNT, String.valueOf(numRegularChunks));

		int currentChunk = Integer.parseInt(stk.nextToken());
		return currentChunk;
	}

	/**
	 * This is a special case when TE is not needed, because all the match
	 * graphs are size 2 or 0.
	 * 
	 */
	protected final void noChunk(final OabaJobMessage sd)
			throws XmlConfException, BlockingException, NamingException,
			JMSException {
		final long jobId = sd.jobID;
		OabaJob oabaJob = getJobController().findOabaJob(jobId);

		// This is because tree ids start with 1 and not 0.
		for (int i = 1; i <= numProcessors; i++) {
			@SuppressWarnings("rawtypes")
			IMatchRecord2Sink mSink =
				OabaFileUtils.getMatchChunkFactory(oabaJob).getSink(i);
			mSink.open();
			mSink.close();
			getLogger().fine("creating " + mSink.getInfo());
		}

		nextSteps(oabaJob, sd);
	}

	/**
	 * This method sends messages out to matchers beans to work on the current
	 * chunk.
	 */
	protected final void startChunk(final OabaJobMessage sd,
			final int currentChunk) throws BlockingException {

		getLogger().fine("startChunk " + currentChunk);

		final long jobId = sd.jobID;
		final OabaJob oabaJob = getJobController().findOabaJob(jobId);
		final OabaParameters params =
			getParametersController().findBatchParamsByJobId(jobId);
		final String modelConfigId = params.getModelConfigurationName();
		final ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);

		getLogger().info("Current chunk " + currentChunk);
		getPropertyController().setJobProperty(oabaJob, PN_CURRENT_CHUNK_INDEX,
				String.valueOf(currentChunk));

		// call to garbage collection
		long t = System.currentTimeMillis();
		ChunkDataStore dataStore = ChunkDataStore.getInstance();
		dataStore.cleanUp();
		System.gc();
		t = System.currentTimeMillis() - t;
		this.timegc += t;

		// read in the data;
		t = System.currentTimeMillis();
		dataStore.init(stageRS[currentChunk], model, masterRS[currentChunk],
				maxChunkSize, oabaJob);

		t = System.currentTimeMillis() - t;
		this.timeReadData += t;

		MemoryEstimator.writeMem();

		// Send messages to matchers. Matcher indices are one-based.
		for (int i = 1; i <= numProcessors; i++) {
			OabaJobMessage sd2 = new OabaJobMessage(sd);
			sd2.treeIndex = i;
			countMessages++;
			sendToMatcher(sd2);
			getLogger().info("outstanding messages: " + countMessages);
		}
	}

}
