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
 * This object handles creating IChunkRecordIDSink and IChunkRecordIDSource.
 * 
 * @author pcheung
 *
 */
public interface IChunkRecordIDSinkSourceFactory {

	/** Gets the next IChunkRecordIDSink in the sequence. */
	public IChunkRecordIDSink getNextSink () throws BlockingException;
	
	/** Gets the next IChunkRecordIDSource in the sequence. */
	public IChunkRecordIDSource getNextSource () throws BlockingException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();
	
	/** Gets the number of sequence sources created. */
	public int getNumSource ();
	
	/** Creates an IChunkRecordIDSource for an existing IChunkRecordIDSink. */
	public IChunkRecordIDSource getSource (IChunkRecordIDSink sink) throws BlockingException;

	/** Creates an IChunkRecordIDSink for an existing IChunkRecordIDSource. */
	public IChunkRecordIDSink getSink (IChunkRecordIDSource source) throws BlockingException;

	/** Removes this sink.
	 * 
	 * @param sink
	 * @throws BlockingException
	 */
	public void removeSink (IChunkRecordIDSink sink) throws BlockingException;


	/** Removes this source.
	 * 
	 * @param source
	 * @throws BlockingException
	 */
	public void removeSource (IChunkRecordIDSource source) throws BlockingException;

}
