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

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSource;

/**
 * @author pcheung
 *
 */
public class PairIDSinkSourceFactory implements IPairIDSinkSourceFactory {

	private String fileDir;
	private String baseName;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;
	
	private static final int TYPE = Constants.STRING;
	
	
	/** This constructor takes in key parameters to create rec_id, val_id files as follows:
	 * 
	 * fileDir + baseName + ind + "." + ext
	 * 
	 * @param fileDir
	 * @param chunkBase
	 * @param ext
	 */
	public PairIDSinkSourceFactory (String fileDir, String baseName, String ext) {
		this.fileDir = fileDir;
		this.baseName = baseName;
		this.ext = ext;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#getNextSink()
	 */
	public IPairIDSink getNextSink() throws BlockingException {
		indSink ++;
		return new PairIDSink (fileDir + baseName + indSink + "." + ext, TYPE);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#getNextSource()
	 */
	public IPairIDSource getNextSource() throws BlockingException {
		indSource ++;
		return new PairIDSource (fileDir + baseName + indSource + "." + ext, TYPE);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#getNumSink()
	 */
	public int getNumSink() {
		return indSink;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#getNumSource()
	 */
	public int getNumSource() {
		return indSource;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#getSource(com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink)
	 */
	public IPairIDSource getSource(IPairIDSink sink) throws BlockingException {
		return new PairIDSource (sink.getInfo(), TYPE);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSource)
	 */
	public IPairIDSink getSink(IPairIDSource source) throws BlockingException {
		return new PairIDSink (source.getInfo(), TYPE);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink)
	 */
	public void removeSink(IPairIDSink sink) throws BlockingException {
		File f = new File (sink.getInfo());
		f.delete();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSource)
	 */
	public void removeSource(IPairIDSource source) throws BlockingException {
		File f = new File (source.getInfo());
		f.delete();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory#move(com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink, com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink)
	 */
	public void move(IPairIDSink sink1, IPairIDSink sink2) throws BlockingException {
		File f = new File (sink1.getInfo());
		File fout = new File (sink2.getInfo());
		fout.delete();
		f.renameTo(fout);
		f.delete();
	}

}
