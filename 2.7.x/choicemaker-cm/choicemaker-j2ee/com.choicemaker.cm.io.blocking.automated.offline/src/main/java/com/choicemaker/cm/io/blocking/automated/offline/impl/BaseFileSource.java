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
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISource;

/**
 * This is a generic file based implementation of ISource. Each descendant must
 * implement hasNext and getNext, and call init.
 * 
 * @author pcheung
 *
 */
public abstract class BaseFileSource<T> implements ISource<T> {

	protected DataInputStream dis = null;
	protected BufferedReader br = null;
	protected int count = 0;
	protected final EXTERNAL_DATA_FORMAT type;
	protected final String fileName;

	/**
	 * The descendants should call this method in their constructor.
	 * 
	 * @param fileName
	 *            - file name of the sink
	 * @param type
	 *            - indicates whether the file is a string or binary file.
	 */
	protected BaseFileSource(String fileName, EXTERNAL_DATA_FORMAT type) {
		if (type == null || fileName == null) {
			throw new IllegalArgumentException("null argument");
		}
		this.type = type;
		this.fileName = fileName;
	}

	@Override
	public boolean exists() {
		File file = new File(fileName);
		boolean exists = file.exists();
		return exists;
	}

	@Override
	public void open() throws BlockingException {
		try {
			if (type == EXTERNAL_DATA_FORMAT.STRING)
				br = new BufferedReader(new FileReader(fileName));
			else if (type == EXTERNAL_DATA_FORMAT.BINARY)
				dis = new DataInputStream(new FileInputStream(fileName));
		} catch (FileNotFoundException ex) {
			throw new BlockingException(ex.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	@Override
	public void close() throws BlockingException {
		try {
			if (type == EXTERNAL_DATA_FORMAT.STRING)
				br.close();
			else if (type == EXTERNAL_DATA_FORMAT.BINARY)
				dis.close();
		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}
	}

	public int getCount() {
		return count;
	}

	@Override
	public String getInfo() {
		return fileName;
	}

	@Override
	public void delete() throws BlockingException {
		File file = new File(fileName);
		file.delete();
	}

	/**
	 * This returns the location in the string str where charAt == key and
	 * location >= start. Returns -1 if no such location.
	 * 
	 * @param str
	 * @param key
	 * @param start
	 * @return
	 */
	protected int getNextLocation(String str, char key, int start) {
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

	@Override
	public String toString() {
		return "BaseFileSource [type=" + type + ", fileName=" + fileName
				+ ", exists=" + exists() + ", count=" + count + "]";
	}

}
