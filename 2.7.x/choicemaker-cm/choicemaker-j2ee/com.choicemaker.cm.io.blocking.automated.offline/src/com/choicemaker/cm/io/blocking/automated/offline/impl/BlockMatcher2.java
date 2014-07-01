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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockMatcher2;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This object performs the matching of the blocks using the block source and chunk data source.
 * It checks the size of the block and calls handleBlock for size &lt;= M and calls handleOversized for
 * size &gt; M.
 * 
 * This reads in a IBlockSource and a corresponding RecordSource containing the data.  It writes the matching
 * pairs to IMatchSink.
 * 
 * This version use ComparisonGroup to allow Integer, Long, and String record IDs.
 * 
 * @author pcheung
 *
 */
public class BlockMatcher2 implements IBlockMatcher2 {

	private static final Logger log = Logger.getLogger(BlockMatcher2.class);

	private int compares = 0; //number of comparisons made.
	private int matches = 0; //number of matches and holds.
	private int numBlocks = 0; //number of blocks.
	
	//variables to track time in IO vs CPU.
	private long inReadMaps = 0;
	private long inHandleBlock = 0;
	private long inWriteMatches = 0;
	
	//debug
	private static IProbabilityModel stageModel;
	
	//default constructor
	public BlockMatcher2 () {
	}
	
	/** This returns the amount of time in ms in reading records into hash map.
	 * This is reset in the matchBlocks method.
	 * 
	 * @return
	 */
	public long getTimeInReadMaps () {
		return inReadMaps;
	}
	
	/** This returns the amount of time in ms in performing matches in the block.  This also includes the
	 * time to write out to match file.  This is reset in the matchBlocks method.
	 * 
	 * @return
	 */
	public long getTimeInHandleBlock () {
		return inHandleBlock;
	}


