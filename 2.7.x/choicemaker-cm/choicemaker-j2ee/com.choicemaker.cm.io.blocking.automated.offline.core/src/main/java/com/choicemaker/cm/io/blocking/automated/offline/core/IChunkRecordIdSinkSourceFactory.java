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
 * This object handles creating IChunkRecordIdSink and IChunkRecordIdSource.
 * 
 * @author pcheung
 *
 */
public interface IChunkRecordIdSinkSourceFactory {

	/** Gets the next IChunkRecordIdSink in the sequence. */
	public IChunkRecordIdSink getNextSink() throws BlockingException;

	/** Gets the next IChunkRecordIdSource in the sequence. */
	public IChunkRecordIdSource getNextSource() throws BlockingException;

	/** Gets the number of sequence sinks created. */
	public int getNumSink();

	/** Gets the number of sequence sources created. */
	public int getNumSource();

	/** Creates an IChunkRecordIdSource for an existing IChunkRecordIdSink. */
	public IChunkRecordIdSource getSource(IChunkRecordIdSink sink)
			throws BlockingException;

	/** Creates an IChunkRecordIdSink for an existing IChunkRecordIdSource. */
	public IChunkRecordIdSink getSink(IChunkRecordIdSource source)
			throws BlockingException;

	/**
	 * Removes this sink.
	 * 
	 * @param sink
	 * @throws BlockingException
	 */
	public void removeSink(IChunkRecordIdSink sink) throws BlockingException;

	/**
	 * Removes this source.
	 * 
	 * @param source
	 * @throws BlockingException
	 */
	public void removeSource(IChunkRecordIdSource source)
			throws BlockingException;

}
