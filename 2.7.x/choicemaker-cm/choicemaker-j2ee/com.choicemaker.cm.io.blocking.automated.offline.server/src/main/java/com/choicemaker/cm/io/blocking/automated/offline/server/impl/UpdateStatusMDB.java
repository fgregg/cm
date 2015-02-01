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

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;

/**
 * This message bean updates the status of the current job.
 * 
 * @author pcheung
 * @deprecated use StatusListenerMDB or listen on 'status' Topic
 */
@Deprecated
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/updateQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class UpdateStatusMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(UpdateStatusMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ UpdateStatusMDB.class.getName());

	@EJB
	private OabaJobController jobController;

	public UpdateStatusMDB() {
	}

	@Override
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());

		log.warning("Received message from deprecated updateQueue: "
				+ inMessage);

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

}
