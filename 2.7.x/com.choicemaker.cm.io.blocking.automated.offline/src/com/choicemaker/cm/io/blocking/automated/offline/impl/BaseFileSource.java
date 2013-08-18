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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISource;

/**
 * This is a generic file based implementation of ISource.
 * Each descendant must implement hasNext and getNext, and call init.
 * 
 * @author pcheung
 *
 */
public abstract class BaseFileSource implements ISource {

	protected DataInputStream dis = null;
	protected BufferedReader br = null;
	protected int count = 0;
	protected int type;
	protected String fileName;
	protected boolean exists;


	/** The descendants should call this method in their constructor.
	 * 
	 * @param fileName - file name of the sink
	 * @param type - indicates whether the file is a string or binary file.
	 */
	public void init (String fileName, int type) {
		this.type = type;
		this.fileName = fileName;
		File file = new File (fileName);
		exists = file.exists();
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	public boolean exists() {
		return exists;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	public void open() throws BlockingException {
		try {
			if (type == Constants.STRING) br = new BufferedReader (new FileReader(fileName));
			else if (type == Constants.BINARY) dis = new DataInputStream (new FileInputStream (fileName));
		} catch (FileNotFoundException ex) {
			throw new BlockingException (ex.toString());
		}
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	public void close() throws BlockingException {
		try {
			if (type == Constants.STRING) br.close();
			else if (type == Constants.BINARY) dis.close();
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public int getCount() {
		return count;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public String getInfo() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	public void remove() throws BlockingException {
		File file = new File (fileName);
		file.delete();
	}
	
	
	/** This returns the location in the string str where charAt == key and
	 * location >= start.
	 * Returns -1 if no such location.
	 * 
	 * @param str
	 * @param key
	 * @param start
	 * @return
	 */
	protected int getNextLocation (String str, char key, int start) {
		int ret = -1;
		int i = start;
		int size = str.length();
		boolean found = false;
		while ((i < size) && !found) {
			if (str.charAt(i) == key) {
				found = true;
				ret = i;
			} else {
				i++;
			}
		}
		
		return ret;
	}


}
