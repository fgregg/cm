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
package com.choicemaker.cm.transitivity.server.ejb;

import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.EJBObject;
import javax.ejb.FinderException;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.choicemaker.cm.transitivity.core.TransitivityException;
import com.choicemaker.cm.transitivity.core.TransitivityResult;
import com.choicemaker.cm.transitivity.server.data.TransitivityJobStatus;

/**This session bean allows the user to start, query, and get result from the TE.  It is
 * to be used with the OABA.
 * 
 * @author pcheung
 *
 */
public interface TransitivityOABAService extends EJBObject {
	
//	/**
//	 * This method starts the transitivity engine.  
//	 * WARNINGS: 
//	 *  1. only call this after the OABA has finished.
//	 *  2. use the same parameters as the OABA.
//	 * 
//	 * @param jobID - job id of the OABA job
//	 * @param staging - staging record source
//	 * @param master - master record source
//	 * @param lowThreshold - probability under which a pair is considered "differ".
//	 * @param highThreshold - probability above which a pair is considered "match".
//	 * @param modelConfigurationName - probability accessProvider of the stage record source.
//	 * @param masterModelName - probability accessProvider of the master record source.
//	 * @return int - the transitivity job id.
//	 * @throws RemoteException
//	 * @throws CreateException
//	 * @throws NamingException
//	 * @throws JMSException
//	 * @throws SQLException
//	 */
///*
//	public long startTransitivity (long jobID, 
//		ISerializableRecordSource staging, 
//		ISerializableRecordSource master, 
//		float lowThreshold, 
//		float highThreshold, 
//		String modelConfigurationName, String masterModelName)
//		throws RemoteException, CreateException, NamingException, JMSException, SQLException;
//*/

	/**
	 * This method starts the transitivity engine.  
	 * WARNINGS: 
	 *  1. only call this after the OABA has finished.
	 *  2. use the same OAB jobID.
	 */
	public long startTransitivity (long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, SQLException;


	/** This method queries the current status of the TE job.
	 * 
	 * @param jobID
	 * @return TransitivityJobStatus
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public TransitivityJobStatus getStatus (long jobID) throws 
		JMSException, FinderException, RemoteException, CreateException, 
		NamingException, SQLException;


	/** This method returns the TransitivityResult to the client.
	 * 
	 * @param jobID - TE job id
	 * @param compact - true if you want the graphs be compacted first
	 * @return TransitivityResult
	 * @throws RemoteException
	 * @throws FinderException
	 * @throws NamingException
	 * @throws TransitivityException
	 */
	public TransitivityResult getTransitivityResult (long jobID, boolean compact) throws 
		RemoteException, FinderException, NamingException, TransitivityException;


				
}
