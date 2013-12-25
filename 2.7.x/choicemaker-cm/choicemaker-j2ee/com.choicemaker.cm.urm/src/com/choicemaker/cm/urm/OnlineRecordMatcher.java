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

import com.choicemaker.cm.urm.base.DbRecordCollection;
import com.choicemaker.cm.urm.base.EvalRecordFormat;
import com.choicemaker.cm.urm.base.EvaluatedRecord;
import com.choicemaker.cm.urm.base.ISingleRecord;
import com.choicemaker.cm.urm.base.MatchScore;
import com.choicemaker.cm.urm.base.ScoreType;
import com.choicemaker.cm.urm.exceptions.*;

/**
 * Allows a client application to match specified records in online(synchronous) mode.
 * <p>
 * Method <code>evaluatePair</code> main arguments are two records. 
 * It compares them and evaluates their match probability and match decision.
 * <p>
 * Method <code>getMatchCandidates</code> main arguments are a (query) record and a (master) records collection. 
 * It finds all records from the master record collection
 * that are matching (evaluated as MATCH or HOLD ) with the query record.
 * <p>
 * The below example demonstrates how the <code>getMatchCandidates</code> would be invoked within the client application.
 * <p>
 * As the query record it passes a record holder. A record holder is a class that stores actual record data as values of it's variables. 
 * Record holders are customer specific classes and generated from the customer matching schema which is a part of the matching model.
 * Customer specific record holder clases are derived from the <code>IRecordHolder</code> interface that is derived from <code>ISingleRecord</code>.  
 * Record holder are included into the deployment package as part of the models.jar library. Another type of a
 * record that could be passed as the query is <code>GlobalRecordRef</code>. It represents a reference to a record and
 * doesn't contain actual data at runtime.
 * <p>
 * As the master record collection it passes a database record collection ( an instance of <code>DbRecordCollection</code>).
 * The <code>DbRecordCollection</code> is a reference to a record collection located in a database.
 * Database is defined by the URL and the record collection by the DB configuration name. DB Configuration is a set ChoiceMaker 
 * specific views that implements the mapping from real customer tables into the ChoiceMaker matching model record structure defined by
 * model's schema file.   
 * <p>
 * For more details regarding record classes and record collection classes 
 * please see the <code>com.choicemaker.cm.urm.base</code> package. For more details regarding the ChoiceMakel model, schema and
 * DB configuration please see ChoiceMaker User Guide.
 * <p>
 * <pre>
 *	InitialContext initialContext = new InitialContext();
 *	Object queryHomeRef = initialContext.lookup("java:comp/env/ejb/OnlineRecordMatcher");
 *	OnlineRecordMatcherHome qsh = 
 *       (OnlineRecordMatcherHome)PortableRemoteObject.narrow(queryHomeRef, OnlineRecordMatcherHome.class);
 *	OnlineRecordMatcher qs = qsh.create();
 * 	PatientHolder ph = new PatientHolder();
 * 
 * 	NamesHolder nh = new NamesHolder();
 *	nh.setFirst_name(request.getParameter("firstName"));
 *	nh.setLast_name(request.getParameter("lastName"));
 *	ph.setNames(new NamesHolder[]{nh});
 * 
 *	String mu = request.getParameter("MasterURL");
 *	String mConf = request.getParameter("MasterConfig");
 *	DbRecordCollection masterCollection = new DbRecordCollection(mu,mConf);
 *		
 *	float differThreshold = Float.parseFloat( request.getParameter("dth") );
 *	float matchThreshold = Float.parseFloat(request.getParameter("mth"));
 *	int maxNumMatches = 100;
 *	String modelName = "onlineMatch";
 *  String trackingId = "doh-test";
 * 
 *	EvalRecordFormat resultFormat = new EvalRecordFormat(ScoreType.NO_NOTE,RecordType.HOLDER);
 *		
 *	EvaluatedRecord[] mc = qs.getMatchCandidates(ph, 
 *				  masterCollection,
 *				  modelName, 
 *				  differThreshold, 
 *				  matchThreshold,
 *				  maxNumMatches,
 *				  resultFormat,
 *				  trackingId);
 *
 *	for(int n=0; n&lt;mc.length; n++){
 *	  NamesBase nb = ((PatientHolder)mc[n].getRecord()).getNames(0);
 *	  MatchScore ms = (MatchScore)mc[n].getScore();
 *	  System.out.println ( "Record ID: "+mc[n].getRecord().getId()+
 *                        " First Name: "+nb.getFirst_name()+
 *                        " Last Name: "+nb.getLast_name()+
 *                        " Decision: "+ms.getDecision()+
 *                        " Probability: "+ms.getProbability()+"\n");
 *	}
 * </pre>
 * <p>
 * Each EvaluatedRecord in the returned array consists of a matched record and a score reflecting the likelihood of the match. 
 * The format of the matched records depends of the <code>resultFormat</code> passed to the <code>getMatchCandidates</code>.
 * In the above case record type is defined as the <code>RecordType.HOLDER</code> and records will be returned as PatientHolder objects (the same way they were passed).
 * The score part of the evaluated record contains the match probability (number between 0.0 and 1.0))and the match decision (MATCH, DIFFER or HOLD). 
 * The decision is calculated by comparing the match probability to the threshold probabilities that are passed into the <code>getMatchCandidates</code>.
 * Only records with the decision match or hold are returned.
 * 
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jun 28, 2005 2:40:13 PM
 * @see
 */
