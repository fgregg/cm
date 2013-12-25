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
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource;

/**
 * This object handles producing ChunkRowSinks.  Given a base file name, it creates
 * subsequent ChunkRow files in order.
 * 
 * @author pcheung
 *
 */
public class ChunkRecordIDSinkSourceFactory implements IChunkRecordIDSinkSourceFactory {

	private static final int TYPE = Constants.STRING;

	private String fileDir;
	private String chunkBase;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;
	
	
	/** This constructor takes in key parameters to create chunkRowSource files as follows:
	 * 
	 * fileDir + chunkBase + ind + "." + ext
	 * 
	 * @param fileDir
	 * @param chunkBase
	 * @param ext
	 */
	public ChunkRecordIDSinkSourceFactory (String fileDir, String chunkBase, String ext) {
		this.fileDir = fileDir;
		this.chunkBase = chunkBase;
		this.ext = ext;
	}
	
	
	/** This creates the next sink in seqence. It creates a binary file, and no append.  */
	public IChunkRecordIDSink getNextSink () throws BlockingException {
		indSink ++;
		return new ChunkRecordIDSink (fileDir + chunkBase + indSink + "." + ext, TYPE);
	}
	
	/** This creates the next source in seqence. It creates a binary file.  */
	public IChunkRecordIDSource getNextSource () throws BlockingException {
		indSource ++;
		return new ChunkRecordIDSource (fileDir + chunkBase + indSource + "." + ext, TYPE);
	}
	
	
	/** This method returns the number of chunkRow data sink files that have been created. */
	public int getNumSink () {
		return indSink;
	}

	/** This method returns the number of chunkRow data source files that have been created. */
	public int getNumSource () {
		return indSource;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#getSource(com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink)
	 */
	public IChunkRecordIDSource getSource(IChunkRecordIDSink sink) throws BlockingException {
		return new ChunkRecordIDSource (sink.getInfo(), TYPE);
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource)
	 */
	public IChunkRecordIDSink getSink(IChunkRecordIDSource source) throws BlockingException {
		return new ChunkRecordIDSink (source.getInfo(), TYPE);
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSink)
	 */
	public void removeSink(IChunkRecordIDSink sink) throws BlockingException {
		File f = new File (sink.getInfo());
		f.delete();
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource)
	 */
	public void removeSource(IChunkRecordIDSource source) throws BlockingException {
		File f = new File (source.getInfo());
		f.delete();
	}

}
