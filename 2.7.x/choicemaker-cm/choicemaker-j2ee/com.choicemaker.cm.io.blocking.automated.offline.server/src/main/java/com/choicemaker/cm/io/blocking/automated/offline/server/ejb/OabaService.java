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

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.Local;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;

/**
 * @author pcheung
 *
 */
@Local
@SuppressWarnings("rawtypes")
public interface OabaService {

	String DEFAULT_EJB_REF_NAME = "ejb/OabaService";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	long INVALID_JOB_ID = Long.MIN_VALUE;

	/**
	 * This method starts the Offline Automated Blocking Algorithm to compare a
	 * set of records against themselves.
	 * @param urmJob may be null
	 */
	public long startDeduplication(String externalID, OabaParameters bp,
			OabaSettings oabaSettings, ServerConfiguration serverConfiguration,
			BatchJob urmJob) throws ServerConfigurationException;

	/**
	 * This method starts the Offline Automated Blocking Algorithm to compare
	 * staging records against themselves or against master records.
	 * @param urmJob may be null
	 */
	public long startLinkage(String externalID, OabaParameters batchParams,
			OabaSettings oabaSettings, ServerConfiguration serverConfiguration,
			BatchJob urmJob) throws ServerConfigurationException;

	/**
	 * This method attempts to abort a job.
	 * 
	 * @param jobID
	 *            - This is the unique job id created by the Choicemaker Batch
	 *            system.
	 * @return 0 means attempt to abort was successful. -1 means cannot abort
	 *         either because the job is already done, already aborted, or
	 *         another error.
	 * 
	 */
	public int abortJob(long jobID);

	/**
	 * This method attempts to suspend a job. Suspended jobs are recoverable
	 * 
	 * @param jobID
	 *            - This is the unique job id created by the Choicemaker Batch
	 *            system.
	 * @return 0 means attempt to abort was successful. -1 means cannot abort
	 *         either because the job is already done, already aborted, or
	 *         another error.
	 * 
	 */
	public int suspendJob(long jobID);

	/**
	 * This method queries the status of a given job.
	 * 
	 * @param jobId
	 *            This is the unique job id created by the ChoiceMaker Batch
	 *            system.
	 * @return a com.choicemaker.cm.custom.nysed.server.generic.BatchJobStatus
	 *         object. This object has getStatus (), getStartDate (), and
	 *         getFinishDate () methods.
	 */
	public BatchJob getOabaJob(long jobId);

	/**
	 * This method is similar to getOabaJob, except that it only returns the
	 * status as an int.
	 * 
	 * @param jobID
	 *            - This is the unique job id created by the Choicemaker Batch
	 *            system.
	 * @return status.
	 */
	public String checkStatus(long jobID);

	/**
	 * This method returns the MatchCandidate List Source for the job ID.
	 * 
	 * @param jobID
	 * @return MatchListSource - returns a source from which to read MatchList
	 *         objects.
	 * @see #getMatchRecordSource(long)
	 */
	@Deprecated
	public Object getMatchList(long jobID) throws RemoteException,
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
	 * This method tries to resume a suspended job.
	 * 
	 * @param jobID
	 *            - job id of the job you want to resume
	 * @return int = 1 if OK, or -1 if failed
	 */
	public int resumeJob(long jobID);

}
