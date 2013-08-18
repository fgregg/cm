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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.choicemaker.cm.analyzer.filter.Filter;
import com.choicemaker.cm.analyzer.sampler.PairSampler;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.MutableMarkedRecordPair;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.OabaProperties;
import com.choicemaker.cm.io.blocking.automated.offline.core.ControlException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.filter.IMatchRecord2Filter;
import com.choicemaker.cm.io.xml.base.XmlMarkedRecordPairSink;

/**
 * This object takes a IMatchRecord2Source, RecordSource and a
 * MarkedRecordPairSink to create an MRPS file from the matched IDs.
 * 
 * It does the following (per processing batch):<ul>
 * <li>Builds a hash map of record ids that show up in IMatchRecord2Source</li>
 * <li>Gets the record objects from RecordSource</li>
 * <li>Outputs the records to MarkedRecordPairSink</li>
 * </ul>
 * @author pcheung (initial version)
 * @author rphall (limited memory version)
 */
public class MRPSCreator {

	private static final Logger log = Logger.getLogger(MRPSCreator.class);

	private static final int CONTROL_INTERVAL = 1000;
	private static final int DEBUG_INTERVAL = 10000;
	private static final String PRETTY_PRINT_INDENT = "   ";

	/**
	 * The prefix to the name of an insecure, temporary file, created in the user's temporary directory, to which
	 * intermediate Sampler results are dumped.
	 */
	private static final String DPV_RESULT_MRPSCREATOR_SAMPLER_DUMP_FILE_PREFIX =
		"SamplerDump";

	/**
	 * The suffix to the name of an insecure, temporary descriptor file, created in the user's temporary directory, to which
	 * intermediate Sampler results are dumped.
	 */
	private static final String DPV_RESULT_MRPSCREATOR_SAMPLER_DUMP_FILE_DESCRIPTOR_SUFFIX =
		".mrps";

	/**
	 * The suffix to the name of an insecure, temporary source file, created in the user's temporary directory, to which
	 * intermediate Sampler results are dumped.
	 */
	private static final String DPV_RESULT_MRPSCREATOR_SAMPLER_DUMP_FILE_SOURCE_SUFFIX =
		".xml";

	/**
	 *  Returns the directory of the MRPS data file, or if that value is not
	 * available, the user's home directory.
	 * @param mrps the MRPS file for final results
	 * @return may be null (in which case dumps are written to the user's temp directory)
	 */
	private static File getSamplerDumpDir(MarkedRecordPairSink mrps) {
		File retVal = null;
		if (mrps instanceof XmlMarkedRecordPairSink) {
			XmlMarkedRecordPairSink xmlMrps = (XmlMarkedRecordPairSink) mrps;
			String xmlDataFileName = xmlMrps.getXmlFileName();
			retVal = new File(xmlDataFileName).getParentFile();
		}
		if (retVal == null) {
			String userHome = System.getProperty("user.home");
			File userDir = new File(userHome);
		}
		return retVal;
	}

	/**
	 * Returns a XmlMarkedRecordPairSink to which intermediate Sampler results are dumped.
	 */
	private static XmlMarkedRecordPairSink getSamplerDumpFile(File dumpDir, ImmutableProbabilityModel immutableModel)
		throws IOException {
		File descriptor =
			File.createTempFile(
				DPV_RESULT_MRPSCREATOR_SAMPLER_DUMP_FILE_PREFIX,
				DPV_RESULT_MRPSCREATOR_SAMPLER_DUMP_FILE_DESCRIPTOR_SUFFIX,
				dumpDir);
		File source =
			File.createTempFile(
				DPV_RESULT_MRPSCREATOR_SAMPLER_DUMP_FILE_PREFIX,
				DPV_RESULT_MRPSCREATOR_SAMPLER_DUMP_FILE_SOURCE_SUFFIX,
				dumpDir);

		XmlMarkedRecordPairSink retVal =
			new XmlMarkedRecordPairSink(
				descriptor.getCanonicalPath(),
				source.getName(),
			immutableModel);
		return retVal;
	}

