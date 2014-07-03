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
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

/**
 * This is no longer used.  Each Matcher2 bean handles its own writing.
 * 
 * @@deprecated
 * 
 * @author pcheung
 *
 */
public class MatchWriter implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MatchWriter.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + MatchWriter.class.getName());
	
	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;
//	private transient OABAConfiguration oabaConfig = null;
	
//	private transient int written = 0;
	

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
		
		log.debug("MatchWriter In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();
				
				if (o instanceof MatchWriterData) {
					MatchWriterData data = (MatchWriterData) o;
					OABAConfiguration oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);

					batchJob = configuration.findBatchJobById(data.jobID);

					long t = System.currentTimeMillis();

					//match sink
					IMatchRecord2Sink mSink = oabaConfig.getMatchTempFactory().getNextSink();
					mSink.append();
					
					for (int i=0; i<data.matches.size(); i++) {
						MatchRecord2 mr = (MatchRecord2) data.matches.get(i);
						mSink.writeMatch(mr);
//						written ++;
					}
					
					mSink.close();
					
					t = System.currentTimeMillis() - t;
					
					data.timeWriting = t;
					data.numMatches = data.matches.size();

					//free up the ArrayList
					data.matches = null;

					t =  System.currentTimeMillis();
					//tell MatchSchedule that it's done.
					sendToMatchScheduler (data);
					t = System.currentTimeMillis() - t;
					log.info("time for message sending " + t);

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

	/** This method sends the message to the match result write bean.
	 * 
	 * @param data
	 * @throws NamingException
	 */
	private void sendToMatchScheduler (MatchWriterData data) throws NamingException, JMSException{
		Queue queue = configuration.getMatchSchedulerMessageQueue();
		configuration.sendMessage(queue, data);
	} 



}
