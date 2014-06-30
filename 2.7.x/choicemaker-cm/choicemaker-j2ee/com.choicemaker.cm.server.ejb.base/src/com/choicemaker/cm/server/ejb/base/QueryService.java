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
package com.choicemaker.cm.server.ejb.base;

import java.rmi.RemoteException;
import java.security.AccessControlException;

import javax.ejb.EJBObject;

import com.choicemaker.cm.core.base.*;
import com.choicemaker.cm.server.base.*;


/**
 * Remote interface for query service.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:13 $
 */
public interface QueryService extends EJBObject {
	/**
	 * Finds matches and possible matches.
	 * 
	 * @param   profile  The query profile. E.g., looking for 'JIM SMITH'.
	 * @param   constraint  Constraints on records to return. The actual semantics and syntax is implementation dependent.
	 *            A typical example is to query only for records in certain states, e.g., active.
	 * @param   probabilityModel  The name of the probability model to use. By convention, "" denotes the default model.
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
	 * @return   A list of record ordered lexicographically by decision (MATCH,
	 *             HOLD, DIFFER), then match probability decreasing.
	 *             All returned records match with a probability of <code>differProbability</code>
	 *             or higher or have a firing report clue or filter. The records are encoded in the same
	 *             format as the <code>profile</code> (<code>null</code>, com.choicemaker.cm.core.base.beanMatchCandidate,
	 *             com.choicemaker.cm.io.xml.xmlMatchCandidate, etc).
	 * @throws    UnderspecifiedProfileException  If the profile is not specific enough (e.g., all JIMs in NYC)
	 *             to perform blocking given <code>maxNumMatches</code>.
	 * @throws   InvalidModelException  if the model does not exist or is not properly configured.
	 * @throws   InvalidProfileException  if <code>profile</code> does not adhere to above.
	 * @throws   IllegalArgumentException if the specified probability model specified does not exist or the
	 *             values of <code>differThreshold</code> and <code>matchThreshold</code> don't satisfy the constraints.
	 * @throws   DatabaseException If a database error prevents ChoiceMaker from fulfilling the request.
	 * @throws   AccessControlException  If authentication or authorization fails.
	 * @throws   RemoteException  If a communication problem occurs.
	 */
	public Result findMatches(
		Profile profile,
		Object constraint,
		String probabilityModel,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		String returnDataFormat,
		String purpose)
		throws InvalidModelException, InvalidProfileException, UnderspecifiedProfileException, DatabaseException, AccessControlException, RemoteException;

}
