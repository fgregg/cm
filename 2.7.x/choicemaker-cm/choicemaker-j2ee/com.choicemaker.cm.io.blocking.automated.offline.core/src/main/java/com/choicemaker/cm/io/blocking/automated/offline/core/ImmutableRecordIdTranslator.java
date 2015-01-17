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

import java.util.List;

import com.choicemaker.cm.core.BlockingException;

/**
 * Extends the IRecordIDTranslator2 interface by adding a lookup method that
 * returns the internal id of a specified record id.
 * 
 * @author rphall
 * @version $Revision$ $Date$
 */
public interface ImmutableRecordIdTranslator<T extends Comparable<T>> {

	/**
	 * Temporary hack: this method will be removed.
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void cleanUp() throws BlockingException;

	/**
	 * Returned from {@link #lookup(Comparable) lookup} if no internal index
	 * exists
	 */
	public int INVALID_INDEX = -1;

	/**
	 * Minimum valid index that will be returned by {@link #lookup(Comparable)
	 * lookup}
	 */
	public int MINIMUM_VALID_INDEX = INVALID_INDEX + 1;

	/** Returns the type of record identifier handled by this translator */
	RECORD_ID_TYPE getRecordIdType();

	/**
	 * This returns the internal id at which the second source begins. This is 0
	 * if there is only one source.
	 * 
	 * RecordSource1 would have internal id from 0 to splitIndex - 1. And
	 * RecordSource2 starts from splitIndex.
	 */
	public int getSplitIndex();

	/**
	 * Returns the internal id (a.k.a. index) of a specified staging record id,
	 * or {@link #INVALID_INDEX} if the record id has not been indexed.
	 */
	int lookupStagingIndex(T recordID);

	/**
	 * Returns the internal id (a.k.a. index) of a specified master record id,
	 * or {@link #INVALID_INDEX} if the record id has not been indexed.
	 */
	int lookupMasterIndex(T recordID);

	/**
	 * This returns a List of record IDs from the first source. Usually, the
	 * staging source.
	 */
	public List<T> getList1();

	/**
	 * This returns a List of record IDs from the second source. Usually, the
	 * master source.
	 */
	public List<T> getList2();

	/**
	 * This method returns the original record ID associated with this internal
	 * ID. Make sure the method initReverseTranslation is called before this
	 * method.
	 * 
	 * @return Comparable<?> - the original record ID associated with this
	 *         internal ID.
	 */
	public Comparable<?> reverseLookup(int internalID);

}
