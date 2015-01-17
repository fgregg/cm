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
 * This interface handles creating ISuffixTreeSource and ISuffixTreeSink.
 * 
 * @author pcheung
 *
 */
public interface ISuffixTreeSinkSourceFactory<T extends Comparable<T>> {

	/** Gets the next ISuffixTreeSink in the sequence. */
	public ISuffixTreeSink getNextSink() throws BlockingException;

	/** Gets the next ISuffixTreeSource in the sequence. */
	public ISuffixTreeSource getNextSource() throws BlockingException;

	/** Gets the number of sequence sinks created. */
	public int getNumSink();

	/** Gets the number of sequence sources created. */
	public int getNumSource();

	/** Creates an ISuffixTreeSource for an existing ISuffixTreeSink. */
	public ISuffixTreeSource getSource(ISuffixTreeSink sink)
			throws BlockingException;

	/** Creates an ISuffixTreeSink for an existing ISuffixTreeSource. */
	public ISuffixTreeSink getSink(IComparisonArraySource<T> source)
			throws BlockingException;

	/**
	 * Removes this sink.
	 * 
	 * @param sink
	 * @throws BlockingException
	 */
	public void removeSink(ISuffixTreeSink sink) throws BlockingException;

	/**
	 * Removes this source.
	 * 
	 * @param source
	 * @throws BlockingException
	 */
	public void removeSource(ISuffixTreeSource source) throws BlockingException;

}
