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
 * This object handles creating IComparisonArraySink and IComparisonArraySource.
 * 
 * @author pcheung
 *
 */
public interface IComparisonArraySinkSourceFactory<T extends Comparable<? super T>> {

	/** Gets the next IComparisonGroupSink in the sequence. */
	public IComparisonArraySink<T> getNextSink () throws BlockingException;
	
	/** Gets the next IComparisonGroupSource in the sequence. */
	public IComparisonArraySource<T> getNextSource () throws BlockingException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();
	
	/** Gets the number of sequence sources created. */
	public int getNumSource ();
	
	/** Creates an IComparisonGroupSource for an existing IComparisonGroupSink. */
	public IComparisonArraySource<T> getSource (IComparisonArraySink<T> sink) throws BlockingException;

	/** Creates an IComparisonGroupSink for an existing IComparisonGroupSource. */
	public IComparisonArraySink<T> getSink (IComparisonArraySource<T> source) throws BlockingException;

	/** Removes this sink.
	 * 
	 * @param sink
	 * @throws BlockingException
	 */
	public void removeSink (IComparisonArraySink<T> sink) throws BlockingException;


	/** Removes this source.
	 * 
	 * @param source
	 * @throws BlockingException
	 */
	public void removeSource (IComparisonArraySource<T> source) throws BlockingException;

}
