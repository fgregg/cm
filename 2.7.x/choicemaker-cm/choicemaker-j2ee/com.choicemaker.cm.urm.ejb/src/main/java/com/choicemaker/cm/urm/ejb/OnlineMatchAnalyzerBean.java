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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.Entity;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.TransitivityException;
import com.choicemaker.cm.transitivity.server.util.ClusteringIteratorFactory;
import com.choicemaker.cm.transitivity.util.CEFromMatchesBuilder;
import com.choicemaker.cm.urm.base.CompositeMatchScore;
import com.choicemaker.cm.urm.base.DbRecordCollection;
import com.choicemaker.cm.urm.base.Decision3;
import com.choicemaker.cm.urm.base.EvalRecordFormat;
import com.choicemaker.cm.urm.base.EvaluatedRecord;
import com.choicemaker.cm.urm.base.ISingleRecord;
import com.choicemaker.cm.urm.base.LinkCriteria;
import com.choicemaker.cm.urm.base.LinkedRecordSet;
import com.choicemaker.cm.urm.base.MatchScore;
import com.choicemaker.cm.urm.base.RecordRef;
import com.choicemaker.cm.urm.base.RecordType;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;
import com.choicemaker.cm.urm.exceptions.RecordException;
import com.choicemaker.cm.urm.exceptions.UrmIncompleteBlockingSetsException;
import com.choicemaker.cm.urm.exceptions.UrmUnderspecifiedQueryException;


/**
 * Allows a client application to match specified records in online(synchronous) mode. This is online part of the RecordMatcher that
 * includes all synchronous methods. 
 * 
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jun 28, 2005 2:40:13 PM
 * @see
 */
public class OnlineMatchAnalyzerBean extends OnlineMatchBaseBean {

	private static final long serialVersionUID = 1L;

	static	{
		log = Logger.getLogger(OnlineMatchAnalyzerBean.class.getName());
	}
	
		/**
	 * 
	 */
	public OnlineMatchAnalyzerBean() {
		super();
	}

