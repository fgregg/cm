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
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource;

/**
 * This is a file implementation of IComparisonTreeSinkSourceFactory.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes"})
public class ComparisonTreeSinkSourceFactory implements IComparisonTreeSinkSourceFactory {


	private String fileDir;
	private String nameBase;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;
	private int dataType;


	/** This constructor takes in key parameters to create RecordIDSink or RecordIDSource files as follows:
	 * 
	 * fileDir + nameBase + ind + "." + ext
	 * 
	 * @param fileDir
	 * @param nameBase
	 * @param ext
	 */
	public ComparisonTreeSinkSourceFactory (String fileDir, String nameBase, String ext, int dataType) {
		this.fileDir = fileDir;
		this.nameBase = nameBase;
		this.ext = ext;
		this.dataType = dataType;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getNextSink()
	 */
	public IComparisonTreeSink getNextSink() throws BlockingException {
		indSink ++;
		return new ComparisonTreeSink (fileDir + nameBase + indSink + "." + ext);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getNextSource()
	 */
	public IComparisonTreeSource getNextSource() throws BlockingException {
		indSource ++;
		return new ComparisonTreeSource (fileDir + nameBase + indSource + "." + ext, dataType);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getNumSink()
	 */
	public int getNumSink() {
		return indSink;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getNumSource()
	 */
	public int getNumSource() {
		return indSource;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getSource(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink)
	 */
	public IComparisonTreeSource getSource(IComparisonTreeSink sink) throws BlockingException {
		return new ComparisonTreeSource (sink.getInfo(), dataType);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource)
	 */
	public IComparisonTreeSink getSink(IComparisonTreeSource source) throws BlockingException {
		return new ComparisonTreeSink (source.getInfo());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink)
	 */
	public void removeSink(IComparisonTreeSink sink) throws BlockingException {
		sink.remove();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource)
	 */
	public void removeSource(IComparisonTreeSource source) throws BlockingException {
		source.remove();
	}

}
