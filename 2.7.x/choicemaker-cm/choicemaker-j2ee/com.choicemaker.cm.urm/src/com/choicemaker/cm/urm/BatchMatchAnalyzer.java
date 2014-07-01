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
import java.util.Iterator;

import com.choicemaker.cm.urm.base.AnalysisResultFormat;
import com.choicemaker.cm.urm.base.IRecordCollection;
import com.choicemaker.cm.urm.base.LinkCriteria;
import com.choicemaker.cm.urm.base.RefRecordCollection;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * Allows a client application to match specified record collections and execute further match
 * result transitivity analysis in a batch (asynchronous) mode.
 * <p>
 * The below example demonstrates how the <code>startMatchAndAnalysis</code> would be invoked within the client application. 
 * <p>
 * <pre>
 * InitialContext initialContext = new InitialContext();
 * Object queryHomeRef = initialContext.lookup("java:comp/env/ejb/BatchMatchAnalyzer");
 * BatchMatchAnalyzerHome qsh = (BatchMatchAnalyzerHome)PortableRemoteObject.narrow(queryHomeRef, BatchMatchAnalyzerHome.class);
 * BatchMatchAnalyzer qs = qsh.create();
 * 					 
 * String qu = request.getParameter("QueryURL");
 * String qConf = request.getParameter("QueryConfig");
 * DbRecordCollection qRc = new DbRecordCollection(qu,qConf);
 *          
 * String mu = request.getParameter("MasterURL"); 
 * String mConf = request.getParameter("MasterConfig");
 * DbRecordCollection mRc = new DbRecordCollection(mu,mConf);
 *          
 * String trackingId = request.getParameter("Tracking Id"); 
 * String temp = request.getParameter("dth");
 * float diffTh = Float.parseFloat(temp);
 * temp = request.getParameter("mth");
 * float matchTh = Float.parseFloat(temp);
 * LinkCriteria lk = new LinkCriteria(GraphProperty.BCM,true);//true means include query record
 *         					
 * long l = qs.startMatchAndAnalysis(
 *                                   qRc, 
 *                                   null,
 *                                   "OfflineMatch", 
 *                                   diffTh, 
 *                                   matchTh,
 *                                   1000,
 *                                   lk,
 *                                   AnalysisResultFormat.XML,
 * 								     trackingId); 
 * 						
 * System.out.println ("Match Anaysis Service created job with ID = " + l);				
 * </pre>
 * Method <code>startMatching</code> stars matching process and returns immediately with the 
 * identifier of the started job. Using this identifier the client application can check the job status
 * as it shown below.
 * <pre>
 * JobStatus st = qs.getJobStatus(l);	
 * </pre>
 * Once the job is completed the result file can be copied to the specified URL or can be traversed using the 
 * iterator. An example of the result copying is shown below.
 * <pre>		
 * String res = request.getParameter("ResultURL");
 * TextRefRecordCollection resRc = new TextRefRecordCollection(res, new DelimitedTextFormat(','));
 * String temp1 = request.getParameter("idres");
 * long id = Long.parseLong (temp1);					
 * qs.copyResult(id, resRc);
 * <p>
 * The result consists of connected record sets. Each connected record set consists of linked record sets and single records.
 * For more details see the overview of the match result transitivity analysis in the API overview.
 * <p>
 * The result is serialized in the XML format. The other two possible formats (H3L and R3L) are described in the <code>startMatchAndAnalysis</code> 
 * method description.  
 * 
 *  
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jun 28, 2005 2:40:13 PM
 * @see
 */
public interface BatchMatchAnalyzer extends BatchBase {

