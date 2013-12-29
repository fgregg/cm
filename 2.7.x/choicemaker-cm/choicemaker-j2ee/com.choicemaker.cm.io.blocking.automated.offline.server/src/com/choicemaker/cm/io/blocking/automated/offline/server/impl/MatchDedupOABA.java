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
import java.util.ArrayList;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.GenericDedupService;

/** This message bean handles the deduping of match records.
 * 
 * @author pcheung
 *
 */
public class MatchDedupOABA implements MessageDrivenBean, MessageListener {

	private static final Logger log = Logger.getLogger(MatchDedupOABA.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + MatchDedupOABA.class.getName());

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;
	private transient OABAConfiguration oabaConfig = null;
	private transient QueueConnection connection = null;

	public void ejbCreate() {
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
		// TODO Auto-generated method stub

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
		
		log.debug("MatchDedupOABA In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();
				batchJob = configuration.findBatchJobById(data.jobID);

				//init values
				ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);				
				oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
				IStatus status = configuration.getStatusLog(data);

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					batchJob.markAsAborted();
					
					if (batchJob.getDescription().equals(BatchJob.CLEAR)) {
						status.setStatus (IStatus.DONE_PROGRAM);
						oabaConfig.removeTempDir();
					}
				} else {
/*
					//max number of match in a temp file					
					String temp = (String) stageModel.properties.get("maxMatchSize");
					int maxMatch = Integer.parseInt(temp);

					//match sink
					IMatchRecord2SinkSourceFactory mFactory = oabaConfig.getMatchTempFactory();
					IMatchRecord2Sink mSink = mFactory.getNextSink();
			
					//dedup match file
					IMatchRecord2Source mSource = mFactory.getSource(mSink);
					
					//final file
					mSink = oabaConfig.getMatchFactory().getSink(Long.toString(data.jobID));

					MatchDedupService3 mDedupService = new MatchDedupService3 (mSource, mSink,
						mFactory, maxMatch, status);
					mDedupService.runService ();
					log.info( "Done match dedup " + mDedupService.getTimeElapsed());

					batchJob.setDescription(mSink.getInfo());
*/

					//since Matcher already dedups the files per chunk
					mergeMatches (data.numChunks, data.jobID, batchJob);
					
					
					//mark as done
					sendToUpdateStatus (data.jobID, 100);
					status.setStatus( IStatus.DONE_PROGRAM);

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


	/** This method merges all the sorted and dedups matches files from the previous step.
	 * 
	 *
	 */
	private void mergeMatches (int numChunk, long jobID, BatchJob batchJob) 
		throws BlockingException, RemoteException {
			
		long t = System.currentTimeMillis();
			
		IMatchRecord2SinkSourceFactory factory = oabaConfig.getMatchChunkFactory();
		ArrayList tempSinks = new ArrayList ();
		for (int i=0; i< numChunk; i++) {
			IMatchRecord2Sink mSink = factory.getSink(i);
			IComparableSink sink =  new ComparableMRSink (mSink);
			tempSinks.add(sink);
			
			log.debug ("file " + sink.getInfo());
		}
		
		IMatchRecord2Sink mSink = oabaConfig.getCompositeMatchSink(jobID);
		IComparableSink sink =  new ComparableMRSink (mSink);
		
		//IMatchRecord2Source mSource = oabaConfig.getMatchFactory().getNextSource();
		//ComparableMRSource source = new ComparableMRSource (mSource);
		
		ComparableMRSinkSourceFactory mFactory = new ComparableMRSinkSourceFactory (factory);
		
		int i = GenericDedupService.mergeFiles(tempSinks, sink, mFactory, true);
		//source.remove();
		
		log.info("Number of Distinct matches after merge: " + i);
		batchJob.setDescription(mSink.getInfo());
		
		t = System.currentTimeMillis() - t;
		
		log.info("Time in match dedup " + t);
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

}