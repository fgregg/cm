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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This is a more intelligent version of MatchRecord2Sink.  It allows the user to define
 * how big a file can get.  The file name follow this mattern:
 * [base]_i.[extension].
 * 
 * [base]_1.[extension] is the first created.  If more files are needed, i gets 
 * incremented.
 * 
 * Please note that file size checking is inexact due to write buffer.  The file size
 * could jump from 0 to 17.6 MB.  If you set the file limit to 100MB, you could get a file
 * that is 117 MB.
 * 
 * 
 * @author pcheung
 *
 */
public class MatchRecord2CompositeSink implements IMatchRecord2Sink {

	
	private String fileBase;
	private String fileExt;
	private long maxFileSize;
	private int interval = 100000;
	
	private IMatchRecord2Sink currentFile;
	private int numberOfFiles = 0; 
	
	private int count = 0;
	
	private boolean isAppend = false;
	

	/** This constructor takes these arguments.
	 * 
	 * @param fileBase - the base name of the MatchRecord2 sink files.
	 * @param fileExt - the file extension of the MatchRecord2 sink files.
	 * @param maxFileSize - The maximum size of each sink file.  When the file size
	 * gets above this threshold, a new file is created.
	 */
	public MatchRecord2CompositeSink (String fileBase, String fileExt, long maxFileSize) {
		this.fileBase = fileBase;
		this.fileExt = fileExt;
		this.maxFileSize = maxFileSize;
	}


	/** This constructor takes these arguments.
	 * 
	 * @param fileBase - the base name of the MatchRecord2 sink files.
	 * @param fileExt - the file extension of the MatchRecord2 sink files.
	 * @param maxFileSize - The maximum size of each sink file.  When the file size
	 * gets above this threshold, a new file is created.
	 * @param interval - This controls how often to check the size of the size.
	 */
	public MatchRecord2CompositeSink (String fileBase, String fileExt, long maxFileSize,
		int interval) {
			
		this.fileBase = fileBase;
		this.fileExt = fileExt;
		this.maxFileSize = maxFileSize;
		this.interval = interval;
	}
	
	


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink#writeMatches(java.util.ArrayList)
	 */
	public void writeMatches(ArrayList matches) throws BlockingException {
		writeMatches(matches.iterator());
		
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink#writeMatches(java.util.Collection)
	 */
	public void writeMatches(Collection c) throws BlockingException {
		writeMatches(c.iterator());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink#writeMatches(java.util.Iterator)
	 */
	public void writeMatches(Iterator it) throws BlockingException {
		while (it.hasNext()) {
			MatchRecord2  mr = (MatchRecord2) it.next();
			writeMatch(mr);
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink#writeMatch(com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2)
	 */
	public void writeMatch(MatchRecord2 match) throws BlockingException {
		currentFile.writeMatch(match);
		count ++;
		
		if (count % interval == 0 && isFull ()) {
			currentFile.close();
			numberOfFiles ++;
			currentFile = new MatchRecord2Sink (getFileName(numberOfFiles), 
				Constants.STRING);
				
			if (isAppend) currentFile.append();
			else currentFile.open();
		}
	}
	
	
	/** This method checks to see if the current file is full.
	 * 
	 * @return
	 */
	private boolean isFull () throws BlockingException {
		currentFile.flush();
		
		File f = new File (getFileName (numberOfFiles));
		
		if (f.length() >= maxFileSize) {
			return true;
		} else {
			return false;
		} 
	}
	
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#exists()
	 */
	public boolean exists() {
		if (numberOfFiles > 0) return true;
		else return false;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#open()
	 */
	public void open() throws BlockingException {
		numberOfFiles = 1;
		currentFile = new MatchRecord2Sink (getFileName(numberOfFiles), 
			Constants.STRING);
		currentFile.open();
	}
	
	
	/** This method produces the file name for the given file number.
	 * 
	 * @param fileNum - fileNum
	 * @return String - fileName
	 */
	private String getFileName (int fileNum) {
		return fileBase + "_" + fileNum + "." + fileExt;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#append()
	 */
	public void append() throws BlockingException {
		numberOfFiles = 1;
		currentFile = new MatchRecord2Sink (getFileName(numberOfFiles),
			Constants.STRING);
		currentFile.append();
		isAppend = true;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#close()
	 */
	public void close() throws BlockingException {
		currentFile.close();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#flush()
	 */
	public void flush() throws BlockingException {
		currentFile.flush();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#getCount()
	 */
	public int getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#getInfo()
	 */
	public String getInfo() {
		return fileBase + "." + fileExt;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#remove()
	 */
	public void remove() throws BlockingException {
		for (int i=1; i<= numberOfFiles; i++) {
			MatchRecord2Sink mrs = new MatchRecord2Sink (getFileName(i), 
				Constants.STRING);
			mrs.remove();
		}
	}
	
}
