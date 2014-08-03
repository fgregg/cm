/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
// package org.jboss.varia.autonumber;
package com.choicemaker.autonumber;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

/**
 * AutoNumber stores autonumbers for items in a collection.
 *
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @version $Revision: 1.1 $
 */
public interface AutoNumber extends EJBObject {

	/*
	 * Internal use only, for use with the Oracle Toplink persistence
	 * manager.
	 * @see AutoNumberFactory for appropriate public methods
	 * @throws RemoteException
	 */
	String getName() throws RemoteException;

	/*
	 * Internal use only, for use with the Oracle Toplink persistence
	 * manager.
	 * @see AutoNumberFactory for appropriate public methods
	 * @throws RemoteException
	 */
	void setName(String name) throws RemoteException;

	/**
	 * Gets the current value of the autonumber.
	 */
	Integer getValue() throws RemoteException;

	/**
	 * Sets the current value of the autonumber.
	 */
	void setValue(Integer value) throws RemoteException;
}