	private final IMatchRecord2Source mrSource;
	private final RecordSource masterRS;
	private final RecordSource stageRS;
	private final MarkedRecordPairSink mrps;
	private final int batchSize;
	private final IControl control;
	private final IMatchRecord2Filter preFilter;
	private final Filter postFilter;
	private final PairSampler sampler;

	/**
	 * Don't use directly.
	 * @see #incrementBatchNum()
	 * @see #getBatchNum()
	 */
	private transient int _batchNum;

	/**
	 * Don't use directly.
	 * @see #incrementPairCountNum()
	 * @see #getPairCountNum()
	 */
	private transient int _pairCount;

	private synchronized void incrementBatchNum() {
		++_batchNum;
	}

	public synchronized int getBatchNum() {
		return _batchNum;
	}

	private synchronized void incrementPairCount(int delta) {
		_pairCount += delta;
	}

	public synchronized int getPairCount() {
		return _pairCount;
	}

	/**
	 * An instance of IControl that always returns <code>false</code>
	 * to <code>shouldStop()</code>
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IControl
	 */
	public static IControl NO_CONTROL = new IControl() {
		public boolean shouldStop() {
			return false;
		}
	};

	/**
	 * An instance of Filter that passes all MatchRecord2 pairs
	 * @see com.choicemaker.cm.analyzer.filter.Filter
	 */
	public static IMatchRecord2Filter NO_PRE_FILTER =
		new IMatchRecord2Filter() {
		public boolean satisfy(MatchRecord2 mr2) {
			return true;
		}
	};

	/**
	 * An instance of Filter that passes all marked-record pairs
	 * @see com.choicemaker.cm.analyzer.filter.Filter
	 */
	public static Filter NO_POST_FILTER = new Filter() {
		public boolean satisfy(MutableMarkedRecordPair mrp) {
			return true;
		}
		public void resetLimiters() {
		}
	};

	public MRPSCreator(
		IMatchRecord2Source mrSource,
		RecordSource stageRS,
		RecordSource masterRS,
		MarkedRecordPairSink mrps) {
		this(
			mrSource,
			stageRS,
			masterRS,
			mrps,
			Integer.MAX_VALUE,
			NO_CONTROL,
			NO_PRE_FILTER,
			NO_POST_FILTER,
			null);
	}

	public MRPSCreator(
		IMatchRecord2Source mrSource,
		RecordSource stageRS,
		RecordSource masterRS,
		MarkedRecordPairSink mrps,
		int batchSize,
		IControl control,
		IMatchRecord2Filter mr2Filter,
		Filter mrpFilter,
		PairSampler sampler) {

		this.mrSource = mrSource;
		this.stageRS = stageRS;
		this.masterRS = masterRS;
		this.mrps = mrps;
		this.batchSize = batchSize;
		this.control = control;
		this.preFilter = mr2Filter;
		this.postFilter = mrpFilter;
		this.sampler = sampler;

		// Check post-conditions after blank finals are initialized
		if (this.mrSource == null) {
			throw new IllegalArgumentException("null IMatchRecord2Source");
		}
		if (this.stageRS == null) {
			throw new IllegalArgumentException("null staging record source");
		}
		if (this.mrps == null) {
			throw new IllegalArgumentException("null Marked Record Pair Sink");
		}
		if (this.batchSize <= 0) {
			throw new IllegalArgumentException("non-positive batch size");
		}
		if (this.control == null) {
			throw new IllegalArgumentException("null control object");
		}
		if (this.preFilter == null) {
			throw new IllegalArgumentException("null MatchRecord2 filter");
		}
		if (this.postFilter == null) {
			throw new IllegalArgumentException("null marked-record pair filter");
		}
	}

