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
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This composite source consists of many MatchRecord2Source files.  It takes the base
 * file name and extension and calculates the number of files in the directory.  It
 * reads all the files one at a time and return MatchRecord2 using getNext.
 * 
 * 
 * @author pcheung
 *
 */
public class MatchRecord2CompositeSource implements IMatchRecord2Source {

	private static final Logger log = Logger.getLogger(MatchRecord2CompositeSource.class.getName());
	
	private String fileBase;
	private String fileExt;
	private int numFiles;
	private int currentInd;
	private IMatchRecord2Source currentSource;
	private int count = 0;
	
	private String info;
	
	
	/**
	 * This constructor takes in a file base and file extension.  All the files
	 * are of the form:
	 * [file base]_i.[file ext] - i starts with 1.
	 * 
	 * @param fileBase
	 * @param fileExt
	 */
	public MatchRecord2CompositeSource (String fileBase, String fileExt) {
		init (fileBase, fileExt);
	}
	
	
	/** This constructor takes in a file name like:
	 * dir/match_108.txt, and look for a set of files of this form:
	 * or dir/match_108_*.txt.
	 * If none are found it looks for dir/match_108.txt.
	 * 
	 * @param fileName
	 */
	public MatchRecord2CompositeSource (String fileName) {
		int i = fileName.indexOf(".");
		String base = fileName.substring(0, i);
		String ext = fileName.substring(i+1);
		init (base, ext);
	}
	
	
	/** This method takes in the file base and file extension for MatchRecord2 sources.
	 * By default, we asssume the input is a set of files, but if they don't exist, we
	 * check to see if it represents a single file. 
	 * 
	 * @param fileBase
	 * @param fileExt
	 */
	private void init (String fileBase, String fileExt) {
		this.fileBase = fileBase;
		this.fileExt = fileExt;
		numFiles = countFiles ();
		
		//special case when the input doesn't represent a set of files.
		if (numFiles == 0) {
			String fileName = fileBase + "." + fileExt;
			File f = new File (fileName);

			if (!f.exists()) throw new IllegalArgumentException ("No files found for " +
				fileBase + " " + fileExt);

			numFiles = 1;
			currentInd = 1;
			currentSource = new MatchRecord2Source (fileName, 
				Constants.STRING);
			info = fileBase + "." + fileExt;
		} else {
			currentInd = 1;
			currentSource = new MatchRecord2Source (getFileName(currentInd), 
				Constants.STRING);
			info = fileBase + "." + fileExt;
		}
	}
	
	
	
	/**
	 * This methods counts the number of files of the form 
	 * [file base]_i.[file ext].
	 * 
	 * @return
	 */
	private int countFiles () {
		int i = 1;
		File f = new File (getFileName(i)); 
		while (f.exists()) {
			i ++;
			f = new File (getFileName(i)); 
		}
		return i - 1;
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
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source#getNext()
	 */
	public MatchRecord2 getNext() throws BlockingException {
		count ++;
		return currentSource.getNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source#getCount()
	 */
	public int getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	public boolean exists() {
		if (numFiles > 0) return true;
		else return false;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	public void open() throws BlockingException {
		//need to reset to the first file
		if (currentInd > 1) {
			log.info("resetting Composite Source");
			init (fileBase, fileExt);
		} 
		currentSource.open();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		boolean ret = false;
		if (currentSource.hasNext()) {
			ret = true;
		} else {
			if (currentInd < numFiles) {
				currentSource.close();
				
				currentInd ++;
				currentSource = new MatchRecord2Source (getFileName (currentInd),
					Constants.STRING);
				currentSource.open();

				if (currentSource.hasNext()) ret = true;
			}
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	public void close() throws BlockingException {
		currentSource.close();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public String getInfo() {
		return info;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	public void remove() throws BlockingException {
		for (int i=1; i<= numFiles; i++) {
			MatchRecord2Source mrs = new MatchRecord2Source (getFileName(i), 
				Constants.STRING);
			mrs.remove();
			log.fine("removing " + mrs.getInfo());
		}
	}

}