	/**
	 * As the first step starts record matchig process (the same as <code>startMatch</code> call). 
	 * After this step is completed divides records on connected record sets based on the match/hold connections betweeen records.
	 * For each connected record set it applies the same analysis as online getCompositeMatchCandidates method identifing
	 * linked record sets.
	 * The result is serialized into a file according the <code>serializationFormat</code> parameter.
	 * <p> 
	 * 
	 * @param   qRc  		query record collection.
	 * @param   mRc  		master record collection. 
	 * @param	modelName	name of the probability model.
	 * @param	differThreshold  matching probability below this threshold constitutes the differ.
	 * @param	matchThreshold   matching probability above this threshold constitutes the match.
	 * @param   maxSingle the number of staging records below which single record matching is used.  
	 * 		              If set to 0, OABA is used.
	 * @param c link criteria
	 * @param serializationFormat the format of the result serialization   
	 * @param trackinglId an arbitrary string that is stored and may be used for later reporting.
	 * 
	 * @return  Job ID of the matching job.
	 * 
	 * @throws RecordCollectionException
	 * @throws ArgumentException
	 * @throws ConfigException
	 * @throws ModelException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long startMatchAndAnalysis(
		IRecordCollection qRc,
		RefRecordCollection mRc,
		String modelName,
		float differThreshold,
		float matchThreshold,
		int maxSingle,
		LinkCriteria c,
		AnalysisResultFormat serializationFormat,
		String trackingId)
		throws
			RecordCollectionException,
			ArgumentException,
			ConfigException,
			ModelException,
			CmRuntimeException,
			RemoteException;

	/**
	 * Works the same way as <code>startMatchAndAnalysis</code> method but at the first step uses 
	 * the result of the record matching process with <code>jobId</code> identifier. 
	 * The job with <code>jobId</code> should be completed otherwise his method throws an exception. 
	 * The result is serialized into a file according the <code>serializationFormat</code> parameter. 
	 * <p> 
	 * 
	 * @param jobId   the identifier of the job which result should be taken as input of the analysis process.
	 * @param c       link criteria
	 * @param serializationFormat   
	 * @param trackingId an arbitrary string that is stored and may be used for later reporting.
	 * 
	 * @return long Job ID of the matching job.	 
	 * 
	 * @throws ModelException
	 * @throws ConfigException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long startAnalysis(
		long jobId,
		LinkCriteria c,
		AnalysisResultFormat serializationFormat,
		String trackingId)
		throws
			ModelException,
			ConfigException,
			ArgumentException,
			CmRuntimeException,
			RemoteException;

	/**
	 * Returns an iterator that allows to traverse the result
	 * of the match analysis stored in a record collection <code>rc</code>.
	 * If format <code>s</code> is XML then the returned iterator is <code>ConntedRecordSetIter</code> .
	 * If format <code>s</code> is H3L or R3L then the returned iterator is <code>TripletIter</code>.
	 * If data in the record collection is not in the format compartible with <code>s</code> this method 
	 * throws the RecordCollectionException.  
	 * <p> 
	 * Developers should be aware that URM API is a remote API and using iterators against the data
	 * located on the remote computer might be very inefficient. The better performance can be archived by
	 * copying the results first to a client application local computer and iterating then against the local data.
	 * 
	 * @param rc record collection with the data that will be traversed
	 * @param s format of the travesed data 
	 * 	 
	 * @return iterator
	 * @throws RecordCollectionException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */

	public Iterator getResultIterator(
		RefRecordCollection rc,
		AnalysisResultFormat s)
		throws
			RecordCollectionException,
			ArgumentException,
			CmRuntimeException,
			RemoteException;
	/**
	 * Returns an iterator that allows to traverse the result of the job with <code>jobId</code> identifier
	 * started by <code>startMatchAndAnalysis</code> (or<code>startAnalysis</code>) method.
	 * If format <code>s</code> is XML then the returned iterator is <code>ConntedRecordSetIter</code> .
	 * If format <code>s</code> is H3L or R3L then the returned iterator is <code>TripletIter</code>.
	 * If data in the record collection is not in the format compartible with <code>s</code> this method 
	 * throws the RecordCollectionException.  
	 * <p> 
	 * Developers should be aware that URM API is a remote API and using iterators against the data
	 * located on the remote computer might be very inefficient. The better performance can be archived by
	 * copying the results first to a client application local computer and iterating then against the local data.
	 * 
	 * @param jobId job identifier
	 * @param s format of the travesed data 
	 * @return iterator
	 * @throws RecordCollectionException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public Iterator getResultIterator(long jobId, AnalysisResultFormat s)
		throws
			RecordCollectionException,
			ArgumentException,
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

	public String getVersion(Object context) throws RemoteException;

}
