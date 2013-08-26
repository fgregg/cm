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

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;


/**
 * A toy example of a long-running backend process. The process accepts an
 * array of integers and for each integer in the array, it sleeps 0.5 seconds
 * (the sleep interval is independent of the element value -- for example, the
 * array may be initialized to all zeros.)</p>
 * <p>
 * As the process progresses, it keeps track of its work via a BatchJob record.
 * When it first receives a batch requested, the process marks the record as
 * started. After every 10 iterations, the process updates the fraction of the
 * job which has been completed. When the job is finished successfully, the
 * process marks the record as completed.</p>
 * <p>
 * The process may be stopped before completion by marking the marking the
 * BatchJob record as 'ABORT_REQUESTED'. The next time the process tries to
 * update the record, it will stop further processing and mark the record as
 * aborted. In this case, the fraction of work actually completed may be higher
 * than the amount recorded by the process.
 * 
 * @version $Revision: 1.3 $ $Date: 2010/10/21 17:40:37 $
 * @author rphall
 */
public class TransitivityStatusListener implements MessageDrivenBean, MessageListener {

	private static final Logger log = Logger.getLogger(TransitivityStatusListener.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + TransitivityStatusListener.class.getName());

	private transient MessageDrivenContext mdc = null;

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public TransitivityStatusListener() {
    	log.debug("constructor");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
		log.debug("setMessageDrivenContext()");
		this.mdc = mdc;
	}

	public void ejbCreate() {
    	log.debug("starting ejbCreate...");
		log.debug("...finished ejbCreate");
	}

	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		
		log.debug("starting onMessage...");
		try {
		 if (inMessage instanceof ObjectMessage) {
			msg = (ObjectMessage) inMessage;
			Long L  =  (Long) msg.getObject();
    		log.debug("received status change notification :" + L.toString());
		 }
		 else
		 	log.debug("received unexpected notification ...");
		} catch (JMSException e) {
      		log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (Exception e) {
      		log.error(e.toString(),e);
			e.printStackTrace();
		}

    log.debug("... finished onMessage");
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
    return;
	} // onMessage(Message)

	public void ejbRemove() {
		log.debug("ejbRemove()");
	}

} // TransitivityStatusListener
