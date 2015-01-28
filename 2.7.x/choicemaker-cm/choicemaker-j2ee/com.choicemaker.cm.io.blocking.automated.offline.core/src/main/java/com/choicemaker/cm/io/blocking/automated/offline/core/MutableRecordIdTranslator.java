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
public interface MutableRecordIdTranslator<T extends Comparable<T>> {

	/**
	 * A magic value returned by {@link #getSplitIndex()} indicating that the
	 * translator is not split. Numerically equal to
	 * {@link ImmutableRecordIdTranslator#NOT_SPLIT}.
	 */
	public static final int NOT_SPLIT = ImmutableRecordIdTranslator.NOT_SPLIT;

	/**
	 * This method performs initialization.
	 */
	public void open() throws BlockingException;

	/**
	 * This method tells the objects that source1 is done and it sets the split
	 * index at where source 2 begins.
	 */
	public void split() throws BlockingException;

	/**
	 * This returns the internal id at which the second source begins, or
	 * {@link #NOT_SPLIT} if this translator has not been split. If this
	 * translator has been split, then:
	 * <ul>
	 * <li>RecordSource1 will have been assigned internal ids from
	 * <code>0</code> to <code>splitIndex - 1</code>.</li>
	 * <li>RecordSource2 will be assigned internal ids starting at the
	 * splitIndex.</li>
	 * </ul>
	 */
	public int getSplitIndex();

	/** Indicates whether the translator has been split */
	boolean isSplit();

	/**
	 * Returns the index at which indices for the second (a.k.a. master) source
	 * begin, if the translator has been split. Otherwise, returns
	 */

	/**
	 * Closes this instance. If a {@link #translate(Comparable) translation} is
	 * attempted after being closed, this instance will throw an
	 * {@link IllegalStateExceptions illegal-state exception}.
	 */
	public void close() throws BlockingException;

	/**
	 * This method cleans any resources that are cached on disk.
	 */
	public void cleanUp() throws BlockingException;

	/**
	 * This method translates input record id to internal system id.
	 * 
	 * @param o
	 *            - the record id object (Long, Integer, String)
	 * @return int - returns internal id for this record id.
	 */
	public int translate(T o) throws BlockingException;

	/**
	 * Returns the type of record identifiers handled by this translator.
	 * 
	 * @throws IllegalStateException
	 *             if no {@link #translate(Comparable) translations} have been
	 *             performed by this instance.
	 */
	RECORD_ID_TYPE getRecordIdType();

}
