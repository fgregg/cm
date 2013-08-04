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
package com.choicemaker.cm.urm.base;

import java.io.Serializable;

import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * A collection of records. Represents records available for matching. 
 * <p>
 * A base interface for all <code>RecordCollection</code> sub-classes.   
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:30:27 PM
 * @see
 */

public interface IRecordCollection extends Serializable{
	
	// 2010-03-17 rphall
	// FIXME misleading interface name
	// This "collection" doesn't look like a Java Collection.
	// There are no operations to add or enumerate members.

	/**
	 * Applies visitor to a record.
	 * <p> 
	 * 
	 */
	void accept(IRecordCollectionVisitor ext) throws RecordCollectionException;

}


