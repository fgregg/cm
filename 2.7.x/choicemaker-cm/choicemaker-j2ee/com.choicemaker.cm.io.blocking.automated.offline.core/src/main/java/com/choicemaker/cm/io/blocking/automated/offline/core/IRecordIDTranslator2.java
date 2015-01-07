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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.io.IOException;
import java.util.List;

import com.choicemaker.cm.core.BlockingException;

/**
 * This is the generic record id to internal id translator. It takes in 1 or 2
 * record sources.
 * 
 * This version allows for record id of the type Long, Integer, and String.
 * 
 * @author pcheung
 *
 */
public interface IRecordIDTranslator2<T extends Comparable<T>> {

	/**
	 * This method returns the range of record ids in the first source.
	 * 
	 * @return Comparable[0] is min and Comparable[1] is max
	 */
	public T[] getRange1();

	/**
	 * This method returns the range of record ids in the second source.
	 * 
	 * @return Comparable[0] is min and Comparable[1] is max
	 */
	public T[] getRange2();

	/**
	 * This returns the internal id at which the second source begins. This is 0
	 * if there is only one source.
	 * 
	 * RecordSource1 would have internal id from 0 to splitIndex - 1. And
	 * RecordSource2 starts from splitIndex.
	 * 
	 * @return int - the index that separates the staging and master records.
	 */
	public int getSplitIndex();

	/**
	 * This method performs initialization.
	 * 
	 * @throws BlockingException
	 */
	public void open() throws BlockingException;

	/**
	 * This method tells the objects that source1 is done and it sets the split
	 * index at where source 2 begins.
	 * 
	 */
	public void split() throws BlockingException;

	/**
	 * This method closes the file or db depending on the implementation.
	 * 
	 * @throws BlockingException
	 */
	public void close() throws BlockingException;

	/**
	 * This method cleans up underlying file or db resources.
	 * 
	 * @throws IOException
	 */
	public void cleanUp() throws BlockingException;

	/**
	 * This method attemps to recover the data from a previous run by reading in
	 * existing sources containing the record ids.
	 * 
	 * @throws BlockingException
	 */
	public void recover() throws BlockingException;

	/**
	 * This method translates input record id to internal system id.
	 * 
	 * @param o
	 *            - the record id object (Long, Integer, String)
	 * @return int - returns internal id for this record id.
	 * @throws BlockingException
	 */
	public int translate(T o) throws BlockingException;

	/**
	 * This method prepares for reverse translation.
	 * 
	 * @throws BlockingException
	 */
	public void initReverseTranslation() throws BlockingException;

	/**
	 * This method returns the original record ID associated with this internal
	 * ID. Make sure the method initReverseTranslation is called before this
	 * method.
	 * 
	 * @param internalID
	 * @return Comparable<?> - the original record ID associated with this
	 *         internal ID.
	 */
	public Comparable<?> reverseLookup(int internalID);

	/**
	 * This returns an List of record IDs from the first source. Usually, the
	 * staging source.
	 * 
	 * @return List
	 */
	public List<T> getList1();

	/**
	 * This returns an List of record IDs from the second source. Usually, the
	 * master source.
	 * 
	 * @return List
	 */
	public List<T> getList2();

}