	/** This method creates the mrps file from the Match Record Source. */
	public void createMRPS() throws BlockingException, ControlException {

		try {

			this.mrSource.open();
			this.mrps.open();

			while (mrSource.hasNext()) {

				incrementBatchNum();

				List matchRecords = new ArrayList();
				Set stageIDSet = new HashSet();
				Set masterIDSet = new HashSet();
				getPairsAndIds(
					this.mrSource,
					this.preFilter,
					matchRecords,
					stageIDSet,
					masterIDSet,
					this.control,
					batchSize);
				incrementPairCount(matchRecords.size());
				if (log.isInfoEnabled()) {
					log.info("Batch number: " + getBatchNum());
					log.info(
						PRETTY_PRINT_INDENT
							+ "Number of pairs: "
							+ matchRecords.size());
					log.info(
						PRETTY_PRINT_INDENT
							+ "Number of staging ids: "
							+ stageIDSet.size());
					log.info(
						PRETTY_PRINT_INDENT
							+ "Number of master ids: "
							+ masterIDSet.size());
				}

				Map stageMap = new HashMap();
				readRecords(stageMap, stageIDSet, stageRS, this.control);
				stageIDSet = null;

				Map masterMap = new HashMap();
				if (masterRS != null) {
					readRecords(masterMap, masterIDSet, masterRS, this.control);
				}
				masterIDSet = null;

				Iterator mrIter = matchRecords.iterator();
				writeMRPS(
					this.mrps,
					mrIter,
					this.postFilter,
					stageMap,
					masterMap,
					this.control,
					this.sampler);

				if (this.sampler != null && OabaProperties.isSamplerDumped()) {
					try {
						File dumpDir = getSamplerDumpDir(mrps);
						XmlMarkedRecordPairSink dump =
							getSamplerDumpFile(dumpDir,mrps.getModel());
						String descriptor = dump.getName();
						String xmlData = dump.getXmlFileName();
						log.info(
							"Dumping sampler to '"
								+ descriptor
								+ "' : '"
								+ xmlData
								+ "'...");
						dump.open();
						List retainedPairs = this.sampler.getRetainedPairs();
						for (Iterator i = retainedPairs.iterator();
							i.hasNext();
							) {
							MutableMarkedRecordPair mrp =
								(MutableMarkedRecordPair) i.next();
							dump.putMarkedRecordPair(mrp);
						}
						dump.close();
						log.info(
							"...Finished dumping sampler to '"
								+ descriptor
								+ "' : '"
								+ xmlData
								+ "'");
					} catch (Exception x) {
						log.warn("Sampler could not be dumped", x);
					}
				}

				// clean up the list and hashMaps
				matchRecords = null;
				stageMap = null;
				masterMap = null;

			}

			if (this.sampler == null) {

				// If there is no sampler, just log the total number of pairs
				if (log.isInfoEnabled()) {
					log.info("Total number of pairs: " + getPairCount());
				}

			} else {

				// If there is a sampler, output the sampled pairs
				int count = 0;
				List retainedPairs = this.sampler.getRetainedPairs();
				for (Iterator i = retainedPairs.iterator(); i.hasNext();) {
					MutableMarkedRecordPair mrp =
						(MutableMarkedRecordPair) i.next();
					this.mrps.putMarkedRecordPair(mrp);
					++count;
					log.info(
						">>>>>>>>>>>>Adding pair: "
							+ mrp.getQueryRecord().getId()
							+ ","
							+ mrp.getMatchRecord().getId());
				}
				if (log.isInfoEnabled()) {
					log.info("Total number of (sampled) pairs: " + count);
				}

			}

		} catch (RemoteException e) {
			throw new BlockingException(e.toString());
		} catch (IOException e) {
			throw new BlockingException(e.toString());
		} finally {
			try {
				mrSource.close();
			} catch (Exception x) {
				log.warn("Unable to close mrSource: " + x.getMessage());
			}
			try {
				mrps.close();
			} catch (Exception x) {
				log.warn("Unable to close mrps: " + x.getMessage());
			}
		}

	} // createMRPS()

