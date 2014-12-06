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
import java.util.Properties;

import javax.ejb.EJBObject;

import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * Allows a client application to convert results of the batch matching process
 * into the MRPS format. Data in the Marker Record Pair Source (MRPS) format is
 * accepted by the ChoiceMaker Analyser and that allows user to review the
 * content of the matched records as well as fired clues, decision and
 * probability in a user friendly grahical interface.
 * 
 * @author emoussikaev
 */
public interface BatchResultProcessor extends EJBObject {

	/**
	 * Starts the conversion of the result of the matching job with the
	 * identifier <code>jobId</code> into MRPS format. The conversion is an
	 * asynchronous process. It converts data in a batches of the size
	 * <code>batchSize</code>
	 * <p>
	 * 
	 * @param jobId
	 *            matching job identifier
	 * @param batchSize
	 *            conversion batch size
	 * @param mrpsFilename
	 *            target file name
	 * @param trackingId
	 *            an arbitrary string that is stored and may be used for later
	 *            reporting.
	 * @return conversion job ID
	 * @throws ModelException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long startResultToMrpsConversion(long jobId, String mrpsFilename,
			String trackingId) throws ModelException,
			RecordCollectionException, ConfigException, ArgumentException,
			CmRuntimeException, RemoteException;

	/**
	 * Starts the conversion of the result of the matching job with the
	 * identifier <code>jobId</code> into MRPS format. The conversion is an
	 * asynchronous process. It converts data in a batches of the size
	 * <code>batchSize</code>
	 * <p>
	 * 
	 * @param jobId
	 *            matching job identifier
	 * @param batchSize
	 *            conversion batch size
	 * @param mrpsFilename
	 *            target file name
	 * @param trackingId
	 *            an arbitrary string that is stored and may be used for later
	 *            reporting.
	 * @param optional
	 *            configuration parameters specified by
	 *            {@link MrpsRequestConfiguration}
	 * @return conversion job ID
	 * @throws ModelException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long startResultToMrpsConversion(long jobId, String mrpsFilename,
			String trackingId, Properties optional) throws ModelException,
			RecordCollectionException, ConfigException, ArgumentException,
			CmRuntimeException, RemoteException;

	/**
	 * Aborts the conversion job with the given identifier.
	 * 
	 * @param jobID
	 *            Job ID.
	 * 
	 * @return true if job is aborted; false if job is already completed,
	 *         aborted or failed.
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public boolean abortJob(long jobId) throws ArgumentException,
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
