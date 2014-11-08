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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.Remote;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchListSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.BatchJobStatus;

/**
 * @author pcheung
 *
 */
@Remote
@SuppressWarnings("rawtypes")
public interface BatchQueryService extends Serializable {

	String DEFAULT_EJB_REF_NAME = "ejb/BatchQueryService";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	/**
	 * This method starts the Offline Automated Blocking Algorithm to compare a
	 * set of records against themselves. Transitivity analysis is conditionally
	 * run depending on the value of <code>runTransitivity</code>
	 * 
	 * @param externalID
	 *            - an id tracker
	 * @param recordSource
	 *            - staging records
	 * @param lowThreshold
	 *            - probability under which a pair is considered "differ".
	 * @param highThreshold
	 *            - probability above which a pair is considered "match".
	 * @param modelConfigurationName
	 *            - probability accessProvider of the stage record source.
	 * @param maxSingle
	 *            - The number of staging records below which single record
	 *            matching is used. If set to 0, OABA is used.
	 * @return long - id of this job
	 */
	public long startOABAStage(String externalID,
			SerializableRecordSource recordSource, float lowThreshold,
			float highThreshold, String modelConfigurationName, int maxSingle,
			boolean runTransitivity);

	/**
	 * This method starts the Offline Automated Blocking Algorithm to compare
	 * staging records against themselves or against master records. (The OABA
	 * does not compare master records against themselves.) Transitivity
	 * analysis is conditionally run depending on the value of
	 * <code>runTransitivity</code>
	 * 
	 * @param externalID
	 *            - an id tracker
	 * @param staging
	 *            - staging records
	 * @param master
	 *            - master records.
	 * @param lowThreshold
	 *            - probability under which a pair is considered "differ".
	 * @param highThreshold
	 *            - probability above which a pair is considered "match".
	 * @param modelConfigurationName
	 *            - probability accessProvider of the stage record source.
	 * @param maxSingle
	 *            - The number of staging records below which single record
	 *            matching is used. If set to 0, OABA is used.
	 * @param runTransitivity
	 *            - set this to true if you want to run the Transitivity Engine
	 *            when OABA completes.
	 * @return long - id of this job
	 * @throws SQLException
	 */
	public long startOABA(String externalID, SerializableRecordSource staging,
			SerializableRecordSource master, float lowThreshold, float highThreshold,
			String modelConfigurationName, int maxSingle,
			boolean runTransitivity);

	/**
	 * This method attempts to abort a job.
	 * 
	 * @param jobID
	 *            - This is the unique job id created by the Choicemaker Batch
	 *            system.
	 * @return 0 means attemp to abort was successful. -1 means cannot abort
	 *         either because the job is already done, already aborted, or
	 *         another error.
	 * 
	 */
	public int abortJob(long jobID);

	/**
	 * This method attemps to suspend a job. Suspended jobs are recoverable
	 * 
	 * @param jobID
	 *            - This is the unique job id created by the Choicemaker Batch
	 *            system.
	 * @return 0 means attemp to abort was successful. -1 means cannot abort
	 *         either because the job is already done, already aborted, or
	 *         another error.
	 * 
	 */
	public int suspendJob(long jobID);

	/**
	 * This method queries the status of a given job.
	 * 
	 * @param jobID
	 *            - This is the unique job id created by the Choicemaker Batch
	 *            system.
	 * @return a com.choicemaker.cm.custom.nysed.server.generic.BatchJobStatus
	 *         object. This object has getStatus (), getStartDate (), and
	 *         getFinishDate () methods.
	 */
	public BatchJobStatus getStatus(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException;

	/**
	 * This method is similar to getStatus, except that it only returns the
	 * status as an int.
	 * 
	 * @param jobID
	 *            - This is the unique job id created by the Choicemaker Batch
	 *            system.
	 * @return status.
	 */
	public String checkStatus(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException;

	/**
	 * This method returns the MatchCandidate List Source for the job ID.
	 * 
	 * @param jobID
	 * @return MatchListSource - returns a source from which to read MatchList
	 *         objects.
	 */
	public MatchListSource getMatchList(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException;

	/**
	 * This method gets the MatchRecordSource.
	 * 
	 * @param jobID
	 * @return IMatchRecordSource - returns a source from which to read
	 *         MatchRecord.
	 */
	public IMatchRecord2Source getMatchRecordSource(long jobID)
			throws RemoteException, CreateException, NamingException,
			JMSException, FinderException;

	/**
	 * This method removes the temp directory associated with this jobID.
	 * 
	 * @param jobID
	 * @return boolean - true if the directory was successfully deleted.
	 */
	public boolean removeDir(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException;

	/**
	 * This method tries to resume a stop job.
	 * 
	 * @param jobID
	 *            - job id of the job you want to resume
	 * @return int = 1 if OK, or -1 if failed
	 */
	public int resumeJob(long jobID);

}
