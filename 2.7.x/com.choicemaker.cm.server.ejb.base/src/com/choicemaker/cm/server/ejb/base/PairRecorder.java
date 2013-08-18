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

import javax.ejb.EJBObject;

import com.choicemaker.cm.core.MarkedRecordPairSource;


/**
 * @author pcheung
 *
 */
public interface PairRecorder extends EJBObject {

	/** This method adds two ids of type String to the pairs-for-review table.
	 * 
	 * @param id1: ID of the first record
	 * @param id2: ID of the second record
	 * @param decision: H for hold, M for match, D for differ
	 * @param userID: user who made this decision
	 * @return 0 if OK, -1 if error.
	 */
	int addPair (String id1, String id2, char decision, String userID) throws RemoteException;
	


	/** This method adds two ids of type long to the pairs-for-review table.
	 * 
	 * @param id1: ID of the first record
	 * @param id2: ID of the second record
	 * @param decision: H for hold, M for match, D for differ
	 * @param userID: user who made this decision
	 * @return 0 if OK, -1 if error.
	 */
	int addPair (long id1, long id2, char decision, String userID) throws RemoteException;
	

	/** This method removes all the data from TB_CMT_PAIRS.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	int removeAllPairs () throws RemoteException;


	/** This method reads from the pairs table, TB_CMT_PAIRS, and produces a MRPS.
	 * 
	 * @param modelName
	 * @param conf
	 * @return
	 */	
	MarkedRecordPairSource getMRPS (String modelName, String conf) throws RemoteException;
	
	
	/** This method reads from the pairs table, TB_CMT_PAIRS, and produces a MRPS xml output file.
	 * 
	 * @param modelName
	 * @param conf
	 * @param fileName - name of the output xml file
	 * @return 0 if OK, -1 if error.
	 */	
	int writeMRPS (String modelName, String conf, String fileName) throws RemoteException;
	
	
}
