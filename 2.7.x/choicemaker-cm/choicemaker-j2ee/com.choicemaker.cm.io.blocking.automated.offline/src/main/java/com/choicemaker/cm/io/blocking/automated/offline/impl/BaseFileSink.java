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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISink;

/**
 * This is a generic file based implementation of ISink. Each descendant must
 * call init.
 * 
 * @author pcheung
 *
 */
public abstract class BaseFileSink implements ISink {

	protected DataOutputStream dos;
	protected FileWriter fw;
	protected int count = 0;
	protected EXTERNAL_DATA_FORMAT type;
	protected String fileName;
	protected boolean exists;

	/**
	 * The descendants should call this method in their constructor.
	 * 
	 * @param fileName
	 *            - file name of the sink
	 * @param type
	 *            - indicates whether the file is a string or binary file.
	 */
	protected BaseFileSink(String fileName, EXTERNAL_DATA_FORMAT type) {
		if (type == null || fileName == null) {
			throw new IllegalArgumentException("null argument");
		}
		this.type = type;
		assert this.type != null;
		this.fileName = fileName;
		File file = new File(fileName);
		exists = file.exists();
	}

	public boolean exists() {
		return exists;
	}

	public void open() throws BlockingException {
		try {
			switch (type) {
			case STRING:
				fw = new FileWriter(fileName, false);
				break;
			case BINARY:
				dos =
					new DataOutputStream(new FileOutputStream(fileName, false));
				break;
			default:
				throw new IllegalArgumentException("invalid type: " + type);
			}
		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}
	}

	public void append() throws BlockingException {
		try {
			switch (type) {
			case STRING:
				fw = new FileWriter(fileName, true);
				break;
			case BINARY:
				dos =
					new DataOutputStream(new FileOutputStream(fileName, true));
				break;
			default:
				throw new IllegalArgumentException("invalid type: " + type);
			}
		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}
	}

	public void close() throws BlockingException {
		try {
			switch (type) {
			case STRING:
				fw.close();
				break;
			case BINARY:
				dos.close();
				break;
			default:
				throw new IllegalArgumentException("invalid type: " + type);
			}
		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}
	}

	public int getCount() {
		return count;
	}

	public String getInfo() {
		return fileName;
	}

	public void remove() throws BlockingException {
		File file = new File(fileName);
		file.delete();
	}

	public void flush() throws BlockingException {
		try {
			switch (type) {
			case STRING:
				fw.flush();
				break;
			case BINARY:
				dos.flush();
				break;
			default:
				throw new IllegalArgumentException("invalid type: " + type);
			}
		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}
	}

}
