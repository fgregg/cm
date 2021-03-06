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
import com.choicemaker.cm.urm.config.UrmSettings2;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * Allows a client application to match specified records collections in a
 * batch(asynchronous) mode.
 * <p>
 * The below example demonstrates how the <code>startMatching</code> would be
 * invoked within the client application.
 * 
 * <pre>
 * InitialContext initialContext = new InitialContext();
 * Object queryHomeRef =
 * 	initialContext.lookup(&quot;java:comp/env/ejb/BatchRecordMatcher&quot;);
 * BatchRecordMatcherHome qsh =
 * 	(BatchRecordMatcherHome) PortableRemoteObject.narrow(queryHomeRef,
 * 			BatchRecordMatcherHome.class);
 * BatchRecordMatcher qs = qsh.create();
 * 
 * String qu = request.getParameter(&quot;QueryURL&quot;);
 * TextRefRecordCollection qRc =
 * 	new TextRefRecordCollection(qu, new XmlTextFormat());
 * String mu = request.getParameter(&quot;MasterURL&quot;);
 * TextRefRecordCollection mRc =
 * 	new TextRefRecordCollection(mu, new XmlTextFormat());
 * String trackingId = request.getParameter(&quot;Tracking Id&quot;);
 * 
 * String temp = request.getParameter(&quot;dth&quot;);
 * float diffTh = Float.parseFloat(temp);
 * temp = request.getParameter(&quot;mth&quot;);
 * float matchTh = Float.parseFloat(temp);
 * 
 * long l =
 * 	qs.startMatching(qRc, mRc, &quot;BatchMatch&quot;, diffTh, matchTh, 1000, trackingId);
 * System.out.println(&quot;Matching Service created job with ID = &quot; + l);
 * </pre>
 * 
 * Method <code>startMatching</code> stars matching process and returns
 * immediately with the identifier of the started job. Using this identifier the
 * client application can check the job status as it shown below.
 * 
 * <pre>
 * JobStatus st = qs.getJobStatus(l);
 * </pre>
 * 
 * Once the job is completed the result file can be copied to the specified URL
 * or can be traversed using the iterator. An example of the result copying is
 * shown below.
 * 
 * <pre>
 * String res = request.getParameter(&quot;ResultURL&quot;);
 * TextRefRecordCollection resRc =
 * 	new TextRefRecordCollection(res, new DelimitedTextFormat(','));
 * String temp1 = request.getParameter(&quot;idres&quot;);
 * long id = Long.parseLong(temp1);
 * qs.copyResult(id, resRc);
 * </pre>
 * <p>
 * The result consists of the evaluated record pairs that are stored in the
 * following format
 * 
 * <pre>
 * &lt;f1, id1, f2, id2, p, d, l&gt;
 * </pre>
 * 
 * where
 * <ul>
 * <li><code>f1</code> is the format of the first (query) record ID</li>
 * <li><code>id1</code> is the query record ID</li>
 * <li><code>f2</code> is the format of the second (query or master) record ID</li>
 * <li><code>id2</code> is the master record ID</li>
 * <li><code>p</code> is the probability of match</li>
 * <li><code>d</code> is the decision (M - match, H - hold, D - differ)</li>
 * <li><code>l</code> is the location of the second record (S - query record
 * collection, D - master record collection)
 * </ul>
 *
 * @author emoussikaev
 */
public interface BatchRecordMatcher extends BatchBase {

	/**
	 * Finds all pairs of records with the first element from the query record
	 * collection qRc and the second element from the master record collection
	 * mRc or query record collection qRc evaluated as MATCH or HOLD. This is an
	 * asynchronous method that returns immediately after the matching process
	 * is started.
	 */
	long startMatching(IRecordCollection qRc, RefRecordCollection mRc,
			String modelName, float differThreshold, float matchThreshold,
			int maxSingle, String trackingId) throws ModelException,
			RecordCollectionException, ConfigException, ArgumentException,
			CmRuntimeException, RemoteException;

	long startMatching(IRecordCollection qRc, RefRecordCollection mRc,
			String confName, String trackingId) throws ModelException,
			RecordCollectionException, ConfigException, ArgumentException,
			CmRuntimeException, RemoteException;

	long startMatching(IRecordCollection qRc, RefRecordCollection mRc,
			String confName, UrmSettings2 overrideProps, String trackingId)
			throws ModelException, RecordCollectionException, ConfigException,
			ArgumentException, CmRuntimeException, RemoteException;

	long[] getJobList() throws ArgumentException, ConfigException,
			CmRuntimeException, RemoteException;

	/**
	 * Returns <code>EvaluatedRecordPairIter</code> ierator that alows to
	 * traverse the result of matching stored in a record collection
	 * <code>rc</code>.
	 * 
	 * Developers should be aware that URM API is a remote API and using
	 * iterators against the data located on the remote computer might be very
	 * inefficient. The better performance can be archived by copying the
	 * results first to a client application local computer and iterating then
	 * against the local data.
	 * <p>
	 * 
	 * @param rc
	 *            record collection with the data that will be traversed
	 * @return iterator
	 * @throws RecordCollectionException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	// FIXME remove this method
	@SuppressWarnings("rawtypes")
	public Iterator getResultIter(RefRecordCollection rc)
			throws RecordCollectionException, ArgumentException,
			CmRuntimeException, RemoteException;

	/**
	 * Returns <code>EvaluatedRecordPairIter</code> ierator that alows to
	 * traverse the result of matching job with <code>jobId</code> identifier.
	 * 
	 * Developers should be aware that URM API is a remote API and using
	 * iterators against the data located on the remote computer might be very
	 * inefficient. The better performance can be archived by copying the
	 * results first to a client application local computer and iterating then
	 * against the local data.
	 * <p>
	 * 
	 * @param jobId
	 *            job identifier
	 * @return iterator
	 * @throws RecordCollectionException
	 * @throws ArgumentException
	 * @throws CmRuntimeException
	 * @throws RemoteException
	 */
	// FIXME remove this method
	@SuppressWarnings("rawtypes")
	public Iterator getResultIter(long jobId) throws RecordCollectionException,
			ArgumentException, CmRuntimeException, RemoteException;

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
