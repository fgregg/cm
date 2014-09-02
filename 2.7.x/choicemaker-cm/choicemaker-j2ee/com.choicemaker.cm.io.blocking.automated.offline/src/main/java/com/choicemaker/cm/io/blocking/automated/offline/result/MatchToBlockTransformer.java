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
package com.choicemaker.cm.io.blocking.automated.offline.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.EquivalenceClass;
import com.choicemaker.cm.core.util.EquivalenceClassBuilder;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.util.LongArrayList;


/**
 * This object reads in an IMatchRecord2Source, creates equivalence classes, and outputs them
 * as blocks to a IBlockSink.
 *
 *
 *
 * @author pcheung
 *
 */
public class MatchToBlockTransformer {

	private static final Logger log = Logger.getLogger(MatchToBlockTransformer.class.getName());

	private IMatchRecord2Source mSource;
	private IBlockSink blockSink;
	private IRecordIDTranslator2 translator;

	private HashMap stageIDs;
	private HashMap masterIDs;

	//this is the number of records that need to be chunked.
	private int numRecords;

	//max size of an EC
	private int max = 0;


	/** This constructor takes these parameters.
	 *
	 * @param mSource - match record pairs source
	 * @param translator - a mapping between record id and internal id
	 * @param blockSink - a block sink to store the equivalence classes
	 */
	public MatchToBlockTransformer (IMatchRecord2Source mSource,
		IRecordIDTranslator2 translator, IBlockSink blockSink) {

		this.mSource = mSource;
		this.blockSink = blockSink;
		this.translator = translator;
	}


	/* This method goes through the MatchRecordSource to get a distinct list of stage and master ids.
	 *
	 */
	private void populateHashMaps () throws BlockingException{
		stageIDs = new HashMap ();
		masterIDs = new HashMap ();

		mSource.open();

		while (mSource.hasNext()) {
			MatchRecord2 mr = mSource.getNext();
			if (!stageIDs.containsKey( mr.getRecordID1())) stageIDs.put(mr.getRecordID1(), null);

			if (mr.getRecord2Source() == MatchRecord2.STAGE_SOURCE) {
				if (!stageIDs.containsKey( mr.getRecordID2())) stageIDs.put(mr.getRecordID2(), null);
			} else {
				if (!masterIDs.containsKey(mr.getRecordID2())) masterIDs.put(mr.getRecordID2(), null);
			}
		}

		mSource.close();
	}


	/* This method uses the arrays in the translator to match the record IDs to internal IDs.
	 *
	 */
	private void setInternalIDs () throws BlockingException {
		translator.initReverseTranslation();

		//stage record ids
		ArrayList list = translator.getList1();
		if (list != null) {
			int size = list.size();
			for (int i=0; i< size; i++) {
				Comparable c = (Comparable) list.get(i);
				if (stageIDs.containsKey(c)) stageIDs.put(c, new Long(i));
			}
		}

		//master record ids
		list = translator.getList2();
		if (list != null) {
			int size = list.size();
			for (int i=0; i< size; i++) {
				Comparable c = (Comparable) list.get(i);
				if (masterIDs.containsKey(c)) masterIDs.put(c, new Long(i));
			}
		}
	}


	/* This method reads the MatchRecord2Source, translates the record ids to internal ids,
	 * build equivalence classes with internal ids, adn write equivalence classes out as blocks.
	 *
	 */
	private EquivalenceClassBuilder buildEquivalenceClasses () {
		EquivalenceClassBuilder builder = new EquivalenceClassBuilder ();
		try {
			mSource.open();

			// 2014-04-24 rphall: Commented out unused local variable.
//			int count = 0;

			while (mSource.hasNext()) {
//				count ++;

				MatchRecord2 mr = mSource.getNext();

				//if (mr.getMatchType() == MatchRecord2.MATCH) {
					Long l1 = (Long) stageIDs.get(mr.getRecordID1());

					Long l2 = null;
					if (mr.getRecord2Source() == MatchRecord2.STAGE_SOURCE) {
						l2 = (Long) stageIDs.get(mr.getRecordID2());
					} else {
						l2 = (Long) masterIDs.get(mr.getRecordID2());
					}

					if (l1 == null) {
						log.severe("l1 is null " + mr.getRecordID1());
					} else if (l2 == null) {
						log.severe("l2 is null " + mr.getRecordID2());
					} else if (l1.equals(l2)) {
						log.severe("l1 == l2 " + l1.toString() + " " + l2.toString());
						log.severe("l1 == l2 " + mr.getRecordID1() + " " + mr.getRecordID2());
					}

					builder.addLink(l1, l2);
				//}
			}

			mSource.close();
		} catch (BlockingException e) {
			log.severe(e.toString());
		}
		return builder;
	}


	/* This method writes the equivlence classes out as blocks.
	 *
	 */
	private void writeBlocks (EquivalenceClassBuilder builder) throws BlockingException {
		blockSink.open();

		SortedSet set = builder.getEquivalenceClasses();
		Iterator it = set.iterator();

		int twos = 0;
		int count = 0;
		int compares = 0;

		while (it.hasNext()) {
			count ++;

			EquivalenceClass ec = (EquivalenceClass) it.next();
			int size = ec.size();
			if (size > 2) {
				Iterator it2 = ec.getMemberIds().iterator();

				BlockSet bs = new BlockSet (0);
				LongArrayList list = new LongArrayList (size);

				while (it2.hasNext()) {
					list.add( ((Long) it2.next()).longValue() );
				}
				bs.setRecordIDs(list);
				blockSink.writeBlock(bs);

				compares += size * (size - 1)/2;
				numRecords += size;

				if (size > max) max = size;
			} else {
				twos ++;
			}

		}

		blockSink.close();

		log.info ("total number of equivalence classes: " + count);
		log.info ("max size of an equivalence class: " + max);
		log.info ("number of size two equivalence classes: " + twos);
		log.info ("total number of records required: " + numRecords);
		log.info ("total number of comparisons required: " + compares);
	}

	/** This method creates blocks of internal ids from match result source of record ids.
	 *
	 * @return int - the number of records needed for the next step.
	 */
	public int process () {
		long t = System.currentTimeMillis();

		try {
			populateHashMaps ();
			setInternalIDs ();
			EquivalenceClassBuilder builder = buildEquivalenceClasses ();

			//clear memory
			stageIDs = null;
			masterIDs = null;

			writeBlocks (builder);

			// clear memory
			builder = null;

		} catch (BlockingException e) {
			log.severe(e.toString());
		}

		t = System.currentTimeMillis() - t;
		log.info("time: " + t);

		return numRecords;
	}


	/** This returns the maximum size of an equivalence class.
	 *
	 * @return int - max size of an EC
	 */
	public int getMaxEC () {
		return max;
	}

}
