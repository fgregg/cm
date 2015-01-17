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
	 * This method performs initialization.
	 */
	public void open() throws BlockingException;

	/**
	 * This method tells the objects that source1 is done and it sets the split
	 * index at where source 2 begins.
	 */
	public void split() throws BlockingException;

	/**
	 * Closes this instance and effectively converts it to an immutable
	 * translator; use the {@link #toImmutableTranslator()} method to get the
	 * immutable translator that this translator becomes. If a
	 * {@link #translate(Comparable) translation} is attempted after being
	 * closed, this instance will throw an {@link IllegalStateExceptions
	 * illegal-state exception}.
	 */
	public void close() throws BlockingException;

	/**
	 * This method closes this instance (if it is not already closed) and
	 * returns an immutable translator that based on this instance.
	 */
	public ImmutableRecordIdTranslator<T> toImmutableTranslator()
			throws BlockingException;

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

}
