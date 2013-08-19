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

import com.choicemaker.cm.urm.base.JobStatus;
import com.choicemaker.cm.urm.base.RefRecordCollection;
import com.choicemaker.cm.urm.exceptions.*;


/**
 * 
 * The base class for the batch match and batch analysis session beans. 
 * Contains functionality related to the job management such as checking status, abort, resume, etc.    
 * 
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jun 28, 2005 2:40:13 PM
 * @see
 */
public interface BatchBase extends EJBObject {
	
	/**
	 * Aborts the job with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 * 
	 * @return  
	 * @throws  RemoteException
	 */	
	public boolean					abortJob(
									long jobID 
								)
								throws	ArgumentException,
										ConfigException,
										CmRuntimeException, 
										RemoteException;


	/**
	 * Suspends the job with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 * 
	 * @return  true if job is aborted; false if job is already completed, aborted or failed.
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */	
	public boolean 					suspendJob(
									long jobID 
								)
								throws	ArgumentException,
										ConfigException,
										CmRuntimeException, 
										RemoteException;

	/**
	 * Resumes the job with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 * 
	 * @return  
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */	
	public boolean 					resumeJob(
									long jobID 
								)
								throws	ArgumentException,
										ConfigException,
										CmRuntimeException, 
										RemoteException;


	/**
	 * Cleans serialized data related to the give job ID (including the file with the matching results).
	 * 
	 * @param   jobID		Job ID.
	 * @return  
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */	
	public boolean 					cleanJob (
									long jobID
								)
								throws	ArgumentException,
										ConfigException,
										CmRuntimeException, 
										RemoteException;
		
								
	/**
	 * Retrieves the status of the job with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 * @return  Job status.
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */	
	public JobStatus	getJobStatus (
							long jobID
					 	)
					 	throws	ArgumentException,
								ConfigException,
								CmRuntimeException, 
								RemoteException;
					


	/**
	 * Copies the matching result to the specified record collection.
	 * It overwrites files that already exist. 
	 * <p> 
	 * In case if ChoiceMaker Server is configured to handle large files the chunk identifiers 
	 * (_1, _2, ...) could be added	to the file name.
	 * 
	 * @param   jobID		Job ID.
	 * @param	resRc		Record collection to serialize matching result.
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */	
	public void 	copyResult (
						long jobId,
						RefRecordCollection resRc
					)
					throws
						ModelException, 	
						RecordCollectionException,
						ConfigException,
						ArgumentException,
						CmRuntimeException, 
						RemoteException;
	
	
	/**
	 * <code>Returns the list of all (started, completed, failed and aborted) jobs. 
	 * <p>
	 * @return list of jobs
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long[] 		getJobList()
							throws	ArgumentException,
									ConfigException,
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


