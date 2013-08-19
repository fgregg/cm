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

import com.choicemaker.cm.urm.base.IRecordCollection;
import com.choicemaker.cm.urm.base.RefRecordCollection;
import com.choicemaker.cm.urm.exceptions.*;

/**
 * Allows a client application to match specified records collections in a batch(asynchronous) mode.   
 * <p>
 * The below example demonstrates  how the <code>startMatching</code> would be invoked within the client application. 
 * <p>
 * <pre>
 * InitialContext initialContext = new InitialContext();
 * Object queryHomeRef = initialContext.lookup("java:comp/env/ejb/BatchRecordMatcher");
 * BatchRecordMatcherHome qsh = (BatchRecordMatcherHome)PortableRemoteObject.narrow(queryHomeRef, BatchRecordMatcherHome.class);
 * BatchRecordMatcher qs = qsh.create();
 * 							 
 * String qu = request.getParameter("QueryURL");
 * TextRefRecordCollection qRc = new TextRefRecordCollection(qu, new XmlTextFormat()); 
 * String mu = request.getParameter("MasterURL");
 * TextRefRecordCollection mRc = new TextRefRecordCollection(mu, new XmlTextFormat());
 * String trackingId = request.getParameter("Tracking Id"); 
 *       
 * String temp = request.getParameter("dth");
 * float diffTh = Float.parseFloat(temp);
 * temp = request.getParameter("mth");
 * float matchTh = Float.parseFloat(temp);
 *      
 * long l = qs.startMatching(
 *                     qRc, 
 *                     mRc,
 *                     "BatchMatch", 
 *                     diffTh, 
 *                     matchTh,
 *                     1000,				
 *                     trackingId); 						
 * System.out.println ("Matching Service created job with ID = " + l);				
 * </pre>
 * Method <code>startMatching</code> stars matching process and returns immediately  with the 
 * identifier of the started job. Using this identifier the client application can check the job status as it shown below.
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
 * </pre>
 * <p>
 * The result consists of the evaluated record pairs that are stored in the following format
 * <p>
 * <code>&lt;f1, id1, f2, id2, p, d, l></code> where
 * <code>f1</code> is the format of the first (query) record ID,
 * <code>id1</code> is the query record ID
 * <code>f2</code> is the format of the second (query or master) record ID,
 * <code>id2</code> is the master record ID,
 * <code>p</code> is the probability of match,
 * <code>d</code> is the decision (M - match, H - hold, D - differ),
 * <code>l</code> is the location of the second record (S - query record collection, D - master record collection)    
 * 
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 2:18:24 PM
 * @see
 */
public interface BatchRecordMatcher extends BatchBase {
	
							
	/**
	 * Finds all pairs of records with the first element from the query record collection qRc and
	 * the second element from the master record collection mRc or query record collection qRc 
	 * evaluated as MATCH or HOLD. This is 
	 * an asynchronous method that returns immidiately after the matching process was started.
	 * <p> 
	 * 
	 * @param   qRc  		the query record collection.
	 * @param   mRc  		the master record collection. 
	 * @param	modelName	the name of the probability model.
	 * @param	differThreshold  matching probability below this threshold constitutes the differ.
	 * @param	matchThreshold   matching probability above this threshold constitutes the match.
	 * @param   maxSingle the number of staging records below which single record matching is used.  
	 * 		              If set to 0, OABA is used.  
	 * @param trackingId an arbitrary string that is stored and may be used for later reporting.
	 * 
	 * @return  Job ID of the matching job.
	 * 
	 * @throws ModelException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public long 	startMatching(
						IRecordCollection qRc, 
						RefRecordCollection mRc,
						String modelName, 
						float differThreshold, 
						float matchThreshold,
						int	  maxSingle,
						String trackingId) 
					throws
						ModelException, 	
						RecordCollectionException,
						ConfigException,
						ArgumentException,
						CmRuntimeException, 
						RemoteException;
 

	/**
	 * Returns <code>EvaluatedRecordPairIter</code> ierator that alows to traverse the result
	 * of matching stored in a record collection <code>rc</code>.
	 * 
 	 * Developers should be aware that URM API is a remote API and using iterators against the data
	 * located on the remote computer might be very inefficient. The better performance can be archived by
	 * copying the results first to a client application local computer and iterating then against the local data.
	 * <p> 
	 * 
	 * @param rc record collection with the data that will be traversed
	 * @return iterator
	 * @throws RecordCollectionException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public Iterator	getResultIter(RefRecordCollection rc)
					throws
						RecordCollectionException,
						ArgumentException,
						CmRuntimeException, 
						RemoteException;

	/**
	 * Returns <code>EvaluatedRecordPairIter</code> ierator that alows to traverse the result of matching job with
	 * <code>jobId</code> identifier. 
	 * 
	 * Developers should be aware that URM API is a remote API and using iterators against the data
	 * located on the remote computer might be very inefficient. The better performance can be archived by
	 * copying the results first to a client application local computer and iterating then against the local data. 
	 * <p> 
	 * 
	 * @param jobId job identifier
	 * @return iterator
	 * @throws RecordCollectionException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public Iterator	getResultIter(long jobId)
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

	public String	getVersion(
						Object context
					)
					throws  RemoteException;							
						
						
}



























//import com.choicemaker.cm.urm.base.ISingleRecord;
/*
 * Finds the record pairs with the first element defined by the profile and the second element from the master record collection 
 * with the matching probability greater then or equal to <code>differThreshold</code> . 
 * This is an asynchronous method that returns immidiately after the matching process was started. 
 * The result is serialized to the provided record collection in the "mathing result" format.
 * 
 * @param   qRecord  	The holder object that stores query record.
 * @param   mRs  		The master record source. 
 * @param	modelName	The name of the probability model.
 * @param	differThreshold  Matching probability below this threshold constitutes the differ.
 * @param	matchThreshold   Matching probability above this threshold constitutes the match.
 * @param	maxNumMatches	 Limits number od records included into the resulting array.
 * @param	clientSticker	 Included in log messages as the way to identify related information	
 * @return  The corresponding <code>MatchResult</code>.
 * @throws  RemoteException
 */
/* 	
public long 		startMatching(
								ISingleRecord qProfile, 
								IDbRecordCollection mRs,
								String modelName, 
								float differThreshold, 
								float matchThreshold,
								String externalId) 
							throws 	InvalidModelException, 	
									RecordException, 
									DatabaseException,
									RemoteException;
									
									 
import java.io.IOException;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.ejb.CreateException;
import java.net.MalformedURLException;
import javax.ejb.FinderException;

import com.choicemaker.cm.server.base.InvalidModelException;
import com.choicemaker.cm.server.base.DatabaseException;
									   
*/
