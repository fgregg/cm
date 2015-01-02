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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.io.blocking.automated.BlockingAccessor;
import com.choicemaker.cm.io.blocking.automated.IBlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.IBlockingField;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;
import com.choicemaker.cm.io.blocking.automated.IDbField;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;
import com.choicemaker.util.IntArrayList;

/**
 * This object performs the creation of rec_id, val_id pairs.

 * It uses input record id to internal id translation.  As a result, it can use a array
 * instead of hashmap to stored the val_id's.
 * This also uses dbField instead of blocking fields to prep for swap.
 *
 * This takes in a stage record source and master record source.  It translates the stage record source first,
 * then the master record source.
 *
 * Version 2 allows the record ID to be Integer, Long, or String.
 *
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RecValService2 {

	private static final Logger log = Logger.getLogger(RecValService2.class.getName());

	private RecordSource master;
	private RecordSource stage;
	private ImmutableProbabilityModel model;

	private IRecValSink [] sinks;
	private int numBlockFields;

	private IRecValSinkSourceFactory rvFactory;

	private static int INTERVAL = 100000;

	private IBlockingConfiguration bc;

	//	this is the input record id to internal id translator
	private IRecordIDTranslator2 translator;

	private OabaEventLog status;

	//This stores if stage record id is Integer, Long, or string
	private boolean firstStage = true;
	private int stageType = -1;

	//This stores if master record id is Integer, Long, or string
	private boolean firstMaster = true;
//	private int masterType = -1;

//	private String blockName;
//	private String dbConf;

	private long time; //this keeps track of time


	/** This constructor take these parameters:
	 *
	 * @param stage - stage record source of the data
	 * @param master - master record source of the data.  This can be null.
	 * @param accessProvider - probability accessProvider of the data
	 * @param rvFactory - factory to get RecValSinks
	 * @param translator - record ID to internal id translator
	 * @param blockName - blocking configuration name in the schema
	 * @param dbConf - db configuration in the schema
	 * @param status - current status of the system
	 */
	public RecValService2(RecordSource stage, RecordSource master,
			ImmutableProbabilityModel model,
			ImmutableProbabilityModel unusedMasterModel,
			IRecValSinkSourceFactory rvFactory,
			IRecordIDTranslator2 translator, OabaEventLog status) {

		this.stage = stage;
		this.master = master;
		this.model = model;
		this.translator = translator;
		this.rvFactory = rvFactory;
		this.status = status;

//		this.blockName = blockName;
//		this.dbConf = dbConf;

		BlockingAccessor ba = (BlockingAccessor) model.getAccessor();
		String blockName = model.getBlockingConfigurationName();
		String dbConf = model.getDatabaseConfigurationName();

		IBlockingConfiguration bc = ba.getBlockingConfiguration(blockName, dbConf);
		IBlockingField[] bfs = bc.getBlockingFields();

		//print blocking info
		for (int i=0; i< bfs.length; i ++) {
			IDbField field = bfs[i].getDbField();
			log.info("i " + i + " field " + field.getName() + " number " + field.getNumber() );
		}

		this.numBlockFields = countFields (bfs);

//		System.out.println ("Numfields " + numBlockFields);
	}


	/** This method returns the time it takes to run the runService method.
	 *
	 * @return long - returns the time (in milliseconds) it took to run this service.
	 */
	public long getTimeElapsed () { return time; }


	public int getNumBlockingFields () { return numBlockFields; }


	/** This returns the type of stage record id.  It is one of the three:
	 * Constants.TYPE_INTEGER, Constants.TYPE_LONG, or Constants.TYPE_STRING.
	 *
	 * It returns -1 if there was not staging data.
	 *
	 * @return
	 */
	public int getStageType () {
		return stageType;
	}


	/** This returns the type of master record id.  It is one of the three:
	 * Constants.TYPE_INTEGER, Constants.TYPE_LONG, or Constants.TYPE_STRING.
	 *
	 * It returns -1 if there was not master data.
	 *
	 * @return
	 */
	public int getMasterType () {
		return stageType;
	}


	/** This method runs the service.
	 *
	 *
	 */
	public void runService () throws BlockingException {
		time = System.currentTimeMillis();

		if (status.getCurrentOabaEventId() >= OabaProcessing.EVT_DONE_REC_VAL &&
			status.getCurrentOabaEventId() < OabaProcessing.EVT_DONE_REVERSE_TRANSLATE_OVERSIZED ) {

			log.info ("recover rec,val files and translator");
			//need to initialize
			init ();

		} else if (status.getCurrentOabaEventId() < OabaProcessing.EVT_CREATE_REC_VAL) {
			log.info ("Creating new rec,val files");

			//create the rec_id, val_id files
			status.setCurrentOabaEvent( OabaEvent.CREATE_REC_VAL);

			createFiles ();

			status.setCurrentOabaEvent( OabaEvent.DONE_REC_VAL);

		} else if (status.getCurrentOabaEventId() == OabaProcessing.EVT_CREATE_REC_VAL) {
			log.info ("Trying to recover rec,val files");

			//started to created, but not done, so we need to recover
			status.setCurrentOabaEvent( OabaEvent.CREATE_REC_VAL);
			recoverFiles ();
			status.setCurrentOabaEvent( OabaEvent.DONE_REC_VAL);
		}
		time = System.currentTimeMillis() - time;
	}


	/** This method sets up the sinks array for future use.
	 *
	 *
	 */
	private void init () throws BlockingException {
		sinks = new IRecValSink [numBlockFields];

		// 2014-04-24 rphall: Commented out unused local variable.
//		int count = 0;

		for (int i=0; i< numBlockFields; i++) {
			sinks[i] = rvFactory.getNextSink();
		}

		translator.recover();
		translator.close();
	}


	/**
	 * This method creates the files from scratch.
	 *
	 */
	private void createFiles () throws BlockingException {
		sinks = new IRecValSink [numBlockFields];

		int count = 0;

		for (int i=0; i< numBlockFields; i++) {
			sinks[i] = rvFactory.getNextSink();
			sinks[i].open();
		}

		translator.open();


		try {
			Record r;

			//write the stage record source
			if (stage != null) {
				stage.setModel(model);
				stage.open();

				String blockName = model.getBlockingConfigurationName();
				String dbConf = model.getDatabaseConfigurationName();
				BlockingAccessor ba = (BlockingAccessor) model.getAccessor();
				bc = ba.getBlockingConfiguration(blockName, dbConf);

				while (stage.hasNext()) {
					count ++;
					r = stage.getNext();

//					if (count < 15) log.info("id: " + r.getId());

					if (count % INTERVAL == 0) MemoryEstimator.writeMem ();

					writeRecord (r, model);

					//This checks the id type
					if (firstStage) {
						Object O = r.getId();
						stageType = Constants.checkType((Comparable)O);
						firstStage = false;
					}

				} // end while
				stage.close ();
			}

			log.info(count + " stage records read");


			//write the master record source
			if (master != null) {
				translator.split();

				master.setModel(model);
				master.open();

				String blockName = model.getBlockingConfigurationName();
				String dbConf = model.getDatabaseConfigurationName();
				BlockingAccessor ba = (BlockingAccessor) model.getAccessor();
				bc = ba.getBlockingConfiguration(blockName, dbConf);

				// 2014-04-24 rphall: Commented out unused local variable.
//				long lastID = Long.MIN_VALUE;
				while (master.hasNext()) {
					count ++;
					r = master.getNext();

//					if (count < 15) log.info("id: " + r.getId());

					if (count % INTERVAL == 0) MemoryEstimator.writeMem ();

					writeRecord (r, model);

					//This checks the id type
					if (firstMaster) {
//						Object O = r.getId();
//						masterType = Constants.checkType((Comparable)O);
						firstMaster = false;
					}

				} // end while
				master.close ();
			}

			log.info(count + " total records read");
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}

		//close rec val sinks
		for (int i=0; i<sinks.length; i++) {
			sinks[i].close();
		}
		translator.close();

	}


	/** This method writes 1 record's rec_id and val_id.
	 *
	 */
	private void writeRecord (Record r, ImmutableProbabilityModel model) throws BlockingException {

		Object O = r.getId();
		int internal = translator.translate((Comparable) O);

		HashSet seen = new HashSet(); //stores field value it has seen
		Hashtable values = new Hashtable ();  //stores values per field

		IBlockingValue[] bvs = bc.createBlockingValues(r);

		//loop over the blocking value for this record
		for (int j=0; j < bvs.length; j++) {
			IBlockingValue bv = bvs[j];
			IBlockingField bf = bv.getBlockingField();

			Integer C = new Integer (bf.getDbField().getNumber());

//			System.out.println (bf.number + " " + bv.value + " " + bf.dbField.number + " " + bf.dbField.name);

			String val = new String (bv.getValue());
			String key = bf.getDbField().getNumber() + val;

			if (!seen.contains(key)) {
				seen.add(key);

				IntArrayList list = (IntArrayList) values.get (C);
				if (list == null) {
					list = new IntArrayList (1);
				}

				list.add(val.hashCode()); //use hashcode of the string
				values.put(C, list);
			}

		} // end for

		Enumeration e = values.keys();
		while (e.hasMoreElements()) {
			Integer C = (Integer) e.nextElement();
			sinks [C.intValue()].writeRecordValue((long)internal, (IntArrayList) values.get(C));

//			log.info("id " + internal + " C " + C + " " + values.get(C));
		}
	}



	/** This method tries to recover all the files previously written.
	 *
	 * It does the following:
	 * 1. Recover existing rec_id, val_id files by check the maximum record ID that was written so far.
	 * 2. Recover the translator file.
	 * 3. Append to rec_id, val_id files.
	 *
	 */
	private void recoverFiles () throws BlockingException {
		IRecValSource source;
		int maxCount = 0;
		int count = 0;

		sinks = new IRecValSink [numBlockFields];
		for (int i=0; i< numBlockFields; i++) {
			sinks[i] = rvFactory.getNextSink();

			source = rvFactory.getSource(sinks[i]);
			source.open();
			count = 0;
			while (source.hasNext()) {
				count ++;
			}
			source.close();

			if (count > maxCount) maxCount = count;

			sinks[i].append();

//			System.out.println (sinks[i].getInfo());
		}

		//recover the translator
		translator.recover();


		//create rec_id, val_id for the records that haven't been processed.
		count = 0; //count the record we are currently at
		int count2 = 0; //count the number of new rec,val added


		try {
			//first recover the stage
			if (stage != null) {
				stage.setModel(model);
				stage.open();
				// 2014-04-24 rphall: Commented out unused local variable.
//				long lastID = Long.MIN_VALUE;
				while (stage.hasNext()) {
					count ++;
					Record r = stage.getNext();

					if (count % INTERVAL == 0) MemoryEstimator.writeMem ();

					if (count > maxCount) {
						writeRecord (r, model);
						count2 ++;
					}
				} // end while
				stage.close();
			}

			//second recover the master
			if (master != null) {

				//this mean that the previous run never got to the second record source, so we need to
				//tell the translator to split
				if (count2 > 0) translator.split();

				master.setModel(model);
				master.open();
				while (master.hasNext()) {
					count ++;
					Record r = master.getNext();
					if (count % INTERVAL == 0) {
						MemoryEstimator.writeMem ();
					}
					if (count > maxCount) {
						writeRecord (r, model);
						count2 ++;
					}
				} // end while
				master.close();
			}
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}

		translator.close();
		for (int i=0; i<sinks.length; i++) {
			sinks[i].close();
		}

		log.info(count2 + " recorded added in recovery");
	}


	/** This counts the number of distinct db fields used in the blocking.
	 *
	 * @param bfs - array of BlockingFields
	 * @return
	 */
	private int countFields (IBlockingField[] bfs) {
		HashSet set = new HashSet();

		for (int i=0; i<bfs.length; i++) {
			IDbField field = bfs[i].getDbField();
			Integer I = new Integer (field.getNumber());

			if (!set.contains(I)) {
				set.add(I);
			}
		}

		return set.size();
	}


}
