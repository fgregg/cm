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

import java.util.logging.Logger;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;

import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaNotification;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;

/**
 * This message bean updates the status of the current Transitivity job.
 * 
 * @author pcheung
 *
 */
public class UpdateTransitivityStatusMDB implements MessageDrivenBean,
		MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger
			.getLogger(UpdateTransitivityStatusMDB.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ UpdateTransitivityStatusMDB.class.getName());

	private transient MessageDrivenContext mdc = null;

	// @PersistenceContext (unitName = "oaba")
	private EntityManager em;

	public UpdateTransitivityStatusMDB() {
		// log.fine("constuctor");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
		// log.fine("setMessageDrivenContext()");
		this.mdc = mdc;
	}

	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaNotification data;

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaNotification) msg.getObject();

				log.fine("Starting to update job ID: " + data.getJobId() + " "
						+ data.getJobPercentComplete());

				// (TransitivityJob) configuration.findBatchJobById(em,
				// TransitivityJobEntity.class, data.getJobID());
				final TransitivityJob job =
					em.find(TransitivityJobEntity.class, data.getJobId());

				if (data.getJobPercentComplete() == 0) {
					job.markAsStarted();
				} else if (data.getJobPercentComplete() == 100) {
					job.markAsCompleted();
				} else {
					job.markAsStarted();
					// job.setFractionComplete( data.getPercentComplete() );
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
		// log.fine("ejbRemove()");
	}

}
