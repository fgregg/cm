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
import java.util.Date;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.MutableMarkedRecordPair;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.util.ChainedIOException;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.util.NameUtils;

/**
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:10:40 $
 */
public class FlatFileMarkedRecordPairSource implements MarkedRecordPairSource {
	private static Logger logger = Logger.getLogger(FlatFileMarkedRecordPairSource.class);

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
	private MutableMarkedRecordPair pair;
	private int[] descWidths;

	public String getFileNamePrefix() {
		return fileNamePrefix;
	}

	public String getRawFileNamePrefix() {
		return rawFileNamePrefix;
	}

	public void setRawFileNamePrefix(String fn) {
		rawFileNamePrefix = fn;
		fileNamePrefix =
			FileUtilities.getAbsoluteFile(new File(fileName).getAbsoluteFile().getParentFile(), fn).toString();
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
	public FlatFileMarkedRecordPairSource() {
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
	public FlatFileMarkedRecordPairSource(
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
		try {
			FlatFileAccessor ffa = (FlatFileAccessor) model.getAccessor();
			descWidths = ffa.getDescWidths();
			String[] fileNames = ffa.getFlatFileFileNames();
			int n = fileNames.length;
			if (multiFile) {
				reader = new BufferedReader[n];
				Tokenizer[] multiTokenizers = new Tokenizer[n];
				for (int i = 0; i < n; ++i) {
					reader[i] =
						new BufferedReader(
							new FileReader(new File(fileNamePrefix + fileNames[i] + fileNameSuffix).getAbsoluteFile()));
					multiTokenizers[i] = new Tokenizer(reader[i], fixedLength, separator, tagged, descWidths[0]);
					multiTokenizers[i].readLine();
				}
				tokenizer = multiTokenizers[0];
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
		} catch (Exception ex) {
			throw new ChainedIOException("Error reading file referenced by " + fileName, ex);
		}
	}

	public boolean hasNext() {
		return pair != null;
	}

	public ImmutableRecordPair getNext() throws IOException {
		return getNextMarkedRecordPair();
	}

	public MutableMarkedRecordPair getNextMarkedRecordPair() throws IOException {
		MutableMarkedRecordPair res = pair;
		getNextMain();
		return res;
	}

	private void getNextMain() throws IOException {
		if (tokenizer.lineRead()) {
			Decision decision = Decision.valueOf(tokenizer.nextTrimedString(descWidths[3]));
			Date date = tokenizer.nextDate(descWidths[4]);
			String user = tokenizer.nextTrimedString(descWidths[5]);
			String src = tokenizer.nextTrimedString(descWidths[6]);
			String comment = tokenizer.nextTrimedString(descWidths[7]);
			if (!singleLine)
				tokenizer.readLine();
			Record q = recordReader.getRecord();
			Record m = recordReader.getRecord();
			if (singleLine)
				tokenizer.readLine();
			pair = new MutableMarkedRecordPair(q, m, decision, date, user, src, comment);
		} else {
			pair = null;
		}
	}

	public void close() throws IOException {
		for (int i = 0; i < reader.length; ++i) {
			reader[i].close();
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
		setName(NameUtils.getNameFromFileName(fileName));
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
		return new FlatFileMarkedRecordPairSink(
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