	/** This returns the amount of time in ms in writing to the match file.
	 * This is reset in the matchBlocks method.
	 * 
	 * @return
	 */
	public long getTimeInWriteMatches () {
		return inWriteMatches;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IBlockMatcher#matchBlocks(com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource, com.choicemaker.cm.core.base.ProbabilityModel, com.choicemaker.cm.core.base.RecordSource, com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSink, float, float, int)
	 */
	public void matchBlocks(
		IComparisonArraySource cgSource, IProbabilityModel stageModel, IProbabilityModel masterModel,
		RecordSource stage, RecordSource master, 
		IMatchRecord2Sink mSink,
		boolean append, 
		float differ,
		float match,
		int maxBlockSize) throws BlockingException {
			
		compares = 0; //number of comparisons made.
		matches = 0; //number of matches and holds.
		numBlocks = 0; //number of blocks.

		inReadMaps = 0;
		inHandleBlock = 0;
		inWriteMatches = 0;
		
		//get the records
		long t = System.currentTimeMillis();
		HashMap stageMap = getRecords (stage, stageModel);
		HashMap masterMap = getRecords (master, masterModel);
		inReadMaps = System.currentTimeMillis() - t;
		
		
		BlockMatcher2.stageModel = stageModel;
		
		cgSource.open();
		
		if (append) mSink.append();
		else mSink.open();
			
		while (cgSource.hasNext()) {
			this.numBlocks ++;
			
			ComparisonArray cg = cgSource.getNext();
			
			//debug
//			if (log.isDebugEnabled()) debugComparisonGroup (cg);
			
			int size = cg.getMasterIDs().size() + cg.getStagingIDs().size();
				
			if (size <= maxBlockSize) {
				t = System.currentTimeMillis();
				handleBlock (cg, stageMap, masterMap, stageModel, mSink, differ, match);
				inHandleBlock += System.currentTimeMillis() - t;
			} else {
				t = System.currentTimeMillis();
				handleOversized (cg, stageMap, masterMap, stageModel, mSink, differ, match, maxBlockSize);
//				handleBlock (cg, stageMap, masterMap, stageModel, mSink, differ, match);
				inHandleBlock += System.currentTimeMillis() - t;
			} 
				
		}
		
		mSink.close();
		cgSource.close();
		
		//cleanup
		stageMap = null;
		masterMap = null;
	}
	
	
	/* Debuging Comparison Group
	 * 
	 */
	private void debugComparisonGroup (ComparisonArray cg) {
		ArrayList list = cg.getStagingIDs();
		
		StringBuffer sb = new StringBuffer ("stage: ");
		for (int i=0; i< list.size(); i++) {
			Comparable c = (Comparable) list.get(i);
			sb.append(c);
			sb.append(" ");
		}
		log.debug(sb.toString());
		
		list = cg.getMasterIDs();
		sb = new StringBuffer ("master: ");
		for (int i=0; i< list.size(); i++) {
			Comparable c = (Comparable) list.get(i);
			sb.append(c);
			sb.append(" ");
		}
		log.debug(sb.toString());
	}
	
	
	/** This method performs round robin matching for the block.  Every element is compared with every 
	 * other element in the block.
	 * 
	 * @param cg - ComparisonGroup that contains the record IDs in this block.
	 * @param stage - hash map of the staging record data
	 * @param master - hash map of the master record data
	 * @param accessProvider - probability accessProvider
	 * @param mSink - Match pair sink
	 * @param low - differ threshold
	 * @param high - match threshold
	 * @return - an array of MatchRecord
	 */
	private void handleBlock (ComparisonArray cg, HashMap stage, HashMap master, ImmutableProbabilityModel model,
		IMatchRecord2Sink mSink, float low, float high) throws BlockingException {
		
		ArrayList list = new ArrayList ();
		
		ClueSet clueSet = model.getClueSet();
		boolean[] enabledClues = model.getCluesToEvaluate();
		
		ArrayList stageList = cg.getStagingIDs();
		ArrayList masterList = cg.getMasterIDs();
		
		for (int i=0; i< stageList.size()-1; i++) {
			Comparable c = (Comparable) stageList.get(i);
			Record q = (Record) stage.get(c);
			if (q == null) {
				log.error("q is null " + c);
			} 
			
			//compare with all the other staging records.
			for (int j=i+1; j < stageList.size(); j++) {
				Comparable c2 = (Comparable) stageList.get(j);
				Record m = (Record) stage.get(c2);
				
				if (m == null) log.error ("m is null " + c2);
		
				MatchRecord2 mr = compareRecords (clueSet, enabledClues, model, q, m, true,
					low, high);
				if (mr != null) {
					list.add(mr);
					this.matches ++;
				} 

			} //end for j
			
			long t = System.currentTimeMillis();
			mSink.writeMatches(list);
			inWriteMatches += System.currentTimeMillis() - t;
			
			list = new ArrayList ();
			
		} //end for i
		
		
		//for each element in the stageList compare it with all the elements in the masterList
		for (int i=0; i< stageList.size(); i++) {
			Comparable c = (Comparable) stageList.get(i);
			Record q = (Record) stage.get(c);
			if (q == null) {
				log.error("q is null " + c);
			} 
			
			for (int j=0; j < masterList.size(); j++) {
				Comparable c2 = (Comparable) masterList.get(j);
				Record m = (Record) master.get(c2);

				if (m == null) log.error ("master m is null " + c2);
		
				MatchRecord2 mr = compareRecords (clueSet, enabledClues, model, q, m, false,
					low, high);
				if (mr != null) {
					list.add(mr);
					this.matches ++;
				} 
			}
						
			long t = System.currentTimeMillis();
			mSink.writeMatches(list);
			inWriteMatches += System.currentTimeMillis() - t;

			list = new ArrayList ();
			
		} //end for i		
		
		
		//clean up
		list = null;
		stageList = null;
		masterList = null;
	}
	

	/** This method handles matching an oversized block.
	 * 
	 * @param block - LongArrayList containing the record ids in an oversized block
	 * @param records - HashMap of the record data
	 * @param accessProvider - ProbabilityModel
	 * @param mSink - Match pair sink
	 * @param low - differ threshold
	 * @param high - match threshold
	 * @param maxBlockSize - maximum size of a regular block
	 * @param validator - indicates if s pair is valid for comparison
	 * @throws IOException
	 */
	private void handleOversized (ComparisonArray cg, HashMap stage, HashMap master, ImmutableProbabilityModel model,
		IMatchRecord2Sink mSink, float low, float high, int maxBlockSize) throws BlockingException {

		ArrayList list = new ArrayList ();
		
		ClueSet clueSet = model.getClueSet();
		boolean[] enabledClues = model.getCluesToEvaluate();
		
		//first split up into S and T sets, S only contains staging records
		ArrayList stageList = cg.getStagingIDs();
		ArrayList S = null;
		ArrayList TStage = null;
		ArrayList TMaster = cg.getMasterIDs();

		//seed the random number generator
		int s = stageList.size() + TMaster.size();
		Random random = new Random (s);
		
		log.debug("Random " + s);
		
		int sSize;
		if (cg.getStagingIDs().size() <= maxBlockSize) {
			S = stageList;
			sSize = stageList.size();
			TStage = new ArrayList (0);
		} else {
			S = new ArrayList (maxBlockSize);
			sSize = maxBlockSize;

			TStage = new ArrayList (stageList.size() - sSize);

			//contains indexes of ids in set S.
			int [] sids = getRandomIDs (random, maxBlockSize, stageList.size());
			int current = 0;
			for (int i=0; i< stageList.size(); i++) {
				if (current < maxBlockSize && i == sids[current]) {
					S.add( stageList.get(i));
					current ++;
				} else {
					TStage.add( stageList.get(i));
				}
			}

		}
		
		
		//perform round robin comparison on S
		for (int i=0; i< S.size()-1; i++) {
			Comparable c1 = (Comparable) S.get(i);
			Record q = (Record) stage.get(c1);
			
			for (int j=i+1; j < S.size(); j++) {
				Comparable c2 = (Comparable) S.get(j);
				Record m = (Record) stage.get(c2);
				
				log.debug ("Round robin s " + c1.toString() + " " + c2.toString());
				
				MatchRecord2 mr = compareRecords (clueSet, enabledClues, model, q, m, true,
					low, high);

				if (mr != null) list.add(mr);
				
			} //end of j
		}// end of i
		this.matches += list.size();
		mSink.writeMatches(list);
		list = new ArrayList ();

		
		//for each T stage, compare to all of S
		for (int i=0; i< TStage.size (); i++) {
			Comparable c1 = (Comparable) TStage.get(i);
			Record q = (Record) stage.get(c1);
			
			for (int j=0; j<S.size(); j++) {
				Comparable c2 = (Comparable) S.get(j);
				Record m = (Record) stage.get(c2);
				
				log.debug ("TStage with S " + c1.toString() + " " + c2.toString());

				MatchRecord2 mr = compareRecords (clueSet, enabledClues, model, q, m, true,
					low, high);
				if (mr != null) list.add(mr);
			}
		} //end for i
		this.matches += list.size();
		mSink.writeMatches(list);
		list = new ArrayList ();

		
		//for each T master, compare to all of S
		for (int i=0; i< TMaster.size (); i++) {
			Comparable c1 = (Comparable) TMaster.get(i);
			Record m = (Record) master.get(c1); //q is always stage record
			
			for (int j=0; j<S.size(); j++) {
				Comparable c2 = (Comparable) S.get(j);
				Record q = (Record) stage.get(c2);
				
				log.debug ("TMaster with S " + c2.toString() + " " + c1.toString());

				MatchRecord2 mr = compareRecords (clueSet, enabledClues, model, q, m, false,
					low, high);
				if (mr != null) list.add(mr);
			}
		} //end for i
		this.matches += list.size();
		mSink.writeMatches(list);
		list = new ArrayList ();


		//for each T master, compare with 4 random TStage
		if (TStage.size() <= 4) {
			for (int i=0; i< TMaster.size (); i++) {
				Comparable c1 = (Comparable) TMaster.get(i);
				Record m = (Record) master.get(c1);
				//compare all
				for (int j=0; j<TStage.size(); j++) {
					Comparable c2 = (Comparable) TStage.get(j);
					Record q = (Record) stage.get(c2);

					log.debug ("TMaster random " + c2.toString() + " " + c1.toString());

					MatchRecord2 mr = compareRecords (clueSet, enabledClues, model, q, m, false,
						low, high);
					if (mr != null) list.add(mr);
				}
			}
		} else {
			for (int i=0; i< TMaster.size (); i++) {
				Comparable c1 = (Comparable) TMaster.get(i);
				Record m = (Record) master.get(c1);
				//compare 4 random
				int [] ids = getRandomIDs (random, 4, TStage.size ());
				for (int j=0; j<4; j++) {
					Comparable c2 = (Comparable) TStage.get(ids[j]);
					Record q = (Record) stage.get(c2);

					log.debug ("TMaster random " + c2.toString() + " " + c1.toString());

					MatchRecord2 mr = compareRecords (clueSet, enabledClues, model, q, m, false,
						low, high);
					if (mr != null) list.add(mr);
				} 
			}
		}
		this.matches += list.size();
		mSink.writeMatches(list);
		list = new ArrayList ();
		
		
		//for each TStage ith record, compare to its i+1 neighbor and 3 random records. 
		for (int i=0; i< TStage.size (); i++) {
			Comparable c1 = (Comparable) TStage.get(i);
			Comparable c2 = null;

			Record q = (Record) stage.get(c1);
			Record m = null;
			
			int n = i+1;
			if (i ==  TStage.size()-1) n = 0;
			
			c2 = (Comparable) TStage.get(n);
			m = (Record) stage.get(c2);

			log.debug ("TStage random i+1 " + c1.toString() + " " + c2.toString());

			MatchRecord2 mr = compareRecords (clueSet, enabledClues, model, q, m, true,
				low, high);
			if (mr != null) list.add(mr);
			
			
			if (TStage.size () + TMaster.size() > 4) {
				//compare to 3 random
				int [] ids = getRandomIDs (random, 5, TStage.size () + TMaster.size());
				
				int count = 0;
				int j = 0;
				while (count < 3 && j < ids.length) {
					if (i == ids[j] || n == ids[j]) j ++; 
				
					if (ids[j] >= TStage.size()) {
						c2 = (Comparable) TMaster.get(ids[j]-TStage.size());
						m = (Record) master.get(c2);
						mr = compareRecords (clueSet, enabledClues, model, q, m, false,low, high);					

						log.debug ("TStage random " + c1.toString() + " " + c2.toString());
					} else {
						c2 = (Comparable) TStage.get(ids[j]);
						m = (Record) stage.get(c2);
						mr = compareRecords (clueSet, enabledClues, model, q, m, true,low, high);					

						log.debug ("TStage random " + c1.toString() + " " + c2.toString());
					}
					if (mr != null) list.add(mr);

					count ++;
					j++;
				}
			}
			
		}
		this.matches += list.size();
		mSink.writeMatches(list);
		list = null;
		
	}


	
	
	/** This method gets the data in the RecordSource and puts them into a hash map.
	 * 
	 * @param rs - RecordSource
	 * @param accessProvider - ProbabilityModel
	 * @return
	 */
	private static HashMap getRecords (RecordSource rs, IProbabilityModel model) throws BlockingException {
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
		
		return records;
	}
	

	/** This method compares two records and returns a MatchRecord2 object.
	 * 
	 * @param clueSet
	 * @param enabledClues
	 * @param model
	 * @param q - first record
	 * @param m - second record
	 * @param isStage - indicates if the second record is staging or master
	 * @param low
	 * @param high
	 * @return
	 */
	private MatchRecord2 compareRecords (ClueSet clueSet, boolean[] enabledClues, ImmutableProbabilityModel model,
		Record q, Record m, boolean isStage, float low, float high) {
			
		this.compares ++;
		MatchRecord2 mr = null;

		if ((q != null) && (m != null)) {
			Evaluator evaluator = model.getEvaluator();
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
			final String noteInfo = MatchRecord2.getNotesAsDelimitedString(activeClues,model);
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


	
	
	/** This method returns a int array of the given size containing random ids from 0 to max.
	 * 
	 * @param size - size of the random array
	 * @param max - maximum number
	 * @return
	 */
	private static int [] getRandomIDs (Random random, int size, int max) {
		int [] list = new int [max];
		int [] list2 = new int [size];
		
		for (int i=0; i<max; i++) {
			list[i] = i;
		}
		
		for (int i=0; i< size; i++) {
			int ind = random.nextInt(max - i);
			list2[i] = list[ind];
			
			//remove ind for list
			for (int j=ind; j < max - i - 1; j ++) {
				list[j] = list[j+1];
			}
		}
		
		Arrays.sort( list2);
		return list2;
	}
	
	


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IBlockMatcher#getComparesMade()
	 */
	public int getNumComparesMade() {
		return this.compares;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IBlockMatcher#getMatches()
	 */
	public int getNumMatches() {
		return this.matches;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IBlockMatcher#getNumBlocks()
	 */
	public int getNumBlocks() {
		return this.numBlocks;
	}
	

}
