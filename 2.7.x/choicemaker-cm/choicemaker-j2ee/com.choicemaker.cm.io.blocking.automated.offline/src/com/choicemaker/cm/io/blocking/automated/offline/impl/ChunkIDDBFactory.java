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
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource;

/**
 * @author pcheung
 *
 */
public class ChunkIDDBFactory implements IChunkRecordIDSinkSourceFactory {

	private DataSource ds;
	private int indSink = 0;
	private int indSource = 0;
	private int startingPoint;

	
	/** This constructor takes these parameters:
	 * 
	 * @param startingPoint - the groupID number on the cmt_chunk_id table
	 */
	public ChunkIDDBFactory (DataSource ds, int startingPoint) {
		this.indSink = startingPoint;
		this.indSource = startingPoint;
		this.startingPoint = startingPoint;
		this.ds = ds;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#getNextSink()
	 */
	public IChunkRecordIDSink getNextSink() throws BlockingException {
		ChunkIDDBSink sink = new ChunkIDDBSink (ds, indSink);
		indSink ++;
		return sink;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#getNextSource()
	 */
	public IChunkRecordIDSource getNextSource() throws BlockingException {
		ChunkIDDBSource source = new ChunkIDDBSource (ds, indSource);
		indSource ++;
		return source;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#getNumSink()
	 */
	public int getNumSink() {
		return indSink - startingPoint;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#getNumSource()
	 */
	public int getNumSource() {
		return indSource - startingPoint;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#getSource(com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink)
	 */
	public IChunkRecordIDSource getSource(IChunkRecordIDSink sink) throws BlockingException {
		int id = Integer.parseInt(sink.getInfo());
		ChunkIDDBSource source = new ChunkIDDBSource (ds, id);
		return source;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource)
	 */
	public IChunkRecordIDSink getSink(IChunkRecordIDSource source) throws BlockingException {
		int id = Integer.parseInt(source.getInfo());
		ChunkIDDBSink sink = new ChunkIDDBSink (ds, id);
		return sink;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink)
	 */
	public void removeSink(IChunkRecordIDSink sink) throws BlockingException {
		sink.remove();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource)
	 */
	public void removeSource(IChunkRecordIDSource source)
		throws BlockingException {
		source.remove();
	}

}
