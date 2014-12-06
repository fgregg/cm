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
import com.choicemaker.cm.urm.base.LinkCriteria;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;
import com.choicemaker.cm.urm.exceptions.RecordException;
import com.choicemaker.cm.urm.exceptions.UrmIncompleteBlockingSetsException;
import com.choicemaker.cm.urm.exceptions.UrmUnderspecifiedQueryException;

/**
 * Allows a client application to find all records matching (evaluated as MATCH
 * or HOLD ) with the specified record in online (synchronous) mode and executes
 * further analysis of the obtained set of records to identify the subsets that
 * are most likely linked together; the linked records are normally denote the
 * same physical entity and are recommended for merging.
 * <p>
 * The below example demonstrates how the
 * <code>getCompositeMatchCandidates</code> would be invoked within the client
 * application.
 * <p>
 * As the query record it passes a record holder. A record holder is a class
 * that stores actual record data as values of it's member variables. Record
 * holders are customer specific classes and generated from the customer's
 * ChoiceMaker schema which is a part of the matching model. Customer specific
 * record holder clases are derived from the <code>IRecordHolder</code>
 * interface that is derived from <code>ISingleRecord</code>. Record holders are
 * included into the deployment package as part of the models.jar library.
 * Another type of a record that could be passed as the query is
 * <code>GlobalRecordRef</code>. It represents a reference to a record and
 * doesn't contain actual data at runtime.
 * <p>
 * As the master record collection it passes a database record collection (an
 * instance of <code>DbRecordCollection</code>). The
 * <code>DbRecordCollection</code> is a reference to a record collection located
 * in a database. Database is defined by the URL and the record collection by
 * the DB configuration name. DB Configuration defines a set ChoiceMaker
 * specific views that implement the mapping from the actual customer tables
 * into the ChoiceMaker matching model record structure defined by the
 * ChoiceMaker schema ( .schema file).
 * <p>
 * For more details regarding record classes and record collection classes
 * please see the <code>com.choicemaker.cm.urm.base</code> package. For more
 * details regarding the ChoiceMakel model, schema and DB configuration please
 * see ChoiceMaker User Guide.
 * <p>
 * <p>
 * 
 * <pre>
 * InitialContext initialContext = new InitialContext();
 * Object queryHomeRef =
 * 	initialContext.lookup(&quot;java:comp/env/ejb/OnlineMatchAnalyzer&quot;);
 * OnlineMatchAnalyzerHome qsh =
 * 	(OnlineMatchAnalyzerHome) PortableRemoteObject.narrow(queryHomeRef,
 * 			OnlineMatchAnalyzerHome.class);
 * OnlineMatchAnalyzer qs = qsh.create();
 * PatientHolder ph = new PatientHolder();
 * 
 * NamesHolder nh = new NamesHolder();
 * nh.setFirst_name(request.getParameter(&quot;firstName&quot;));
 * nh.setLast_name(request.getParameter(&quot;lastName&quot;));
 * ph.setNames(new NamesHolder[] { nh });
 * 
 * String mu = request.getParameter(&quot;MasterURL&quot;);
 * String mConf = request.getParameter(&quot;MasterConfig&quot;);
 * DbRecordCollection masterCollection = new DbRecordCollection(mu, mConf);
 * 
 * float differThreshold = Float.parseFloat(request.getParameter(&quot;dth&quot;));
 * float matchThreshold = Float.parseFloat(request.getParameter(&quot;mth&quot;));
 * int maxNumMatches = 100;
 * String modelName = &quot;onlineMatch&quot;;
 * 
 * EvalRecordFormat resultFormat =
 * 	new EvalRecordFormat(ScoreType.NO_NOTE, RecordType.HOLDER);
 * LinkCriteria lk = new LinkCriteria(GraphProperty.BCM, true);// true - must
 * 															// include query
 * 															// record
 * 
 * EvaluatedRecord[] mc =
 * 	qs.getCompositeMatchCandidates(ph, masterCollection, modelName,
 * 			differThreshold, matchThreshold, maxNumMatches, lk, resultFormat,
 * 			&quot;test&quot;);
 * for (int n = 0; n &lt; mc.length; n++) {
 * 	IRecord r = mc[n].getRecord();
 * 	if (r instanceof LinkedRecordSet) {
 * 		System.out.println(&quot;Linked Record Set: \n&quot;);
 * 		LinkedRecordSet lrs = (LinkedRecordSet) r;
 * 		for (int i = 0; i &lt; lrs.getRecords().length; i++) {
 * 			MatchScore rs =
 * 				((CompositeMatchScore) mc[n].getScore()).getInnerScores()[i];
 * 			System.out.println(&quot;Record ID: &quot; + lrs.getRecords()[i].getId()
 * 					+ &quot; Decision: &quot; + rs.getDecision() + &quot; Prob: &quot;
 * 					+ rs.getProbability() + &quot;\n&quot;);
 * 		}
 * 	} else {
 * 		MatchScore rs = (MatchScore) mc[n].getScore();
 * 		System.out.println(&quot;Single record: &quot; + r.getId() + &quot; Decision: &quot;
 * 				+ rs.getDecision() + &quot; Prob: &quot; + rs.getProbability() + &quot;\n&quot;);
 * 	}
 * }
 * </pre>
 * <p>
 * Each EvaluatedRecord in the returned array consists of a matched record and a
 * score reflecting the likelihood of a match. In contrast to the result of
 * <code>getMatchCandidates</code> each returned evaluated record could be
 * either linked record set or a single recors. If a link criteria is passed to
 * the method requires linked record sets to include query record then the
 * output (by the definition of the linked record set) will contain only one
 * linked set. Otherwise it is possible that the result will include more then
 * one linked record set.
 * 
 * 
 * @author emoussikaev
 * @version Revision: 2.5 Date: Jun 28, 2005 2:40:13 PM
 * @see
 */
