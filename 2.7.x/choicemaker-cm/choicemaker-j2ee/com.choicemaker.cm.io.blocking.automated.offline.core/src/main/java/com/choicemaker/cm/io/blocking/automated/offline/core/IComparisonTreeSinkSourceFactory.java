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
 * This object handles creating IComparisonTreeSink and IComparisonTreeSource.
 * 
 * @author pcheung
 *
 */
public interface IComparisonTreeSinkSourceFactory<T extends Comparable<? super T>> {

	/** Gets the next IComparisonTreeSink in the sequence. */
	public IComparisonTreeSink<T> getNextSink () throws BlockingException;
	
	/** Gets the next IComparisonTreeSource in the sequence. */
	public IComparisonTreeSource<T> getNextSource () throws BlockingException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();
	
	/** Gets the number of sequence sources created. */
	public int getNumSource ();
	
	/** Creates an IComparisonTreeSource for an existing IComparisonTreeSink. */
	public IComparisonTreeSource<T> getSource (IComparisonTreeSink<T> sink) throws BlockingException;

	/** Creates an IComparisonTreeSink for an existing IComparisonTreeSource. */
	public IComparisonTreeSink<T> getSink (IComparisonTreeSource<T> source) throws BlockingException;

	/** Removes this sink.
	 * 
	 * @param sink
	 * @throws BlockingException
	 */
	public void removeSink (IComparisonTreeSink<T> sink) throws BlockingException;


	/** Removes this source.
	 * 
	 * @param source
	 * @throws BlockingException
	 */
	public void removeSource (IComparisonTreeSource<T> source) throws BlockingException;

}
