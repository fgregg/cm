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
package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 * Interface to an entity bean which stores Transitivity job status.
 * 
 * @author pcheung
 *
 */
public interface TransitivityJobHome extends EJBHome {
  String DEFAULT_EJB_REF_NAME = "ejb/TransitivityJob";
  String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME ;

	TransitivityJob create(long jobId) throws RemoteException, CreateException;

	TransitivityJob findByPrimaryKey(Long id) throws RemoteException, FinderException;

	Collection<TransitivityJob> findAll() throws RemoteException, FinderException;

}
