/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     rphall -- extracted this interface from MultiStatus class
 *******************************************************************************/
package com.choicemaker.e2;

public interface CMMultiStatus extends CMStatus {

	/**
	 * Adds the given status to this multi-status.
	 *
	 * @param status the new child status
	 */
	void add(CMStatus status);

	/**
	 * Adds all of the children of the given status to this multi-status.
	 * Does nothing if the given status has no children (which includes
	 * the case where it is not a multi-status).
	 *
	 * @param status the status whose children are to be added to this one
	 */
	void addAll(CMStatus status);

	/**
	 * Merges the given status into this multi-status.
	 * Equivalent to <code>add(status)</code> if the
	 * given status is not a multi-status. 
	 * Equivalent to <code>addAll(status)</code> if the
	 * given status is a multi-status. 
	 *
	 * @param status the status to merge into this one
	 * @see #add
	 * @see #addAll
	 */
	void merge(CMStatus status);

}