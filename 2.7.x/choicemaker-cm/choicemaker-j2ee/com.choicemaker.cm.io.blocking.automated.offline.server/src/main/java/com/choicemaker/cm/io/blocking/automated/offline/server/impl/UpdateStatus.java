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

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaUpdateMessage;

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

	public UpdateStatus() {
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
//		log.fine("setMessageDrivenContext()");
		this.mdc = mdc;
	}

	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaUpdateMessage data;

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaUpdateMessage) msg.getObject();

				log.fine("Starting to update job ID: " + data.getJobID() + " "
						+ data.getPercentComplete());

				final BatchJob job =
					em.find(OabaJobEntity.class, data.getJobID());

				if (data.getPercentComplete() == 0) {
					job.markAsStarted();
				} else if (data.getPercentComplete() == 100) {
					job.markAsCompleted();
				} else {
					job.markAsStarted();
					job.setFractionComplete(data.getPercentComplete());
				}

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
