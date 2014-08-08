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
package com.choicemaker.cm.urm.ejb;

import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.urm.base.JobStatus;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;

/**
 * @version  $Revision: 1.5 $ $Date: 2010/10/27 22:19:50 $
 */
public abstract class WorkflowControlListener implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(WorkflowControlListener.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + WorkflowControlListener.class.getName());

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public WorkflowControlListener() {
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
	}

	public void ejbCreate() {
    	log.debug("ejbCreate");
	}

	public void onMessage(Message inMessage) {
		log.debug("<<< onMessage");
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		
	    if ( ! (inMessage instanceof ObjectMessage) ) {
			log.error("Incorrect message type. Message is ignored.");
			return;
	    }

		long id;
		long urmJobId = JobStatus.UNDEFINED_ID;
		UrmJob urmJob = null;
		try {
			ObjectMessage msg = (ObjectMessage) inMessage;
			id = ((Long) msg.getObject()).longValue();
			log.debug("received step job "+id+" completed message");
			//TODO: confirm that if this message came then the status of batchQueryServoce is completed
			urmJobId = getUrmJobId(id);
	    	
			if(urmJobId == JobStatus.UNDEFINED_ID){
				log.debug("urm job id is undefined. no further urm processing required.");
				return;
			}
			urmJob = Single.getInst().findUrmJobById(urmJobId);
		} catch (Exception e) {
			log.error(e);
			return;
		}
	    
		try {
			Long step = urmJob.moveToNextStep(new Long(id),isAbortCheckRequired());			
			if( null == step )
				return;
			
			long stepJobId = JobStatus.UNDEFINED_ID;
			try {
				stepJobId = startStep(urmJob, id);
				urmJob.updateCurStepJobId(new Long(stepJobId),step);
			} catch (JobAlreadyAbortedException e1) {
				return;
			}		 	
			
			if(urmJob.isAbortRequested())
				this.abortJobStep(stepJobId);
					
		} catch (Exception  e) {
			log.error(e);
			try {
				if(urmJob !=  null)
					urmJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1);
				e1.printStackTrace();
			}
			return;
		}

		log.debug(">>> onMessage");
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	} // onMessage(Message)
	
	protected abstract boolean isAbortCheckRequired();
	
	protected abstract long getUrmJobId(long id) 
							throws NamingException,RemoteException,JMSException,ConfigException,
							CmRuntimeException,SQLException,CreateException,ArgumentException,
							ModelException;

	protected abstract long startStep(UrmJob urmJob, long prevStepId) 
								throws JobAlreadyAbortedException,
								NamingException,
								RemoteException,
								JMSException,
								ConfigException,
								CmRuntimeException,
								SQLException,
								CreateException,
								ArgumentException,
								ModelException;
	public abstract void abortJobStep(long id) throws ConfigException, CmRuntimeException;

	public void ejbRemove() {
		log.debug("ejbRemove()");
	}

}
