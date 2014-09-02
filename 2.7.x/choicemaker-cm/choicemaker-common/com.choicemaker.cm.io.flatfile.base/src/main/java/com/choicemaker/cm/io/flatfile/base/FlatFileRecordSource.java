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
package com.choicemaker.cm.io.flatfile.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.util.NameUtils;
import com.choicemaker.util.FileUtilities;

/**
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:10:40 $
 */
public class FlatFileRecordSource implements RecordSource {

	private String fileName;
	private String name;
	private String rawFileNamePrefix;
	private String fileNamePrefix;
	private String fileNameSuffix;
	private boolean multiFile;
	private boolean singleLine;
	private boolean fixedLength;
	private char separator;
	private boolean tagged;
	private ImmutableProbabilityModel model;
	private FlatFileReader recordReader;
	private BufferedReader[] reader;
	private Tokenizer tokenizer;
	private Record record;
	private int[] descWidths;

	/**
	 * Remember whether this multi-file FlatFileReader has a next line (meaning a next record).
	 * 
	 * Multi-file FlatFileReader's read the next record ahead of time.  When you open() a multi-file FlatFileReader,
	 * it builds the first record.  Contrast this to a single-file FlatFileReader: when you open() a single-file
	 * FlatFileReader it just checks that it <it>could</it> read a record if asked to do so at a later time.
	 * 
	 * This is generally a failing in the generated code and should be fixed, but this fix is much simpler.
	 */
	private boolean multiFileLineRead;

	public String getFileNamePrefix() {
		return fileNamePrefix;
	}

	public String getRawFileNamePrefix() {
		return rawFileNamePrefix;	
	}

	public void setRawFileNamePrefix(String fn) {
		rawFileNamePrefix = fn;
		File f = new File(this.fileName);
		File f2 = f.getAbsoluteFile();
		File p = f2.getParentFile();
		File f3 = FileUtilities.getAbsoluteFile(p, fn);
		fileNamePrefix = f3.toString();
	}

	public String getFileNameSuffix() {
		return fileNameSuffix;
	}

	public void setFileNameSuffix(String s) {
		fileNameSuffix = s;
	}

	public boolean isMultiFile() {
		return multiFile;
	}

	public void setMultiFile(boolean v) {
		multiFile = v;
	}

	public boolean isSingleLine() {
		return singleLine;
	}

	public void setSingleLine(boolean v) {
		singleLine = v;
	}

	public boolean isFixedLength() {
		return fixedLength;
	}

	public void setFixedLength(boolean v) {
		fixedLength = v;
	}

	public char getSeparator() {
		return separator;
	}

	public void setSeparator(char s) {
		separator = s;
	}

	/**
	 * Get the value of tagged.
	 * @return value of tagged.
	 */
	public boolean isTagged() {
		return tagged;
	}

	/**
	 * Set the value of tagged.
	 * @param v  Value to assign to tagged.
	 */
	public void setTagged(boolean v) {
		this.tagged = v;
	}

	/**
	 * Creates an uninitialized instance.
	 */
	public FlatFileRecordSource() {
		fileName = "";
		rawFileNamePrefix = "";
		fileNamePrefix = "";
		fileNameSuffix = "";
		multiFile = false;
		singleLine = false;
		fixedLength = false;
		separator = ',';
		tagged = true;
	}

	/**
	 * Constructor.
	 */
	public FlatFileRecordSource(
		String fileName,
		String rawFileNamePrefix,
		String fileNameSuffix,
		boolean multiFile,
		boolean singleLine,
		boolean fixedLength,
		char separator,
		boolean tagged,
		ImmutableProbabilityModel model) {
		setFileName(fileName);
		setRawFileNamePrefix(rawFileNamePrefix);
		this.fileNameSuffix = fileNameSuffix;
		this.multiFile = multiFile;
		this.singleLine = singleLine;
		this.fixedLength = fixedLength;
		this.separator = separator;
		this.tagged = tagged;
		setModel(model);
	}

	public void open() throws IOException {
		FlatFileAccessor ffa = (FlatFileAccessor) model.getAccessor();
		descWidths = ffa.getDescWidths();
		String[] fileNames = ffa.getFlatFileFileNames();
		int n = fileNames.length;
		if (multiFile) {
			reader = new BufferedReader[n];
			Tokenizer[] multiTokenizers = new Tokenizer[n];
			for (int i = 1; i < n; ++i) {
				reader[i] =
					new BufferedReader(
						new FileReader(new File(fileNamePrefix + fileNames[i] + fileNameSuffix).getAbsoluteFile()));
				multiTokenizers[i] = new Tokenizer(reader[i], fixedLength, separator, tagged, descWidths[0]);
				multiTokenizers[i].readLine();
			}
			tokenizer = multiTokenizers[1];
			
			// AJW
			multiFileLineRead = tokenizer.lineRead();
			
			recordReader = ffa.getMultiFileFlatFileReader(multiTokenizers, tagged);
		} else {
			reader = new BufferedReader[1];
			reader[0] =
				new BufferedReader(
					new FileReader(new File(fileNamePrefix + fileNames[0] + fileNameSuffix).getAbsoluteFile()));
			tokenizer = new Tokenizer(reader[0], fixedLength, separator, tagged, descWidths[0]);
			tokenizer.readLine();
			recordReader = ffa.getSingleFileFlatFileReader(tokenizer, tagged, singleLine);
		}
		recordReader.open();
		getNextMain();
	}

	public boolean hasNext() {
		return record != null;
	}

	public Record getNext() throws IOException {
		return getNextRecord();
	}

	public Record getNextRecord() throws IOException {
		Record res = record;
		getNextMain();
		return res;
	}

	private void getNextMain() throws IOException {		
		record = null;

		if (multiFile) {
			if (multiFileLineRead) {
				multiFileLineRead = tokenizer.lineRead();
				record = recordReader.getRecord();
			}
		} else {
			if (tokenizer.lineRead()) {
				record = recordReader.getRecord();
				if (singleLine) {
					tokenizer.readLine();
				}
			}
		}
	}

	public void close() throws IOException {
		for (int i = 0; i < reader.length; ++i) {
			if (reader[i] != null) {
				reader[i].close();
			}
		}
	}

	/**
	 * Get the value of name.
	 * @return value of name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the value of name.
	 * @param v  Value to assign to name.
	 */
	public void setName(String v) {
		this.name = v;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		setName(NameUtils.getNameFromFilePath(fileName));
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * Get the value of model.
	 * @return value of model.
	 */
	public ImmutableProbabilityModel getModel() {
		return model;
	}

	/**
	 * Set the value of model.
	 * @param v  Value to assign to model.
	 */
	public void setModel(ImmutableProbabilityModel v) {
		this.model = v;
	}

	public String toString() {
		return name;
	}

	public boolean hasSink() {
		return true;
	}

	public Sink getSink() {
		return new FlatFileRecordSink(
			name,
			fileNamePrefix,
			fileNameSuffix,
			multiFile,
			singleLine,
			fixedLength,
			separator,
			tagged,
			true,
			model);
	}
}
