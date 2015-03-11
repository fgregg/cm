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
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IControl;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSink;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IIDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;
import com.choicemaker.util.LongArrayList;

/**
 * This version takes in blocks that contains internal id instead of the record
 * id.
 * 
 * This service creates does the following: 1. Read in blocks and/or oversized
 * blocks to create chunk id files in internal ids. 2. Read in the record
 * source, translator and chunk id files to create chunk data files. 3. Create
 * comparing block groups.
 * 
 * This version is more abstracted. It takes in IISSetSource instead of
 * IBlockSource. It also uses transformers to write internal id arrays/trees to
 * record id arrays/trees.
 * 
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class ChunkService3 {

	private static final String DELIM = "|";

	private static final Logger log = Logger.getLogger(ChunkService3.class
			.getName());

	protected static final String SOURCE = "ChunkService3";

	private IIDSetSource bSource;
	private IIDSetSource osSource;
	private RecordSource stage;
	private RecordSource master;
	private ImmutableProbabilityModel model;

	// these two variables are used to stop the program in the middle
	private IControl control;
	private boolean stop;

	// transformer for the regular blocks.
	private ITransformer transformer;

	// transformer for the oversized blocks.
	private ITransformer transformerO;

	private IChunkRecordIdSinkSourceFactory recIDFactory;
	private IChunkDataSinkSourceFactory stageSinkFactory;
	private IChunkDataSinkSourceFactory masterSinkFactory;

	private ProcessingEventLog status;
	private int maxChunkSize;

	private int splitIndex;

	private ArrayList recIDSinks = new ArrayList(); // list of chunk id sinks

	// private int totalBlocks = 0;
	private int numChunks = 0;
	private int maxFiles = 0;

	/**
	 * There are two types of chunks, regular and oversized.
	 * <pre>
	 * numOS = numChunks - numRegularChunks;
	 * </pre>
	 * 
	 */
	private int numRegularChunks = 0;

	// FIXME Define system property to control this setting
	private boolean keepFiles = false;

	private long time; // this keeps track of time

	/**
	 * This version of the constructor takes in a block source and oversized
	 * block source.
	 * 
	 * @param bSource
	 *            - block source
	 * @param osSource
	 *            - oversized block source
	 * @param stage
	 *            - stage record source
	 * @param master
	 *            - master record source
	 * @param accessProvider
	 *            - probability accessProvider
	 * @param recIDFactory
	 *            - this factory creates chunk id files
	 * @param stageSinkFactory
	 *            - this factory creates chunk data files for the staging data
	 * @param masterSinkFactory
	 *            - this factory creates chunk data files for the master data
	 * @param splitIndex
	 *            - This indicates when the internal goes from stage to master/
	 * @param transformer
	 *            - ID transformer for the regular blocks
	 * @param transformerO
	 *            - ID transformer for the oversized blocks
	 * @param maxChunkSize
	 *            - maximum size of a chunk
	 * @param maxFiles
	 *            - maximum number of files to open.
	 * @param status
	 *            - status of the system
	 */
	public ChunkService3(IIDSetSource bSource, IIDSetSource osSource,
			RecordSource stage, RecordSource master,
			ImmutableProbabilityModel model,
			IChunkRecordIdSinkSourceFactory recIDFactory,
			IChunkDataSinkSourceFactory stageSinkFactory,
			IChunkDataSinkSourceFactory masterSinkFactory, int splitIndex,
			ITransformer transformer, ITransformer transformerO,
			int maxChunkSize, int maxFiles, ProcessingEventLog status,
			IControl control) {

		this.bSource = bSource;
		this.osSource = osSource;
		this.stage = stage;
		this.master = master;
		this.model = model;
		this.transformer = transformer;
		this.transformerO = transformerO;
		this.recIDFactory = recIDFactory;
		this.stageSinkFactory = stageSinkFactory;
		this.masterSinkFactory = masterSinkFactory;
		this.maxChunkSize = maxChunkSize;
		this.maxFiles = maxFiles;
		this.status = status;
		this.splitIndex = splitIndex;

		this.control = control;
		this.stop = false;
	}

	public int getNumChunks() {
		return numChunks;
	}

	public int getNumRegularChunks() {
		return numRegularChunks;
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
	 * This method runs the service.
	 * 
	 * @throws IOException
	 */
	public void runService() throws BlockingException {

		final String METHOD = "runService()";
		log.entering(SOURCE, METHOD);
		time = System.currentTimeMillis();

		if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_DONE_CREATE_CHUNK_DATA) {
			// just need to recover numChunks for the matching step
			final String s = status.getCurrentProcessingEventInfo();
			log.info("numChunks, numRegularChunks: " + s);
			StringTokenizer temp = new StringTokenizer(s, DELIM);
			numChunks = Integer.parseInt(temp.nextToken());
			numRegularChunks = Integer.parseInt(temp.nextToken());
			log.info("Recovery, numChunks " + numChunks + " numRegularChunks "
					+ numRegularChunks);

		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_DONE_DEDUP_OVERSIZED) {
			// create ids
			log.info("Creating ids for block source " + bSource.getInfo());
			createIDs(bSource, false, 0, transformer);
			numRegularChunks = numChunks;

			if (osSource != null && osSource.exists()) {
				log.info("Creating ids for oversized block source "
						+ osSource.getInfo());
				int count = createIDs(osSource, true, 0, transformerO);
				if (count == 0) {
					transformerO.cleanUp();
				}
			}

			if (!stop) {
				createDataFiles();
			}

		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_DONE_CREATE_CHUNK_IDS
				|| status.getCurrentProcessingEventId() == OabaProcessing.EVT_CREATE_CHUNK_OVERSIZED_IDS) {

			// create the chunk data files
			StringTokenizer temp =
				new StringTokenizer(status.getCurrentProcessingEventInfo(), DELIM);
			numChunks = Integer.parseInt(temp.nextToken());
			numRegularChunks = Integer.parseInt(temp.nextToken());
			log.info("Recovery, numChunks " + numChunks + " numRegularChunks "
					+ numRegularChunks);

			recoverCreateIDs(numChunks);
			createDataFiles();

		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_CREATE_CHUNK_IDS) {
			// time to create Oversized ID files
			if (osSource != null && osSource.exists()) {
				log.info("Creating ids for oversized block source "
						+ osSource.getInfo());
				int count = createIDs(osSource, true, 0, transformerO);
				if (count == 0) {
					transformerO.cleanUp();
				}
			}

			if (!stop) {
				createDataFiles();
			}

		}

		time = System.currentTimeMillis() - time;
	}

	/**
	 * This method makes sure that the program doesn't overwrite the existing
	 * files. It flushes the factories by calling getNext ().
	 *
	 */
	private void recoverCreateIDs(int numFiles) throws BlockingException {
		for (int i = 0; i < numFiles; i++) {
			recIDSinks.add(recIDFactory.getNextSink());
		}
	}

	/**
	 * This method creates the chunk data files for stage and master record
	 * sources.
	 * 
	 * @throws IOException
	 * @throws XmlConfException
	 */
	private void createDataFiles() throws BlockingException {
		try {
			// list of rows files to handle. The rows files is already sorted.
			IChunkRecordIdSource[] crSources =
				new IChunkRecordIdSource[numChunks];

			// record sinks for the stage file we are handling, one per chunk
			RecordSink[] stageRecordSinks = new RecordSink[numChunks];

			// record sinks for the master file we are handling, one per chunk
			RecordSink[] masterRecordSinks = new RecordSink[numChunks];

			// the current record id of the chunk file
			long[] ind = new long[numChunks];

			// set up
			for (int i = 0; i < numChunks; i++) {
				IChunkRecordIdSink recSink =
					(IChunkRecordIdSink) recIDSinks.get(i);

				// read in rows file.
				crSources[i] = recIDFactory.getSource(recSink);

				stageRecordSinks[i] = stageSinkFactory.getNextSink();

				masterRecordSinks[i] = masterSinkFactory.getNextSink();
			} // end for

			int start = 0;
			int end = maxFiles;

			if (numChunks <= maxFiles) {
				end = numChunks;
				createDataFiles(start, end, crSources, stageRecordSinks, 0,
						ind, stage, model);

				if (master != null) {
					createDataFiles(start, end, crSources, masterRecordSinks,
							splitIndex, ind, master, model);
				} else {
					openMaster(masterRecordSinks);
				}

			} else {
				while (start < numChunks) {
					createDataFiles(start, end, crSources, stageRecordSinks, 0,
							ind, stage, model);

					if (master != null) {
						createDataFiles(start, end, crSources,
								masterRecordSinks, splitIndex, ind, master,
								model);
					} else {
						openMaster(masterRecordSinks);
					}

					start = end;
					end = end + maxFiles;
					if (end > numChunks)
						end = numChunks;
				}
			}

			if (!stop) {
				String temp =
					Integer.toString(numChunks) + DELIM
							+ Integer.toString(numRegularChunks);
				status.setCurrentProcessingEvent(OabaProcessingEvent.DONE_CREATE_CHUNK_DATA,
						temp);

				if (!keepFiles) {
					// remove all the chunk record id files
					for (int i = 0; i < numChunks; i++) {
						IChunkRecordIdSink recIDSink =
							(IChunkRecordIdSink) recIDSinks.get(i);
						recIDSink.remove();
					}
				}

				recIDSinks = null;
			}

		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}

	}

	/**
	 * This method just opens the sink so that a empty file will be created for
	 * the master sink. This is necessary because matching requires empty files.
	 * 
	 * @param masterRecordSinks
	 * @throws IOException
	 */
	private void openMaster(RecordSink[] masterRecordSinks) throws IOException {
		int s = masterRecordSinks.length;
		for (int i = 0; i < s; i++) {
			masterRecordSinks[i].open();
			masterRecordSinks[i].close();
		}
	}

	/**
	 * This method write out chunk data for elements in the arrays from start to
	 * end.
	 * 
	 * @param start
	 *            - The location in the array to start writing. Inclusive.
	 * @param end
	 *            - The location in the array to stop writing. Exclusive.
	 * @param crSources
	 *            - The array containing chunk record ids.
	 * @param recordSinks
	 *            - The record sink to which to write the data.
	 * @param ind
	 *            - The array the contains the current chunk record id.
	 * @param rs
	 *            - record source
	 * @param accessProvider
	 *            - ImmutableProbabilityModel of the record source.
	 * @throws BlockingException
	 * @throws XmlConfException
	 * @throws IOException
	 */
	private void createDataFiles(int start, int end,
			IChunkRecordIdSource[] crSources, RecordSink[] recordSinks,
			int offset, long[] ind, RecordSource rs,
			ImmutableProbabilityModel model) throws BlockingException {

		log.fine("starting " + start + " ending " + end);
		final String METHOD = "createDataFiles(..)";
		log.entering(SOURCE, METHOD, new Object[] {
				start, end, offset });
		assert rs != null;
		assert model != null;
		assert ind != null;
		assert crSources != null;
		assert recordSinks != null;

		// set up
		for (int i = start; i < end; i++) {
			log.finer("opening chunk record source[" + i + "]");
			crSources[i].open();
			if (crSources[i].hasNext()) {
				ind[i] = crSources[i].next();
				log.finer("starting record index of chunk source[" + i + "]: "
						+ ind[i]);
			} else {
				log.severe("missing record indices for chunk source[" + i + "]");
			}
			log.finer("opening record sink[" + i + "]");
			try {
				recordSinks[i].open();
			} catch (IOException e) {
				throw new BlockingException(e.getMessage(), e);
			}
		} // end for

		createDataFile(rs, model, start, end, offset, ind, crSources,
				recordSinks);

		// close sinks and sources
		for (int i = start; i < end; i++) {
			// close the chunk data sink
			log.finer("closing record sink[" + i + "]");
			try {
				recordSinks[i].close();
			} catch (IOException e) {
				log.warning(e.toString());
			}

			// close the record id source
			log.finer("closing chunk record source[" + i + "]");
			crSources[i].close();
		}

	}

	/**
	 * This method creates the chunk data files from the chunk id files in the
	 * range.
	 * 
	 * @param rs
	 *            - the record source
	 * @param accessProvider
	 *            - the probability accessProvider
	 * @param start
	 *            - the chunk id file to start from
	 * @param end
	 *            - the chunk id file to end with, excluding itself
	 * @param offset
	 *            - This is offset of the internal id. Master file's offset is
	 *            the number stageng records. Stage file has offset of 0.
	 * @param ind
	 *            - array of current id in the chunk id file
	 * @param crSources
	 *            - array of chunk id files
	 * @param recordSinks
	 *            - chunk data files to which to write the data
	 * @throws BlockingException
	 * @throws XmlConfException
	 */
	private void createDataFile(RecordSource rs,
			ImmutableProbabilityModel model, int start, int end, int offset,
			long[] ind, IChunkRecordIdSource[] crSources,
			RecordSink[] recordSinks) throws BlockingException {

		final String METHOD = "createDataFile(..)";
		log.entering(SOURCE, METHOD, new Object[] {
				start, end, offset });
		assert rs != null;
		assert model != null;
		assert ind != null;
		assert crSources != null;
		assert recordSinks != null;

		String context = null;
		try {
			rs.setModel(model);
			context = "opening record source (" + rs.toString() + ")";
			rs.open();

			// count is the index of a record in the record source
			int count = offset;

			// read each source record and check each of the files.
			context =
				"checking next record[" + count + "] from source ("
						+ rs.toString() + ")";
			while (rs.hasNext() && !stop) {
				context =
					"retrieving next record[" + count + "] from source ("
							+ rs.toString() + ")";
				Record r = rs.getNext();

				// for each chunk data file, check if this record belongs there.
				for (int i = start; i < end; i++) {
					// make sure the ind[i] is in the same range
					while (ind[i] < count && crSources[i].hasNext()) {
						ind[i] = crSources[i].next();
					}

					if (ind[i] == count) {
						context =
							"writing record[" + count + "] to sink[" + i + "]";
						recordSinks[i].put(r);
						// recordSinks[i].flush(); // FIXME REMOVEME ?
						if (crSources[i].hasNext()) {
							ind[i] = crSources[i].next();
						}
					}
				}
				count++;
				stop = ControlChecker.checkStop(control, count);
				context =
					"checking next record[" + count + "] from source ("
							+ rs.toString() + ")";
			}

			context = "closing record source (" + rs.toString() + ")";
			rs.close();
		} catch (IOException ex) {
			String msg = "Error while " + context + ": " + ex.toString();
			log.severe(msg);
			throw new BlockingException(msg);
		}
		log.exiting(SOURCE, METHOD);
	}

	/**
	 * This method creates the smaller block sink files and rec id files. These
	 * files correspond to a single chunk.
	 * 
	 * @param source
	 *            - block source
	 * @param isOS
	 *            - true if we are processing the oversized file
	 * @param skip
	 *            - number of blocks to skip
	 * @throws IOException
	 */
	private int createIDs(IIDSetSource source, boolean isOS, int skip,
			ITransformer transformer) throws BlockingException {
		// initialize the translator
		transformer.init();

		source.open();

		// this stores the unique recID's in a chunk
		TreeSet rows = new TreeSet();

		IChunkRecordIdSink recIDSink = recIDFactory.getNextSink();
		recIDSinks.add(recIDSink);

		int count = 0;
		int countAll = 0;

		// skipping
		while ((count < skip) && (source.hasNext())) {
			source.next();
			count++;
		}

		count = 0;
		while (source.hasNext() && !stop) {
			count++;
			// totalBlocks ++;
			countAll++;

			stop = ControlChecker.checkStop(control, countAll);

			IIDSet bs = source.next();
			LongArrayList block = bs.getRecordIDs();

			// add to the set of distinct record ids
			for (int i = 0; i < block.size() && !stop; i++) {
				// put the internal id in the set
				Long I = new Long(block.get(i));
				if (!rows.contains(I))
					rows.add(I);
			}

			// transform and write out array or tree
			transformer.transform(bs);

			// when the hashset gets too big, clear it and start a new file
			if (rows.size() > maxChunkSize) {
				// write the ids to sink
				writeChunkRows(recIDSink, rows);

				log.info(recIDSink.getInfo() + " has " + count + " blocks "
						+ rows.size() + " rows");

				// write status
				numChunks++;
				String temp =
					Integer.toString(numChunks) + OabaProcessing.DELIMIT
							+ Integer.toString(skip + countAll);
				if (isOS)
					status.setCurrentProcessingEvent(
							OabaProcessingEvent.CREATE_CHUNK_OVERSIZED_IDS, temp);
				else
					status.setCurrentProcessingEvent(OabaProcessingEvent.CREATE_CHUNK_IDS, temp);

				// use the next sink
				transformer.useNextSink();

				// create a new recIDSink
				recIDSink = recIDFactory.getNextSink();
				recIDSinks.add(recIDSink);

				// reset variables
				rows = new TreeSet();
				count = 0;
			}

		} // end while

		source.close();

		// One last write to sink
		if (rows.size() > 0 && !stop) {
			writeChunkRows(recIDSink, rows);
			log.info(recIDSink.getInfo() + " has " + count + " blocks "
					+ rows.size() + " rows");

			numChunks++;

			if (isOS) {
				String temp =
					Integer.toString(numChunks) + DELIM
							+ Integer.toString(numRegularChunks);
				status.setCurrentProcessingEvent(
						OabaProcessingEvent.CREATE_CHUNK_OVERSIZED_IDS, temp);
			} else {
				String temp = Integer.toString(numChunks);
				status.setCurrentProcessingEvent(OabaProcessingEvent.CREATE_CHUNK_IDS, temp);
			}
		}

		// cleanup
		if (!keepFiles)
			source.delete();

		transformer.close();

		return countAll;
	}

	/**
	 * This method writes the ids in the tree set to the sink. The ids are
	 * written in ascending order.
	 * 
	 * @param recSink
	 *            - chunk record id sink
	 * @param rows
	 *            - hash set containing the distinct ids
	 * @throws IOException
	 */
	private static void writeChunkRows(IChunkRecordIdSink recSink, TreeSet rows)
			throws BlockingException {
		recSink.open();

		Iterator it = rows.iterator();
		long id;
		while (it.hasNext()) {
			id = ((Long) it.next()).longValue();
			recSink.writeRecordID(id);
		}

		recSink.close();
	}

	@Override
	public String toString() {
		return "ChunkService3 [model=" + model + ", status=" + status
				+ ", maxChunkSize=" + maxChunkSize + ", splitIndex="
				+ splitIndex + ", recIDSinks=" + recIDSinks + ", numChunks="
				+ numChunks + ", maxFiles=" + maxFiles + ", numRegularChunks="
				+ numRegularChunks + ", keepFiles=" + keepFiles + "]";
	}

}
