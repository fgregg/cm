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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSource;

/**
 * This is a file implementation of IRecordIDSinkSourceFactory.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
	"rawtypes"})
public class RecordIDSinkSourceFactory implements IRecordIDSinkSourceFactory {
	
	private static final EXTERNAL_DATA_FORMAT TYPE = EXTERNAL_DATA_FORMAT.STRING;

	private String fileDir;
	private String nameBase;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;
	
	
	/** This constructor takes in key parameters to create RecordIDSink or RecordIDSource files as follows:
	 * 
	 * fileDir + nameBase + ind + "." + ext
	 * 
	 * @param fileDir
	 * @param nameBase
	 * @param ext
	 */
	public RecordIDSinkSourceFactory (String fileDir, String nameBase, String ext) {
		this.fileDir = fileDir;
		this.nameBase = nameBase;
		this.ext = ext;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory#getNextSink()
	 */
	public IRecordIDSink getNextSink() throws BlockingException {
		indSink ++;
		return new RecordIDSink (fileDir + nameBase + indSink + "." + ext, TYPE);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory#getNextSource()
	 */
	public IRecordIDSource getNextSource() throws BlockingException {
		indSource ++;
		return new RecordIDSource (fileDir + nameBase + indSource + "." + ext, TYPE);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory#getNumSink()
	 */
	public int getNumSink() {
		return indSink;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory#getNumSource()
	 */
	public int getNumSource() {
		return indSource;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory#getSource(com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSink)
	 */
	public IRecordIDSource getSource(IRecordIDSink sink)
		throws BlockingException {
		return new RecordIDSource (sink.getInfo(), TYPE);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSource)
	 */
	public IRecordIDSink getSink(IRecordIDSource source)
		throws BlockingException {
		return new RecordIDSink (source.getInfo(), TYPE);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSink)
	 */
	public void removeSink(IRecordIDSink sink) throws BlockingException {
		sink.remove();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSource)
	 */
	public void removeSource(IRecordIDSource source) throws BlockingException {
		source.delete();
	}

}