	/**
	 * This creates a distinct set of record ids for master and staging
	 * record sources.
	 * @param mrSource an already opened source
	 * @param matchRecords an empty list to hold MatchRecord2 instances
	 * @param stageIDSet an empty set to hold staging ids
	 * @param masterIDSet an empty set to hold master ids
	 * @param batchSize the number of pairs to read from the source
	 * during this processing step
	 * @throws BlockingException
	 */
	private static void getPairsAndIds(
		IMatchRecord2Source mrSource,
		IMatchRecord2Filter preFilter,
		List matchRecords,
		Set stageIDSet,
		Set masterIDSet,
		IControl control,
		int batchSize)
		throws BlockingException, ControlException, RemoteException {

		// Preconditions
		if (mrSource == null) {
			throw new IllegalArgumentException("null source");
		}
		if (matchRecords == null || matchRecords.size() != 0) {
			throw new IllegalArgumentException("null or non-empty list");
		}
		if (stageIDSet == null || stageIDSet.size() != 0) {
			throw new IllegalArgumentException("null or non-empty stage set");
		}
		if (masterIDSet == null || masterIDSet.size() != 0) {
			throw new IllegalArgumentException("null or non-empty master set");
		}
		if (control == null) {
			throw new IllegalArgumentException("null control object");
		}
		if (batchSize <= 0) {
			throw new IllegalArgumentException("non-positive batch size");
		}

		MatchRecord2 mr = null;
		Comparable id1 = null;
		Comparable id2 = null;
		char source = '\0';

		while (matchRecords.size() < batchSize && mrSource.hasNext()) {

			if (matchRecords.size() % CONTROL_INTERVAL == 0
				&& control.shouldStop()) {
				throw new ControlException("Aborted in getPairsAndIds");
			}

			mr = mrSource.getNext();
			id1 = mr.getRecordID1();
			id2 = mr.getRecordID2();
			source = mr.getRecord2Source();
			if (preFilter.satisfy(mr)) {

				matchRecords.add(mr);

				//add id1, which is always a staging id.
				if (!stageIDSet.contains(id1)) {
					stageIDSet.add(id1);
				}

				if (source == MatchRecord2.MASTER_SOURCE) {
					//master
					if (!masterIDSet.contains(id2)) {
						masterIDSet.add(id2);
					}
				} else if (source == MatchRecord2.STAGE_SOURCE) {
					//stage
					if (!stageIDSet.contains(id2)) {
						stageIDSet.add(id2);
					}
				} else {
					throw new BlockingException(
						"Invalid record2Source " + source);
				}

			} // if preFilter
		} // while

	}

