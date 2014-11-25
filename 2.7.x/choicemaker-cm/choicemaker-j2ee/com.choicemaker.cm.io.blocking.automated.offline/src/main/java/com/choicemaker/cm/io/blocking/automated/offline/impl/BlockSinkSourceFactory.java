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
import java.io.Serializable;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;

/**
 * @author pcheung
 *
 */
public class BlockSinkSourceFactory implements IBlockSinkSourceFactory, Serializable {

	private static final int TYPE = Constants.STRING;

	/* As of 2010-03-10 */
	static final long serialVersionUID = 7099928573984617133L;

	private String fileDir;
	private String baseName;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;


	/** This constructor takes in key parameters to create oversized block files as follows:
	 * 
	 * fileDir + baseName + ind + "." + ext
	 * 
	 * @param fileDir
	 * @param chunkBase
	 * @param ext
	 */
	public BlockSinkSourceFactory (String fileDir, String baseName, String ext) {
		this.fileDir = fileDir;
		this.baseName = baseName;
		this.ext = ext;
	}
	
	/** This creates the next sink in seqence. It creates a binary file, and no append.  */
	public IBlockSink getNextSink () throws BlockingException {
		indSink ++;
		return new BlockSink (fileDir + baseName + indSink + "." + ext, TYPE);
	}
	
	/** This creates the next sink in seqence. It creates a String file for debug, and no append.  */
//	public IBlockSink getNextSinkText () throws FileNotFoundException, IOException {
//		indSink ++;
//		return new BlockSink (fileDir + baseName + indSink + "." + ext,
//			Constants.STRING);
//	}
	
	/** This creates the next source in seqence. It creates a binary file.  */
	public IBlockSource getNextSource () throws BlockingException {
		indSource ++;
		return new BlockSource (fileDir + baseName + indSource + "." + ext, TYPE);
	}
	
	/** Creates an IOverSizedSource for an existing IOversizedSink. */
	public IBlockSource getSource (IBlockSink sink) throws BlockingException {
		return new BlockSource (sink.getInfo(), TYPE);
	}


	/** Creates an IOverSizedSource for an existing IOversizedSink. */
	public IBlockSink getSink (IBlockSource source) throws BlockingException {
		return new BlockSink (source.getInfo(), TYPE);
	}


	/** This method returns the number of oversized block data sink files that have been created. */
	public int getNumSink () {
		return indSink;
	}

	/** This method returns the number of oversized block data source files that have been created. */
	public int getNumSource () {
		return indSource;
	}

	public void removeSink (IBlockSink sink) throws BlockingException {
		File f = new File (sink.getInfo());
		f.delete();
	}

	public void removeSource (IBlockSource source) throws BlockingException {
		File f = new File (source.getInfo());
		f.delete();
	}

	@Override
	public String toString() {
		return "BlockSinkSourceFactory [fileDir=" + fileDir + ", baseName="
				+ baseName + ", ext=" + ext + ", indSink=" + indSink
				+ ", indSource=" + indSource + "]";
	}

}
