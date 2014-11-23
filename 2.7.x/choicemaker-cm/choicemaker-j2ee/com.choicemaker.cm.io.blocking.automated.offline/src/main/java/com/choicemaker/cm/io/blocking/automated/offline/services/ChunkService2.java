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
import java.util.TreeSet;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSink;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.OabaEvent;
import com.choicemaker.util.LongArrayList;

/**This version takes in blocks that contains internal id instead of the record id.
 * 
 * This service creates does the following:
 * 1.	Read in blocks and/or oversized blocks to create chunk id files in internal ids.
 * 2.	Read in the record source, translator and chunk id files to create chunk data files.
 * 3.	Create comparing block groups.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ChunkService2 {
	
	private static final Logger log = Logger.getLogger(ChunkService2.class.getName());

	private IBlockSource bSource;
	private IBlockSource osSource;
	private RecordSource stage;
	private RecordSource master;
	private ImmutableProbabilityModel stageModel;
	private ImmutableProbabilityModel masterModel;
	private IRecordIDTranslator2 translator;
	private IChunkRecordIDSinkSourceFactory recIDFactory;
	
	private IChunkDataSinkSourceFactory stageSinkFactory;
	private IChunkDataSinkSourceFactory masterSinkFactory;
	
	private IComparisonArraySinkSourceFactory cFactory;
	private OabaProcessing status;
	private int maxChunkSize;
	
	private ArrayList recIDSinks = new ArrayList (); //list of chunk id sinks
	
	//	list of block sinks, the ith comparisonSinks correspond to the ith recSinks
	private ArrayList comparisonSinks = new ArrayList ();   
	
//	private RecordSink [] recordSinks;

//	private int totalBlocks = 0;
	private int numChunks;
	
	private long time; //this keeps track of time

	
	/** This version of the constructor takes in a block source and oversized block source. 
	 * 
	 * @param bSource - block source
	 * @param osSource - oversized block source
	 * @param stage - stage record source
	 * @param master - master record source
	 * @param accessProvider - probability accessProvider
	 * @param translator - this contains mapping from record id to internal id 
	 * @param recIDFactory - this factory creates chunk id files
	 * @param stageSinkFactory - this factory creates chunk data files for the staging data
	 * @param masterSinkFactory - this factory creates chunk data files for the master data
	 * @param cFactory - factory to store comparison groups
	 * @param maxChunkSize - maximum size of a chunk
	 * @param status - status of the system
	 */	
	public ChunkService2 (IBlockSource bSource, IBlockSource osSource, RecordSource stage, RecordSource master,
		ImmutableProbabilityModel stageModel, ImmutableProbabilityModel masterModel,
		IRecordIDTranslator2 translator,
		IChunkRecordIDSinkSourceFactory recIDFactory, IChunkDataSinkSourceFactory stageSinkFactory,
		IChunkDataSinkSourceFactory masterSinkFactory,
		IComparisonArraySinkSourceFactory cFactory, int maxChunkSize, OabaProcessing status) {
			
		this.bSource = bSource;
		this.osSource = osSource;
		this.stage = stage;
		this.master = master;
		this.stageModel = stageModel;
		this.masterModel = masterModel;
		this.translator = translator;
		this.recIDFactory = recIDFactory;
		this.stageSinkFactory = stageSinkFactory;
		this.masterSinkFactory = masterSinkFactory;
		this.cFactory = cFactory;
		this.maxChunkSize = maxChunkSize;
		this.status = status;
		
	}
	
	
	
	public int getNumChunks () { return numChunks; }
	
	/** This method returns the time it takes to run the runService method.
	 * 
	 * @return long - returns the time (in milliseconds) it took to run this service.
	 */
	public long getTimeElapsed () { return time; }


	/** This method runs the service.
	 * 
	 * @throws IOException
	 */
	public void runService () throws XmlConfException, BlockingException {
		time = System.currentTimeMillis();

		if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_DONE_CREATE_CHUNK_DATA ) {
			//do nothing here
//			numChunks = Integer.parseInt( status.getAdditionalInfo() );
			
		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_DONE_DEDUP_OVERSIZED ) {
			//create ids
			log.info("Creating ids for block source " + bSource.getInfo());
			createIDs (bSource, false, 0);
			
			if (osSource != null) {
				log.info("Creating ids for oversized block source");
				createIDs (osSource, true, 0);
			} 
			
			status.setCurrentProcessingEvent( OabaEvent.DONE_CREATE_CHUNK_IDS, Integer.toString(comparisonSinks.size()) );
			
			createDataFiles ();

		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_DONE_CREATE_CHUNK_IDS ) {
			//create the chunk data files
			int numFiles = Integer.parseInt( status.getAdditionalInfo() );
			log.info("Recovering from creating chunk data, numFiles " + numFiles);

			recoverCreateIDs (numFiles);

			createDataFiles ();
			
		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_CREATE_CHUNK_IDS ) {
			//recover chunk id creation
			String temp =  status.getAdditionalInfo();
			int ind = temp.indexOf( OabaProcessing.DELIMIT);
			int numFiles = Integer.parseInt( temp.substring(0,ind) );
			int numBlocks = Integer.parseInt( temp.substring(ind + 1) );
			
			log.info("Recovering from block " + numBlocks);
			
			recoverCreateIDs (numFiles);
			
			createIDs (bSource, false, numBlocks);

			if (osSource != null) {
				log.info("Creating ids for oversized block source");
				createIDs (osSource, true, 0);
			} 
			
			status.setCurrentProcessingEvent( OabaEvent.DONE_CREATE_CHUNK_IDS, Integer.toString(comparisonSinks.size()) );

			createDataFiles ();

		} else if (status.getCurrentProcessingEventId() == OabaProcessing.EVT_CREATE_CHUNK_OVERSIZED_IDS ) {
			//recover chunk id creation
			String temp =  status.getAdditionalInfo();
			int ind = temp.indexOf( OabaProcessing.DELIMIT);
			int numFiles = Integer.parseInt( temp.substring(0,ind) );
			int numBlocks = Integer.parseInt( temp.substring(ind + 1) );
			
			log.info("Recovering from oversized block " + numBlocks);

			recoverCreateIDs (numFiles);
			
			createIDs (osSource, true, numBlocks);
			
			status.setCurrentProcessingEvent( OabaEvent.DONE_CREATE_CHUNK_IDS, Integer.toString(comparisonSinks.size()) );

			createDataFiles ();
		}

		time = System.currentTimeMillis() - time;
	}
	
	
	/** This method makes sure that the program doesn't overwrite the existing files.
	 * It flushes the factories by calling getNext ().
	 *
	 */
	private void recoverCreateIDs (int numFiles) throws BlockingException {
		for (int i=0; i< numFiles; i++) {
			recIDSinks.add( recIDFactory.getNextSink() );
			comparisonSinks.add( cFactory.getNextSink() );
		}
	}
	
	
	/** This method creates the chunk data files for stage and master record sources.
	 * 
	 * @throws IOException
	 * @throws XmlConfException
	 */
	private void createDataFiles () throws BlockingException, XmlConfException {
		translator.initReverseTranslation();
		
		try {
			numChunks = comparisonSinks.size();
		
			//list of rows files to handle.  The rows files is already sorted.
			IChunkRecordIDSource [] crSources = new IChunkRecordIDSource [numChunks];
				
			//each a record sink for each rows file we are handling.
			RecordSink [] recordSinks = new RecordSink [numChunks];
			
			//the current record id of the chunk file
			long [] ind = new long [numChunks]; 
		
			//set up	
			for (int i=0; i < numChunks; i++) {
				IChunkRecordIDSink recSink = (IChunkRecordIDSink) recIDSinks.get(i);
				
				//read in rows file.
				crSources[i] = recIDFactory.getSource(recSink);
				crSources[i].open();
				
				if (crSources[i].hasNext()) {
					ind[i] = crSources[i].getNext();
				}

				recordSinks[i] = stageSinkFactory.getNextSink();
				recordSinks[i].open();
			} //end for

			//first write the stage data out first
			createDataFile (stage, stageModel, ind, crSources, recordSinks, 0);
			
			//close the sinks
			for (int i=0; i < numChunks; i++) {
				recordSinks[i].close();

				//set up for master records
				recordSinks[i] = masterSinkFactory.getNextSink();
				recordSinks[i].open();
			}
		

			if (master != null) {
				//reset the array before the next run
				for (int i=0; i < numChunks; i++) {
					crSources[i].close();
					crSources[i].open();
				
					if (crSources[i].hasNext()) ind[i] = crSources[i].getNext();
				}			

				//next write the master data out
				createDataFile (master, masterModel, ind, crSources, recordSinks, translator.getSplitIndex());
			}

			status.setCurrentProcessingEvent( OabaEvent.DONE_CREATE_CHUNK_DATA, Integer.toString(comparisonSinks.size()));

			//close sinks and cleanup
			for (int i=0; i < numChunks; i++) {
				recordSinks[i].close();
				crSources[i].close(); //close the record id sources
			
				IChunkRecordIDSink recIDSink = (IChunkRecordIDSink) recIDSinks.get(i);
				recIDSink.remove();
			}
			recIDSinks = null;

		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
		
	}
	
	
	/** This method creates the chunk data files from the chunk id files for the given record source.
	 * 
	 *
	 */
	private void createDataFile(RecordSource rs,
			ImmutableProbabilityModel model, long[] ind,
			IChunkRecordIDSource[] crSources, RecordSink[] recordSinks,
			int startNum) throws BlockingException, XmlConfException {

		try {
			//open data file for reading.
			rs.setModel(model);
			rs.open();

			//count indicates the ith record in the record source				
			long count = startNum;

			//read each source record and check each of the files.
			while (rs.hasNext()) {
				Record r = rs.getNext();
				
				//for each chunk data file, check if this record belongs there.
				for (int i =0; i< numChunks; i++) {
					//make sure the ind[i] is in the same range
					while (ind[i] < count && crSources[i].hasNext()) {
						ind[i] = crSources[i].getNext();
					}

					if (ind[i] == count) {
						recordSinks[i].put(r);
						if (crSources[i].hasNext()) ind[i] = crSources[i].getNext();
					
					} 
				}
				
				count ++;
					
			} //end while rs next

			//close source
			rs.close() ;

		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}
	
	
	
	/** This method creates the smaller block sink files and rec id files.  These files correspond
	 * to a single chunk.
	 * 
	 * @param source - block source
	 * @param isOS - true if we are processing the oversized file
	 * @param skip - number of blocks to skip
	 * @throws IOException
	 */
	private void createIDs (IBlockSource source, boolean isOS, int skip) throws BlockingException {
		//initialize the translator
		translator.initReverseTranslation();
		
		source.open();
		
		//this stores the unique recID's in a chunk
		TreeSet rows = new TreeSet ();

		IComparisonArraySink cOut = cFactory.getNextSink();
		comparisonSinks.add(cOut);
		cOut.open();
		
		IChunkRecordIDSink recIDSink = recIDFactory.getNextSink();
		recIDSinks.add(recIDSink);

		int count = 0;
		int countAll = 0;
		
		//skipping
		while ((count < skip) && (source.hasNext()))  {
			source.getNext();
			count ++;
		}

		count = 0;
		while (source.hasNext()) {
			count ++;
//			totalBlocks ++;
			countAll ++;
			
			BlockSet bs = source.getNext();
			LongArrayList block = bs.getRecordIDs();
			
			//set up comparison group
			ArrayList stage = new ArrayList ();
			ArrayList master = new ArrayList ();
			int stageType = 0;
			int masterType = 0;
			
			//add to the set of distinct record ids
			for (int i=0; i< block.size(); i++) {
				//put the internal id in the set
				Long I = new Long (block.get(i));
				if (!rows.contains(I))
				rows.add(I);
				
				//get the original record id
				Comparable comp = translator.reverseLookup((int) block.get(i) );
				
				if (translator.getSplitIndex() == 0) {
					//only staging record source
					
					if (stage.size()== 0) stageType = Constants.checkType(comp);
					stage.add(comp);

				} else {
					//two record sources
					if (block.get(i) < translator.getSplitIndex()) {
						//stage
						if (stage.size()== 0) stageType = Constants.checkType(comp);
						stage.add(comp);
					} else {
						//master
						if (master.size()== 0) masterType = Constants.checkType(comp);
						master.add(comp);
					}
				}
			}
			
			ComparisonArray cg = new ComparisonArray (stage, master, stageType, masterType);
			
			cOut.writeComparisonArray (cg); //write out the comparison group

			//when the hashset gets too big, clear it and start a new file
			if (rows.size() > maxChunkSize) {
				//write the ids to sink
				writeChunkRows (recIDSink, rows);

				log.info ( recIDSink.getInfo() + " has " + count + " blocks " + rows.size() + " rows");
				
				//write status
				String temp = Integer.toString(comparisonSinks.size()) + OabaProcessing.DELIMIT + Integer.toString(skip + countAll);
				if (isOS) status.setCurrentProcessingEvent( OabaEvent.CREATE_CHUNK_OVERSIZED_IDS, temp );
				else status.setCurrentProcessingEvent( OabaEvent.CREATE_CHUNK_IDS, temp );
						
				//create a new block sink
				cOut.close();
				cOut = cFactory.getNextSink();
				comparisonSinks.add(cOut);
				cOut.open();

				//create a new recIDSink
				recIDSink = recIDFactory.getNextSink();		
				recIDSinks.add(recIDSink);
				
				//reset variables
				rows = new TreeSet ();
				count = 0;
				
//				if (countAll > 2000) throw new RuntimeException ("failed test");
			}
			
		} //end while
		
		//One last write to sink
		if (rows.size() > 0) {
			writeChunkRows (recIDSink, rows);
			log.info ( recIDSink.getInfo() +" has " + count + " blocks " + rows.size() + " rows");

			String temp = Integer.toString(comparisonSinks.size()) + OabaProcessing.DELIMIT + Integer.toString(skip + countAll);
			if (isOS) status.setCurrentProcessingEvent( OabaEvent.CREATE_CHUNK_OVERSIZED_IDS, temp );
			else status.setCurrentProcessingEvent( OabaEvent.CREATE_CHUNK_IDS, temp );
		}
		
		
		//cleanup
		cOut.close();
		source.close ();
		source.remove();
		
		//If this source has nothing, remove it from the ArrayList
		if (countAll == 0) {
			cOut.remove();
			comparisonSinks.remove( comparisonSinks.size() - 1);
		} 
	}
	
	
	
	/** This method writes the ids in the tree set to the sink.  The ids are written in ascending order.
	 * 
	 * @param recSink - chunk record id sink
	 * @param rows - hash set containing the distinct ids
	 * @throws IOException
	 */
	private static void writeChunkRows (IChunkRecordIDSink recSink, TreeSet rows) throws BlockingException {
		recSink.open();
		
		Iterator it = rows.iterator();
		long id;
		while (it.hasNext()) {
			id = ((Long) it.next ()).longValue();
			recSink.writeRecordID(id);
		}

		recSink.close();
	}

	
}
