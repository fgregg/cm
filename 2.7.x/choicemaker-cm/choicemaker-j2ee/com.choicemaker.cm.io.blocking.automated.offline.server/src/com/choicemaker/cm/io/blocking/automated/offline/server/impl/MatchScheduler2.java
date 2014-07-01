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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.rmi.RemoteException;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * This bean delegates the different chunks to different matcher message beans.
 * It listens for done messages from the matchers bean and when every chunk is done, it
 * calls the MatchDedup bean.
 * 
 * This version reads in one chunk at a time and splits the trees for processing by different
 * Matcher2 beans. 
 * 
 * @author pcheung
 *
 */
public class MatchScheduler2 implements MessageDrivenBean, MessageListener {
	
	private static final long serialVersionUID = 1L;

	private static final String DELIM = "|";

	private static final Logger log = Logger.getLogger(MatchScheduler2.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + MatchScheduler2.class.getName());
	
	/** This should be greater or equal to the number of instances of the Matcher Bean.
	 * 
	 */
	private transient MessageDrivenContext mdc = null;
	protected transient EJBConfiguration configuration = null;
	protected transient OABAConfiguration oabaConfig = null;
	
	private RecordSource [] stageRS = null;
	private RecordSource [] masterRS = null;
	
	
	//This counts the number of messages sent to matcher and number of done messages got back.
	private int countMessages;
	
	//this indicates which chunks is currently being processed.
	private int currentChunk = -1;
	
	private long numCompares;
	
	private long numMatches;
	
	private long currentJobID = -1;

	//time trackers	
	private long timeStart;
	private long timeReadData;
	private long timegc;
	
	//array size = number of processors
	//these time tracker are active only in log debug
	private long [] timeWriting;
	private long [] inHMLookUp;
	private long [] inCompare;
	
	
	//message data
	protected StartData data;
	
	//number of processors to use
	private int numProcessors;
	
	//maxchunk
	private int maxChunkSize;


	public void ejbCreate() {
		log.debug("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
	 */
	public void setMessageDrivenContext(MessageDrivenContext mdc)
		throws EJBException {
			this.mdc = mdc;
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		BatchJob batchJob = null;
		
		log.debug ("MatchScheduler2 In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();
				
				if (o instanceof StartData) {
					data = (StartData) o;
					oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
					
					//get the number of processors
					ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);				
					String temp = (String) stageModel.properties().get("numProcessors");
					numProcessors = Integer.parseInt(temp);

					temp = (String) stageModel.properties().get("maxChunkSize");
					maxChunkSize = Integer.parseInt(temp);

					countMessages = 0;
					
					IStatus status = configuration.getStatusLog(data);
					if (status.getStatus() >= IStatus.DONE_MATCHING_DATA) {
						//matching is already done, so go on to the next step.
						nextSteps ();
					} else {
						if (data.jobID != currentJobID) {
							//reset counters
							numCompares = 0;
							numMatches = 0;
							currentChunk = -1;
							currentJobID = data.jobID;
							timeStart = System.currentTimeMillis();
							timeReadData = 0;
							timegc = 0;

							timeWriting = new long [numProcessors];
							inCompare = new long [numProcessors];
							inHMLookUp = new long [numProcessors];
						}
					
						batchJob = configuration.findBatchJobById(data.jobID);
					
						//start matching
						startMatch ();
					}

				} else if (o instanceof MatchWriterData) {
					MatchWriterData d = (MatchWriterData) o;
					
					handleNextChunk (d);
				}
				
			} else {			
				log.warn("wrong type: " + inMessage.getClass().getName());
			}
			
		} catch (JMSException e) {
			log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			try {
				log.error(e);
				if (batchJob != null) batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(),e1);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}
	
	
	/** This method is called when a chunk is done and the system is ready for the next
	 * chunk.
	 * 
	 * It tabulates the statistics from the chunk that just finished and it starts
	 * the next available chunk.
	 * 
	 * @param d - the message data from the chunk that just finished.
	 * @throws RemoteException
	 * @throws FinderException
	 * @throws XmlConfException
	 * @throws BlockingException
	 * @throws NamingException
	 * @throws JMSException
	 */
	protected void handleNextChunk (MatchWriterData d) 
		throws RemoteException, 
		FinderException, XmlConfException, BlockingException, NamingException, JMSException {
		
		oabaConfig = new OABAConfiguration (d.stageModelName, d.jobID);
		BatchJob batchJob = configuration.findBatchJobById(d.jobID);
		data = new StartData (d);
		IStatus status = configuration.getStatusLog(data);

		//keeping track of messages sent and received.
		countMessages --;

		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob (batchJob, status, oabaConfig);
			
		} else if (!BatchJob.STATUS_ABORTED.equals(batchJob.getStatus())) {
			//if there are multiple processors, we have don't do anything for STATUS_ABORTED.
			
			//getting information that a segment is done
			numCompares += d.numCompares;
			numMatches += d.numMatches;
					
			//update time trackers
			if (log.isDebugEnabled()) {
				timeWriting[d.treeInd - 1] += d.timeWriting;
				inHMLookUp[d.treeInd - 1] += d.inLookup;
				inCompare[d.treeInd - 1] += d.inCompare;
			}
					
			log.info ("Chunk " + d.ind + " tree " + d.treeInd + " is done.");

			//Go on to the next chunk
			if (countMessages == 0) {
				String temp = Integer.toString(data.numChunks) + DELIM
					+ Integer.toString(data.numRegularChunks) + DELIM + 
					Integer.toString(currentChunk);
				status.setStatus(IStatus.MATCHING_DATA, temp);
				
				log.info("Chunk " + d.ind + " is done.");
						
				currentChunk ++;
						
				if (currentChunk < d.numChunks) {
					startChunk (currentChunk, batchJob);
				} else {
					//all the chunks are done
					status.setStatus(IStatus.DONE_MATCHING_DATA);

					log.info("total comparisons: " + numCompares + " total matches: " + numMatches);
					timeStart = System.currentTimeMillis() - timeStart;
					log.info("total matching time: " + timeStart);
					log.info("total reading data time: " + timeReadData);
					log.info("total garbage collection time: " + timegc);
							
					//writing out time break downs
					if (log.isDebugEnabled()) {
						for (int i=0; i < numProcessors; i++) {
							log.debug("Processor " + i + " writing time: " + timeWriting[i] +
							" lookup time: " + inHMLookUp[i] + " compare time: " + inCompare[i]);
						}
					}
							
					nextSteps ();
				}
			} //end countMessages == 0
		} //end if abort requested
	}
	
	
	
