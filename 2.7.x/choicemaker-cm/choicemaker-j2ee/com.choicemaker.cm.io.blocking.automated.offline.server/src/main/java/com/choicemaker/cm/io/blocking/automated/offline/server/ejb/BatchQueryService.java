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
package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.EJBObject;
import javax.ejb.FinderException;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchListSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.BatchJobStatus;

/**
 * @author pcheung
 *
 */
@SuppressWarnings("rawtypes")
public interface BatchQueryService extends EJBObject {

	/** This method starts the Offline Automated Blocking Algorithm.  
	 * The OABA doesn't compare two master records.  It compares two staging records or
	 * a staging and master record.
	 * 
	 * @param externalId - an id tracker
	 * @param staging - staging records
	 * @param master - master records.
	 * @param lowThreshold - probability under which a pair is considered "differ".
	 * @param highThreshold - probability above which a pair is considered "match".
	 * @param stageModelName - probability accessProvider of the stage record source.
	 * @param masterModelName - probability accessProvider of the master record source.
	 * @param maxSingle - The number of staging records below which single record matching is used.  
	 * 		If set to 0, OABA is used.  
	 * @return long - id of this job
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws SQLException
	 */
	public long startOABA (String externalID, 
		SerialRecordSource staging, 
		SerialRecordSource master, 
		float lowThreshold, 
		float highThreshold, 
		String stageModelName, String masterModelName,
		int maxSingle)
		throws RemoteException, CreateException, NamingException, JMSException, SQLException;


	/**
	 * 
	 * @param externalId - an id tracker
	 * @param staging - staging records
	 * @param master - master records.
	 * @param lowThreshold - probability under which a pair is considered "differ".
	 * @param highThreshold - probability above which a pair is considered "match".
	 * @param stageModelName - probability accessProvider of the stage record source.
	 * @param masterModelName - probability accessProvider of the master record source.
	 * @param maxSingle - The number of staging records below which single record matching is used.  
	 * 		If set to 0, OABA is used.  
	 * @param runTransitivity - set this to true if you want to run the Transitivity Engine
	 * 		when OABA completes.
	 * @return long - id of this job
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws SQLException
	 */
	public long startOABA (String externalID, 
		SerialRecordSource staging, 
		SerialRecordSource master, 
		float lowThreshold, 
		float highThreshold, 
		String stageModelName, String masterModelName,
		int maxSingle,
		boolean runTransitivity)
		throws RemoteException, CreateException, NamingException, JMSException, SQLException;


	/** In this variation of the startOABA, you could specify the transaction id.  
	 * In the other variations where the transactionID is not specified, it is set to 
	 * job id.
	 * 
	 * @param externalID - an id tracker
	 * @param transactionId - transaction id associated with this job.
	 * @param staging - staging records
	 * @param master - master records
	 * @param lowThreshold - probability under which a pair is considered "differ".
	 * @param highThreshold - probability above which a pair is considered "match".
	 * @param stageModelName - probability accessProvider of the stage record source.
	 * @param masterModelName - probability accessProvider of the master record source.
	 * @param maxSingle - The number of staging records below which single record matching is used.  
	 * 		If set to 0, OABA is used.  
	 * @param runTransitivity - set this to true if you want to run the Transitivity Engine
	 * 		when OABA completes.
	 * @return long - id of this job
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws SQLException
	 */
	public long startOABA (String externalID, 
		int transactionId,
		SerialRecordSource staging, 
		SerialRecordSource master, 
		float lowThreshold, 
		float highThreshold, 
		String stageModelName, String masterModelName,
		int maxSingle,
		boolean runTransitivity)
		throws RemoteException, CreateException, NamingException, JMSException, SQLException;



	/** This version of the OABA takes in only a staging records source.  There is no master data source
	 * in this case.
	 * 
	 * @param externalId - an id tracker
	 * @param staging - staging records
	 * @param lowThreshold - probability under which a pair is considered "differ".
	 * @param highThreshold - probability above which a pair is considered "match".
	 * @param stageModelName - probability accessProvider of the stage record source.
	 * @param maxSingle - The number of staging records below which single record matching is used.  
	 * 		If set to 0, OABA is used.  
	 * @return long - id of this job
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws SQLException
	 */
	public long startOABAStage (String externalID, 
		SerialRecordSource staging, 
		float lowThreshold, 
		float highThreshold, 
		String stageModelName,
		int maxSingle)
		throws RemoteException, CreateException, NamingException, JMSException, SQLException;


	/**
 	* This method attemps to abort a job.
 	* 
 	* @param jobID - This is the unique job id created by the Choicemaker Batch system.
 	* @return 0 means attemp to abort was successful.  -1 means cannot abort either because the job is 
 	* already done, already aborted, or another error.
 	* 
 	* @throws RemoteException
 	* @throws CreateException
 	* @throws NamingException
 	* @throws JMSException
 	* @throws FinderException
 	*/
	public int abortJob(long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException;

	/**
	* This method attemps to suspend a job.  Suspended jobs are recoverable
	* 
	* @param jobID - This is the unique job id created by the Choicemaker Batch system.
	* @return 0 means attemp to abort was successful.  -1 means cannot abort either because the job is 
	* already done, already aborted, or another error.
	* 
	* @throws RemoteException
	* @throws CreateException
	* @throws NamingException
	* @throws JMSException
	* @throws FinderException
	*/
	public int suspendJob(long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException;

	/**
	 * This method queries the status of a given job.
	 * 
	 * @param jobID - This is the unique job id created by the Choicemaker Batch system.
	 * @return a com.choicemaker.cm.custom.nysed.server.generic.BatchJobStatus object.
	 * This object has getStatus (), getStartDate (), and getFinishDate () methods.
	 * 
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public BatchJobStatus getStatus (long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException;


	/**
	 * This method is similar to getStatus, except that it only returns the status as an int.
	 * 
	 * @param jobID - This is the unique job id created by the Choicemaker Batch system.
	 * @return status.
	 * 
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */		
	public String checkStatus (long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException;
		

	/** This method returns the MatchCandidate List Source for the job ID.
	 * 
	 * @param jobID
	 * @return MatchListSource - returns a source from which to read MatchList objects.
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public MatchListSource getMatchList (long jobID) 
		throws RemoteException, CreateException, NamingException, JMSException, FinderException;
		
	
	/** This method gets the MatchRecordSource.
	 * 
	 * @param jobID
	 * @return IMatchRecordSource - returns a source from which to read MatchRecord.
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public IMatchRecord2Source getMatchRecordSource (long jobID) 
		throws RemoteException, CreateException, NamingException, JMSException, FinderException;
		
	
	/** This method removes the temp directory associated with this jobID.
	 * 
	 * @param jobID
	 * @return boolean - true if the directory was successfully deleted.
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public boolean removeDir (long jobID) 
	throws RemoteException, CreateException, NamingException, JMSException, FinderException;


	/** This method tries to resume a stop job.
	 * 
	 * @param modelName - staging accessProvider name
	 * @param jobID - job id of the job you want to resume
	 * @return int = 1 if OK, or -1 if failed
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public int resumeJob (long jobID) 
	throws RemoteException, CreateException, NamingException, JMSException, FinderException;

}
