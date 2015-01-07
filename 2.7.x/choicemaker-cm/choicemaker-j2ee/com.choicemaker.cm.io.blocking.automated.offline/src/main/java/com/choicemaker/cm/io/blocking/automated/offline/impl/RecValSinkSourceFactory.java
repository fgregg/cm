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
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;

/**
 * @author pcheung
 *
 */
public class RecValSinkSourceFactory implements IRecValSinkSourceFactory{

	private static final EXTERNAL_DATA_FORMAT TYPE = EXTERNAL_DATA_FORMAT.STRING;

	private String fileDir;
	private String baseName;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;
	
	
	/** This constructor takes in key parameters to create rec_id, val_id files as follows:
	 * 
	 * fileDir + baseName + ind + "." + ext
	 * 
	 * @param fileDir
	 * @param chunkBase
	 * @param ext
	 */
	public RecValSinkSourceFactory (String fileDir, String baseName, String ext) {
		this.fileDir = fileDir;
		this.baseName = baseName;
		this.ext = ext;
	}
	
	/** This creates the next sink in sequence. It creates a binary file, and no append.  */
	public IRecValSink getNextSink () throws BlockingException {
		indSink ++;
		return new RecValSink (fileDir + baseName + indSink + "." + ext, TYPE);
	}
	
	/** This creates the next source in sequence. It creates a binary file.  */
	public IRecValSource getNextSource () throws BlockingException {
		indSource ++;
		return new RecValSource (fileDir + baseName + indSource + "." + ext, TYPE);
	}


	/** Creates an IRecValSource for an existing IRecValSink. */
	public IRecValSource getSource (IRecValSink sink) throws BlockingException {
		return new RecValSource (sink.getInfo(), TYPE);
	}


	/** Creates an IRecValSink for an existing IRecValSource. */
	public IRecValSink getSink (IRecValSource source) throws BlockingException {
		return new RecValSink (source.getInfo(), TYPE);
	}


	/** This method returns the number of rec_id, val_id data sink files that have been created. */
	public int getNumSink () {
		return indSink;
	}

	/** This method returns the number of rec_id, val_id data source files that have been created. */
	public int getNumSource () {
		return indSource;
	}

	public void removeSink (IRecValSink sink) throws BlockingException {
		File f = new File (sink.getInfo());
		f.delete();
	}

	public void removeSource (IRecValSource source) throws BlockingException {
		File f = new File (source.getInfo());
		f.delete();
	}

}
