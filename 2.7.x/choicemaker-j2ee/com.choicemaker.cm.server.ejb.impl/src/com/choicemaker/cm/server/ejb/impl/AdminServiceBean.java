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
package com.choicemaker.cm.server.ejb.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.security.AccessControlException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;

import com.choicemaker.cm.core.compiler.DoNothingCompiler;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cm.server.base.DatabaseException;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 22:02:32 $
 */
public class AdminServiceBean implements SessionBean {
	private NameServiceLookup nameServiceLookup = new NameServiceLookup();

	public void ejbCreate() throws CreateException {
		try {
			ICompiler compiler = DoNothingCompiler.instance;
			XmlConfigurator.embeddedInit(compiler);
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			pw.flush();
			throw new CreateException(sw.toString());
		}
	}

	public void ejbActivate() throws EJBException, RemoteException {
		throw new UnsupportedOperationException("Stateless bean should never be passivated");
	}

	public void ejbPassivate() throws EJBException, RemoteException {
		throw new UnsupportedOperationException("Stateless bean should never be passivated");
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
	}

	public void updateCounts(String probabilityModel) throws DatabaseException, AccessControlException, RemoteException {
		try {
			DataSource dataSource = (DataSource) nameServiceLookup.lookup(QueryServiceBean.BLOCKING_SOURCE, DataSource.class);
			new CountsUpdate().updateCounts(dataSource, false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updateDerivedFields() throws DatabaseException, RemoteException {
	}

	public void updateAllDerivedFields() throws DatabaseException, RemoteException {
	}

	public void updateDerivedFields(Comparable id) throws DatabaseException, RemoteException {
	}

}
