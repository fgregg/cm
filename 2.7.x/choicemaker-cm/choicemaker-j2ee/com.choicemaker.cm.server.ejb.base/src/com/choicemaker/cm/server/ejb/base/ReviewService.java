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

import javax.ejb.EJBObject;

import com.choicemaker.cm.core.base.Descriptor;
import com.choicemaker.cm.core.base.Record;
import com.choicemaker.cm.core.sort.SortCondition;
import com.choicemaker.cm.reviewmaker.base.ClientData;
import com.choicemaker.cm.reviewmaker.base.ServerData;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:14 $
 */
public interface ReviewService extends EJBObject {
	Descriptor getDescriptor() throws RemoteException;

	/**
	 * @return the number of ReviwCases pending review.
	 */
	int getNumPendingReviews() throws RemoteException;
    
	/**
	 * @return a new Case to Review or null if no cases are available.
	 */
	ServerData getNextReviewCase() throws RemoteException;

	/**
	 * Records the human decisions and releases the Records involved in the Case.
	 */
	void completeReview(ClientData clientData) throws RemoteException;
    
	/**
	 * Leaves the Records involved in the Case untouched and releases them.
	 */
	void releaseCase(int reviewCaseId) throws RemoteException;
	
	Record[] getRecordsNeedingReview(Record query, SortCondition sortCondition, int startIndex, int endIndex) throws RemoteException;

	ServerData getReviewCase(Record r) throws RemoteException;
}
