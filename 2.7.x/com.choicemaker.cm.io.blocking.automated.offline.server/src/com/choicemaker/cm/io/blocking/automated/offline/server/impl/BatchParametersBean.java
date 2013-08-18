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

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import com.choicemaker.cm.core.SerialRecordSource;

/**
 * @author pcheung
 *
 */
public abstract class BatchParametersBean implements EntityBean {

	/** For CMP only */
	public abstract void setId(Long id);
	public abstract Long getId();

	/** For CMP only */
	public abstract void setStageModel(String stageModel);
	public abstract String getStageModel();

	/** For CMP only */
	public abstract void setMasterModel(String masterModel);
	public abstract String getMasterModel();

	/** For CMP only */
	public abstract void setMaxSingle(Integer ms);
	public abstract Integer getMaxSingle();
	
	/** For CMP only */
	public abstract void setLowThreshold(Float low);
	public abstract Float getLowThreshold();

	/** For CMP only */
	public abstract void setHighThreshold(Float low);
	public abstract Float getHighThreshold();
	
	/** For CMP only */
	public abstract void setStageRs(SerialRecordSource rs);
	public abstract SerialRecordSource getStageRs();
	
	/** For CMP only */
	public abstract void setMasterRs(SerialRecordSource rs);
	public abstract SerialRecordSource getMasterRs();

	public Long ejbCreate(long id) throws CreateException {
		Long longId = new Long(id);
		setId(longId);
		return longId;
	}

	public void ejbPostCreate(long id) { }


	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbLoad()
	 */
	public void ejbLoad() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbRemove()
	 */
	public void ejbRemove()
		throws RemoveException, EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbStore()
	 */
	public void ejbStore() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
	 */
	public void setEntityContext(EntityContext arg0)
		throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#unsetEntityContext()
	 */
	public void unsetEntityContext() throws EJBException, RemoteException {
	}

}
