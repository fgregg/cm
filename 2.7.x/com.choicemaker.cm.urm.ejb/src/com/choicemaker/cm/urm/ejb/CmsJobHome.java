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
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 * Represents a ChoiceMaker serialization job
 */
public interface CmsJobHome extends EJBHome {
	String DEFAULT_EJB_REF_NAME = "ejb/UrmSerializationJob";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	String AUTONUMBER_IDENTIFIER = "UrmSerializationID";

	CmsJob create(String externalId, long transId)
		throws RemoteException, CreateException;

	Collection findAll() throws RemoteException, FinderException;

	CmsJob findByPrimaryKey(Long id) throws RemoteException, FinderException;
}
