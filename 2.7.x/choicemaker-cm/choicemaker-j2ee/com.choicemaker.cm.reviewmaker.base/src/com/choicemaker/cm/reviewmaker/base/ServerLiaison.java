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

import java.rmi.RemoteException;

import javax.security.auth.login.LoginException;

import com.choicemaker.cm.core.base.*;
import com.choicemaker.cm.core.datamodel.CompositeObservableData;
import com.choicemaker.cm.core.sort.*;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1 $ $Date: 2010/01/23 13:28:14 $
 */
public interface ServerLiaison extends CompositeObservableData{
	Descriptor getDescriptor() throws RemoteException;

    /**
     * @return the number of ReviwCases pending review.
     */
    int getNumPendingReviews() throws RemoteException;
    
	Record[] getRecordsNeedingReview(Record query, SortCondition sc, int startIndex, int endIndex) throws RemoteException;

	ReviewCase getReviewCase(Record r) throws RemoteException;

    /**
     * @return a new Case to Review or null if no cases are available.
     */
    ReviewCase getNextReviewCase() throws RemoteException;

    /**
     * Records the human decisions and releases the Records involved in the Case.
     */
    void completeReview(ReviewCase reviewCase) throws RemoteException;
    
    /**
     * Leaves the Records involved in the Case untouched and releases them.
     */
    void releaseCase(ReviewCase reviewCase) throws RemoteException;
    
	void login() throws LoginException;
	
	void logout();
}
