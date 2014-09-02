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
package com.choicemaker.cm.transitivity.server.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.ejb.FinderException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2Factory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MatchDedupOABA2;

/**
 * This match dedup bean is used by the Transitivity Engine.  It dedups the temporary
 * match results and merge them with the orginal OABA results.
 *
 * @author pcheung
 *
 */
public class TransMatchDedup extends MatchDedupOABA2 {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TransMatchDedup.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + TransMatchDedup.class.getName());


	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;

		log.debug("MatchDedupOABA2 In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				handleMerge(o);
			} else {
				log.warn("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			try {
				log.error(e.toString(),e);
				if (batchJob != null) batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(),e1);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}


	/** This method handles merging individual processor match files.
	 *
	 * @param o
	 * @throws FinderException
	 * @throws RemoteException
	 * @throws BlockingException
	 * @throws NamingException
	 * @throws JMSException
	 */
	protected void handleMerge (Object o)
		throws FinderException, RemoteException, BlockingException, NamingException, JMSException {

		log.debug("in handleMerge");

		StartData d = (StartData) o;
		batchJob = configuration.findBatchJobById(d.jobID);

		//init values
		ImmutableProbabilityModel stageModel = PMManager.getModelInstance(d.stageModelName);
		oabaConfig = new OABAConfiguration (d.stageModelName, d.jobID);
		IStatus status = configuration.getStatusLog(d.jobID);

		//get the number of processors
		String temp = (String) stageModel.properties().get("numProcessors");
		int numProcessors = Integer.parseInt(temp);

		//now merge them all together
		mergeMatches (numProcessors, d.jobID, batchJob, d);

		//mark as done
		sendToUpdateTransStatus (d.jobID, 100);
		status.setStatus( IStatus.DONE_PROGRAM);

	}


	/** This method sends a message to the UpdateStatus message bean.
	 *
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	protected void sendToUpdateTransStatus (long jobID, int percentComplete) throws NamingException, JMSException {
		Queue queue = configuration.getUpdateTransMessageQueue();

		UpdateData data = new UpdateData();
		data.jobID = jobID;
		data.percentComplete = percentComplete;

		configuration.sendMessage(queue, data);
	}


	/** This method does the following:
	 * 1.	concat all the MatchRecord2 files from the processors.
	 * 2.	Merge in the size 2 equivalence classes MatchRecord2's.
	 *
	 * The output file contains MatchRecord2 with separator records.
	 *
	 */
	protected void mergeMatches (int num, long jobID, BatchJob batchJob, StartData d)
		throws BlockingException, RemoteException {

		// 2014-04-24 rphall: Commented out unused local variable.
//		long t = System.currentTimeMillis();

		//final sink
		IMatchRecord2Sink finalSink = oabaConfig.getCompositeTransMatchSink(jobID);

		IMatchRecord2SinkSourceFactory factory = oabaConfig.getMatchChunkFactory();
		ArrayList tempSinks = new ArrayList ();

		//the match files start with 1, not 0.
		for (int i=1; i<= num; i++) {
			IMatchRecord2Sink mSink = factory.getSink(i);
			tempSinks.add(mSink);

			log.info ("concatenating file " + mSink.getInfo());
		}

		//concat all the other chunk MatchRecord2 sinks.
		finalSink.append();
		Comparable C = null;

		for (int i=0; i<tempSinks.size(); i++) {
			IMatchRecord2Sink mSink = (IMatchRecord2Sink) tempSinks.get(i);

			IMatchRecord2Source mSource = factory.getSource(mSink);
			if (mSource.exists()) {
				mSource.open ();
				while (mSource.hasNext()) {
					MatchRecord2 mr = mSource.getNext();
					finalSink.writeMatch(mr);

					if (C == null) {
						C = mr.getRecordID1();
					}
				}
				mSource.close ();

				//clean up
				mSource.remove();
			} //end if
		}

		//finally concat the size two EC file
		IMatchRecord2Source mSource = oabaConfig.getSet2MatchFactory().getNextSource();
		MatchRecord2 separator = null;
		if (C != null) separator = MatchRecord2Factory.getSeparator(C);

		if (mSource.exists()) {
			mSource.open();
			int i =0;
			while (mSource.hasNext()) {
				i ++;
				MatchRecord2 mr = mSource.getNext();
				if (C == null) {
					C = mr.getRecordID1();
					separator = MatchRecord2Factory.getSeparator(C);
				}
				finalSink.writeMatch(mr);
				finalSink.writeMatch(separator);
			}
			mSource.close();
			log.info("Num of size 2s read in " + i);

			mSource.remove();
		}

		finalSink.close();

		log.info ("final output " + finalSink.getInfo());

		try {
			TransitivityJob transJob = configuration.getTransitivityJob(d.jobID);
			transJob.setDescription(finalSink.getInfo());
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}


}
