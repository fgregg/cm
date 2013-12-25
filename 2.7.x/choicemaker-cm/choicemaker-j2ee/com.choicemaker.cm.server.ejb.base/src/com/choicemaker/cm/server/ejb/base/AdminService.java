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
import java.security.AccessControlException;

import javax.ejb.EJBObject;

import com.choicemaker.cm.server.base.DatabaseException;

/**
 * ChoiceMaker admin interface.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:13 $
 */
public interface AdminService extends EJBObject {
	/**
	 * Updates the counts used for the specified probability model. This method should be called
	 * whenever the database has grown by roughly 20% in size or after adding many records with
	 * similar characteristics, e.g., people with same year of birth.
	 * 
	 * ChoiceMaker uses a counts table in the first-pass matching step (blocking) to find
	 * the best tradeoff between speed and accuracy.
	 * 
	 * Typically this function is called by a scheduler.  
	 * 
	 * @param   probabilityModel  The model whose counts should be updated. Pass <code>null</code> to
	 *            update the counts for all models. An implementation may always update the counts
	 *            for other models also.
	 * @throws   IllegalArgumentException if the specified probability model specified does not exist.
	 * @throws   DatabaseException If a database error prevents ChoiceMaker from fulfilling the request.
	 * @throws   AccessControlException  If authentication or authorization fails.
	 * @throws   RemoteException  If a communication problem occurs.
	 */
	void updateCounts(String probabilityModel)
		throws DatabaseException, AccessControlException, RemoteException;
		
	/**
	 * Updates derived fields for all records that have changed.
	 * This method is only used for databases that do not update
	 * derived fields via triggers.</p>
	 * <p>
	 * As an optimization, records that have changed should be marked
	 * with a dirty flag, so that derived fields of unchanged records
	 * are not recalculated.</p>
	 * 
	 * @throws	DatabaseException If a database error prevents
	 * 			ChoiceMaker from fulfilling the request.
	 * @throws	RemoteException  If a communication problem occurs.
	 */
	void updateDerivedFields() throws DatabaseException, RemoteException;

	/**
	 * Updates derived fields for all records, regardless of whether they
	 * have changed. This method is only used for databases that do not update
	 * derived fields via triggers.</p>
	 * <p>
	 * As an optimization, records that have changed should be marked
	 * with a dirty flag, so that derived fields of unchanged records
	 * are not recalculated.</p>
	 * 
	 * @throws	DatabaseException If a database error prevents
	 * 			ChoiceMaker from fulfilling the request.
	 * @throws	RemoteException  If a communication problem occurs.
	 */
	void updateAllDerivedFields() throws DatabaseException, RemoteException;

	/**
	 * Updates the derived fields of the record with the specified id.
	 * This method is only used for databases that do not update
	 * derived fields via triggers.</p>
	 * @see com.choicemaker.cm.core.Profile#getRecord()
	 * @see com.choicemaker.cm.core.Record#getId()
	 * 
	 * @param id the key that identifies the record that should be updated.
	 * @throws	DatabaseException If a database error prevents
	 * 			ChoiceMaker from fulfilling the request.
	 * @throws	RemoteException  If a communication problem occurs.
	 */
	void updateDerivedFields(Comparable id)
		throws DatabaseException, RemoteException;

}
