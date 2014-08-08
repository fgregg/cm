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
 * This object handles getting IMatchRecordSink and IMatchRecordSource.
 * 
 * @author pcheung
 *
 */
public interface IMatchRecordSinkSourceFactory<T extends Comparable<? super T>> {

	/** Gets the next IMatchRecordSink in the sequence. */
	public IMatchRecordSink<T> getNextSink () throws BlockingException;
	
	/** Gets the next IMatchRecordSource in the sequence. */
	public IMatchRecordSource getNextSource () throws BlockingException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();
	
	/** Gets the number of sequence sources created. */
	public int getNumSource ();
	
	/** Creates an IMatchRecordSource for an existing IMatchRecordSink. */
	public IMatchRecordSource getSource (IMatchRecordSink<T> sink) throws BlockingException;
	
	/** Creates an IMatchRecordSink for an existing IMatchRecordSource. */
	public IMatchRecordSink<T> getSink (IMatchRecordSource source) throws BlockingException;

	/** Removes a given IMatchRecordSink. */
	public void removeSink (IMatchRecordSink<T> sink) throws BlockingException;

	/** Removes a given IMatchRecordSource. */
	public void removeSource (IMatchRecordSource source) throws BlockingException;
	
	/** Moves source1 to source2. */
	public void move (IMatchRecordSource source1, IMatchRecordSource source2) throws BlockingException;

	/** Moves sink1 to sink2. */
	public void move (IMatchRecordSink<T> sink1, IMatchRecordSink<T> sink2) throws BlockingException;
	
	/** This gets a specific sink. 
	 * 
	 * @param info
	 * @return IMatchRecordSink - a sink with the corresponding info.
	 * @throws BlockingException
	 */
	public IMatchRecordSink<T> getSink (String info) throws BlockingException;
	

}
