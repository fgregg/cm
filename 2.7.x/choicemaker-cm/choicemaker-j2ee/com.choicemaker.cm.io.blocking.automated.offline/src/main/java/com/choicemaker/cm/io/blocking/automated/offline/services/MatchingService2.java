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
package com.choicemaker.cm.io.blocking.automated.offline.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockMatcher2;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * This service object handles the matching of blocks in each chunk.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class MatchingService2 {

	private static final Logger log = Logger.getLogger(MatchingService2.class
			.getName());

	private IChunkDataSinkSourceFactory stageFactory;
	private IChunkDataSinkSourceFactory masterFactory;
	private IComparisonArraySinkSourceFactory cgFactory;
	private ImmutableProbabilityModel stageModel;
	private ImmutableProbabilityModel masterModel;
	private IMatchRecord2Sink mSink;
	private IBlockMatcher2 matcher;
	private float low;
	private float high;
	private int maxBlockSize;
	private ProcessingEventLog status;

	private int numChunks;

	// book keeping
	private long numBlocks = 0;
	private long numCompares = 0;
	private long numMatches = 0;

	private long inReadHM = 0;
	private long inHandleBlocks = 0;
	private long inWriteMatches = 0;

	private ArrayList cgSources = new ArrayList();
	private ArrayList stageSources = new ArrayList();
	private ArrayList masterSources = new ArrayList();

	private long time; // this keeps track of time

	/**
	 * This constructor takes these parameters:
	 * 
	 * @param stageFactory
	 *            - factory containing info on how to get staging chunk data
	 *            files
	 * @param masterFactory
	 *            - factory containing info on how to get master chunk data
	 *            files
	 * @param cgFactory
	 *            - factory containing info on how to get comparison groups
	 * @param stageModel
	 *            - probability accessProvider of the staging records
	 * @param masterModel
	 *            - probability accessProvider of the master records
	 * @param mSink
	 *            - matching pair sink
	 * @param matcher
	 *            - the object that performs the actual matching
	 * @param low
	 *            - differ threshold
	 * @param high
	 *            - match threshold
	 * @param validator
	 *            - determines if a pair for comparison is valid
	 * @param maxBlockSize
	 *            - maximum size of a regular block. blocks of size >
	 *            maxBlockSize is an oversized block.
	 */
	public MatchingService2(IChunkDataSinkSourceFactory stageFactory,
			IChunkDataSinkSourceFactory masterFactory,
			IComparisonArraySinkSourceFactory cgFactory,
			ImmutableProbabilityModel stageModel,
			ImmutableProbabilityModel masterModel, IMatchRecord2Sink mSink,
			IBlockMatcher2 matcher, float low, float high, int maxBlockSize,
			ProcessingEventLog status) {

		this.stageFactory = stageFactory;
		this.masterFactory = masterFactory;
		this.cgFactory = cgFactory;
		this.stageModel = stageModel;
		this.masterModel = masterModel;
		this.mSink = mSink;
		this.matcher = matcher;

		this.low = low;
		this.high = high;
		this.maxBlockSize = maxBlockSize;

		this.status = status;
	}

	/**
	 * This method runs the service.
	 * 
	 * @throws IOException
	 */
	public void runService() throws BlockingException, XmlConfException {
		time = System.currentTimeMillis();

		if (status.getCurrentProcessingEventId() >= OabaProcessing.EVT_DONE_MATCHING_DATA) {
			// do nothing

		} else if (status.getCurrentProcessingEventId() >= OabaProcessing.EVT_DONE_CREATE_CHUNK_DATA
				&& status.getCurrentProcessingEventId() <= OabaProcessing.EVT_DONE_ALLOCATE_CHUNKS) {

			numChunks = Integer.parseInt(status.getCurrentProcessingEventInfo());

			// start matching
			log.info("start matching, number of chunks " + numChunks);

			init();

			startMatching(0);

		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_MATCHING_DATA) {
			// recovery mode
			String temp = status.getCurrentProcessingEventInfo();
			int ind = temp.indexOf(OabaProcessing.DELIMIT);
			numChunks = Integer.parseInt(temp.substring(0, ind));
			int startPoint = Integer.parseInt(temp.substring(ind + 1)) + 1;

			log.info("start recovery, chunks " + numChunks
					+ ", starting point " + startPoint);

			init();
			startMatching(startPoint);

		}
		time = System.currentTimeMillis() - time;
	}

	/**
	 * This method returns the time it takes to run the runService method.
	 * 
	 * @return long - returns the time (in milliseconds) it took to run this
	 *         service.
	 */
	public long getTimeElapsed() {
		return time;
	}

	/**
	 * This method performs the initialization
	 * 
	 *
	 */
	private void init() throws XmlConfException, BlockingException {
		if (stageFactory.getNumSink() == 0) {
			// initialize this thing
			for (int i = 0; i < numChunks; i++) {
				stageFactory.getNextSink();
				masterFactory.getNextSink();
			}
		}
		for (int i = 0; i < numChunks; i++) {
			stageSources.add(stageFactory.getNextSource());
			masterSources.add(masterFactory.getNextSource());
		}

		if (cgSources.size() == 0) {
			for (int i = 0; i < numChunks; i++) {
				cgSources.add(cgFactory.getNextSource());
			}
		}
	}

	/**
	 * This method starts the matching process.
	 * 
	 * @param startPoint
	 *            - which chunk file to start matching on.
	 */
	private void startMatching(int startPoint) throws BlockingException,
			XmlConfException {
		for (int i = startPoint; i < numChunks; i++) {
			IComparisonArraySource cgSource =
				(IComparisonArraySource) cgSources.get(i);
			RecordSource stage = (RecordSource) stageSources.get(i);
			RecordSource master = (RecordSource) masterSources.get(i);

			log.info("matching " + cgSource.getInfo());
			MemoryEstimator.writeMem();

			matcher.matchBlocks(cgSource, stageModel, masterModel, stage,
					master, mSink, true, low, high, maxBlockSize);

			int b = matcher.getNumBlocks();
			int c = matcher.getNumComparesMade();
			int m = matcher.getNumMatches();

			numBlocks += b;
			numCompares += c;
			numMatches += m;

			inReadHM += matcher.getTimeInReadMaps();
			inHandleBlocks += matcher.getTimeInHandleBlock();
			inWriteMatches += matcher.getTimeInWriteMatches();

			log.info("blocks: " + b + " comparisons: " + c + " matches: " + m);

			double cps =
				1000.0
						* c
						/ (matcher.getTimeInReadMaps()
								+ matcher.getTimeInHandleBlock() + matcher
									.getTimeInWriteMatches());
			log.info("comparisons per second " + cps);

			// log the status
			String temp =
				Integer.toString(numChunks) + OabaProcessing.DELIMIT
						+ Integer.toString(i);
			status.setCurrentProcessingEvent(OabaProcessingEvent.MATCHING_DATA, temp);

			// clean up
			stage = null;
			master = null;
			cgSource = null;
		}

		log.info("total blocks: " + numBlocks + " comparisons: " + numCompares
				+ " matches: " + numMatches);
		log.info("Time in readMaps " + inReadHM);
		log.info("Time in handleBlocks " + inHandleBlocks);
		log.info("Time in writeMatches " + inWriteMatches);

		double cps =
			1000.0 * numCompares / (inReadHM + inHandleBlocks + inWriteMatches);
		log.info("comparisons per second " + cps);

		status.setCurrentProcessingEvent(OabaProcessingEvent.DONE_MATCHING_DATA);

		// cleanup
		stageFactory.removeAllSinks();
		masterFactory.removeAllSinks();
		for (int i = 0; i < numChunks; i++) {
			((IComparisonArraySource) cgSources.get(i)).delete();
		}
	}

}
