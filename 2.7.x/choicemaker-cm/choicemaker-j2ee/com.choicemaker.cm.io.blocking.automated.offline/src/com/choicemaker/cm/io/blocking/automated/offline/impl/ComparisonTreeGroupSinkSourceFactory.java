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

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource;

/**
 * This object create sinks that are groups of sinks.
 * 
 * WARNING: Not all the methods of IComparisonTreeSinkSourceFactory are supported!
 * getNext is the only one supported ().
 * 
 * @author pcheung
 *
 */
public class ComparisonTreeGroupSinkSourceFactory implements IComparisonTreeSinkSourceFactory {

	private String fileDir;
	private String nameBase;
	private String ext;
	private int indSink = 0;
	private int indSource = 0;
	private int dataType;
	private int num;


	/** This constructor takes in key parameters to create ComparisonTreeGroupSink or 
	 * ComparisonTreeGroupSource files as follows:
	 * 
	 * BASE: fileDir + nameBase + chunkId + "." + ext
	 * 
	 * EACH GROUP SINK: fileDir + nameBase + chunkId + "_" + treeId + "." + ext.
	 * 
	 * treeId goes from 0 to num.
	 * 
	 * @param fileDir
	 * @param nameBase
	 * @param ext
	 * @param num - the number of sinks in a group
	 * @param dataType - indicates if the record id is LONG, INTEGER, or String.
	 */
	public ComparisonTreeGroupSinkSourceFactory 
		(String fileDir, String nameBase, String ext, int num, int dataType) {
			
		this.fileDir = fileDir;
		this.nameBase = nameBase;
		this.ext = ext;
		this.dataType = dataType;
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
	public IComparisonTreeSource getSource (int chunkId, int treeId) {
		return new ComparisonTreeSource (fileDir + nameBase + chunkId + "_" + treeId + "." + ext, dataType);
	}
	


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getNextSink()
	 */
	public IComparisonTreeSink getNextSink() throws BlockingException {
		ComparisonTreeSinkSourceFactory factory = new ComparisonTreeSinkSourceFactory 
			(fileDir, nameBase + indSink + "_", ext, dataType);
		indSink ++;
		return new ComparisonTreeGroupSink (factory, num);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getNextSource()
	 */
	public IComparisonTreeSource getNextSource() throws BlockingException {
		throw new BlockingException ("getNextSource is not supported.");
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
		throw new BlockingException ("getSource is not supported.");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#getSink(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource)
	 */
	public IComparisonTreeSink getSink(IComparisonTreeSource source) throws BlockingException {
		throw new BlockingException ("getSink is not supported.");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#removeSink(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink)
	 */
	public void removeSink(IComparisonTreeSink sink) throws BlockingException {
		throw new BlockingException ("removeSink is not supported.");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory#removeSource(com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource)
	 */
	public void removeSource(IComparisonTreeSource source) throws BlockingException {
		throw new BlockingException ("removeSource is not supported.");
	}

}
