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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.logging.Logger;

import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.io.blocking.automated.BlockingAccessor;
import com.choicemaker.cm.io.blocking.automated.IBlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.IBlockingField;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;
import com.choicemaker.cm.io.blocking.automated.IDbField;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;
import com.choicemaker.util.IntArrayList;

/**
 * This object performs the creation of rec_id, val_id pairs.
 * 
 * It uses input record id to internal id translation. As a result, it can use a
 * array instead of hashmap to stored the val_id's. This also uses dbField
 * instead of blocking fields to prep for swap.
 *
 * This takes in a stage record source and master record source. It translates
 * the stage record source first, then the master record source.
 *
 * Version 2 allows the record ID to be Integer, Long, or String.
 *
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class RecValService2 {

	private static final Logger log = Logger.getLogger(RecValService2.class
			.getName());

	private static int INTERVAL = 100000;

	private final RecordSource master;
	private final RecordSource stage;
	private final ImmutableProbabilityModel model;
	private final String blockingConfiguration;
	private final String databaseConfiguration;
	private final int numBlockFields;
	private final IRecValSinkSourceFactory rvFactory;
	private final MutableRecordIdTranslator mutableTranslator;
	private final ProcessingEventLog status;

	private IBlockingConfiguration bc;
	private IRecValSink[] sinks;
	private int stageType = -1;
	private boolean firstStage = true;
	private boolean firstMaster = true;

	private long time; // this keeps track of time

	/**
	 * This constructor take these parameters:
	 *
	 * @param stage
	 *            - stage record source of the data
	 * @param master
	 *            - master record source of the data. This can be null.
	 * @param accessProvider
	 *            - probability accessProvider of the data
	 * @param rvFactory
	 *            - factory to get RecValSinks
	 * @param mutableTranslator
	 *            - record ID to internal id mutableTranslator
	 * @param blockName
	 *            - blocking configuration name in the schema
	 * @param dbConf
	 *            - db configuration in the schema
	 * @param status
	 *            - current status of the system
	 */
	public RecValService2(RecordSource stage, RecordSource master,
			ImmutableProbabilityModel model, String blockName, String dbConf,
			IRecValSinkSourceFactory rvFactory,
			MutableRecordIdTranslator translator, ProcessingEventLog status) {

		this.stage = stage;
		this.master = master;
		this.model = model;
		this.blockingConfiguration = blockName;
		this.databaseConfiguration = dbConf;
		this.mutableTranslator = translator;
		this.rvFactory = rvFactory;
		this.status = status;

		// this.blockName = blockName;
		// this.dbConf = dbConf;

		BlockingAccessor ba = (BlockingAccessor) model.getAccessor();
		IBlockingConfiguration bc =
			ba.getBlockingConfiguration(blockingConfiguration,
					databaseConfiguration);
		IBlockingField[] bfs = bc.getBlockingFields();

		// print blocking info
		for (int i = 0; i < bfs.length; i++) {
			IDbField field = bfs[i].getDbField();
			log.info("i " + i + " field " + field.getName() + " number "
					+ field.getNumber());
		}

		this.numBlockFields = countFields(bfs);

		// System.out.println ("Numfields " + numBlockFields);
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

	public int getNumBlockingFields() {
		return numBlockFields;
	}

	/**
	 * This returns the type of stage record id. It is one of the three:
	 * Constants.TYPE_INTEGER, Constants.TYPE_LONG, or Constants.TYPE_STRING.
	 *
	 * It returns -1 if there was not staging data.
	 *
	 * @return
	 */
	public int getStageType() {
		return stageType;
	}

	/**
	 * This returns the type of master record id. It is one of the three:
	 * Constants.TYPE_INTEGER, Constants.TYPE_LONG, or Constants.TYPE_STRING.
	 *
	 * It returns -1 if there was not master data.
	 *
	 * @return
	 */
	public int getMasterType() {
		return stageType;
	}

	/**
	 * This method runs the service.
	 *
	 *
	 */
	public void runService() throws BlockingException {
		time = System.currentTimeMillis();

		if (status.getCurrentProcessingEventId() >= OabaProcessing.EVT_DONE_REC_VAL
				&& status.getCurrentProcessingEventId() < OabaProcessing.EVT_DONE_REVERSE_TRANSLATE_OVERSIZED) {

			// need to initialize
			log.info("recover rec,val files and mutableTranslator");
			init();

			// } else if (status.getCurrentOabaEventId() <
			// OabaProcessing.EVT_CREATE_REC_VAL) {
			// // create the rec_id, val_id files
			// log.info("Creating new rec,val files");
			// status.setCurrentOabaEvent(OabaProcessingEvent.CREATE_REC_VAL);
			// createFiles();
			// status.setCurrentOabaEvent(OabaProcessingEvent.DONE_REC_VAL);
			//
			// } else if (status.getCurrentOabaEventId() ==
			// OabaProcessing.EVT_CREATE_REC_VAL) {
			// log.info("Trying to recover rec,val files");
			//
			// // started to created, but not done, so we need to recover
			// status.setCurrentOabaEvent(OabaProcessingEvent.CREATE_REC_VAL);
			// recoverFiles();
			// status.setCurrentOabaEvent(OabaProcessingEvent.DONE_REC_VAL);
			// }

		} else if (status.getCurrentProcessingEventId() < OabaProcessing.EVT_DONE_REC_VAL) {
			// create the rec_id, val_id files
			log.info("Creating new rec,val files");
			status.setCurrentProcessingEvent(OabaProcessingEvent.CREATE_REC_VAL);
			createFiles();
			status.setCurrentProcessingEvent(OabaProcessingEvent.DONE_REC_VAL);

		} else {
			log.info("Skipping RecValService2.runService()");

		}
		time = System.currentTimeMillis() - time;
	}

	/**
	 * This method closes the translator and sets up the sinks array for future
	 * use.
	 */
	private void init() throws BlockingException {
		sinks = new IRecValSink[numBlockFields];
		for (int i = 0; i < numBlockFields; i++) {
			sinks[i] = rvFactory.getNextSink();
		}

		mutableTranslator.close();
	}

	/**
	 * This method creates the files from scratch.
	 *
	 */
	private void createFiles() throws BlockingException {
		sinks = new IRecValSink[numBlockFields];

		int count = 0;

		for (int i = 0; i < numBlockFields; i++) {
			sinks[i] = rvFactory.getNextSink();
			sinks[i].open();
		}

		mutableTranslator.open();

		try {
			Record r;

			// write the stage record source
			if (stage != null) {
				stage.setModel(model);
				stage.open();

				BlockingAccessor ba = (BlockingAccessor) model.getAccessor();
				bc =
					ba.getBlockingConfiguration(blockingConfiguration,
							databaseConfiguration);

				while (stage.hasNext()) {
					count++;
					r = stage.getNext();

					// if (count < 15) log.info("id: " + r.getId());

					if (count % INTERVAL == 0)
						MemoryEstimator.writeMem();

					writeRecord(r, model);

					// This checks the id type
					if (firstStage) {
						Object O = r.getId();
						stageType =
							RECORD_ID_TYPE.fromInstance((Comparable) O)
									.getIntValue();
						firstStage = false;
					}

				} // end while
				stage.close();
			}

			log.info(count + " stage records read");

			// write the master record source
			if (master != null) {
				mutableTranslator.split();

				master.setModel(model);
				master.open();

				BlockingAccessor ba = (BlockingAccessor) model.getAccessor();
				bc =
					ba.getBlockingConfiguration(blockingConfiguration,
							databaseConfiguration);

				while (master.hasNext()) {
					count++;
					r = master.getNext();

					if (count % INTERVAL == 0)
						MemoryEstimator.writeMem();

					writeRecord(r, model);

					if (firstMaster) {
						firstMaster = false;
					}

				} // end while
				master.close();
			}

			log.info(count + " total records read");
		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}

		// close rec val sinks
		for (int i = 0; i < sinks.length; i++) {
			sinks[i].close();
		}
		mutableTranslator.close();

	}

	/**
	 * This method writes 1 record's rec_id and val_id.
	 *
	 */
	private void writeRecord(Record r, ImmutableProbabilityModel model)
			throws BlockingException {

		Object O = r.getId();
		int internal = mutableTranslator.translate((Comparable) O);

		HashSet seen = new HashSet(); // stores field value it has seen
		Hashtable values = new Hashtable(); // stores values per field

		IBlockingValue[] bvs = bc.createBlockingValues(r);

		// loop over the blocking value for this record
		for (int j = 0; j < bvs.length; j++) {
			IBlockingValue bv = bvs[j];
			IBlockingField bf = bv.getBlockingField();

			Integer C = new Integer(bf.getDbField().getNumber());

			// System.out.println (bf.number + " " + bv.value + " " +
			// bf.dbField.number + " " + bf.dbField.name);

			String val = new String(bv.getValue());
			String key = bf.getDbField().getNumber() + val;

			if (!seen.contains(key)) {
				seen.add(key);

				IntArrayList list = (IntArrayList) values.get(C);
				if (list == null) {
					list = new IntArrayList(1);
				}

				list.add(val.hashCode()); // use hashcode of the string
				values.put(C, list);
			}

		} // end for

		Enumeration e = values.keys();
		while (e.hasMoreElements()) {
			Integer C = (Integer) e.nextElement();
			sinks[C.intValue()].writeRecordValue((long) internal,
					(IntArrayList) values.get(C));

			// if (internal%DEBUG_INTERVAL == 0) {
			// log.fine("id " + internal + " C " + C + " " + values.get(C));
			// }
		}
	}

	// /**
	// * This method tries to recover all the files previously written. It does
	// * the following
	// * <ol>
	// * <li>Recover existing rec_id, val_id files by checking the maximum
	// record
	// * ID that was written so far.</li>
	// * <li>Recover the mutableTranslator file.</li>
	// * <li>Append to rec_id, val_id files.</li>
	// * </ol>
	// */
	// private void recoverFiles() throws BlockingException {
	// IRecValSource source;
	// int maxCount = 0;
	// int count = 0;
	//
	// sinks = new IRecValSink[numBlockFields];
	// for (int i = 0; i < numBlockFields; i++) {
	// sinks[i] = rvFactory.getNextSink();
	//
	// source = rvFactory.getSource(sinks[i]);
	// source.open();
	// count = 0;
	// while (source.hasNext()) {
	// count++;
	// }
	// source.close();
	//
	// if (count > maxCount) {
	// maxCount = count;
	// }
	//
	// sinks[i].append();
	// }
	//
	// // recover the mutableTranslator
	// mutableTranslator.recover();
	//
	// // create rec_id, val_id for the records that haven't been processed.
	// count = 0; // count the record we are currently at
	// int count2 = 0; // count the number of new rec,val added
	//
	// try {
	// // first recover the stage
	// if (stage != null) {
	// stage.setModel(model);
	// stage.open();
	// while (stage.hasNext()) {
	// count++;
	// Record r = stage.getNext();
	//
	// if (count % INTERVAL == 0)
	// MemoryEstimator.writeMem();
	//
	// if (count > maxCount) {
	// writeRecord(r, model);
	// count2++;
	// }
	// } // end while
	// stage.close();
	// }
	//
	// // second recover the master
	// if (master != null) {
	//
	// // this mean that the previous run never got to the second
	// // record source, so we need to tell the mutableTranslator to
	// // split
	// if (count2 > 0) {
	// mutableTranslator.split();
	// }
	//
	// master.setModel(model);
	// master.open();
	// while (master.hasNext()) {
	// count++;
	// Record r = master.getNext();
	// if (count % INTERVAL == 0) {
	// MemoryEstimator.writeMem();
	// }
	// if (count > maxCount) {
	// writeRecord(r, model);
	// count2++;
	// }
	// } // end while
	// master.close();
	// }
	// } catch (IOException ex) {
	// throw new BlockingException(ex.toString());
	// }
	//
	// mutableTranslator.close();
	// for (int i = 0; i < sinks.length; i++) {
	// sinks[i].close();
	// }
	//
	// log.info(count2 + " recorded added in recovery");
	// }

	/**
	 * This counts the number of distinct db fields used in the blocking.
	 *
	 * @param bfs
	 *            - array of BlockingFields
	 * @return
	 */
	private int countFields(IBlockingField[] bfs) {
		HashSet set = new HashSet();

		for (int i = 0; i < bfs.length; i++) {
			IDbField field = bfs[i].getDbField();
			Integer I = new Integer(field.getNumber());

			if (!set.contains(I)) {
				set.add(I);
			}
		}

		return set.size();
	}

}
