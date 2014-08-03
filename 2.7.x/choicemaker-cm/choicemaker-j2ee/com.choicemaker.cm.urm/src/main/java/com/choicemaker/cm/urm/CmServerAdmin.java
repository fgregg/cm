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
package com.choicemaker.cm.urm;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import com.choicemaker.cm.urm.base.DbRecordCollection;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * Allows a client application to excute administrative tasks such as updating derived fields and frequency counts. 
 * 
 * 
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jun 28, 2005 2:40:13 PM
 * @see
 */
//TODO: code sample
public interface CmServerAdmin extends EJBObject {
	

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
	 * @param   probabilityModel. The model whose counts should be updated. Pass <code>null</code> to
	 *            update the counts for all models. An implementation may always update the counts
	 *            for other models also.
	 * @param   urlString URL that defines the database. 
	 
	 * @throws ArgumentException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws ModelException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 *
	 */
	void updateCounts(String probabilityMode,String urlString)
							throws  ArgumentException,
							RecordCollectionException,
							ConfigException,
							ModelException,
							CmRuntimeException, 
							RemoteException;
		


	/**
	 * Updates derived fields for all records that have changed.
	 * This method is only used for databases that do not update
	 * derived fields via triggers.
	 * <p>
	 * As an optimization, records that have changed should be marked
	 * with a dirty flag, so that derived fields of unchanged records
	 * are not recalculated.</p>
	 * <p>
	 * @param probabilityModel The model that will be used for the update.
	 * @param rc The database record collection that defines the database and db configuration
	 * 			 that will be updated. 
	 * @throws ArgumentException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws ModelException
	 * @throws CmRuntimeException
	 * @throws RemoteException	 
	 */
	
	void updateDerivedFields(String probabilityModel,DbRecordCollection rc) 
					throws  ArgumentException,
							RecordCollectionException,
							ConfigException,
							ModelException,
							CmRuntimeException, 
							RemoteException;


	/**
	 * Updates derived fields for all records, regardless of whether they
	 * have changed. This method is only used for databases that do not update
	 * derived fields via triggers.</p>
	 * <p>
	 * As an optimization, records that have changed should be marked
	 * with a dirty flag, so that derived fields of unchanged records
	 * are not recalculated.</p>
	 * 
	 * @param probabilityModel The model that will be used for the update.
	 * @param rc The database record collection that defines the database and db configuration
	 * 			 that will be updated. 
	 * @throws ArgumentException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws ModelException
	 * @throws CmRuntimeException
	 * @throws RemoteException	 
	 */
	void updateAllDerivedFields(String probabilityModel,DbRecordCollection rc) 
				throws  ArgumentException,
						RecordCollectionException,
						ConfigException,
						ModelException,
						CmRuntimeException, 
						RemoteException;
	

								
	/**
	 * Returns the version of the interface implementation.
	 * <p> 
	 * 
	 * @param context reserved
	 * @return version
	 * @throws RemoteException
	 */

	public String	getVersion(
						Object context
					)
					throws  RemoteException;							
						
							
}


/*
 * Updates database auxiliary information. 
 * The most important auxiliary information is the counts table used for the specified probability model. 
 * Counts should be updated whenever the database has grown by roughly 20% in size or after adding many records with
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
//void updateDbAuxiliaryInfo(DbAuxiliaryInfoType acType,String probabilityModel,DbRecordCollection rc,Object context)
//					throws  ArgumentException,
//							RecordCollectionException,
//							ConfigException,
//							ModelException,
//							CmRuntimeException, 
//							RemoteException;