	private static void writeMRPS(
		MarkedRecordPairSink mrps,
		Iterator mrIter,
		Filter postFilter,
		Map stageMap,
		Map masterMap,
		IControl control,
		PairSampler sampler)
		throws BlockingException, ControlException, IOException, RemoteException {

		// Preconditions
		if (mrps == null) {
			throw new IllegalArgumentException("null mrps");
		}
		if (mrIter == null) {
			throw new IllegalArgumentException("null mrIter");
		}
		if (stageMap == null) {
			throw new IllegalArgumentException("null stage map");
		}
		if (masterMap == null) {
			throw new IllegalArgumentException("null master map");
		}
		if (control == null) {
			throw new IllegalArgumentException("null control object");
		}

		// Stage map shouldn't be empty (usually), but master map may be
		if (stageMap.isEmpty()) {
			log.warn("empty stage map");
		}
		if (masterMap.isEmpty() && log.isInfoEnabled()) {
			log.info("empty master map");
		}

		MutableMarkedRecordPair mrp;
		MatchRecord2 mr;
		Record r1, r2;
		Comparable id1, id2;
		char source;
		Decision decision = null;
		Date date = new Date(System.currentTimeMillis());

		int count = 0;
		int count2 = 0;

		while (mrIter.hasNext()) {

			if (count % CONTROL_INTERVAL == 0 && control.shouldStop()) {
				throw new ControlException("Aborted in writeMRPS");
			}

			mr = (MatchRecord2) mrIter.next();
			count++;

			id1 = mr.getRecordID1();
			id2 = mr.getRecordID2();
			source = mr.getRecord2Source();

			//get record2 1 and 2
			r1 = (Record) stageMap.get(id1);
			if (source == MatchRecord2.MASTER_SOURCE) {
				r2 = (Record) masterMap.get(id2);
			} else if (source == MatchRecord2.STAGE_SOURCE) {
				r2 = (Record) stageMap.get(id2);
			} else {
				throw new BlockingException(
					"Invalid record2Source " + mr.getRecord2Source());
			}

			//get decision
			if (mr.getMatchType() == MatchRecord2.MATCH) {
				decision = Decision.MATCH;
			} else if (mr.getMatchType() == MatchRecord2.HOLD) {
				decision = Decision.HOLD;
			}

			String comment = "" + mr.getProbability();
			mrp =
				new MutableMarkedRecordPair(
					r1,
					r2,
					decision,
					date,
					"",
					"",
					comment);
			if (postFilter.satisfy(mrp)) {
				if (sampler == null) {
					mrps.putMarkedRecordPair(mrp);
				} else {
					sampler.processPair(mrp);
				}
				count2++;
			}

		}

		if (sampler == null) {
			log.info(
				PRETTY_PRINT_INDENT
					+ count2
					+ " pairs added to the MRPS file.");
		} else {
			log.info(
				PRETTY_PRINT_INDENT + count2 + " pairs added to the sampler.");
		}
	}

	/** This method takes in a set of record ids and a records source.
	 * Each record with id in the set is put on the output hash map.
	 * 
	 * @param ids
	 * @param rs
	 * @return
	 * @throws BlockingException
	 */
	private void readRecords(
		Map map,
		Set ids,
		RecordSource rs,
		IControl control)
		throws BlockingException, ControlException, IOException, RemoteException {

		// Preconditions
		if (map == null || !map.isEmpty()) {
			throw new IllegalArgumentException("null or non-empty map");
		}
		if (ids == null) {
			throw new IllegalArgumentException("null id set");
		}
		if (rs == null) {
			throw new IllegalArgumentException("null record set");
		}
		if (control == null) {
			throw new IllegalArgumentException("null control object");
		}

		// id set shouldn't be empty (usually)
		if (ids.isEmpty()) {
			log.warn("empty id set");
		}

		try {
			Record r = null;
			Comparable c = null;
			//int count = 0;
			int count2 = 0;
			final int size = ids.size();

			try {
				rs.open();
			} catch (IOException x) {
				String msg = "Unable to open record source: " + x.getMessage();
				log.error(msg);
				throw new BlockingException(msg);
			}

			//while ((count < size) && rs.hasNext()) {
			while ((map.size() < size) && rs.hasNext()) {

				if (count2++ % CONTROL_INTERVAL == 0 && control.shouldStop()) {
					throw new ControlException("Aborted in readRecords");
				}

				count2++;
				r = rs.getNext();
				c = r.getId();
				if (ids.contains(c)) {
					map.put(c, r);
					//count++;
				}

				if (log.isDebugEnabled() && count2 % DEBUG_INTERVAL == 0) {
					log.debug(
						PRETTY_PRINT_INDENT
							+ PRETTY_PRINT_INDENT
							+ count2
							+ " "
							+ map.size());
				}
			}

			if (map.size() != size)
				throw new BlockingException(
					"Could not find all match records in the record source.  Set size = "
						+ size
						+ " records found = "
						+ map.size());

		} finally {
			try {
				rs.close();
			} catch (Exception x) {
				String msg = "Unable to close record source: " + x.getMessage();
				log.warn(msg);
			}
		} // finally

	} // readRecords(Map,Set,RecordSource)

} // MRPSCreator

