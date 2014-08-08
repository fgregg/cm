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
 * This object handles getting IMatchRecord2Sink and IMatchRecord2Source.
 * 
 * @author pcheung
 *
 */
public interface IMatchRecord2SinkSourceFactory<T extends Comparable<? super T>> {

	/** Gets the next IMatchRecord2Sink in the sequence. */
	public IMatchRecord2Sink<T> getNextSink() throws BlockingException;

	/** Gets the next IMatchRecord2Source in the sequence. */
	public IMatchRecord2Source<T> getNextSource() throws BlockingException;

	/** Gets the number of sequence sinks created. */
	public int getNumSink();

	/** Gets the number of sequence sources created. */
	public int getNumSource();

	/** Creates an IMatchRecord2Source for an existing IMatchRecord2Sink. */
	public IMatchRecord2Source<T> getSource(IMatchRecord2Sink<T> sink)
			throws BlockingException;

	/** Creates an IMatchRecord2Sink for an existing IMatchRecord2Source. */
	public IMatchRecord2Sink<T> getSink(IMatchRecord2Source<T> source)
			throws BlockingException;

	/** Removes a given IMatchRecord2Sink. */
	public void removeSink(IMatchRecord2Sink<T> sink) throws BlockingException;

	/** Removes a given IMatchRecordSource. */
	public void removeSource(IMatchRecord2Source<T> source)
			throws BlockingException;

	/** Moves source1 to source2. */
	public void move(IMatchRecord2Source<T> source1,
			IMatchRecord2Source<T> source2) throws BlockingException;

	/** Moves sink1 to sink2. */
	public void move(IMatchRecord2Sink<T> sink1, IMatchRecord2Sink<T> sink2)
			throws BlockingException;

	/**
	 * This gets a specific sink.
	 * 
	 * @param info
	 * @return IMatchRecordSink - a sink with the corresponding info.
	 * @throws BlockingException
	 */
	public IMatchRecord2Sink<T> getSink(String info) throws BlockingException;

	/**
	 * This gets the sink with the specific number.
	 * 
	 * @param i
	 * @return IMatchRecord2Sink
	 * @throws BlockingException
	 */
	public IMatchRecord2Sink<T> getSink(int i) throws BlockingException;

}
