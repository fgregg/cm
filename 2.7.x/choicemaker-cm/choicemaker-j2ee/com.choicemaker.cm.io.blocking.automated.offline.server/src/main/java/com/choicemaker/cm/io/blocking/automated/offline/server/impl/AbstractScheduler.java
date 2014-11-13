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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * Common functionality of {@link MatcherScheduler2} and {@link TransMatchScheduler}.
 */
public abstract class AbstractScheduler implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;

	protected static final String DELIM = "|";

	protected abstract Logger getLogger();
	
	protected abstract Logger getJMSTrace();
	
	protected abstract EntityManager getEntityManager();

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

	public final void onMessage(Message inMessage) {
		getJMSTrace().info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		BatchJob batchJob = null;
		EJBConfiguration configuration = EJBConfiguration.getInstance();

		getLogger().fine("MatchScheduler2 In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof StartData) {
					final StartData sd = (StartData) o;

					// get the number of processors
					final long jobId = sd.jobID;
					batchJob =
						configuration.findBatchJobById(getEntityManager(), BatchJobBean.class,
								jobId);

					// init values
					BatchParameters params =
						configuration.findBatchParamsByJobId(getEntityManager(), jobId);
					final String modelConfigId =
						params.getModelConfigurationName();
					ImmutableProbabilityModel stageModel =
						PMManager.getModelInstance(modelConfigId);
					String temp =
						(String) stageModel.properties().get("numProcessors");
					numProcessors = Integer.parseInt(temp);

					temp = (String) stageModel.properties().get("maxChunkSize");
					maxChunkSize = Integer.parseInt(temp);

					countMessages = 0;

					OabaProcessing status =
						configuration.getProcessingLog(getEntityManager(), sd);
					if (status.getCurrentProcessingEventId() >= OabaProcessing.EVT_DONE_MATCHING_DATA) {
						// matching is already done, so go on to the next step.
						nextSteps(sd);
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

				} else if (o instanceof MatchWriterData) {
					final MatchWriterData mwd = (MatchWriterData) o;
					handleNextChunk(mwd);
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
	protected final void handleNextChunk(MatchWriterData mwd)
			throws BlockingException {

		OABAConfiguration oabaConfig = new OABAConfiguration(mwd.jobID);
		EJBConfiguration configuration = EJBConfiguration.getInstance();
		BatchJob batchJob =
			configuration.findBatchJobById(getEntityManager(), BatchJobBean.class, mwd.jobID);
		StartData sd = new StartData(mwd);
		OabaProcessing status = configuration.getProcessingLog(getEntityManager(), sd);

		// keeping track of messages sent and received.
		countMessages--;

		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob(batchJob, status, oabaConfig);

		} else if (!BatchJob.STATUS_ABORTED.equals(batchJob.getStatus())) {
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
				status.setCurrentProcessingEvent(OabaEvent.MATCHING_DATA, temp);

				getLogger().info("Chunk " + mwd.ind + " is done.");

				currentChunk++;

				if (currentChunk < mwd.numChunks) {
					startChunk(sd, currentChunk);
				} else {
					// all the chunks are done
					status.setCurrentProcessingEvent(OabaEvent.DONE_MATCHING_DATA);

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

					nextSteps(sd);
				}
			} // end countMessages == 0
		} // end if abort requested
	}

	/**
	 * This method is called when all the chunks are done.
	 */
	protected final void nextSteps(final StartData sd) throws BlockingException {
		cleanUp(sd);
		sendToUpdateStatus(sd.jobID, 90);
		sendToMatchDebup(sd);
	}

	/**
	 * This method sends the different chunks to different beans.
	 */
	protected final void startMatch(final StartData sd) throws RemoteException, FinderException,
			BlockingException, NamingException, JMSException, XmlConfException {

		// init values
		OABAConfiguration oabaConfig = new OABAConfiguration(sd.jobID);
		EJBConfiguration configuration = EJBConfiguration.getInstance();
		OabaProcessing status = configuration.getProcessingLog(getEntityManager(), sd);
		BatchJob batchJob =
			configuration.findBatchJobById(getEntityManager(), BatchJobBean.class, sd.jobID);

		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob(batchJob, status, oabaConfig);

		} else {
			currentChunk = 0;
			if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_MATCHING_DATA) {
				currentChunk = recover(sd, status) + 1;
				getLogger().info("recovering from " + currentChunk);
			}

			// set up the record source arrays.
			IChunkDataSinkSourceFactory stageFactory =
				oabaConfig.getStageDataFactory();
			IChunkDataSinkSourceFactory masterFactory =
				oabaConfig.getMasterDataFactory();

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

	protected final int recover(StartData sd, OabaProcessing status) throws BlockingException {
		StringTokenizer stk =
			new StringTokenizer(status.getAdditionalInfo(), DELIM);
		sd.numChunks = Integer.parseInt(stk.nextToken());
		sd.numRegularChunks = Integer.parseInt(stk.nextToken());
		return Integer.parseInt(stk.nextToken());
	}

	/**
	 * This is a special case when TE is not needed, because all the match
	 * graphs are size 2 or 0.
	 * 
	 */
	protected final void noChunk(final StartData sd) throws XmlConfException, BlockingException,
			NamingException, JMSException {

		// This is because tree ids start with 1 and not 0.
		OABAConfiguration oabaConfig = new OABAConfiguration(sd.jobID);
		for (int i = 1; i <= numProcessors; i++) {
			@SuppressWarnings("rawtypes")
			IMatchRecord2Sink mSink =
				oabaConfig.getMatchChunkFactory().getSink(i);
			mSink.open();
			mSink.close();
			getLogger().fine("creating " + mSink.getInfo());
		}

		nextSteps(sd);
	}

	/**
	 * This method sends messages out to matchers beans to work on the current
	 * chunk.
	 */
	protected final void startChunk(final StartData sd, int currentChunk) throws BlockingException {

		getLogger().fine("startChunk " + currentChunk);

		final long jobId = sd.jobID;
		EJBConfiguration configuration = EJBConfiguration.getInstance();
		BatchJob batchJob =
				configuration.findBatchJobById(getEntityManager(), BatchJobBean.class,
						jobId);
		BatchParameters params =
			configuration.findBatchParamsByJobId(getEntityManager(), jobId);
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
				batchJob);

		t = System.currentTimeMillis() - t;
		this.timeReadData += t;

		MemoryEstimator.writeMem();

		// send messages to matcher
		sd.ind = currentChunk;

		// This is because tree ids start with 1 and not 0.
		for (int i = 1; i <= numProcessors; i++) {
			StartData sd2 = new StartData(sd);
			sd.treeInd = i;
			countMessages++;
			sendToMatcher(sd2);
		}
	}

	protected abstract void cleanUp(StartData sd) throws BlockingException;

	protected abstract void sendToMatcher(StartData sd);

	protected abstract void sendToUpdateStatus(long jobID, int percentComplete);

	protected abstract void sendToMatchDebup(StartData sd);

}
