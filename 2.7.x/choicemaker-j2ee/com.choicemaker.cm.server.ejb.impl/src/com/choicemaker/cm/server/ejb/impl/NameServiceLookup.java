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

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:14 $
 */
public class NameServiceLookup {
	private transient Context initialContext;
	
	private synchronized void initContext() throws NamingException {
		if (initialContext == null) {
			initialContext = new InitialContext();
		}
	}
	
	public synchronized Object lookup(String name, Class clazz) throws NamingException {
		initContext();
		Object objref = initialContext.lookup(name);
		return objref;
	}

	public synchronized Object lookupRemote(String name, Class clazz) throws NamingException {
		initContext();
		Object objref = initialContext.lookup(name);
		if (objref != null) {
			System.err.println("Class: " + objref.getClass());
			return PortableRemoteObject.narrow(name, clazz);
		} else {
			return null;
		}
	}
}
