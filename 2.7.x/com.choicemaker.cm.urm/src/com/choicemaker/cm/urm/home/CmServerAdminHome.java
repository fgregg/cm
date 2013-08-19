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
package com.choicemaker.cm.urm.home;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

import com.choicemaker.cm.urm.CmServerAdmin;

/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jul 7, 2005 9:33:48 AM
 * @see
 */
public interface CmServerAdminHome extends EJBHome {
  String DEFAULT_EJB_REF_NAME = "ejb/CmServerAdmin";
  String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME ;
	CmServerAdmin create() throws CreateException, RemoteException;
}
