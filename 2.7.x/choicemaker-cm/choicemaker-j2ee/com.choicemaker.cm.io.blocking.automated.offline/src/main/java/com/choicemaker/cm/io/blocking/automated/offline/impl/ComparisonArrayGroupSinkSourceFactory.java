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
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;

/**
 * This object create sinks that are groups of sinks.
 * 
 * WARNING: Not all the methods of IComparisonArraySinkSourceFactory are supported!
 * getNext is the only one supported ().
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes"})
public class ComparisonArrayGroupSinkSourceFactory implements
		IComparisonArraySinkSourceFactory {

	private String fileDir;
	private String nameBase;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;
	private int num;

	/** This constructor takes in key parameters to create ComparisonTreeGroupSink or 
	 * ComparisonTreeGroupSource files as follows:
	 * 
	 * BASE: fileDir + nameBase + ind + "." + ext
	 * 
	 * EACH GROUP SINK: fileDir + nameBase + ind + "_" + ind2 + "." + ext.
	 * 
	 * ind represents the chunk and ind2 represents the ith sink in the chunk.
	 * 
	 * @param fileDir
	 * @param nameBase
	 * @param ext
	 * @param num - the number of sinks in a group
	 * @param dataType - indicates if the record id is LONG, INTEGER, or String.
	 */
	public ComparisonArrayGroupSinkSourceFactory 
		(String fileDir, String nameBase, String ext, int num) {
			
		this.fileDir = fileDir;
		this.nameBase = nameBase;
		this.ext = ext;
		this.num = num;
	}


	/** This method returns the Comparison Tree source for this given chunk and tree ids.
	 * For example, it returns the file  
	 * 
	 * fileDir + nameBase + chunkId + "_" + treeId + "." + ext.
	 * 
	 * @param chunkId
	 * @param treeId
	 * @return
	 */
	public IComparisonArraySource getSource(int chunkId, int treeId) {
		return new ComparisonArraySource(fileDir + nameBase + chunkId + "_"
				+ treeId + "." + ext, EXTERNAL_DATA_FORMAT.STRING);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory#getNextSink()
	 */
	public IComparisonArraySink getNextSink() throws BlockingException {
		ComparisonArraySinkSourceFactory factory = new ComparisonArraySinkSourceFactory 
			(fileDir, nameBase + indSink + "_", ext);
		indSink ++;
		return new ComparisonArrayGroupSink (factory, num);
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory#getNextSource()
	 */
	public IComparisonArraySource getNextSource() throws BlockingException {
		throw new BlockingException ("getNextSource is not supported.");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory#getNumSink()
	 */
	public int getNumSink() {
		return indSink;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory#getNumSource()
	 */
	public int getNumSource() {
		return indSource;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory#getSource(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink)
	 */
	public IComparisonArraySource getSource(IComparisonArraySink sink) throws BlockingException {
		throw new BlockingException ("getSource is not supported.");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource)
	 */
	public IComparisonArraySink getSink(IComparisonArraySource source) throws BlockingException {
		throw new BlockingException ("getSink is not supported.");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink)
	 */
	public void removeSink(IComparisonArraySink sink) throws BlockingException {
		throw new BlockingException ("removeSink is not supported.");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource)
	 */
	public void removeSource(IComparisonArraySource source) throws BlockingException {
		throw new BlockingException ("removeSource is not supported.");
	}

}
