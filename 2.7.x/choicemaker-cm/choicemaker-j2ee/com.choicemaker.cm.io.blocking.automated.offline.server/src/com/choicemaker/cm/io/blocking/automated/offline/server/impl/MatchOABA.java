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

import javax.ejb.EJBException;
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
import com.choicemaker.cm.core.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockMatcher2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonSetOSSources;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSetSources;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.MatchingService3;

/**
 * This message bean handles the matching of chunks and deduping of match pairs.
 * 
 * @author pcheung
 *
 */
public class MatchOABA implements MessageDrivenBean, MessageListener {

	private static final Logger log = Logger.getLogger(MatchOABA.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + MatchOABA.class.getName());

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;
	private transient OABAConfiguration oabaConfig = null;

	public void ejbCreate() {
//	log.debug("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
//	log.debug("...finished ejbCreate");
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
		StartData data = null;
		BatchJob batchJob = null;
		
		log.info("MatchOABA In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();

				batchJob = configuration.findBatchJobById(data.jobID);
				
				//init values
				IProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);				
				IProbabilityModel masterModel = PMManager.getModelInstance(data.masterModelName);				
				OABAConfiguration oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
//				Status status = data.status;
				IStatus status = configuration.getStatusLog(data);

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					batchJob.markAsAborted();
					
					if (batchJob.getDescription().equals(BatchJob.CLEAR)) {
						status.setStatus (IStatus.DONE_PROGRAM);
						oabaConfig.removeTempDir();
					}
				} else {
					
					//max block size					
					String temp = (String) stageModel.properties().get("maxBlockSize");
					int maxBlock = Integer.parseInt(temp);

					//match sink
					IMatchRecord2SinkSourceFactory mFactory = oabaConfig.getMatchTempFactory();
					IMatchRecord2Sink mSink = mFactory.getNextSink();
					
					//matcher is the code that does the matching.
					BlockMatcher2 matcher = new BlockMatcher2 ();

/*
					MatchingService2 matchingService = new MatchingService2 (oabaConfig.getStageDataFactory(), 
						oabaConfig.getMasterDataFactory(), oabaConfig.getCGFactory(), stageModel, masterModel, 
						mSink, matcher, data.low, 
						data.high, maxBlock, status);
					matchingService.runService();
					log.info( "Done matching " + matchingService.getTimeElapsed());
*/

					//create the tree source
					ComparisonTreeSetSources sources = new ComparisonTreeSetSources (
						oabaConfig.getComparisonTreeFactory(data.stageType));
						
					//create the oversized source
					ComparisonSetOSSources sourcesO = new ComparisonSetOSSources (
						oabaConfig.getComparisonArrayFactoryOS(), maxBlock);
					
					MatchingService3 matchingService = new MatchingService3 (
						oabaConfig.getStageDataFactory(), 
						oabaConfig.getMasterDataFactory(), 
						sources, sourcesO, 
						stageModel, masterModel, mSink, data.low, 
						data.high, maxBlock, status);
					matchingService.runService();
					log.info( "Done matching " + matchingService.getTimeElapsed());


					//call matchdedup
					sendToMatchDebup (data);

					//update status
					sendToUpdateStatus (data.jobID, 90);
				}
				
			} else {
				log.warn("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			try {
				if (batchJob != null) batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(),e1);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
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



}