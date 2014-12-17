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


/**
 * 
 * @version $Revision: 1.3 $ $Date: 2010/10/21 17:40:37 $
 * @author rphall
 */
public class TransitivityStatusListenerMDB implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TransitivityStatusListenerMDB.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + TransitivityStatusListenerMDB.class.getName());

	private transient MessageDrivenContext mdc = null;

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public TransitivityStatusListenerMDB() {
    	log.fine("constructor");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
		log.fine("setMessageDrivenContext()");
		this.mdc = mdc;
	}

	public void ejbCreate() {
    	log.fine("starting ejbCreate...");
		log.fine("...finished ejbCreate");
	}

	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		
		log.fine("starting onMessage...");
		try {
		 if (inMessage instanceof ObjectMessage) {
			msg = (ObjectMessage) inMessage;
			Long L  =  (Long) msg.getObject();
    		log.fine("received status change notification :" + L.toString());
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

	public void ejbRemove() {
		log.fine("ejbRemove()");
	}

} // TransitivityStatusListenerMDB