	  public EvaluatedRecord[]	getCompositeMatchCandidates(
									ISingleRecord queryRecord, 
									DbRecordCollection masterCollection,
									String modelName, 
									float differThreshold, 
									float matchThreshold,
									int maxNumMatches,
									LinkCriteria linkCriteria,
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
									RemoteException
 	 {
		ArrayList  evalRecords = new ArrayList();					
		
		IProbabilityModel model = getProbabilityModel(modelName);
		Record q = getInternalRecord(model,queryRecord);
				
		try {
			long startTime = System.currentTimeMillis();
			log.debug("<< getMatchCandidates");
			
			if (resultFormat == null || resultFormat.getRecordType() == null
					|| resultFormat.getScoreType() == null
					|| resultFormat.getRecordType() == RecordType.NONE) {
				String errMessage = "Invalid result format argument.";
				log.error(errMessage);
				throw new ArgumentException(errMessage);
			}
			if(linkCriteria == null ){
				String errMessage ="Invalid link criteria argument.";  
				log.error(errMessage);
				throw new ArgumentException(errMessage);
			}
			if(queryRecord.getId() == null){
				String errMessage ="Query record must have valid ID.";  
				log.error(errMessage);
				throw new ArgumentException(errMessage);
			}			
			log.debug("link criteria: "
					+ linkCriteria.getGraphPropType().toString() + " "
					+ linkCriteria.isMustIncludeQuery());
			if (log.isDebugEnabled()) {
				writeDebugInfo(
					queryRecord,
					modelName,
					differThreshold,
					matchThreshold,
					maxNumMatches,
					resultFormat,
					externalId,
					Priority.DEBUG);
			}
			SortedSet sortedMatches = getMatches(startTime,
						 queryRecord, 
						 masterCollection,
						 modelName, 
						 differThreshold, 
						 matchThreshold,
						 maxNumMatches, 
						 externalId);
			 
			HashMap matches = new HashMap();
			Iterator it = sortedMatches.iterator();
			while(it.hasNext()){
				Match match = (Match)it.next();
				matches.put(match.id,match);	
			}

			// Get a iterator over the returned records
			CEFromMatchesBuilder builder =	new CEFromMatchesBuilder (
				q, sortedMatches.iterator(),modelName, differThreshold, matchThreshold);
			Iterator ceIter = builder.getCompositeEntities();
			
			// Get an iterator to group the records into clusters
			String name = linkCriteria.getGraphPropType().getName();
			ClusteringIteratorFactory f = ClusteringIteratorFactory.getInstance();
			Iterator compactedCeIter = f.createClusteringIterator(name,ceIter);
			 
			// Get the clusters (there should be just one, with every record
			// connected by a hold or a match to the query record)
			CompositeEntity compositeEntity = null;
			if(compactedCeIter.hasNext()) {
				compositeEntity = (CompositeEntity)compactedCeIter.next();
			}
			if(compositeEntity == null ) {
				log.info("no matching composite entity");
				return null;
			} else if (compactedCeIter.hasNext()) {
				String msg = "algorithm error: too many matching composite entities";
				log.error(msg);
				throw new Error(msg);
			}

			// Get the groups of records in the cluster
			List childEntities = compositeEntity.getChildren();
			if(childEntities == null) {
				log.info("empty composite entity");
				return null;
			}
			Comparable queryId  = q.getId();
			for(Iterator itChildEntities = childEntities.iterator(); itChildEntities.hasNext(); ){

				INode childNode = (INode) itChildEntities.next();

				// Handle an isolated record
				if(childNode instanceof Entity){
					Comparable nodeId = childNode.getNodeId();
					if(!nodeId.equals(queryId)) {
						Match m = (Match) matches.get(nodeId);
						EvaluatedRecord er = getEvaluatedRecord(resultFormat,m,model);
						evalRecords.add(er);
					}

				// Handle a group of records
				} else if (childNode instanceof CompositeEntity){
					CompositeEntity group = (CompositeEntity) childNode;
					ArrayList groupRecords = new ArrayList();
					ArrayList groupScores =  new ArrayList();
					boolean isContainQuery = false;
					
					// Iterate over the records in the group
					for(Iterator itGroup = group.getChildren().iterator(); itGroup.hasNext(); ){

						INode groupChildNode = (INode) itGroup.next();

						// Check if this record is the query record and whether it should be included
						if(queryId.equals( groupChildNode.getNodeId() )){
							isContainQuery = true;
							if(linkCriteria.isMustIncludeQuery()){
								if(resultFormat.getRecordType()== RecordType.REF) {
									groupRecords.add( new RecordRef(queryId));
								} else {
									groupRecords.add(queryRecord);
								}
								groupScores.add(new MatchScore(1.0f,Decision3.MATCH,""));
							}

						// Otherwise add the record from the group as a single record match
						// to the query record
						} else {
							Match match = (Match)matches.get( groupChildNode.getNodeId() );
							groupRecords.add(getSingleRecord(resultFormat,match,model));  
							groupScores.add(getMatchScore(resultFormat.getScoreType(),match,model));
						}
												
					} // for groupNodeChildren
					
					// If every evaluated record must be linked to the query record, but the
					// query record is not part of this group, then bust the group into
					// individual records
					if( linkCriteria.isMustIncludeQuery()&& !isContainQuery )	{
						for(int n =0; n<groupRecords.size(); n++) {
							ISingleRecord singleRecord = (ISingleRecord)groupRecords.get(n);
							MatchScore singleScore = (MatchScore)groupScores.get(n);
							EvaluatedRecord er = new EvaluatedRecord(singleRecord,singleScore);
							evalRecords.add(er);					
						}
					// Otherwise, add the group as a whole
					} else {
						ISingleRecord[] arGroupRecords = (ISingleRecord[])groupRecords.toArray(new ISingleRecord[0]);
						LinkCriteria criteria = new LinkCriteria(linkCriteria.getGraphPropType(),linkCriteria.isMustIncludeQuery());
						LinkedRecordSet lrs = new LinkedRecordSet(null,arGroupRecords,criteria);
						MatchScore[] scores = (MatchScore[])groupScores.toArray(new MatchScore[0]);
						CompositeMatchScore compositeScore = new CompositeMatchScore(scores);
						//TODO - set an id for the record set
						EvaluatedRecord er = new EvaluatedRecord(lrs, compositeScore);
						evalRecords.add(er);
					}
					
				} else {
					String msg = "internal error: unexpected node type";
					log.error(msg);
					throw new CmRuntimeException(msg);
				}
					
			}
				
		} catch (TransitivityException ex) {
			String msg = "transitivity exception: " + ex.toString();
			log.error(msg);
			throw new RemoteException(msg);
		}

		return (EvaluatedRecord[])evalRecords.toArray(new EvaluatedRecord[0]);
	 }

	public String getVersion(Object context)
						throws  RemoteException {
		return Single.getInst().getVersion();					
	}											
	 
}

		
