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

import com.choicemaker.cm.urm.base.RefRecordCollection;
import com.choicemaker.cm.urm.config.IFilterConfiguration;
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
public interface BatchResultProcessor extends BatchBase {

	/**
	 * Starts the process of copyinf of the match job results into the specified records collection  
	 * 
	 * @param   processedJobId		Job ID.
	 * @param	resRc		Record collection to serialize matching result.
	 * @param   trackingId an arbitrary string that is stored and may be used for later reporting. 
	 *
	 * @return  copy job ID
	 *  
	 * @throws ModelException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long startResultCopy(
		long processedJobId,
		RefRecordCollection resRc,
		String trackingId)
		throws
			ModelException,
			RecordCollectionException,
			ConfigException,
			ArgumentException,
			CmRuntimeException,
			RemoteException;

	/**
	 * Starts the conversion of the result of the matching job with the
	 * identifier <code>jobId</code> into MRPS format. The conversion is an
	 * asynchronous process. It converts data in a batches of the size
	 * <code>batchSize</code>
	 * <p>
	 * @param processedJobId  job identifier
	 * @param mrpsUrl target location URL 
	 * @param overrideProps set of MRPS generation parameters that replace the parameters defined by the named configuration
	 * @param trackingId an arbitrary string that is stored and may be used for later reporting.
	 * 
	 * @return conversion job ID
	 * 
	 * @throws ModelException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long startMrpsGeneration(
		long processedJobId,
		String mrpsUrl,
		String filterConfName,
		IFilterConfiguration overrideFilterConfig,
		String trackingId
		)
		throws
			ModelException,
			RecordCollectionException,
			ConfigException,
			ArgumentException,
			CmRuntimeException,
			RemoteException;

	/**
	 * <code>Returns the list of all (started, completed, failed and aborted) copy jobs. 
	 * <p>
	 * @return array of job ids
	 * 
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long[] getResultCopyJobList()
		throws
			ArgumentException, ConfigException, CmRuntimeException, RemoteException;

	/**
	 * <code>Returns the list of all (started, completed, failed and aborted) MRPS generation jobs. 
	 * <p>
	 * @return array of job ids
	 * 
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long[] getMrpsGenerationJobList()
		throws
			ArgumentException, ConfigException, CmRuntimeException, RemoteException;

}