public interface OnlineRecordMatcher extends EJBObject {

	/**
	 * Compares two records and evaluates their match probability and match decision 
	 * based on the differThreshold, matchThreshold and the clues and rules of the matching model.
	 * The resulting match score is provided in the format defined by the <code>resultFormat</code> parameter.
	 * <p> 
	 * 
	 * @param queryRecord the first(query) record.
	 * @param masterRecord the second(master) record.
	 * @param modelName the name of the probability model.
	 * @param differThreshold matching probability below this threshold constitutes the differ.
	 * @param matchThreshold matching probability above this threshold constitutes the match.
	 * @param resultFormat the format of the match score that will be returned as the result. 
	 * @param externalId an arbitrary string that is stored and may be used for later reporting.
	 * 
	 * @return the match score between the first and the second record.
	 *  
	 * @throws ModelException
	 * @throws RecordException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */

	public MatchScore evaluatePair(
		ISingleRecord queryRecord,
		ISingleRecord masterRecord,
		String modelName,
		float differThreshold,
		float matchThreshold,
		ScoreType resultFormat,
		String externalId)
		throws
			ModelException,
			ArgumentException,
			RecordException,
			ConfigException,
			CmRuntimeException,
			RemoteException;

	/**
	 * Finds all records from the master record collection <code>masterCollection</code> 
	 * that are evaluated as MATCH or HOLD with the query record <code>queryRecord</code>. 
	 * Each evaluated record from the result array is a pair of a found record
	 * and a match score to the query record. 
	 * <p> 
	 * 
	 * @param queryRecord a query record.
	 * @param mRc a master record collection.
	 * @param modelName the name of the probability model.
	 * @param differThreshold matching probability below this threshold constitutes the differ.
	 * @param matchThreshold matching probability above this threshold constitutes the match.
	 * @param maxNumMatches the limit of number of records included into the resulting array.
	 *                 The value <code>-1</code> means bring back all matches and holds.
	 * @param resultFormat the format of the evaluated records that will be returned as the result.
	 * @param externalId an arbitrary string that is stored and may be used for later reporting.
	 * 
	 * @return the array of the evaluated records
	 * 
	 * @throws ModelException
	 * @throws UrmIncompleteBlockingSetsException
	 * @throws UrmUnderspecifiedQueryException
	 * @throws RecordException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public EvaluatedRecord[] getMatchCandidates(
		ISingleRecord queryRecord,
		DbRecordCollection mRc,
		String modelName,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		EvalRecordFormat resultFormat,
		String externalId)
		throws
			ModelException,
			ArgumentException,
			UrmIncompleteBlockingSetsException,
			UrmUnderspecifiedQueryException,
			RecordException,
			RecordCollectionException,
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

	public String getVersion(Object context) throws RemoteException;

}
