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
import java.util.logging.Logger;

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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSources;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonSetOSSources;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSetSources;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

/**
 * This bean delegates the different chunks to different matcher message beans.
 * It listens for done messages from the matchers bean and when every chunk is done, it
 * calls the MatchDedup bean.
 * 
 * @@deprecated
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes"})
public class MatchScheduler implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MatchScheduler.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + MatchScheduler.class.getName());
	
	/** This should be greater or equal to the number of instances of the Matcher Bean.
	 * 
	 */
	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;
	private transient OABAConfiguration oabaConfig = null;
	
	//This counts the number of messages sent to matcher and number of done messages got back.
	private int countMessages;
	
	//this indicates which chunks is currently being processed.
	private int ind;
	
	private int numChunks;
	
	private long numCompares;
	
	private long numMatches;
	
	private long currentJobID = -1;
	
	private long timeStart;
	private long timeWriting;


	public void ejbCreate() {
		log.fine("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.severe(e.toString());
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
		
		log.fine("MatchScheduler In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();
				
				if (o instanceof StartData) {
					StartData data = (StartData) o;
					oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
					
					if (data.jobID != currentJobID) {
						//reset counters
						ind = 0;
						numChunks = 0;
						numCompares = 0;
						numMatches = 0;
						countMessages = 0;
						currentJobID = data.jobID;
						timeStart = System.currentTimeMillis();
						timeWriting = 0;
					}
					
					batchJob = configuration.findBatchJobById(data.jobID);
					
					//start matching
					startMatch (data);
					
				} else if (o instanceof MatchWriterData) {
					MatchWriterData data = (MatchWriterData) o;
					oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
					
					//getting informed that a segment is done
					countMessages --;
					numCompares += data.numCompares;
					numMatches += data.numMatches;
					timeWriting += data.timeWriting;
					log.info("Chunk " + data.ind + " is done.");

					//no more chunk, so everything is done, call match dedup
					if (countMessages == 0) {
						IStatus status = configuration.getStatusLog(data.jobID);
						status.setStatus( IStatus.DONE_MATCHING_DATA);

						log.info("total comparisons: " + numCompares + " total matches: " + numMatches);
						timeStart = System.currentTimeMillis() - timeStart;
						log.info("total matching time: " + timeStart);
						log.info("total match writing time: " + timeWriting);
						
						cleanUp (data);
						
						//update status
						sendToUpdateStatus (data.jobID, 90);
						
						//call matchdedup
						StartData sd = new StartData (data);
						sendToMatchDebup (sd);
					}
				}
				
			} else {			
				log.warning("wrong type: " + inMessage.getClass().getName());
			}
			
		} catch (JMSException e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			try {
				if (batchJob != null) batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.severe(e1.toString());
			}
		} catch (Exception e) {
			log.severe(e.toString());
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
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
	private void startMatch (StartData data) throws 
		RemoteException, FinderException, BlockingException, NamingException, JMSException {
		
		//init values
		IStatus status = configuration.getStatusLog(data);
		BatchJob batchJob = configuration.findBatchJobById(data.jobID);

		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			batchJob.markAsAborted();
					
			if (batchJob.getDescription().equals(BatchJob.CLEAR)) {
				status.setStatus (IStatus.DONE_PROGRAM);
				oabaConfig.removeTempDir();
			}
		} else {
			numChunks = data.numChunks;
			while (ind < numChunks) {
				data.ind = ind;
				sendToMatcher (data);
				
				ind ++;
				countMessages ++;
			}
		}
	}
	
	
	/**
	 * This method cleans up the chunk files.
	 *
	 */
	private void cleanUp (MatchWriterData data) throws XmlConfException, BlockingException {
		ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);				
		//max block size					
		String temp = (String) stageModel.properties().get("maxBlockSize");
		int maxBlockSize = Integer.parseInt(temp);

		IChunkDataSinkSourceFactory stageFactory = oabaConfig.getStageDataFactory();
		IChunkDataSinkSourceFactory masterFactory= oabaConfig.getMasterDataFactory();
		IComparisonSetSources sources = new ComparisonTreeSetSources (
			oabaConfig.getComparisonTreeFactory(data.stageType));
		IComparisonSetSources sourcesO = new ComparisonSetOSSources (
			oabaConfig.getComparisonArrayFactoryOS(), maxBlockSize);

		stageFactory.removeAllSinks(numChunks);
		masterFactory.removeAllSinks(numChunks);
		
		for (int i=0; i<numChunks; i++) {
			if (sources.hasNextSource()) {
				IComparisonSetSource source = sources.getNextSource ();
				source.remove();
			} else if (sourcesO.hasNextSource()) {
				IComparisonSetSource source = sourcesO.getNextSource();
				source.remove();
			} 
		}
	}
	

	/** This method sends the message to the matcher bean.
	 * 
	 * @param data
	 * @throws NamingException
	 */
	private void sendToMatcher (StartData data) throws NamingException, JMSException{
		Queue queue = configuration.getMatcherMessageQueue();
		
		log.info(" Sending chunk " + ind + " to " + queue.getQueueName());
		
		configuration.sendMessage(queue, data);
	} 



	/** This method sends a message to the UpdateStatus message bean.
	 * 
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	private void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException, JMSException {
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


/*
	private void sendMessage (Queue queue, Serializable data) {
		QueueSession session = null;
		
		try {
			if (connection == null) {
				QueueConnectionFactory factory = configuration.getQueueConnectionFactory();
				connection = factory.createQueueConnection();
			}
			this.connection.start ();
			session = this.connection.createQueueSession (false, QueueSession.AUTO_ACKNOWLEDGE);
//			QueueSession session = this.connection.createQueueSession(true, 0);

			ObjectMessage message = session.createObjectMessage(data);
			QueueSender sender = session.createSender(queue);

			sender.send(message);


			log.fine ("Sending on queue '" + queue.getQueueName()) ;
			log.fine("connection " + connection);
			log.fine("session " + session);
			log.fine("message " + message);
			log.fine("sender " + sender);

		} catch (Exception ex) {
			log.severe(ex.toString());
		} finally {
			try {
				if (session != null) session.close();
			} catch (JMSException ex) {
				log.severe(ex.toString());
			}
		}
		
		log.fine ("...finished sendMessage");
	}
*/



}
