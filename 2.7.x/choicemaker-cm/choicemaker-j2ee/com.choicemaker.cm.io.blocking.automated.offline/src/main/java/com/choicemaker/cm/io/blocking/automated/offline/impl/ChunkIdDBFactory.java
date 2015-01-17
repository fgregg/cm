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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import javax.sql.DataSource;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSource;

/**
 * @author pcheung
 *
 */
public class ChunkIdDBFactory implements IChunkRecordIdSinkSourceFactory {

	private DataSource ds;
	private int indSink = 0;
	private int indSource = 0;
	private int startingPoint;

	/**
	 * This constructor takes these parameters:
	 * 
	 * @param startingPoint
	 *            - the groupID number on the cmt_chunk_id table
	 */
	public ChunkIdDBFactory(DataSource ds, int startingPoint) {
		this.indSink = startingPoint;
		this.indSource = startingPoint;
		this.startingPoint = startingPoint;
		this.ds = ds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IChunkRecordIdSinkSourceFactory#getNextSink()
	 */
	@Override
	public IChunkRecordIdSink getNextSink() throws BlockingException {
		ChunkIdDBSink sink = new ChunkIdDBSink(ds, indSink);
		indSink++;
		return sink;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IChunkRecordIdSinkSourceFactory#getNextSource()
	 */
	@Override
	public IChunkRecordIdSource getNextSource() throws BlockingException {
		ChunkIdDBSource source = new ChunkIdDBSource(ds, indSource);
		indSource++;
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IChunkRecordIdSinkSourceFactory#getNumSink()
	 */
	@Override
	public int getNumSink() {
		return indSink - startingPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IChunkRecordIdSinkSourceFactory#getNumSource()
	 */
	@Override
	public int getNumSource() {
		return indSource - startingPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IChunkRecordIdSinkSourceFactory
	 * #getSource(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IChunkRecordIdSink)
	 */
	@Override
	public IChunkRecordIdSource getSource(IChunkRecordIdSink sink)
			throws BlockingException {
		int id = Integer.parseInt(sink.getInfo());
		ChunkIdDBSource source = new ChunkIdDBSource(ds, id);
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IChunkRecordIdSinkSourceFactory
	 * #getSink(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IChunkRecordIdSource)
	 */
	@Override
	public IChunkRecordIdSink getSink(IChunkRecordIdSource source)
			throws BlockingException {
		int id = Integer.parseInt(source.getInfo());
		ChunkIdDBSink sink = new ChunkIdDBSink(ds, id);
		return sink;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IChunkRecordIdSinkSourceFactory
	 * #removeSink(com.choicemaker.cm.io.blocking.
	 * automated.offline.core.IChunkRecordIdSink)
	 */
	@Override
	public void removeSink(IChunkRecordIdSink sink) throws BlockingException {
		sink.remove();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IChunkRecordIdSinkSourceFactory
	 * #removeSource(com.choicemaker.cm.io.blocking
	 * .automated.offline.core.IChunkRecordIdSource)
	 */
	@Override
	public void removeSource(IChunkRecordIdSource source)
			throws BlockingException {
		source.delete();
	}

}
