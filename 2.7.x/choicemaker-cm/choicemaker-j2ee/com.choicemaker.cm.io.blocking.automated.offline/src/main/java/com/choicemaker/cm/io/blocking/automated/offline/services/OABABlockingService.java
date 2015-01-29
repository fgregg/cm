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
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IControl;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IOversizedGroup;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.OversizedGroup;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;
import com.choicemaker.cm.io.blocking.automated.offline.utils.RecordValue2;
import com.choicemaker.util.IntArrayList;
import com.choicemaker.util.LongArrayList;

/**
 * @author pcheung
 *
 *         This service blocks by 1 column then trims the oversized blocks.
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class OABABlockingService {

	private static final Logger log = Logger
			.getLogger(OABABlockingService.class.getName());

	private int maxBlockSize;

	private IBlockSink bSink;

	// these two variables are used to stop the program in the middle
	private IControl control;
	private boolean stop = false;

	private IBlockSinkSourceFactory osFactory;
	private IValidatorBase validator;

	private int totalOversized = 0;

	private int numBlockingFields; // number of database blocking fields.

	// This contains a list of record ID's that show up in oversized blocks
	// this is used to remove small sets from rec_id, val_id pairs file
	private LongArrayList osIDs;

	private RecValSinkSourceFactory rvFactory;

	// this is used to stored special oversized blocks that cannot be trimmed
	// further
	private IBlockSink osSpecial;
	private IBlockSink osDump;

	// this is the minimun number of blocking fields an oversized blocks needs
	// to have in order to be saved
	// in the special oversized blocks file.
	private int minFields;
	private int maxOversized;

	private OabaEventLog status;

	private IRecValSource[] rvSources;

	private OversizedGroup osGroup;

	private int numBlocks = 0; // number of blocks
	private int numOS = 0; // number of "good" oversized blocks

	private int numInvalid = 0; // number of invalid blocks as defined by the
								// validator

	private long time; // this keeps track of time

	/**
	 * This constructor takes the following parameters
	 *
	 * @param maxSize
	 *            - max size of blocking set
	 * @param bSink
	 *            - sink to write the blocks
	 * @param osFactory
	 *            - oversized factory for creating temp files
	 * @param osSpecial
	 *            - sink to save special oversized blocks
	 * @param osDump
	 *            - sink to save oversized block that the algorithm throws away
	 * @param rvs
	 *            - RecValService
	 * @param validator
	 *            - this defines what's a good block
	 * @param status
	 *            - current status of the system
	 * @param control
	 *            - a mechanism to get out of a long running loop
	 * @param minFields
	 *            - minimum number of blocking fields an oversized block needs
	 *            to have in order to be considered "good".
	 * @param maxOversized
	 *            - maximum number of elements a "good" oversized block can have
	 * @throws IOException
	 */
	public OABABlockingService(int maxSize, IBlockSink bSink,
			IBlockSinkSourceFactory osFactory, IBlockSink osSpecial,
			IBlockSink osDump, RecValSinkSourceFactory rvFactory,
			int numBlockingFields, IValidatorBase validator,
			OabaEventLog status, IControl control, int minFields,
			int maxOversized) throws IOException {

		this.validator = validator;
		this.maxBlockSize = maxSize;
		this.bSink = bSink;
		this.osFactory = osFactory;

		this.osSpecial = osSpecial;
		this.minFields = minFields;
		this.maxOversized = maxOversized;
		this.osDump = osDump;

		this.rvFactory = rvFactory;
		this.numBlockingFields = numBlockingFields;
		this.status = status;
		this.control = control;
		this.stop = false;
	}

	public int getNumBlocks() {
		return numBlocks;
	}

	public int getNumOversized() {
		return numOS;
	}

	public int getNumInvalid() {
		return numInvalid;
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
	 * This method checks the current status and runs the appropriate method.
	 *
	 *
	 */
	public void runService() throws BlockingException {
		time = System.currentTimeMillis();

		if (status.getCurrentOabaEventId() >= OabaProcessing.EVT_DONE_OVERSIZED_TRIMMING) {
			// do nothing here

		} else if (status.getCurrentOabaEventId() < OabaProcessing.EVT_BLOCK_BY_ONE_COLUMN) {
			log.info("Blocking By 1 column ");

			init();

			blockByOneColumn();

			if (!stop)
				trimOversized();

		} else if (status.getCurrentOabaEventId() == OabaProcessing.EVT_BLOCK_BY_ONE_COLUMN) {
			log.info("Trying to recover blocking by one column");
			init();

			// started to created, but not done, so we need to recover
			recoverBlockByOneColumn();

			if (!stop)
				trimOversized();

		} else if (status.getCurrentOabaEventId() == OabaProcessing.EVT_DONE_BLOCK_BY_ONE_COLUMN) {
			log.info("Starting from trimOversized");
			init();

			trimOversized();

		} else if (status.getCurrentOabaEventId() == OabaProcessing.EVT_OVERSIZED_TRIMMING) {
			log.info("Trying to recover oversized trimming");
			init();

			recoverTrimOversized();
		}
		time = System.currentTimeMillis() - time;
	}

	private void init() throws BlockingException {
		this.rvSources = new IRecValSource[numBlockingFields];
		for (int i = 0; i < numBlockingFields; i++) {
			rvSources[i] = rvFactory.getNextSource();
		}
	}

	/**
	 * This method blocks one blocking field at a time.
	 *
	 * @throws IOException
	 */
	private void blockByOneColumn() throws BlockingException {
		log.info("blockByOneColumn");

		numBlocks = 0;

		// use osGroup to group same max column sets into the same file.
		osGroup = new OversizedGroup(numBlockingFields, osFactory);
		osGroup.openAllSinks();
		bSink.open();
		if (osDump != null)
			osDump.open();

		// block one column at a time.
		for (int i = 0; i < numBlockingFields && !stop; i++) {
			stop =
				ControlChecker.checkStop(control,
						ControlChecker.CONTROL_INTERVAL);

			IRecValSource rvSource = rvSources[i];
			numBlocks += blockByField(i, rvSource, bSink, osGroup);

			if (!stop)
				status.setCurrentOabaEvent(OabaEvent.BLOCK_BY_ONE_COLUMN,
						Integer.toString(i) + "|" + Integer.toString(numBlocks));
		}

		bSink.close();
		osGroup.closeAllSinks();
		if (osDump != null)
			osDump.close();

		if (!stop)
			status.setCurrentOabaEvent(OabaEvent.DONE_BLOCK_BY_ONE_COLUMN);
	}

	/**
	 * This method resumes blocking from the last column written in the status
	 * file. It is possible that there are dups if the previous process died in
	 * the middle of a column, but we'll rely on blocks dedup to take care of
	 * that.
	 *
	 * @throws IOException
	 */
	private void recoverBlockByOneColumn() throws BlockingException {
		StringTokenizer stk =
			new StringTokenizer(status.getCurrentOabaEventInfo(), "|");
		int currentCol = Integer.parseInt(stk.nextToken()) + 1;
		numBlocks = Integer.parseInt(stk.nextToken());

		log.info("recovering starting at column " + currentCol + " "
				+ numBlocks);

		// use osGroup to group same max column sets into the same file.
		osGroup = new OversizedGroup(numBlockingFields, osFactory);

		totalOversized = countOversized(osGroup);
		osGroup.appendAllSinks();

		bSink.append();
		if (osDump != null)
			osDump.append();

		// block one column at a time starting from the last incomplete.
		for (int i = currentCol; i < numBlockingFields && !stop; i++) {
			stop =
				ControlChecker.checkStop(control,
						ControlChecker.CONTROL_INTERVAL);

			IRecValSource rvSource = rvSources[i];
			numBlocks += blockByField(i, rvSource, bSink, osGroup);

			if (!stop)
				status.setCurrentOabaEvent(OabaEvent.BLOCK_BY_ONE_COLUMN,
						Integer.toString(i) + "|" + Integer.toString(numBlocks));
		}

		osGroup.closeAllSinks();
		bSink.close();
		if (osDump != null)
			osDump.close();

		if (!stop)
			status.setCurrentOabaEvent(OabaEvent.DONE_BLOCK_BY_ONE_COLUMN);

	}

	/**
	 * This method trims oversized blocks by adding more blocking fields.
	 *
	 * @throws IOException
	 */
	private void trimOversized() throws BlockingException {
		if (osGroup == null) {
			osGroup = new OversizedGroup(numBlockingFields, osFactory);
			totalOversized = countOversized(osGroup);
		}

		// this is for writing out
		OversizedGroup osGroupNew =
			new OversizedGroup(numBlockingFields, osFactory);
		osGroupNew.openAllSinks();

		// number of blockings in the oversized blocks
		int numFields = 1;

		if (osDump != null)
			osDump.open();
		osSpecial.open();
		bSink.append();

		long t1 = System.currentTimeMillis();

		while (totalOversized > 0 && !stop) {
			log.info(totalOversized + " Oversized blocks, blocking with "
					+ numFields + " field");

			int num = blockOversized(numFields, osGroup, osGroupNew);
			numBlocks += num;

			osGroupNew.closeAllSinks();

			if (!stop) {
				log.info("Done Trimming.  blocks: " + num + " oversized:"
						+ totalOversized);

				// prepare for the next pass of the loop
				osGroup.cleanUp();
				osGroup = osGroupNew;
				osGroupNew = new OversizedGroup(numBlockingFields, osFactory);
				osGroupNew.openAllSinks();

				String info = Integer.toString(numFields);
				status.setCurrentOabaEvent(OabaEvent.OVERSIZED_TRIMMING, info);
			}

			numFields++;
		}

		if (osDump != null)
			osDump.close();
		osSpecial.close();
		bSink.close();

		if (!stop) {
			// final cleaup for osGroups
			osGroup.cleanUp();
			osGroupNew.closeAllSinks();
			osGroupNew.cleanUp();

			// clean up rec,val files
			cleanUp();

			status.setCurrentOabaEvent(OabaEvent.DONE_OVERSIZED_TRIMMING);
		}

		t1 = System.currentTimeMillis() - t1;
		log.info("Time in oversized block trimming " + t1);
	}

	/**
	 * This method recovers the last number oversized blocking fields and
	 * continue to trim from there.
	 *
	 * @throws IOException
	 */
	private void recoverTrimOversized() throws BlockingException {
		// recover info first
		int numFields = Integer.parseInt(status.getCurrentOabaEventInfo()) + 1;

		log.info("recovering starting at " + numFields);

		// recover osGroup
		// jump ahead on the osFactory
		int m = numBlockingFields * (numFields - 1) + 1;
		for (int i = 2; i <= m; i++) {
			// 2014-04-24 rphall: Commented out unused local variable.
			// Note: method 'getNextSink()' has side effects
			/* IBlockSink sink = */osFactory.getNextSink();
		}

		// count the number of oversized
		if (osGroup == null)
			osGroup = new OversizedGroup(numBlockingFields, osFactory);
		totalOversized = countOversized(osGroup);

		log.info("number of oversized: " + totalOversized);

		// this is for writing out
		OversizedGroup osGroupNew =
			new OversizedGroup(numBlockingFields, osFactory);
		osGroupNew.appendAllSinks();

		// open sinks
		if (osDump != null)
			osDump.append();
		osSpecial.append();
		bSink.append();

		long t1 = System.currentTimeMillis();

		while (totalOversized > 0) {
			String info = Integer.toString(numFields);
			status.setCurrentOabaEvent(OabaEvent.OVERSIZED_TRIMMING, info);

			log.info(totalOversized + " Oversized blocks, blocking with "
					+ numFields + " field");

			int num = blockOversized(numFields, osGroup, osGroupNew);
			numBlocks += num;

			osGroupNew.closeAllSinks();
			log.info("blocks: " + num + " oversized:" + totalOversized);

			// prepare for the next pass of the loop
			osGroup.cleanUp();
			osGroup = osGroupNew;
			osGroupNew = new OversizedGroup(numBlockingFields, osFactory);
			osGroupNew.openAllSinks();

			numFields++;

			info = Integer.toString(numFields);
			status.setCurrentOabaEvent(OabaEvent.OVERSIZED_TRIMMING, info);

		}

		if (osDump != null)
			osDump.close();
		osSpecial.close();
		bSink.close();

		// final cleaup for osGroups
		osGroup.cleanUp();
		osGroupNew.closeAllSinks();
		osGroupNew.cleanUp();

		if (!stop) {
			// clean up rec,val files
			cleanUp();

			status.setCurrentOabaEvent(OabaEvent.DONE_OVERSIZED_TRIMMING);
		}

		t1 = System.currentTimeMillis() - t1;
		log.info("Time in oversized block trimming " + t1);
	}

	/**
	 * This cleans up the rec_id, val_id files by deleting them.
	 *
	 * @throws IOException
	 */
	private void cleanUp() throws BlockingException {
		// now delete the files
		for (int i = 0; i < rvSources.length; i++) {
			if (rvSources[i] != null)
				rvSources[i].delete();
		}
	}

	// This method removes dups in osIDs after it is sorted.
	private LongArrayList removeDups(LongArrayList osIDs) {
		osIDs.sort();

		LongArrayList newList = new LongArrayList(100);

		// NOTE Not a bug 2010-10-19 rphall
		// This can assume record ids > 0, because it refers to internal ids,
		// which are 0 or greater. It would be nice to define a manifest
		// constant
		// for this magic number, however.
		long last = -1;
		// ENDNOTE
		for (int i = 0; i < osIDs.size(); i++) {
			if (osIDs.get(i) != last) {
				last = osIDs.get(i);
				newList.add(last);
			}
		}

		return newList;
	}

	/**
	 * This method removes these IDs from the recVal pair. If an ID already
	 * shows up in a regular block there is no need to keep it any further for
	 * oversize trimming.
	 *
	 * @param osIDs
	 *            - IDs to keep
	 * @param rvSource
	 *            - the source from which comes these IDs
	 */
	private void removeIDs(LongArrayList osIDs, IRecValSource rvSource)
			throws BlockingException {
		osIDs = removeDups(osIDs);

		RecordValue2 recVal = new RecordValue2(rvSource);

		IRecValSink sink = rvFactory.getSink(rvSource);
		sink.open();

		// 2014-04-24 rphall: Commented out unused local variable.
		// int count = 0;

		// only keep those id in osIDs in the rec_id, val_id file.
		for (int i = 0; i < osIDs.size(); i++) {
			IntArrayList list = (IntArrayList) recVal.get(osIDs.get(i));
			sink.writeRecordValue(osIDs.get(i), list);
			// count ++;
		}

		sink.close();
	}

	private int countOversized(IOversizedGroup osg) throws BlockingException {
		int ret = 0;

		for (int i = 0; i < numBlockingFields; i++) {
			IBlockSource source = osg.getSource(i);
			source.open();
			while (source.hasNext()) {
				source.next();
				ret++;
			}
			source.close();
		}

		return ret;
	}

	/**
	 * This method blocks one column at a time and outputs the blocking sets to
	 * blocks.dat and oversized.dat. It groups record IDs that have the same
	 * column value into blocks. It reads in the rec_id, val_id file that has
	 * already been created.
	 *
	 * @param col
	 *            - the ith field to block on.
	 * @param rvSource
	 *            - source of rec_id, val_id information.
	 * @param bSink
	 *            - BlockSet sink.
	 * @param osSink
	 *            - Oversized Blocks sink for future trimming.
	 */
	private int blockByField(int col, IRecValSource rvSource, IBlockSink bSink,
			IOversizedGroup osGroup) throws BlockingException {

		int count = 0;
		HashMap map = new HashMap();

		long recID = 0;

		// this keeps track of oversized block IDs
		osIDs = new LongArrayList(100);

		RecordValue2 records = new RecordValue2(rvSource);
		if (records.size() == 0) {
			// Egregious -- useless blocking field or something may be wrong
			String msg =
				"No records for column " + col + ", source " + rvSource;
			log.warning(msg);
		}

		ArrayList recordList = records.getList();

		for (int j = 0; j < recordList.size() && !stop; j++) {
			stop = ControlChecker.checkStop(control, j);
			recID = j;
			IntArrayList values = (IntArrayList) recordList.get(j);

			if (values != null) {
				if (values.size() == 0) {
					// Typically an invalid value in a non-stacked
					// field, or all invalid values in a stacked field
					String msg =
						"No values for column " + col + ", record id " + j
								+ ", source " + rvSource;
					log.finer(msg);
				} else {
					// Also usually boring or non-informative
					String msg =
						values.size() + " values for column " + col
								+ ", record id " + j + ", source " + rvSource;
					log.finer(msg);
				}
				for (int i = 0; i < values.size(); i++) {
					Integer val = new Integer(values.get(i));
					BlockSet bs = (BlockSet) map.get(val);

					if (bs != null) {
						LongArrayList ids = bs.getRecordIDs();
						ids.add(recID);
					} else {
						bs = new BlockSet(col);
						LongArrayList ids = bs.getRecordIDs();
						ids.add(recID);
						map.put(val, bs);
					}
				}

			} else {
				// Typically boring: an invalid value in a non-stacked
				// field, or all invalid values in a stacked field
				String msg =
					"Null values for column " + col + ", record id " + j
							+ ", source " + rvSource;
				log.finer(msg);
			}

		} // end for
		if (map.size() == 0) {
			// Egregious
			String msg = "Added 0 (zero) blocks for column " + col;
			log.warning(msg);
		} else {
			// Informative
			String msg = "Added " + map.size() + " blocks for column " + col;
			log.fine(msg);
		}

		if (!stop) {
			count += writeBlocks(map, bSink, osGroup, col);
			// only keep oversized block row id on the rec_id, val_id file.
			removeIDs(osIDs, rvSource);
		}

		return count;
	}

	/**
	 * This method writes the BlockSet to either the block file or the oversized
	 * file depending on the block size.
	 *
	 * @param map
	 *            - hash map containing the record ID's group by value
	 * @param max
	 *            - maximum size of a BlockSet
	 * @param bSink
	 *            - BlockSet sink
	 * @param osSink
	 *            - Oversized BlockSet sink
	 * @param col
	 *            - the column we are currently blocking on
	 * @return
	 */
	private int writeBlocks(HashMap map, IBlockSink bSink,
			IOversizedGroup osGroup, int col) throws BlockingException {

		Iterator it = map.values().iterator();
		BlockSet bs;
		int currBlocks = 0;
		int currOversized = 0;

		while (it.hasNext()) {
			bs = (BlockSet) it.next();

			// check to see if this block is valid, no need to write out invalid
			// blocks
			if (validator.validBlockSet(bs)) {
				LongArrayList list = bs.getRecordIDs();

				if (list.size() > maxBlockSize) {

					// don't write the last blocking column, because we can't
					// trim it further.
					if (col < numBlockingFields - 1) {
						osGroup.writeBlock(bs);
						currOversized++;
						totalOversized++;
					} else {
						if (osDump != null)
							osDump.writeBlock(bs);
					}

					// keep track of all the oversized IDs
					osIDs.addAll(list);

				} else if (list.size() > 1) {
					bSink.writeBlock(bs);
					currBlocks++;
				}
			} else {
				if (bs.getRecordIDs().size() > 1) {
					numInvalid++;
				}
			}
		}

		log.info("Done with initial blocking on " + col + "; blocks: "
				+ currBlocks + "; oversized: " + currOversized);

		return currBlocks;
	}

	/**
	 * This method compares the given block to the record hash. It returns a
	 * hash map keyed by valueid and value of IntArrayList. This version allows
	 * for stacked data by using IntArrayList instead of Integer.
	 *
	 * @param bs
	 *            - Oversized BlockSet object to compare
	 * @param map
	 *            - map of record source.
	 * @return
	 */
	private static HashMap findMatching(BlockSet bs, RecordValue2 record) {
		HashMap match = new HashMap();

		LongArrayList recs = bs.getRecordIDs();

		// debug
		// System.out.println ("block " + recs);
		// System.out.println ("recs " + record.getList());

		for (int i = 0; i < recs.size(); i++) {
			long row = recs.get(i);

			// get the values of the given row
			IntArrayList values = (IntArrayList) record.get(row);

			if (values != null) {

				for (int j = 0; j < values.size(); j++) {

					Integer val = new Integer(values.get(j));
					Object obj = match.get(val);
					if (obj == null) {
						LongArrayList list = new LongArrayList();
						list.add(row);
						match.put(val, list);
					} else {
						LongArrayList list = (LongArrayList) obj;
						list.add(row);
					}
				}
			}
		}

		return match;
	}

	/**
	 * This method is the new algorithm that chop Oversized blocking sets into
	 * smaller ones. This algorithm works as follows:
	 *
	 * 1. Start with a list of Oversized blocks. 2a. Build a HashMap (record id,
	 * value id) of the whole data set on blocking field 2. 2b. Compare all
	 * Oversized blocks that blocks on field1 with 2a to find common rows.
	 * Create new blocking on for these rows. This set is either OK or get
	 * passed into multi field blocking later. 2c. Build a HashMap on blocking
	 * field 3, then field 4 , etc 2d. Compare 2c with single field blocks based
	 * on field1 or field2. 2e. In general, HashMap on fieldN only compares to
	 * single field blocks based on field k, where k < N. 3a. Build a HashMap on
	 * blocking field 3. 3b. Compare 3a with only two field blocks consist of
	 * field1 and field2. 3c. In general, compare HashMap on fieldN with 2-field
	 * blocks where k1 and k2 are both less than N. 4. Compared I-field blocks
	 * with HashMap on field N, where N > I, and each of the blocks has no
	 * column >= N.
	 *
	 * @param numField
	 *            - The number of blocking fields these oversized blocks have
	 * @param osGroupIn
	 *            - Input OversizedGroup
	 * @param osGroupOut
	 *            - Output OversizedGroup
	 * @param startPoint
	 *            - if this is not 0, it will load the corresponding rec,val
	 *            first.
	 * @return number of new blocks created
	 */
	private int blockOversized(int numField, IOversizedGroup osGroupIn,
			IOversizedGroup osGroupOut) throws BlockingException {

		int count = 0; // count the number of blocks
		totalOversized = 0; // number of oversized blocks

		int c = 0;

		for (int j = numField; j < rvSources.length && !stop; j++) {

			RecordValue2 records = new RecordValue2(rvSources[j]);

			// compare map to all Oversized blocks on field k < j.
			for (int k = 0; k < j && !stop; k++) {
				IBlockSource osSource = osGroupIn.getSource(k);

				osSource.open();

				while (osSource.hasNext() && !stop) {

					stop = ControlChecker.checkStop(control, ++c);

					BlockSet bs = osSource.next();

					// returns hashmap of value, IntArrayList
					HashMap matching = findMatching(bs, records);
					Iterator it = matching.keySet().iterator();

					boolean any = false;

					while (it.hasNext()) {
						Integer colVal = (Integer) it.next();

						BlockSet inter = new BlockSet();
						inter.addColumns(bs.getColumns());
						inter.addColumn(j);

						LongArrayList overlap =
							(LongArrayList) matching.get(colVal);
						inter.setRecordIDs(overlap);

						if (validator.validBlockSet(inter)) {
							if (inter.getRecordIDs().size() > maxBlockSize) {

								// if trimming by the last column, this set
								// cannot be further trimmed.
								// only write to oversized if j is not the last
								// column.
								if (j < rvSources.length - 1) {
									osGroupOut.writeBlock(inter);
									totalOversized++;
								}

							} else if (inter.getRecordIDs().size() > 1) {
								bSink.writeBlock(inter);
								count++;

							}
						} else {
							if (inter.getRecordIDs().size() > 1) {
								numInvalid++;
							}
						}
						any = true;
					} // end while

					// keeps oversized blocks with minFields or more fields that
					// can't be further trimmed
					if (!any) {
						if (numField >= minFields
								&& bs.getRecordIDs().size() <= maxOversized) {
							osSpecial.writeBlock(bs);
							numOS++;
						} else {
							if (osDump != null)
								osDump.writeBlock(bs);
						}
					}

				} // end while hasNext

				osSource.close();

			} // end for k

		} // end for j

		return count;
	}

}
