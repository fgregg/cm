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

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSink;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.util.LongArrayList;

/**
 * This service creates does the following:
 * 1.	Read in blocks and/or oversized blocks to create chunk id files.
 * 2.	Read in the record source and chunk id files to create chunk data files.
 * 
 * @author pcheung
 *
 */
public class ChunkService {
	
	private static final Logger log = Logger.getLogger(ChunkService.class);

	private IBlockSource bSource;
	private IBlockSource osSource;
	private RecordSource stage;
	private RecordSource master;
	private IProbabilityModel model;
	private IChunkRecordIDSinkSourceFactory recIDFactory;
	private IChunkDataSinkSourceFactory sinkFactory;
	private IBlockSinkSourceFactory bFactory;
	private IStatus status;
	private int maxChunkSize;
	
	private ArrayList recIDSinks = new ArrayList (); //list of chunk id sinks
	
	//	list of block sinks, the ith blockSinks correspond to the ith recSinks
	private ArrayList blockSinks = new ArrayList ();   
	
	private RecordSink [] recordSinks;

//	private int totalBlocks = 0;
	private int numChunks;
	
	private long time; //this keeps track of time

	
	/** This version of the constructor takes in a block source and oversized block source. 
	 * 
	 * @param bSource - block source
	 * @param osSource - oversized block source
	 * @param rs - record source
	 * @param accessProvider - probability accessProvider
	 * @param recIDFactory - this factory creates chunk id files
	 * @param sinkFactory - this factory creates chunk data files
	 * @param bFactory - factory to get block sinks 
	 * @param maxChunkSize - maximum size of a chunk
	 * @param status - status of the system
	 */	
	public ChunkService (IBlockSource bSource, IBlockSource osSource, RecordSource stage, IProbabilityModel model,
		IChunkRecordIDSinkSourceFactory recIDFactory, IChunkDataSinkSourceFactory sinkFactory,
		IBlockSinkSourceFactory bFactory, int maxChunkSize, IStatus status) {
			
		this.bSource = bSource;
		this.osSource = osSource;
		this.stage = stage;
		this.master = null;
		this.model = model;
		this.recIDFactory = recIDFactory;
		this.sinkFactory = sinkFactory;
		this.bFactory = bFactory;
		this.maxChunkSize = maxChunkSize;
		this.status = status;
	}
	
	
	/** This version of the constructor takes in a block source and oversized block source. 
	 * 
	 * @param bSource - block source
	 * @param osSource - oversized block source
	 * @param stage - stage record source
	 * @param master - master record source
	 * @param accessProvider - probability accessProvider
	 * @param recIDFactory - this factory creates chunk id files
	 * @param sinkFactory - this factory creates chunk data files
	 * @param bFactory - factory to get block sinks 
	 * @param maxChunkSize - maximum size of a chunk
	 * @param status - status of the system
	 */	
	public ChunkService (IBlockSource bSource, IBlockSource osSource, RecordSource stage, RecordSource master,
		IProbabilityModel model,
		IChunkRecordIDSinkSourceFactory recIDFactory, IChunkDataSinkSourceFactory sinkFactory,
		IBlockSinkSourceFactory bFactory, int maxChunkSize, IStatus status) {
			
		this.bSource = bSource;
		this.osSource = osSource;
		this.stage = stage;
		this.master = master;
		this.model = model;
		this.recIDFactory = recIDFactory;
		this.sinkFactory = sinkFactory;
		this.bFactory = bFactory;
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

		if (status.getStatus() == IStatus.DONE_CREATE_CHUNK_DATA ) {
			//do nothing here
//			numChunks = Integer.parseInt( status.getAdditionalInfo() );
			
		} else if (status.getStatus() == IStatus.DONE_REVERSE_TRANSLATE_OVERSIZED ) {
			//create ids
			log.info("Creating ids for block source");
			createIDs (bSource, false, 0);
			
			if (osSource != null) {
				log.info("Creating ids for oversized block source");
				createIDs (osSource, true, 0);
			} 
			
			status.setStatus( IStatus.DONE_CREATE_CHUNK_IDS, Integer.toString(blockSinks.size()) );
			
			createDataFiles ();

		} else if (status.getStatus() == IStatus.DONE_CREATE_CHUNK_IDS ) {
			//create the chunk data files
			int numFiles = Integer.parseInt( status.getAdditionalInfo() );
			log.info("Recovering from creating chunk data, numFiles " + numFiles);

			recoverCreateIDs (numFiles);

			createDataFiles ();
			
		} else if (status.getStatus() == IStatus.CREATE_CHUNK_IDS ) {
			//recover chunk id creation
			String temp =  status.getAdditionalInfo();
			int ind = temp.indexOf( IStatus.DELIMIT);
			int numFiles = Integer.parseInt( temp.substring(0,ind) );
			int numBlocks = Integer.parseInt( temp.substring(ind + 1) );
			
			log.info("Recovering from block " + numBlocks);
			
			recoverCreateIDs (numFiles);
			
			createIDs (bSource, false, numBlocks);

			if (osSource != null) {
				log.info("Creating ids for oversized block source");
				createIDs (osSource, true, 0);
			} 
			
			status.setStatus( IStatus.DONE_CREATE_CHUNK_IDS, Integer.toString(blockSinks.size()) );

			createDataFiles ();

		} else if (status.getStatus() == IStatus.CREATE_CHUNK_OVERSIZED_IDS ) {
			//recover chunk id creation
			String temp =  status.getAdditionalInfo();
			int ind = temp.indexOf( IStatus.DELIMIT);
			int numFiles = Integer.parseInt( temp.substring(0,ind) );
			int numBlocks = Integer.parseInt( temp.substring(ind + 1) );
			
			log.info("Recovering from oversized block " + numBlocks);

			recoverCreateIDs (numFiles);
			
			createIDs (osSource, true, numBlocks);
			
			status.setStatus( IStatus.DONE_CREATE_CHUNK_IDS, Integer.toString(blockSinks.size()) );

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
			blockSinks.add( bFactory.getNextSink() );
		}
	}
	
	
	/** This method creates the chunk data files for stage and master record sources.
	 * 
	 * @throws IOException
	 * @throws XmlConfException
	 */
	private void createDataFiles () throws BlockingException, XmlConfException {
		try {
			numChunks = blockSinks.size();
		
			//list of rows files to handle.  The rows files is already sorted.
			IChunkRecordIDSource [] crSources = new IChunkRecordIDSource [numChunks];
				
			//each a record sink for each rows file we are handling.
			recordSinks = new RecordSink [numChunks];
			
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

				recordSinks[i] = sinkFactory.getNextSink();
				recordSinks[i].open();
			} //end for

			//first write the stage data out first
			createDataFile (stage, ind, crSources);
		

			if (master != null) {
				//reset the array before the next run
				for (int i=0; i < numChunks; i++) {
					crSources[i].close();
					crSources[i].open();
				
					if (crSources[i].hasNext()) ind[i] = crSources[i].getNext();
				}			

				//next write the master data out
				createDataFile (master, ind, crSources);
		
			}
		

			status.setStatus( IStatus.DONE_CREATE_CHUNK_DATA, Integer.toString(blockSinks.size()));

			//close sinks and cleanup
			for (int i=0; i < numChunks; i++) {
				recordSinks[i].close(); //close the data sinks
				crSources[i].close(); //close the record id sources
			
				IChunkRecordIDSink recIDSink = (IChunkRecordIDSink) recIDSinks.get(i);
				recIDFactory.removeSink( recIDSink);
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
	private void createDataFile (RecordSource rs, long [] ind, IChunkRecordIDSource [] crSources) 
		throws BlockingException, XmlConfException {

		try {
			//open data file for reading.
			rs.setModel(model);
			rs.open();
				
			//read each source record and check each of the files.
			while (rs.hasNext()) {
				Record r = rs.getNext();
					
				Object O = r.getId();
				long recID = 0;
				if (O.getClass().equals( java.lang.Long.class )) {
					Long L = (Long) r.getId();
					recID = L.longValue();
				} else if (O.getClass().equals( java.lang.Integer.class )) {
					recID = ((Integer) r.getId()).longValue ();
				}
				
				//for each chunk data file, check if this record belongs there.
				for (int i =0; i< numChunks; i++) {
					//make sure the ind[i] is in the same range
					while (ind[i] < recID && crSources[i].hasNext()) {
						ind[i] = crSources[i].getNext();
					}

					if (ind[i] == recID) {
						recordSinks[i].put(r);
						if (crSources[i].hasNext()) ind[i] = crSources[i].getNext();
					
					} 
				}
					
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
		source.open();
		
		//this stores the unique recID's in a chunk
		TreeSet rows = new TreeSet ();

		IBlockSink bOut = bFactory.getNextSink();
		blockSinks.add(bOut);
		bOut.open();
		
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
			bOut.writeBlock(bs); //write out to the smaller block sink

			//add to the set of distinct record ids
			for (int i=0; i< block.size(); i++) {
				Long I = new Long (block.get(i));
				if (!rows.contains(I))
				rows.add(I);
			}
			
			//when the hashset gets too big, clear it and start a new file
			if (rows.size() > maxChunkSize) {
				//write the ids to sink
				writeChunkRows (recIDSink, rows);

				log.info ( recIDSink.getInfo() + " has " + count + " blocks " + rows.size() + " rows");
				
				//write status
				String temp = Integer.toString(blockSinks.size()) + IStatus.DELIMIT + Integer.toString(skip + countAll);
				if (isOS) status.setStatus( IStatus.CREATE_CHUNK_OVERSIZED_IDS, temp );
				else status.setStatus( IStatus.CREATE_CHUNK_IDS, temp );
						
				//create a new block sink
				bOut.close();
				bOut = bFactory.getNextSink();
				blockSinks.add(bOut);
				bOut.open();

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

			String temp = Integer.toString(blockSinks.size()) + IStatus.DELIMIT + Integer.toString(skip + countAll);
			if (isOS) status.setStatus( IStatus.CREATE_CHUNK_OVERSIZED_IDS, temp );
			else status.setStatus( IStatus.CREATE_CHUNK_IDS, temp );
		}
		
		
		//cleanup
		bOut.close();
		source.close ();
		source.remove();
		
		//If this source has nothing, remove it from the ArrayList
		if (countAll == 0) {
			bOut.remove();
			blockSinks.remove( blockSinks.size() - 1);
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