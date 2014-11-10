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
import java.util.HashMap;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSources;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * This service object handles the matching of blocks in each chunk.
 *
 * This version is more abstracted and takes in IComparisonSet for regular and oversized blocks.
 * This version doesn't use IBlockMatcher2, because the pair information is captured in
 * IComparisonSetSource.
 *
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MatchingService3 {

	private static final Logger log = Logger.getLogger(MatchingService3.class.getName());

	private static int BUFFER_SIZE = 1000;

	private IChunkDataSinkSourceFactory stageFactory;
	private IChunkDataSinkSourceFactory masterFactory;
	private IProbabilityModel model;
	private IComparisonSetSources sources;
	private IComparisonSetSources Osources;

	private IMatchRecord2Sink mSink;
	private float low;
	private float high;
//	private int maxBlockSize;
	private OabaProcessing status;

	private int numChunks;

	//book keeping
	private long numSets = 0;
	private long compares = 0;
	private long numMatches = 0;

	private long inReadHM = 0;
	private long inHandleBlocks = 0;
	private long inWriteMatches = 0;

	private ArrayList stageSources = new ArrayList ();
	private ArrayList masterSources = new ArrayList ();

	private long time; //this keeps track of time


	private Evaluator evaluator;
	private ClueSet clueSet;
	private boolean[] enabledClues;


	/** This constructor takes these parameters:
	 *
	 * @param stageFactory - factory containing info on how to get staging chunk data files
	 * @param masterFactory - factory containing info on how to get master chunk data files
	 * @param sources - source of comparison sets for regular blocks
	 * @param Osources - source of comparison sets for oversized blocks
	 * @param model - probability accessProvider of the staging records
	 * @param mSink - matching pair sink
	 * @param low - differ threshold
	 * @param high - match threshold
	 * @param validator - determines if a pair for comparison is valid
	 * @param maxBlockSize - maximum size of a regular block.  blocks of size > maxBlockSize is an
	 * 		oversized block.
	 */
	public MatchingService3(IChunkDataSinkSourceFactory stageFactory,
			IChunkDataSinkSourceFactory masterFactory,
			IComparisonSetSources sources, IComparisonSetSources Osources,
			IProbabilityModel model, IMatchRecord2Sink mSink, float low,
			float high, int maxBlockSize, OabaProcessing status) {

		this.stageFactory = stageFactory;
		this.masterFactory = masterFactory;
		this.sources = sources;
		this.Osources = Osources;
		this.model = model;
		this.mSink = mSink;

		this.low = low;
		this.high = high;
//		this.maxBlockSize = maxBlockSize;

		this.status = status;

		//set up the clues and evaluators.
		this.evaluator = model.getEvaluator();
		this.clueSet = model.getClueSet();
		this.enabledClues = model.getCluesToEvaluate();

	}


	/** This method runs the service.
	 *
	 * @throws IOException
	 */
	public void runService () throws BlockingException, XmlConfException {
		time = System.currentTimeMillis();

		if (status.getCurrentProcessingEventId() >= OabaProcessing.EVT_DONE_MATCHING_DATA ) {
			//do nothing

		} else if (status.getCurrentProcessingEventId() >= OabaProcessing.EVT_DONE_CREATE_CHUNK_DATA  &&
			status.getCurrentProcessingEventId() <= OabaProcessing.EVT_DONE_ALLOCATE_CHUNKS ) {

			numChunks = Integer.parseInt( status.getAdditionalInfo() );

			//start matching
			log.info ("start matching, number of chunks " + numChunks);

			init ();
			mSink.open();
			startMatching (0);
			mSink.close();

		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_MATCHING_DATA ) {
			//recovery mode
			String temp =  status.getAdditionalInfo();
			int ind = temp.indexOf( OabaProcessing.DELIMIT);
			numChunks = Integer.parseInt( temp.substring(0,ind) );
			int startPoint = Integer.parseInt( temp.substring(ind + 1)) + 1;

			log.info ("start recovery, chunks " + numChunks + ", starting point " + startPoint);

			init ();
			mSink.append();
			startMatching (startPoint);
			mSink.close();

		}
		time = System.currentTimeMillis() - time;
	}


	/** This method returns the time it takes to run the runService method.
	 *
	 * @return long - returns the time (in milliseconds) it took to run this service.
	 */
	public long getTimeElapsed () { return time; }


	/** This method performs the initialization
	 *
	 *
	 */
	private void init () throws XmlConfException, BlockingException {
		if (stageFactory.getNumSink() == 0) {
			//initialize this thing
			for (int i=0; i< numChunks; i++) {
				stageFactory.getNextSink();
				masterFactory.getNextSink();
			}
		}
		for (int i=0; i< numChunks; i++) {
			stageSources.add(stageFactory.getNextSource());
			masterSources.add(masterFactory.getNextSource());
		}
	}


	/** This method starts the matching process.
	 *
	 * @param startPoint - which chunk file to start matching on.
	*/
	private void startMatching (int startPoint) throws BlockingException, XmlConfException {
		//recovering, so skip over the first startPoint files.
		for (int i=0; i<startPoint; i++) {
			// 2014-04-24 rphall: Commented out unused local variable.
			// Note: method 'getNextSource()' has side effects
//			IComparisonSetSource source = null;
			if (sources.hasNextSource()) /* source = */ sources.getNextSource ();
			else if (Osources.hasNextSource()) /* source = */ Osources.getNextSource();
			else throw new BlockingException ("Could not open any comparison set source for chunk " + i);
		}


		for (int i=startPoint; i< numChunks; i++) {

			long t = System.currentTimeMillis();

			IComparisonSetSource source = null;

			if (sources.hasNextSource()) source = sources.getNextSource ();
			else if (Osources.hasNextSource()) source = Osources.getNextSource();
			else throw new BlockingException ("Could not open any comparison set source for chunk " + i);

			source.open();

			RecordSource stage = (RecordSource) stageSources.get(i);
			RecordSource master = (RecordSource) masterSources.get(i);

			log.info ("matching chunk " + i + " " + source.getInfo());
			MemoryEstimator.writeMem();

			//get the records into memory
			HashMap stageMap = getRecords (stage, model);
			HashMap masterMap = getRecords (master, model);

			ArrayList buffer = new ArrayList ();

			int b = 0;
			int c = 0;
			int n = 0;

			long y = System.currentTimeMillis();

			while (source.hasNext()) {

				//get the next tree or array
				IComparisonSet cSet = source.getNextSet();

				while (cSet.hasNextPair()) {
					ComparisonPair p = cSet.getNextPair();

					Record q = (Record) stageMap.get(p.getId1());
					Record m;
					if (p.isStage) m =  (Record) stageMap.get(p.getId2());
					else m =  (Record) masterMap.get(p.getId2());

					MatchRecord2 match = compareRecords (q, m, p.isStage);
					if (match != null) {
						n++;
						buffer.add(match);
					}

					c ++;
				}

				//write to match sink
				if (buffer.size() > BUFFER_SIZE) {
					long x = System.currentTimeMillis();
					mSink.writeMatches(buffer);
					x = System.currentTimeMillis() - x;
					inWriteMatches += x;
					buffer = new ArrayList ();
				}

				b ++;
			} //end while

			//update timer
			y = System.currentTimeMillis() - y;
			inHandleBlocks += y;

			//one last write on this chunk
			if (buffer.size() > 0) {
				long x = System.currentTimeMillis();
				mSink.writeMatches(buffer);
				x = System.currentTimeMillis() - x;
				inWriteMatches += x;
				buffer = null;
			}

			numSets += b;
			compares += c;
			numMatches += n;

			log.info ("blocks: " + b + " comparisons: " +  c + " matches: " + n);

			t = System.currentTimeMillis() - t;
			double cps = 1000.0 * c / t;
			log.info ("comparisons per second " + cps );

			//log the status
			String temp = Integer.toString(numChunks) + OabaProcessing.DELIMIT + Integer.toString(i);
			status.setCurrentProcessingEvent( OabaEvent.MATCHING_DATA, temp );

			source.close();

			//clean up
			stage = null;
			master = null;
			source = null;

		} //end for i

		log.info ("total sets: " + numSets + " comparisons: " +  compares + " matches: " + numMatches);
		log.info ("Time in readMaps " + inReadHM);
		log.info ("Time in handleBlocks " + inHandleBlocks);
		log.info ("Time in writeMatches " + inWriteMatches);

		double cps = 1000.0 * compares / (inReadHM + inHandleBlocks + inWriteMatches);
		log.info ("comparisons per second " + cps );

		//cleanup
		stageFactory.removeAllSinks();
		masterFactory.removeAllSinks();
		sources.cleanUp();
		Osources.cleanUp();

		status.setCurrentProcessingEvent( OabaEvent.DONE_MATCHING_DATA);
	}


	/** This method gets the data in the RecordSource and puts them into a hash map.
	 *
	 * @param rs - RecordSource
	 * @param accessProvider - ProbabilityModel
	 * @return
	 */
	private HashMap getRecords (RecordSource rs, IProbabilityModel model) throws BlockingException {
		long t = System.currentTimeMillis();

		HashMap records = new HashMap ();

		try {
			if (rs != null && model != null) {
				rs.setModel(model);
				rs.open();

				// put the whole chunk dataset into memory.
				while (rs.hasNext()) {
					Record r = rs.getNext();
					Object O = r.getId();

					records.put(O, r);
				}

				rs.close();
			}
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}

		inReadHM += System.currentTimeMillis() - t;

		return records;
	}


	/** This method compares two records and returns a MatchRecord2 object.
	 *
	 * @param clueSet
	 * @param enabledClues
	 * @param evaluator
	 * @param q - first record
	 * @param m - second record
	 * @param isStage - indicates if the second record is staging or master
	 * @param low
	 * @param high
	 * @return
	 */
	private MatchRecord2 compareRecords (Record q, Record m, boolean isStage) {

		MatchRecord2 mr = null;

		if ((q != null) && (m != null)) {
			ActiveClues activeClues = clueSet.getActiveClues(q, m, enabledClues);
			float matchProbability = evaluator.getProbability(activeClues);
			Decision decision = evaluator.getDecision(activeClues, matchProbability, low, high);

			char source = MatchRecord2.MASTER_SOURCE;

			Comparable i1 = q.getId();
			Comparable i2 = m.getId();

			if (isStage) {
				source = MatchRecord2.STAGE_SOURCE;

				//make sure the smaller id is first
				if (i1.compareTo(i2) > 0) {
					Comparable i3 = i1;
					i1 = i2;
					i2 = i3;
				}
			}

			// 2009-08-17 rphall
			// BUG FIX? clue notes added here
			final String noteInfo = MatchRecord2.getNotesAsDelimitedString(activeClues,this.model);
			if (decision == Decision.MATCH) {
				mr = new MatchRecord2 (i1, i2, source, matchProbability, MatchRecord2.MATCH,noteInfo);
			} else if (decision == Decision.DIFFER) {
			} else if (decision == Decision.HOLD) {
				mr = new MatchRecord2 (i1, i2, source, matchProbability, MatchRecord2.HOLD,noteInfo);
			}
			// END BUG FIX?

		}

		return mr;
	}


}
