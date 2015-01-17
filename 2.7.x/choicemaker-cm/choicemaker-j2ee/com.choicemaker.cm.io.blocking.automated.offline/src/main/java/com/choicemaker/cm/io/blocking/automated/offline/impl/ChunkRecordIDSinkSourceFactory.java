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

import java.io.File;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSource;

/**
 * This object handles producing ChunkRowSinks. Given a base file name, it
 * creates subsequent ChunkRow files in order.
 * 
 * @author pcheung
 *
 */
public class ChunkRecordIDSinkSourceFactory implements
		IChunkRecordIdSinkSourceFactory {

	private String fileDir;
	private String chunkBase;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;

	/**
	 * This constructor takes in key parameters to create chunkRowSource files
	 * as follows:
	 * 
	 * fileDir + chunkBase + ind + "." + ext
	 * 
	 * @param fileDir
	 * @param chunkBase
	 * @param ext
	 */
	public ChunkRecordIDSinkSourceFactory(String fileDir, String chunkBase,
			String ext) {
		this.fileDir = fileDir;
		this.chunkBase = chunkBase;
		this.ext = ext;
	}

	/**
	 * This creates the next sink in seqence. It creates a binary file, and no
	 * append.
	 */
	@Override
	public IChunkRecordIdSink getNextSink() throws BlockingException {
		indSink++;
		return new ChunkRecordIDSink(fileDir + chunkBase + indSink + "." + ext,
				EXTERNAL_DATA_FORMAT.STRING);
	}

	/** This creates the next source in seqence. It creates a binary file. */
	@Override
	public IChunkRecordIdSource getNextSource() throws BlockingException {
		indSource++;
		return new ChunkRecordIDSource(fileDir + chunkBase + indSource + "."
				+ ext, EXTERNAL_DATA_FORMAT.STRING);
	}

	/**
	 * This method returns the number of chunkRow data sink files that have been
	 * created.
	 */
	@Override
	public int getNumSink() {
		return indSink;
	}

	/**
	 * This method returns the number of chunkRow data source files that have
	 * been created.
	 */
	@Override
	public int getNumSource() {
		return indSource;
	}

	@Override
	public IChunkRecordIdSource getSource(IChunkRecordIdSink sink)
			throws BlockingException {
		return new ChunkRecordIDSource(sink.getInfo(),
				EXTERNAL_DATA_FORMAT.STRING);
	}

	@Override
	public IChunkRecordIdSink getSink(IChunkRecordIdSource source)
			throws BlockingException {
		return new ChunkRecordIDSink(source.getInfo(),
				EXTERNAL_DATA_FORMAT.STRING);
	}

	@Override
	public void removeSink(IChunkRecordIdSink sink) throws BlockingException {
		File f = new File(sink.getInfo());
		f.delete();
	}

	@Override
	public void removeSource(IChunkRecordIdSource source)
			throws BlockingException {
		File f = new File(source.getInfo());
		f.delete();
	}

}
