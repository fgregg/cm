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
package com.choicemaker.cm.matching.cfg.train;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.choicemaker.util.StringUtils;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class FlatFileRawDataReader implements RawDataReader {
	
	protected File file;
	protected BufferedReader reader;
	protected String delim;
	protected boolean trim;
	
	protected String[] nextRawData;

	public FlatFileRawDataReader(String fileName) throws IOException {
		this(fileName, "|", true);	
	}

	public FlatFileRawDataReader(String fileName, String delim, boolean trim) throws IOException {
		this(new File(fileName), delim, trim);
	}

	public FlatFileRawDataReader(File file) throws IOException {
		this(file, "|", true);	
	}

	public FlatFileRawDataReader(File file, String delim, boolean trim) throws IOException {
		this.file = file;
		this.delim = delim;
		this.trim = trim;
		
		open();
	}

	private void open() throws IOException {
		FileReader fr = new FileReader(file);
		reader = new BufferedReader(fr);
		
		getNextRawData();
	}

	public void close() throws IOException {
		reader.close();
	}

	public boolean hasNext() {
		return nextRawData != null;
	}

	public String[] next() throws IOException {
		String[] ret = nextRawData;
		getNextRawData();
		return ret;
	}

	private void getNextRawData() throws IOException {
		nextRawData = null;
		String line, trimmed;
		while (reader.ready()) {
			line = reader.readLine();
			if (line != null) {
				trimmed = line.trim();
				if (trimmed.length() > 0) {
					if (delim != null) {
						nextRawData = StringUtils.split(line, delim);
					} else {
						nextRawData = new String[] {line};
					}
					
					if (trim) {
						for (int i = 0; i < nextRawData.length; i++) {
							nextRawData[i] = nextRawData[i].trim();
						}
					}
					
					break;
				}
			}	
		}
	}

}