public interface OnlineMatchAnalyzer extends EJBObject {

	/**
	 * Finds all records from the master record collection <code>mRc</code> that
	 * are evaluated as a MATCH or HOLD to the query record
	 * <code>queryRecord</code>. Then it analyzes those records to extract sets
	 * of records that are tightly connected together my match/hold
	 * relationships. The exact meaning of being <i>tightly connected
	 * together</i> is defined by the link criteria <code>c</code>. Records
	 * placed in one group are recommended for merging. The result array of
	 * evaluated record includes extracted <code>LinkedRecordSets</code> as well
	 * as the rest single records that were not included into any linked set.
	 * Each evaluated record includes match score to the query record. Array is
	 * ordered by the match probability.
	 * <p>
	 * If LinkCriteria requires to include query record, then, by the definition
	 * of the linked record set, resulting array will include maximum one set.
	 * Records of this set and the query record are recommended for merged. The
	 * rest records are recommended for a clerical review.
	 * <p>
	 * If link criteria does not require to include query record then resulting
	 * array may include more then one linked record set. Query record will
	 * belong to exacly one set. Records of each set are recommended to be
	 * merged. The rest of the records are recommended for a clerical review.
	 * 
	 * @param queryRecord
	 *            query record
	 * @param mRc
	 *            a master record collection.
	 * @param modelName
	 *            the name of the probability model.
	 * @param differThreshold
	 *            matching probability below this threshold constitutes the
	 *            differ.
	 * @param matchThreshold
	 *            matching probability above this threshold constitutes the
	 *            match.
	 * @param maxNumMatches
	 *            the limit of number of records included into the resulting
	 *            array. The value <code>-1</code> means bring back all matches
	 *            and holds.
	 * @param c
	 *            link criteria
	 * @param resultFormat
	 *            the format of the evaluated records that will be returned as
	 *            the result.
	 * @param trackingId
	 *            an arbitrary string that is stored and may be used for later
	 *            reporting.
	 * @return an array of evaluated records
	 * @throws ModelException
	 * @throws UrmIncompleteBlockingSetsException
	 * @throws UrmUnderspecifiedQueryException
	 * @throws RecordException
	 * @throws RecordCollectionException
	 * @throws ConfigException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	public EvaluatedRecord[] getCompositeMatchCandidates(
			ISingleRecord queryRecord, DbRecordCollection mRc,
			String modelName, float differThreshold, float matchThreshold,
			int maxNumMatches, LinkCriteria c, EvalRecordFormat resultFormat,
			String externalId) throws ArgumentException, ModelException,
			UrmIncompleteBlockingSetsException,
			UrmUnderspecifiedQueryException, RecordException,
			RecordCollectionException, ConfigException, CmRuntimeException,
			RemoteException;

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
