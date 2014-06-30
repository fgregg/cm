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

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.core.base.IProbabilityModel;
import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.Record;
import com.choicemaker.cm.core.base.RecordSource;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.io.blocking.automated.base.BlockingAccessor;
import com.choicemaker.cm.io.blocking.automated.base.BlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.base.BlockingField;
import com.choicemaker.cm.io.blocking.automated.base.BlockingValue;
import com.choicemaker.cm.io.blocking.automated.base.DbField;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

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
 * Version 3 allows a break in the loop.  This version also starts recovery from the
 * beginning of the record source.
 *
 * @author pcheung
 *
 */
public class RecValService3 {

	private static final Logger log = Logger.getLogger(RecValService3.class);

	private RecordSource master;
	private RecordSource stage;
	private IProbabilityModel stageModel;
	private IProbabilityModel masterModel;

	private static final int OUTPUT_INTERVAL = 100000;

	// these two variables are used to stop the program in the middle
	private IControl control;
	private boolean stop;

	private IRecValSink [] sinks;
	private int numBlockFields;

	private IRecValSinkSourceFactory rvFactory;

	private BlockingConfiguration bc;

	//	this is the input record id to internal id translator
	private IRecordIDTranslator2 translator;

	private IStatus status;

	//This stores if stage record id is Integer, Long, or string
	private boolean firstStage = true;
	private int stageType = -1;

	//This stores if master record id is Integer, Long, or string
	private boolean firstMaster = true;
	private int masterType = -1;

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
	 * @param control - a mechanism to get out of a long running loop
	 */
	public RecValService3 (RecordSource stage, RecordSource master, IProbabilityModel stageModel,
		IProbabilityModel masterModel,
		IRecValSinkSourceFactory rvFactory, IRecordIDTranslator2 translator,
		IStatus status, IControl control) {

		this.stage = stage;
		this.master = master;
		this.stageModel = stageModel;
		this.masterModel = masterModel;
		this.translator = translator;
		this.rvFactory = rvFactory;
		this.status = status;
		this.control = control;

		this.stop = false;

		BlockingAccessor ba = (BlockingAccessor) stageModel.getAccessor();
		String blockName = (String) stageModel.properties().get("blockingConfiguration");
		String dbConf = (String) stageModel.properties().get("dbConfiguration");

		BlockingConfiguration bc = ba.getBlockingConfiguration(blockName, dbConf);
		BlockingField[] bfs = bc.blockingFields;

		//print blocking info
		for (int i=0; i< bfs.length; i ++) {
			DbField field = bfs[i].dbField;
			log.info("i " + i + " field " + field.name + " number " + field.number );
		}

		this.numBlockFields = countFields (bfs);
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

		if (status.getStatus() >= IStatus.DONE_REC_VAL) {

			log.info ("recover rec,val files and translator");
			//need to initialize
			init ();

		} else if (status.getStatus() < IStatus.DONE_REC_VAL) {
			log.info ("Creating new rec,val files");

			//create the rec_id, val_id files
			status.setStatus( IStatus.CREATE_REC_VAL);

			createFiles ();

			if (!stop) status.setStatus( IStatus.DONE_REC_VAL);

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


		try {
			//need to get record if type of stage and master
			if (stage != null) {
				stage.setModel(stageModel);
				stage.open();
				if (stage.hasNext()) {
					Record r = stage.getNext();
					Object O = r.getId();
					stageType = Constants.checkType((Comparable)O);
				}
				stage.close();
			}

			if (master != null) {
				master.setModel(masterModel);
				master.open();
				if (master.hasNext()) {
					Record r = master.getNext();
					Object O = r.getId();
					masterType = Constants.checkType((Comparable)O);
				}
				master.close();
			}

		} catch (IOException e) {
			throw new BlockingException (e.toString());
		}

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
				stage.setModel(stageModel);
				stage.open();  // FIXME! try { stage.open(); ... } finally{ stage.close(); }

				String blockName = (String) stageModel.properties().get("blockingConfiguration");
				String dbConf = (String) stageModel.properties().get("dbConfiguration");
				BlockingAccessor ba = (BlockingAccessor) stageModel.getAccessor();
				bc = ba.getBlockingConfiguration(blockName, dbConf);

				while (stage.hasNext() && !stop) {
					count ++;
					r = stage.getNext();

					if (count % OUTPUT_INTERVAL == 0) MemoryEstimator.writeMem ();

					stop = ControlChecker.checkStop (control, count);

					writeRecord (r, stageModel);

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

				master.setModel(masterModel);
				master.open(); // FIXME! try { master.open(); ... } finally{ master.close(); }

				String blockName = (String) masterModel.properties().get("blockingConfiguration");
				String dbConf = (String) masterModel.properties().get("dbConfiguration");
				BlockingAccessor ba = (BlockingAccessor) masterModel.getAccessor();
				bc = ba.getBlockingConfiguration(blockName, dbConf);

				// 2014-04-24 rphall: Commented out unused local variable.
//				long lastID = Long.MIN_VALUE;
				while (master.hasNext() && !stop) {
					count ++;
					r = master.getNext();

					if (count % OUTPUT_INTERVAL == 0) MemoryEstimator.writeMem ();

					stop = ControlChecker.checkStop (control, count);

					writeRecord (r, masterModel);

					//This checks the id type
					if (firstMaster) {
						Object O = r.getId();
						masterType = Constants.checkType((Comparable)O);
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

		BlockingValue[] bvs = bc.createBlockingValues(r);

		//loop over the blocking value for this record
		for (int j=0; j < bvs.length; j++) {
			BlockingValue bv = bvs[j];
			BlockingField bf = bv.blockingField;

			Integer C = new Integer (bf.dbField.number);

//			System.out.println (bf.number + " " + bv.value + " " + bf.dbField.number + " " + bf.dbField.name);

			String val = new String (bv.value);
			String key = bf.dbField.number + val;

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


	/** This counts the number of distinct db fields used in the blocking.
	 *
	 * @param bfs - array of BlockingFields
	 * @return
	 */
	private int countFields (BlockingField[] bfs) {
		HashSet set = new HashSet();

		for (int i=0; i<bfs.length; i++) {
			DbField field = bfs[i].dbField;
			Integer I = new Integer (field.number);

			if (!set.contains(I)) {
				set.add(I);
			}
		}

		return set.size();
	}


}
