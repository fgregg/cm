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
package com.choicemaker.cm.reviewmaker.base;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.sort.SortCondition;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/23 13:28:14 $
 */
public interface ReviewService extends Remote {
	Descriptor getDescriptor(Object session) throws RemoteException;

	/**
	 * @return the number of ReviwCases pending review.
	 */
	int getNumPendingReviews(Object session) throws RemoteException;
    
	/**
	 * @return a new Case to Review or null if no cases are available.
	 */
	ServerData getNextReviewCase(Object session) throws RemoteException;

	/**
	 * Records the human decisions and releases the Records involved in the Case.
	 */
	void completeReview(Object session, ClientData clientData) throws RemoteException;
    
	/**
	 * Leaves the Records involved in the Case untouched and releases them.
	 */
	void releaseCase(Object session, int reviewCaseId) throws RemoteException;
	
	Record[] getRecordsNeedingReview(Object session, Record query, SortCondition sortCondition, int startIndex, int endIndex) throws RemoteException;

	ServerData getReviewCase(Object session, Record r) throws RemoteException;
}
