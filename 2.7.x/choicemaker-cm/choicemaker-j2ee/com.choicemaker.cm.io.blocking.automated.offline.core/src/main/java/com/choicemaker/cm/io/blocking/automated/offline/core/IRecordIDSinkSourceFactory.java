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
 * This object handles creating IRecordIDSink and IRecordIDSource.
 * 
 * @author pcheung
 *
 */
public interface IRecordIDSinkSourceFactory<T extends Comparable<? super T>> {

	/** Gets the next IRecordIDSink in the sequence. */
	public IRecordIDSink<T> getNextSink () throws BlockingException;
	
	/** Gets the next IRecordIDSource in the sequence. */
	public IRecordIDSource<T> getNextSource () throws BlockingException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();
	
	/** Gets the number of sequence sources created. */
	public int getNumSource ();
	
	/** Creates an IRecordIDSource for an existing IRecordIDSink. */
	public IRecordIDSource<T> getSource (IRecordIDSink<T> sink) throws BlockingException;

	/** Creates an IRecordIDSink for an existing IRecordIDSource. */
	public IRecordIDSink<T> getSink (IRecordIDSource<T> source) throws BlockingException;

	/** Removes this sink.
	 * 
	 * @param sink
	 * @throws BlockingException
	 */
	public void removeSink (IRecordIDSink<T> sink) throws BlockingException;


	/** Removes this source.
	 * 
	 * @param source
	 * @throws BlockingException
	 */
	public void removeSource (IRecordIDSource<T> source) throws BlockingException;

}