	/** This method is called when all the chunks are done.
	 * 
	 */
	protected void nextSteps () 
		throws XmlConfException, BlockingException, NamingException, JMSException {
		
		cleanUp ();
						
		//update status
		sendToUpdateStatus (data.jobID, 90);
						
		//call matchdedup
		sendToMatchDebup (data);
	}
	
	
	/** This method sends the different chunks to different beans.
	 * 
	 * @param data
	 * @throws BlockingException
	 * @throws RemoteException
	 * @throws FinderException
	 * @throws XmlConfException
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void startMatch () throws 
		RemoteException, FinderException, BlockingException, NamingException, 
		JMSException, XmlConfException {
		
		//init values
		IStatus status = configuration.getStatusLog(data);
		BatchJob batchJob = configuration.findBatchJobById(data.jobID);
		
		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob (batchJob, status, oabaConfig);
			
		} else {
			currentChunk = 0;
			if (status.getStatus() == IStatus.MATCHING_DATA ) {
				currentChunk = recover (status) + 1;
				log.info("recovering from " + currentChunk);
			}

			//set up the record source arrays.
			IChunkDataSinkSourceFactory stageFactory = oabaConfig.getStageDataFactory();
			IChunkDataSinkSourceFactory masterFactory= oabaConfig.getMasterDataFactory();
			
			stageRS = new RecordSource [data.numChunks];
			masterRS = new RecordSource [data.numChunks];
			 
			for (int i=0; i< data.numChunks; i++) {
				stageRS [i] = stageFactory.getNextSource();
				masterRS [i] = masterFactory.getNextSource();
			}
			
			if (data.numChunks > 0) {
				startChunk (currentChunk, batchJob);
			} else {
				//special case of nothing to do, except to clean up
				log.info ("No matching chunk found.");
				noChunk ();
			}
		}
	}
	
	
	private int recover (IStatus status) throws BlockingException {
		StringTokenizer stk = new StringTokenizer (status.getAdditionalInfo(), DELIM);
		data.numChunks = Integer.parseInt(stk.nextToken());
		data.numRegularChunks = Integer.parseInt(stk.nextToken());
		return Integer.parseInt(stk.nextToken());
	}
	
	
	/** This is a special case when TE is not needed, because all the
	 * match graphs are size 2 or 0.
	 * 
	 */
	protected void noChunk () throws 
		XmlConfException, BlockingException, NamingException, JMSException {
		
		//This is because tree ids start with 1 and not 0.
		for (int i=1; i<=numProcessors; i++) {
			IMatchRecord2Sink mSink = oabaConfig.getMatchChunkFactory().getSink(i);
			mSink.open();
			mSink.close();
			log.debug("creating " + mSink.getInfo());
		}
		
		nextSteps ();
	}
	
	
	/**
	 * This method sends messages out to matchers beans to work on the current chunk.
	 */
	private void startChunk (int ind, BatchJob batchJob) 
		throws BlockingException, NamingException, JMSException {

		log.debug("startChunk " + ind);
			
		IProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);				
		IProbabilityModel masterModel = PMManager.getModelInstance(data.masterModelName);
		
