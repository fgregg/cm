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
package com.choicemaker.cm.urm.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;

/**
 * 
 * @author emoussikaev
 * @version Revision: 2.5 Date: Jun 28, 2005 2:40:13 PM
 * @see
 */
public interface TransSerializer extends EJBObject {

	/**
	 * Works the same way as <code>startMatchAndAnalysis</code> n exception. The
	 * result is serialized into a file according the
	 * <code>serializationFormat</code> parameter.
	 * <p>
	 * 
	 * @param jobId
	 *            the identifier of the job which result should be taken as
	 *            input of the analysis process.
	 * @param modelName
	 *            the name of the probability accessProvider.
	 * @param differThreshold
	 *            matching probability below this threshold constitutes the
	 *            differ.
	 * @param matchThreshold
	 *            matching probability above this threshold constitutes the
	 *            match.
	 * @param maxSingle
	 *            the number of staging records below which single record
	 *            matching is used. If set to 0, OABA is used.
	 * @param c
	 *            link criteria
	 * @param serializationFormat
	 * @param externalId
	 *            an arbitrary string that is stored and may be used for later
	 *            reporting.
	 * 
	 * @return Job ID of the matching job.
	 * @throws ModelException
	 * @throws ConfigException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long startSerialization(long transactionId, long batchJobId,
			String groupMatchType, String serializationType, String modelName,
			String externalId) throws ModelException, ConfigException,
			ArgumentException, CmRuntimeException, RemoteException;

	/**
	 * Aborts the matching process with the given job ID.
	 * 
	 * @param jobID
	 *            Job ID.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public boolean abortJob(long jobID) throws ArgumentException,
			ConfigException, CmRuntimeException, RemoteException;

	/**
	 * Suspends the matching process with the given job ID.
	 * 
	 * @param jobID
	 *            Job ID.
	 * 
	 * @return Job ID.
	 * @throws RemoteException
	 */
	public boolean suspendJob(long jobID) throws ArgumentException,
			ConfigException, CmRuntimeException, RemoteException;

	/**
	 * Resumes the matching job with the given job ID.
	 * 
	 * @param jobID
	 *            Job ID.
	 * 
	 * @return Job ID.
	 * @throws RemoteException
	 */
	public boolean resumeJob(long jobID) throws ArgumentException,
			ConfigException, CmRuntimeException, RemoteException;

	/**
	 * Cleans serialized data related to the process with the give job ID
	 * (including the file with matching results).
	 * 
	 * @param jobID
	 *            Job ID.
	 * @return Job ID.
	 * @throws RemoteException
	 */
	public boolean cleanJob(long jobID) throws ArgumentException,
			ConfigException, CmRuntimeException, RemoteException;

	/**
	 * Returns the version of the interface implementation.
	 * <p>
	 * 
	 * @param context
	 *            reserved
	 * @return version
	 * @throws RemoteException
	 */

	public String getVersion(Object context) throws RemoteException;

}
