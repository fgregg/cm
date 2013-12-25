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

import org.apache.log4j.Category;

/**
 * This is the EJB implemenation of the OABA IStatus interface.
 * 
 * @author pcheung
 *
 */
public abstract class StatusLogBean implements EntityBean{

	private static Category log = Category.getInstance(StatusLogBean.class.getName());
	private EntityContext ctx;

	public abstract void setJobId(Long id) throws RemoteException;
	public abstract Long getJobId() throws RemoteException;

	public abstract void setStatusId(Integer statusId) throws RemoteException;
	public abstract Integer getStatusId() throws RemoteException;

	public abstract void setInfo(String info) throws RemoteException;
	public abstract String getInfo() throws RemoteException;


	public Long ejbCreate(long id) throws CreateException, RemoteException {
		Long longId = new Long (id);
		setJobId(longId);
		setStatusId(new Integer (0) );
		setInfo(null);
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
		ctx = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#unsetEntityContext()
	 */
	public void unsetEntityContext() throws EJBException, RemoteException {
		ctx = null;
	}

}
