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

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import org.apache.log4j.Logger;

import com.choicemaker.autonumber.AutoNumberFactory;

/**
 * A BatchJobBean tracks the progress of a (long-running) batch
 * process. A successful request goes through a sequence of states: NEW, QUEUED,
 * STARTED, and COMPLETED. A request may be aborted at any point, in which
 * case it goes through the ABORT_REQUESTED and the ABORT states.</p>
 *
 * A long-running process should provide some indication that it is making
 * progress. Since the process is handling a finite array of records, it
 * should be able to estimate the number of records completed. It can provide
 * this estimate as a fraction between 0.00 and 1.00 (inclusive) by updating
 * the getFractionComplete() fild.</p>
 *
 * 
 */
public abstract class UrmStepJobBean implements EntityBean {

	private static final long serialVersionUID = 1L;


	protected static Logger log = Logger.getLogger(UrmStepJobBean.class);


	protected EntityContext ctx;

	public abstract void setId(Long id) ;
	public abstract Long getId() ;
	
	public abstract void setStepIndex(Long stepIndex) ;
	public abstract Long getStepIndex() ;	

	public abstract void setStepJobId(Long stepJobId) ;
	public abstract Long getStepJobId() ;	
	
	public abstract void setUrmJobId(Long urmJobId) ;
	public abstract Long getUrmJobId() ;
		
	// Business methods
	/** This method publishes the status to a topic queue.
	 * 
	 * @param status
	 */


	public Long ejbCreate(Long urmJobId, Long stepIndex) throws CreateException {
		Integer nextId = AutoNumberFactory.getNextInteger(UrmStepJobHome.AUTONUMBER_IDENTIFIER);
		Long batchId = new Long(nextId.longValue());
		setId(batchId);
		setUrmJobId(urmJobId);
		setStepIndex(stepIndex);
		log.info("Created new ChoiceMaker Server Job"+batchId);
		return batchId;
	}
	
	public void ejbPostCreate(Long urmJobId, Long stepIndex) {
	}
	
	// EJB callbacks

	public void setEntityContext(EntityContext context) {
		ctx = context;
	}

	public void unsetEntityContext() {
		ctx = null;
	}

	public void ejbActivate() {
	}

	public void ejbPassivate() {
	}

	public void ejbRemove() {
		log.info("Removing " + getUrmJobId());
	}

	public void ejbStore() {
	}

	public void ejbLoad() {
	}

}

