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
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;

/**
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes"})
public class MatchRecord2SinkSourceFactory implements IMatchRecord2SinkSourceFactory {

	private String fileDir;
	private String baseName;
	private String ext;
	private int ind = 0; 
	private int indSource = 0; 
	

	public MatchRecord2SinkSourceFactory (String fileDir, String baseName, String ext) {
		this.fileDir = fileDir;
		this.baseName = baseName;
		this.ext = ext;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getNextSink()
	 */
	public IMatchRecord2Sink getNextSink() throws BlockingException {
		ind ++;
		return new MatchRecord2Sink (fileDir + baseName + ind + "." + ext, Constants.STRING);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getNextSource()
	 */
	public IMatchRecord2Source getNextSource() throws BlockingException {
		indSource ++;
		return new MatchRecord2Source (fileDir + baseName + indSource + "." + ext, Constants.STRING);
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
	public IMatchRecord2Source getSource(IMatchRecord2Sink sink) throws BlockingException {
		return new MatchRecord2Source (sink.getInfo(), Constants.STRING);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSource)
	 */
	public IMatchRecord2Sink getSink(IMatchRecord2Source source) throws BlockingException {
		return new MatchRecord2Sink (source.getInfo(), Constants.STRING);
	}

	public IMatchRecord2Sink getSink (String info) throws BlockingException {
		return new MatchRecord2Sink (fileDir + baseName + "_" + info + "." + ext, Constants.STRING);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSink)
	 */
	public void removeSink(IMatchRecord2Sink sink) throws BlockingException {
		File f = new File (sink.getInfo());
		f.delete();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSource)
	 */
	public void removeSource(IMatchRecord2Source source) throws BlockingException {
		File f = new File (source.getInfo());
		f.delete();
	}
	

	public void move (IMatchRecord2Sink sink1, IMatchRecord2Sink sink2) throws BlockingException {
		File f = new File (sink1.getInfo());
		File fout = new File (sink2.getInfo());
		fout.delete();
		f.renameTo(fout);
		f.delete();
	}
	
	
	public void move (IMatchRecord2Source source1, IMatchRecord2Source source2) throws BlockingException {
		File f = new File (source1.getInfo());
		File fout = new File (source2.getInfo());
		fout.delete();
		f.renameTo(fout);
		f.delete();
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory#getSink(int)
	 */
	public IMatchRecord2Sink getSink(int i) throws BlockingException {
		return new MatchRecord2Sink (fileDir + baseName + i + "." + ext, Constants.STRING);
	}


}
