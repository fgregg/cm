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
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;
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
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * Common functionality of {@link MatcherScheduler2} and {@link TransMatchScheduler}.
 */
public abstract class AbstractScheduler implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;

	protected static final String DELIM = "|";

	protected abstract Logger getLogger();
	
	protected abstract Logger getJMSTrace();
	
	protected abstract OabaJobControllerBean getJobController();
	
	protected abstract OabaParametersControllerBean getParametersController();
	
	protected abstract OabaProcessingController getProcessingController();
	
	protected abstract ServerConfigurationController getServerController();
	
	protected abstract OabaSettingsController getSettingsController();

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

	// maxchunk
	protected int maxChunkSize;

	public /* final */ void onMessage(Message inMessage) {
		getJMSTrace().info("Entering onMessage for " + this.getClass().getName());
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
					ServerConfiguration serverConfig = getServerController().findServerConfigurationByJobId(jobId);
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
		OabaEventLog status = getProcessingController().getProcessingLog(oabaJob);

		// keeping track of messages sent and received.
		countMessages--;
		getLogger().info("outstanding messages: " + countMessages);

		if (BatchJobStatus.ABORT_REQUESTED == oabaJob.getStatus()) {
			MessageBeanUtils.stopJob(oabaJob, status);

		} else if (BatchJobStatus.ABORTED != oabaJob.getStatus()) {
			// if there are multiple processors, we have don't do anything for
			// STATUS_ABORTED.

			// getting information that a segment is done
			numCompares += mwd.numCompares;
			numMatches += mwd.numMatches;

			// update time trackers
			if (getLogger().isLoggable(Level.FINE)) {
				timeWriting[mwd.treeInd - 1] += mwd.timeWriting;
				inHMLookUp[mwd.treeInd - 1] += mwd.inLookup;
				inCompare[mwd.treeInd - 1] += mwd.inCompare;
			}

			getLogger().info("Chunk " + mwd.ind + " tree " + mwd.treeInd + " is done.");

			// Go on to the next chunk
			if (countMessages == 0) {
				String temp =
					Integer.toString(sd.numChunks) + DELIM
							+ Integer.toString(sd.numRegularChunks) + DELIM
							+ Integer.toString(currentChunk);
				status.setCurrentOabaEvent(OabaEvent.MATCHING_DATA, temp);

				getLogger().info("Chunk " + mwd.ind + " is done.");

				currentChunk++;

				if (currentChunk < mwd.numChunks) {
					startChunk(sd, currentChunk);
				} else {
					// all the chunks are done
					status.setCurrentOabaEvent(OabaEvent.DONE_MATCHING_DATA);

					getLogger().info("total comparisons: " + numCompares
							+ " total matches: " + numMatches);
					timeStart = System.currentTimeMillis() - timeStart;
					getLogger().info("total matching time: " + timeStart);
					getLogger().info("total reading data time: " + timeReadData);
					getLogger().info("total garbage collection time: " + timegc);

					// writing out time break downs
					if (getLogger().isLoggable(Level.FINE)) {
						for (int i = 0; i < numProcessors; i++) {
							getLogger().fine("Processor " + i + " writing time: "
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
	protected final void nextSteps(final OabaJob job, OabaJobMessage sd) throws BlockingException {
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
			MessageBeanUtils.stopJob(oabaJob, processingLog);

		} else {
			currentChunk = 0;
			if (processingLog.getCurrentOabaEventId() == OabaProcessing.EVT_MATCHING_DATA) {
				currentChunk = recover(sd, processingLog) + 1;
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

			stageRS = new RecordSource[sd.numChunks];
			masterRS = new RecordSource[sd.numChunks];

			for (int i = 0; i < sd.numChunks; i++) {
				stageRS[i] = stageFactory.getNextSource();
				masterRS[i] = masterFactory.getNextSource();
			}

			if (sd.numChunks > 0) {
				startChunk(sd, currentChunk);
			} else {
				// special case of nothing to do, except to clean up
				getLogger().info("No matching chunk found.");
				noChunk(sd);
			}
		}
	}

	protected final int recover(OabaJobMessage sd, OabaEventLog status) throws BlockingException {
		StringTokenizer stk =
			new StringTokenizer(status.getCurrentOabaEventInfo(), DELIM);
		sd.numChunks = Integer.parseInt(stk.nextToken());
		sd.numRegularChunks = Integer.parseInt(stk.nextToken());
		return Integer.parseInt(stk.nextToken());
	}

	/**
	 * This is a special case when TE is not needed, because all the match
	 * graphs are size 2 or 0.
	 * 
	 */
	protected final void noChunk(final OabaJobMessage sd) throws XmlConfException, BlockingException,
			NamingException, JMSException {
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
	protected final void startChunk(final OabaJobMessage sd, int currentChunk) throws BlockingException {

		getLogger().fine("startChunk " + currentChunk);

		final long jobId = sd.jobID;
		OabaJob oabaJob = getJobController().findOabaJob(jobId);
		OabaParameters params =
			getParametersController().findBatchParamsByJobId(jobId);
		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);

		// call to garbage collection
		long t = System.currentTimeMillis();
		ChunkDataStore dataStore = ChunkDataStore.getInstance();
		dataStore.cleanUp();
		System.gc();
		t = System.currentTimeMillis() - t;
		this.timegc += t;

		// read in the data;
		t = System.currentTimeMillis();
		dataStore.init(stageRS[currentChunk], model, masterRS[currentChunk], maxChunkSize,
				oabaJob);

		t = System.currentTimeMillis() - t;
		this.timeReadData += t;

		MemoryEstimator.writeMem();

		// send messages to matcher
		sd.ind = currentChunk;

		// This is because tree ids start with 1 and not 0.
		for (int i = 1; i <= numProcessors; i++) {
			OabaJobMessage sd2 = new OabaJobMessage(sd);
			sd2.treeInd = i;
			countMessages++;
			sendToMatcher(sd2);
			getLogger().info("outstanding messages: " + countMessages);
		}
	}

	protected abstract void cleanUp(OabaJob job, OabaJobMessage sd) throws BlockingException;

	protected abstract void sendToMatcher(OabaJobMessage sd);

	protected abstract void sendToUpdateStatus(OabaJob job, OabaEvent event,
			Date timestamp, String info);

	protected abstract void sendToMatchDebup(OabaJob job, OabaJobMessage sd);

}
