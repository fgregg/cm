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
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSource;

/**
 * @author pcheung
 *
 */
public class MatchRecordSinkSourceFactory implements IMatchRecordSinkSourceFactory {

	private String fileDir;
	private String baseName;
	private String ext;
	private int ind = 0; 
	private int indSource = 0; 
	

	public MatchRecordSinkSourceFactory (String fileDir, String baseName, String ext) {
		this.fileDir = fileDir;
		this.baseName = baseName;
		this.ext = ext;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getNextSink()
	 */
	public IMatchRecordSink getNextSink() throws BlockingException {
		ind ++;
		return new MatchRecordSink (fileDir + baseName + ind + "." + ext, Constants.STRING);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getNextSource()
	 */
	public IMatchRecordSource getNextSource() throws BlockingException {
		indSource ++;
		return new MatchRecordSource (fileDir + baseName + indSource + "." + ext, Constants.STRING);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getNumSink()
	 */
	public int getNumSink() {
		return ind;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getNumSource()
	 */
	public int getNumSource() {
		return indSource;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getSource(com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSink)
	 */
	public IMatchRecordSource getSource(IMatchRecordSink sink) throws BlockingException {
		return new MatchRecordSource (sink.getInfo(), Constants.STRING);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSource)
	 */
	public IMatchRecordSink getSink(IMatchRecordSource source) throws BlockingException {
		return new MatchRecordSink (source.getInfo(), Constants.STRING);
	}

	public IMatchRecordSink getSink (String info) throws BlockingException {
		return new MatchRecordSink (fileDir + baseName + "_" + info + "." + ext, Constants.STRING);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSink)
	 */
	public void removeSink(IMatchRecordSink sink) throws BlockingException {
		File f = new File (sink.getInfo());
		f.delete();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSource)
	 */
	public void removeSource(IMatchRecordSource source) throws BlockingException {
		File f = new File (source.getInfo());
		f.delete();
	}
	

	public void move (IMatchRecordSink sink1, IMatchRecordSink sink2) {
		File f = new File (sink1.getInfo());
		File fout = new File (sink2.getInfo());
		fout.delete();
		f.renameTo(fout);
		f.delete();
	}
	
	
	public void move (IMatchRecordSource source1, IMatchRecordSource source2) {
		File f = new File (source1.getInfo());
		File fout = new File (source2.getInfo());
		fout.delete();
		f.renameTo(fout);
		f.delete();
	}

}
