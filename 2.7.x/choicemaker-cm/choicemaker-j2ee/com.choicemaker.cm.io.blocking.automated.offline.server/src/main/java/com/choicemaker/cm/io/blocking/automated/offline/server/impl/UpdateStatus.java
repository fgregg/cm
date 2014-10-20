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

import java.util.logging.Logger;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;


/**
 * This message bean updates the status of the current job.
 * 
 * @author pcheung
 *
 */
public class UpdateStatus implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(UpdateStatus.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + UpdateStatus.class.getName());

	@PersistenceContext (unitName = "oaba")
	EntityManager em;

	private transient MessageDrivenContext mdc = null;
	private EJBConfiguration configuration = null;

	public UpdateStatus() {
//	log.fine("constuctor");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
//		log.fine("setMessageDrivenContext()");
		this.mdc = mdc;
	}


	public void ejbCreate() {
//	log.fine("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();
			
		} catch (Exception e) {
	  log.severe(e.toString());
		}
//	log.fine("...finished ejbCreate");
	}

	
	
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		UpdateData data;

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (UpdateData) msg.getObject();
				
				log.fine("Starting to update job ID: " + data.jobID + " " + data.percentComplete);

				final BatchJob job = this.configuration.findBatchJobById(em, data.jobID);
				
				if (data.percentComplete == 0) job.markAsStarted();
				else if (data.percentComplete == 100) job.markAsCompleted();
				else job.updateFractionCompleted( data.percentComplete );

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (Exception e) {
			log.severe(e.toString());
			e.printStackTrace();
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
		return;
	} // onMessage(Message)
	
	
	public void ejbRemove() {
//		log.fine("ejbRemove()");
	}
	

}
