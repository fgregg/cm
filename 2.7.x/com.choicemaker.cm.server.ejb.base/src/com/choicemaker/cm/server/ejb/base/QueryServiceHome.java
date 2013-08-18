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
package com.choicemaker.cm.server.ejb.base;

import java.rmi.RemoteException;

import javax.ejb.*;

/**
 * Home interface of query service.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 22:00:38 $
 * @deprecated see {@link com.choicemaker.cm.urm.home.OnlineRecordMatcherHome}
 */
public interface QueryServiceHome extends EJBHome {
  String DEFAULT_EJB_REF_NAME = "ejb/QueryService";
  String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME ;
	QueryService create() throws RemoteException, CreateException;
}
