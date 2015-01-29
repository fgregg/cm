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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSource;

/**
 * This is a file implementation of IRecordIdSinkSourceFactory.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({ "rawtypes" })
class RecordIdSinkSourceFactory implements IRecordIdSinkSourceFactory {

	static final EXTERNAL_DATA_FORMAT TYPE = EXTERNAL_DATA_FORMAT.STRING;

	private String fileDir;
	private String nameBase;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;

	/**
	 * This constructor takes in key parameters to create RecordIdSink or
	 * RecordIdSource files as follows:
	 * 
	 * fileDir + nameBase + ind + "." + ext
	 * 
	 * @param fileDir
	 * @param nameBase
	 * @param ext
	 */
	RecordIdSinkSourceFactory(String fileDir, String nameBase, String ext) {
		this.fileDir = fileDir;
		this.nameBase = nameBase;
		this.ext = ext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecordIdSinkSourceFactory#getNextSink()
	 */
	@Override
	public IRecordIdSink getNextSink() throws BlockingException {
		indSink++;
		return new RecordIdSink(fileDir + nameBase + indSink + "." + ext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecordIdSinkSourceFactory#getNextSource()
	 */
	@Override
	public IRecordIdSource getNextSource() throws BlockingException {
		indSource++;
		return new RecordIdSource(fileDir + nameBase + indSource + "." + ext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecordIdSinkSourceFactory#getNumSink()
	 */
	@Override
	public int getNumSink() {
		return indSink;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecordIdSinkSourceFactory#getNumSource()
	 */
	@Override
	public int getNumSource() {
		return indSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecordIdSinkSourceFactory
	 * #getSource(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IRecordIdSink)
	 */
	@Override
	public IRecordIdSource getSource(IRecordIdSink sink)
			throws BlockingException {
		return new RecordIdSource(sink.getInfo());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecordIdSinkSourceFactory
	 * #getSink(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IRecordIdSource)
	 */
	@Override
	public IRecordIdSink getSink(IRecordIdSource source)
			throws BlockingException {
		return new RecordIdSink(source.getInfo());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecordIdSinkSourceFactory
	 * #removeSink(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IRecordIdSink)
	 */
	@Override
	public void removeSink(IRecordIdSink sink) throws BlockingException {
		sink.remove();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecordIdSinkSourceFactory
	 * #removeSource(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IRecordIdSource)
	 */
	@Override
	public void removeSource(IRecordIdSource source) throws BlockingException {
		source.delete();
	}

}
