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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISink;

/**
 * This is a generic file based implementation of ISink.
 * Each descendant must call init.
 * 
 * @author pcheung
 *
 */
public abstract class BaseFileSink implements ISink {

	protected DataOutputStream dos;
	protected FileWriter fw;
	protected int count = 0;
	protected int type;
	protected String fileName;
	protected boolean exists;
	
	
	/** The descendants should call this method in their constructor.
	 * 
	 * @param fileName - file name of the sink
	 * @param type - indicates whether the file is a string or binary file.
	 */
	protected void init (String fileName, int type) {
		this.type = type;
		this.fileName = fileName;
		File file = new File (fileName);
		exists = file.exists();
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#exists()
	 */
	public boolean exists() {
		return exists;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#open()
	 */
	public void open() throws BlockingException {
		try {
			if (type == Constants.STRING) fw = new FileWriter (fileName, false);
			else if (type == Constants.BINARY) dos = new DataOutputStream (new FileOutputStream (fileName, false));
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#append()
	 */
	public void append() throws BlockingException {
		try {
			if (type == Constants.STRING) fw = new FileWriter (fileName, true);
			else if (type == Constants.BINARY) dos = new DataOutputStream (new FileOutputStream (fileName, true));
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#close()
	 */
	public void close() throws BlockingException {
		try {
			if (type == Constants.STRING) fw.close();
			else if (type == Constants.BINARY) dos.close();
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
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
		return fileName;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#remove()
	 */
	public void remove() throws BlockingException {
		File file = new File (fileName);
		file.delete();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#flush()
	 */
	public void flush() throws BlockingException {
		try {
			if (type == Constants.STRING) fw.flush();
			else if (type == Constants.BINARY) dos.flush();
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}

}
