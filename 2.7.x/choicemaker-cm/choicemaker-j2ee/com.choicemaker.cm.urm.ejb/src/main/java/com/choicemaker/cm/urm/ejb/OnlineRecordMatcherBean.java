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
import java.sql.SQLException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.urm.adaptor.tocmcore.UrmRecordBuilder;
import com.choicemaker.cm.urm.base.DbRecordCollection;
import com.choicemaker.cm.urm.base.EvalRecordFormat;
import com.choicemaker.cm.urm.base.EvaluatedRecord;
import com.choicemaker.cm.urm.base.ISingleRecord;
import com.choicemaker.cm.urm.base.MatchScore;
import com.choicemaker.cm.urm.base.ScoreType;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;
import com.choicemaker.cm.urm.exceptions.RecordException;
import com.choicemaker.cm.urm.exceptions.UrmIncompleteBlockingSetsException;
import com.choicemaker.cm.urm.exceptions.UrmUnderspecifiedQueryException;


/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jul 15, 2005 3:40:43 PM
 * @see
 */
@SuppressWarnings({"rawtypes"})
public class OnlineRecordMatcherBean extends OnlineMatchBaseBean {
	
	private static final long serialVersionUID = 1L;

	static {
		log = Logger.getLogger(OnlineRecordMatcherBean.class.getName());
	}
		
	/**
	 * 
	 */
	public OnlineRecordMatcherBean() {
		super();
	}

	
	/**
	 * Checks if two records defined by the profiles are a match
	 * 
	 * @param   qRecord  	The holder object that stores query record.
	 * @param   mRecord  	The holder object that stores master record. 
	 * @param	modelName	The name of the probability accessProvider.
	 * @param	differThreshold  Matching probability below this threshold constitutes the differ.
	 * @param	matchThreshold   Matching probability above this threshold constitutes the match.   	
	 * @return  The corresponding <code>MatchResult</code>.
	 * @throws  RemoteException
	 */						
	 public MatchScore 			evaluatePair(
										ISingleRecord queryRecord,
										ISingleRecord masterRecord,
										String modelName, 
										float differThreshold, 
										float matchThreshold,
										ScoreType resultFormat,
										String externalId) 
								throws 	ModelException, 
										ArgumentException,	
										RecordException,
										ConfigException,
										CmRuntimeException, 
										RemoteException
	{
		MatchScore resMs = null;	
		ImmutableProbabilityModel model = getProbabilityModel(modelName);
		if(resultFormat == null){
			log.severe("Invalid resultFormat argument." );
			throw new ArgumentException("Invalid resultFormat argument.");
		}
			
		
		UrmRecordBuilder irb = new UrmRecordBuilder(model);
		queryRecord.accept(irb);
		Record q = irb.getResultRecord();
		masterRecord.accept(irb);
		Record m = irb.getResultRecord();
		Evaluator eval = model.getEvaluator();
		Match match = eval.getMatch(q, m, differThreshold, matchThreshold);
		if (match != null) {
			resMs = getMatchScore(resultFormat,match,model); 
		}
		else
			resMs = new MatchScore(0.0f,com.choicemaker.cm.urm.base.Decision3.DIFFER, "");
				
 		return resMs;	
	}

		/**
		 * Finds records from the record collection with the probability of match to the record defined by the profile greater then or equal to <code>differThreshold</code>.
		 * 
		 * @param   q  	The holder object that stores query record.
		 * @param   m  		The master record collection. 
		 * @param	modelName	The name of the probability accessProvider.
		 * @param	differThreshold  Matching probability below this threshold constitutes the differ.
		 * @param	matchThreshold   Matching probability above this threshold constitutes the match.
		 * @param	maxNumMatches	 Limits number od records included into the resulting array.
		 *                 The value <code>-1</code> means bring back all matches and holds.
		 * @param	returnRecordRef  If true <code>findMatches</code> returns array of <code>MatchRecordRef</code>, otherwise it returns an array of <code>MatchRecordVal</code>	
		 * @return  An array of matched records.
		 * @throws  RemoteException
		 * @throws SQLException 
		 */	
	
	public EvaluatedRecord[] 	getMatchCandidates(
										ISingleRecord queryRecord, 
										DbRecordCollection masterCollection,
										String modelName, 
										float differThreshold, 
										float matchThreshold,
										int maxNumMatches,
										EvalRecordFormat resultFormat,
										String externalId) 
								throws 	ModelException,
										ArgumentException, 	
										UrmIncompleteBlockingSetsException,
										UrmUnderspecifiedQueryException,
										RecordException,
										RecordCollectionException,
										ConfigException,
										CmRuntimeException, 
										RemoteException, SQLException
	{
		EvaluatedRecord[] matchCand = null;					
				
		try {
			long startTime = System.currentTimeMillis();
			log.fine("<< getMatchCandidates");
			if(resultFormat == null || resultFormat.getRecordType() == null || resultFormat.getScoreType() == null ){
				String errMessage ="Invalid result format argument.";  
				log.severe(errMessage);
				throw new ArgumentException(errMessage);
			}
			if (log.isLoggable(Level.FINE)) {
				writeDebugInfo(
					queryRecord,
					modelName,
					differThreshold,
					matchThreshold,
					maxNumMatches,
					resultFormat,
					externalId,
					Level.FINE);
			}
			SortedSet s = getMatches(startTime,
									 queryRecord, 
			   						 masterCollection,
			   						 modelName, 
			   						 differThreshold, 
			   						 matchThreshold,
									 maxNumMatches,
									 externalId);

			// 2009-09-15 rphall
			// BUG: maxNumMatches may be -1 (as a flag to getMatches(..))
			//
			// matchCand = new EvaluatedRecord[Math.min(s.size(), maxNumMatches)];
			//
			// BUGFIX return all the results returned by getMatches (which should be
			// responsible for throttling back the number of returned values)
			matchCand = new EvaluatedRecord[s.size()];
			// END BUGFIX

			Iterator iS = s.iterator();
			ImmutableProbabilityModel model = getProbabilityModel(modelName);
			for (int i = 0; i < matchCand.length; i++) {
				matchCand[i] = getEvaluatedRecord(resultFormat,(Match) iS.next(),model);
			}
		} catch (RuntimeException ex) {
			log.severe("runtime exception: " + ex);
			throw new CmRuntimeException(ex.toString());
		}		
		log.fine (">> getMatchCandidates");
		return matchCand;
	}

	public String getVersion(Object context)
						throws  RemoteException {
		return Single.getInst().getVersion();					
	}											

}



