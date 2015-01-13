/*
 * Copyright (c) 2009, 2010 Rick Hall and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Rick Hall - initial API and implementation
 */
package com.choicemaker.cm.io.blocking.automated.offline.core;

/**
 * Extends the IRecordIDTranslator2 interface by adding a lookup method
 * that returns the internal id of a specified record id.
 * @author rphall
 * @version $Revision$ $Date$
 */
@SuppressWarnings("rawtypes")
public interface IRecordIdTranslator3 extends IRecordIdTranslator2 {
	
	/** Returned from {@link #lookup(Comparable) lookup} if no internal index exists */
	public int INVALID_INDEX = -1;

	/** Minimum valid index that will be returned by {@link #lookup(Comparable) lookup} */
	public int MINIMUM_VALID_INDEX = INVALID_INDEX + 1;

	/**
	 * Returns the internal id (a.k.a. index) of a specified staging record id,
	 * or {@link #INVALID_INDEX} if the record id has not been indexed.
	 */
	int lookupStagingIndex(Comparable recordID);

	/**
	 * Returns the internal id (a.k.a. index) of a specified master record id,
	 * or {@link #INVALID_INDEX} if the record id has not been indexed.
	 */
	int lookupMasterIndex(Comparable recordID);

}