		//call to garbage collection
		long t = System.currentTimeMillis();
		ChunkDataStore dataStore = ChunkDataStore.getInstance();
		dataStore.cleanUp();
		System.gc();
		t = System.currentTimeMillis() - t;
		this.timegc += t;

		//read in the data;
		t = System.currentTimeMillis();
		dataStore.init(stageRS[ind], stageModel, masterRS[ind], masterModel, 
			maxChunkSize, batchJob);

		t = System.currentTimeMillis() - t;
		this.timeReadData += t;

		MemoryEstimator.writeMem();
		
		//send messages to matcher
		data.ind = ind;
		
		//This is because tree ids start with 1 and not 0.
		for (int i=1; i<= numProcessors; i++) {
			StartData sd = new StartData (data);
			sd.treeInd = i; 
			countMessages ++;
			sendToMatcher (sd);
		}
	}
	
	
	/**
	 * This method cleans up the chunk files.
	 *
	 */
	protected void cleanUp () throws XmlConfException, BlockingException {
		log.info("cleanUp");

		ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);				
		//get the number of processors
		String temp = (String) stageModel.properties().get("numProcessors");
		int numProcessors = Integer.parseInt(temp);

		//remove the data
		IChunkDataSinkSourceFactory stageFactory = oabaConfig.getStageDataFactory();
		IChunkDataSinkSourceFactory masterFactory= oabaConfig.getMasterDataFactory();
		stageFactory.removeAllSinks(data.numChunks);
		masterFactory.removeAllSinks(data.numChunks);
		
		//remove the trees
		ComparisonTreeGroupSinkSourceFactory factory =
			oabaConfig.getComparisonTreeGroupFactory(data.stageType, numProcessors);
		for (int i=0; i<data.numRegularChunks; i++) {
			for (int j=1; j<=numProcessors; j++) {
				IComparisonTreeSource source = factory.getSource(i, j);
				source.remove();
			}
		}
		
		int numOS = data.numChunks - data.numRegularChunks;
		
		//remove the oversized array files
		ComparisonArrayGroupSinkSourceFactory factoryOS =
			oabaConfig.getComparisonArrayGroupFactoryOS(numProcessors);
		for (int i=0; i<numOS; i++) {
			for (int j=1; j<=numProcessors; j++) {
				IComparisonArraySource sourceOS = factoryOS.getSource(i, j);
				sourceOS.remove();
			}
		}
	}
	

	/** This method sends the message to the matcher bean.
	 * 
	 * @param data
	 * @throws NamingException
	 */
	protected void sendToMatcher (StartData sd) throws NamingException, JMSException{
		Queue queue = configuration.getMatcherMessageQueue();
		
		log.debug(" Sending chunkId " + sd.ind + " treeId " + sd.treeInd + " to " + queue.getQueueName());
		
		configuration.sendMessage(queue, sd);
	} 



	/** This method sends a message to the UpdateStatus message bean.
	 * 
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	protected void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException, JMSException {
		Queue queue = configuration.getUpdateMessageQueue();

		UpdateData data = new UpdateData();
		data.jobID = jobID;
		data.percentComplete = percentComplete;
		
		configuration.sendMessage(queue, data);
	} 


	/** This method sends the message to the match dedup bean.
	 * 
	 * @param data
	 * @throws NamingException
	 */
	private void sendToMatchDebup (StartData data) throws NamingException, JMSException{
		Queue queue = configuration.getMatchDedupMessageQueue();
		configuration.sendMessage(queue, data);
	} 


}
