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

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/statusTopic"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Topic") })
public class StatusListener implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(StatusListener.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + StatusListener.class.getName());

	@PersistenceContext (unitName = "oaba")
	private EntityManager em;

	@Resource
	private MessageDrivenContext mdc;

	@Inject
	JMSContext jmsContext;

	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		
		log.fine("starting onMessage...");
		try {
		 if (inMessage instanceof ObjectMessage) {
			msg = (ObjectMessage) inMessage;
			Long L  =  (Long) msg.getObject();
    		log.info("received status change notification :" + L.toString());
		 }
		 else
		 	log.fine("received unexpected notification ...");
		} catch (JMSException e) {
      		log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (Exception e) {
      		log.severe(e.toString());
			e.printStackTrace();
		}

    log.fine("... finished onMessage");
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
    return;
	} // onMessage(Message)

} // StatusListener

