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

import java.io.Serializable;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaUpdateMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

/**
 * This message bean updates the status of the current job.
 * 
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/updateQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class UpdateStatusMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(UpdateStatusMDB.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + UpdateStatusMDB.class.getName());

	@EJB
	private OabaJobControllerBean jobController;

	public UpdateStatusMDB() {
	}

	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaUpdateMessage data;
		OabaJob oabaJob = null;

		log.info("UpdateStatusMDB In onMessage");

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaUpdateMessage) msg.getObject();

				final long jobId = data.getJobID();
				oabaJob = jobController.findOabaJob(jobId);
//				oabaJob.setFractionComplete(data.getPercentComplete());
//				if (data.getPercentComplete() == 0) {
//					job.markAsStarted();
//				} else if (data.getPercentComplete() == 100) {
//					job.markAsCompleted();
//				} else {
//					job.markAsStarted();
//					job.setFractionComplete(data.getPercentComplete());
//				}

				log.info("Updating job " + data.getJobID()
						+ " setting percent complete to "
						+ data.getPercentComplete());

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			log.severe(e.toString());
			if (oabaJob != null) {
				oabaJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
		return;
	}

}
