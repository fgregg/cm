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
package com.choicemaker.cm.transitivity.server.ejb;

import java.rmi.RemoteException;
import java.security.AccessControlException;

import javax.ejb.EJBObject;

import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.core.InvalidModelException;
import com.choicemaker.cm.core.InvalidProfileException;
import com.choicemaker.cm.core.Profile;
import com.choicemaker.cm.core.UnderspecifiedProfileException;
import com.choicemaker.cm.core.base.MatchCandidate;
import com.choicemaker.cm.transitivity.core.TransitivityException;
import com.choicemaker.cm.transitivity.core.TransitivityResult;

/**
 * This session bean starts TE for online matching.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public interface TransitivityService extends EJBObject {

	  String DEFAULT_EJB_REF_NAME = "ejb/TransitivityService";
	  String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME ;

	/**
	 * Finds matches and possible matches.
	 * 
	 * @param   profile  The query profile. E.g., looking for 'JIM SMITH'.
	 * @param   constraint  Constraints on records to return. The actual semantics and syntax is implementation dependent.
	 *            A typical example is to query only for records in certain states, e.g., active.
	 * @param   probabilityModel  The name of the probability accessProvider to use. By convention, "" denotes the default accessProvider.
	 * @param   differThreshold  The differ threshold.
	 *             The value must satisfy <code>0 &lt; differThreshold &lt;=
	 *             highProbabilityThreshold</code>.
	 * @param    matchThreshold  The match threshold. See above for explanation and constraints.
	 * @param    maxNumMatches The maximum number of matches that ChoiceMaker may return.
	 *             See also <code>UnderspecifiedProfileException</code> below.
	 * @param    returnDataFormat  The format in which to return the actual record data, e.g., XML or bean. No
	 *             data is returned if <code>returnDataFormat</code> is <code>null</code> or an unknown value.
	 * @param    purpose  An arbitrary string that is stored and may be used for
	 *             later reporting.
	 * @param	 compact - set this to true if you want the CompositeEntity in the
	 * 				TransitivityResult to be compacted before returning. 
	 * @return   A TransitivityResult object
	 * @throws    UnderspecifiedProfileException  If the profile is not specific enough (e.g., all JIMs in NYC)
	 *             to perform blocking given <code>maxNumMatches</code>.
	 * @throws   InvalidModelException  if the accessProvider does not exist or is not properly configured.
	 * @throws   InvalidProfileException  if <code>profile</code> does not adhere to above.
	 * @throws   IllegalArgumentException if the specified probability accessProvider specified does not exist or the
	 *             values of <code>differThreshold</code> and <code>matchThreshold</code> don't satisfy the constraints.
	 * @throws   DatabaseException If a database error prevents ChoiceMaker from fulfilling the request.
	 * @throws   AccessControlException  If authentication or authorization fails.
	 * @throws   RemoteException  If a communication problem occurs.
	 */
	public TransitivityResult findClusters(
		Profile profile,
		Object constraint,
		String probabilityModel,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		String returnDataFormat,
		String purpose,
		boolean compact)
		throws AccessControlException, InvalidProfileException, RemoteException, 
		InvalidModelException, UnderspecifiedProfileException, DatabaseException;
		
	
	/** This method takes the output of findMatches and runs the match result through the
	 * Transitivity Engine.
	 * 
	 * @param profile - contains the query record
	 * @param candidates - match candidates to the query record
	 * @param modelName - probability accessProvider name
	 * @param differThreshold - differ threshold
	 * @param matchThreshold - match threshold
	 * @param compact - set this to true if you want the CompositeEntity in the
	 * 			TransitivityResult to be compacted before returning. 
	 * @return A TransitivityResult object
	 * @throws   RemoteException  If a communication problem occurs.
	 * @throws InvalidProfileException
	 * @throws TransitivityException
	 * @throws InvalidModelException  if the accessProvider does not exist or is not properly configured.
	 */
	public TransitivityResult findClusters(
		Profile profile,
		MatchCandidate[] candidates,
		String modelName,
		float differThreshold,
		float matchThreshold,
		boolean compact) throws 
		RemoteException, InvalidProfileException, TransitivityException, InvalidModelException;

}
