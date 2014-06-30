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

import java.math.BigInteger;
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

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;

/**
 * This message bean compares the pairs given to it and sends a list of matches to the match writer bean.
 *
 * In this version, there is only one chunk data in memory and different processors work on different
 * trees/arrays of this chunk.
 *
 * @author pcheung
 *
 */
public class MatcherTest implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MatcherTest.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + MatcherTest.class.getName());

	private transient MessageDrivenContext mdc = null;
	protected transient EJBConfiguration configuration = null;
	protected transient OABAConfiguration oabaConfig = null;

	private transient Evaluator evaluator;
	//private transient ClueSet clueSet;
	//private transient boolean[] enabledClues;
	protected transient float low;
	protected transient float high;

	protected StartData data;

	//These two tracker are set only in log debug mode
	private long inHMLookup;
	private long inCompare;

	//number of comparisons made
	private int compares;

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


	protected void setHighLow () {
		low = data.low;
		high = data.high;
	}


	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		BatchJob batchJob = null;

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof StartData) {
					//start matching
					data = ((StartData) o);

					log.debug("Matcher In onMessage " + data.ind + " " + data.treeInd);

					oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
					batchJob = configuration.findBatchJobById(data.jobID);
					IStatus status = configuration.getStatusLog(data);

					if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
						MessageBeanUtils.stopJob (batchJob, status, oabaConfig);

					} else {
						handleMatching (data, batchJob);
					}


				} else {
					log.warn("wrong type: " + inMessage.getClass().getName());
				}

			} else {
				log.warn("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			log.error(e);
			assert batchJob != null;
			try {
				batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(),e1);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}


	private void handleMatching (StartData data, BatchJob batchJob)
		throws BlockingException, RemoteException, NamingException, JMSException {

		long t = System.currentTimeMillis();
		//TEST loop
		BigInteger bi = new BigInteger ("85642");
		BigInteger one = new BigInteger ("1");
		BigInteger ret = new BigInteger ("1");
		BigInteger current = new BigInteger ("1");
		while (current.compareTo(bi) <= 0) {
			ret = ret.multiply(current);
			current = current.add (one);
		}
		log.info ("factorial of " + bi.toString() + " has " + ret.toString().length() + "digits");
		//end TEST loop

		t = System.currentTimeMillis() - t;

		log.debug("Times: lookup " + inHMLookup + " compare: " + inCompare
			+ " writeMatches: " + t);

		MatchWriterData mwd = new MatchWriterData (data);
		mwd.numCompares = compares;
		mwd.timeWriting = t;
		mwd.inCompare = inCompare;
		mwd.inLookup = inHMLookup;
		mwd.numMatches = 0;

		sendToMatchScheduler (mwd);
	}




	/** This method sends the message to the match result write bean.
	 *
	 * @param data
	 * @throws NamingException
	 */
	protected void sendToMatchScheduler (MatchWriterData data) throws NamingException, JMSException{
		Queue queue = configuration.getMatchSchedulerMessageQueue();
		configuration.sendMessage(queue, data);
	}



}
