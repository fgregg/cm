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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.choicemaker.cm.matching.cfg.ParsedData;
import com.choicemaker.cm.matching.cfg.Parser;

/**
 * @author Adam Winkel
 */
public class ParserBenchmarkTest {

	public static final String SPEED = "-speed";
	public static final String PARSABILITY = "-parsability";

	protected String type;
	protected Parser parser;
	protected RawDataReader reader;

	protected long startTime;
	protected long endTime;

	protected int numParsed;
	protected int numUnparsed;

	public ParserBenchmarkTest() { }

	public ParserBenchmarkTest(Parser parser, RawDataReader rawDataReader) {
		this(parser, rawDataReader, PARSABILITY);
	}

	public ParserBenchmarkTest(Parser parser, RawDataReader reader, String type) {
		setParser(parser);
		setRawDataReader(reader);
		setType(type);
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}

	public void setRawDataReader(RawDataReader reader) {
		this.reader = reader;
	}

	public void setType(String type) {
		this.type = type.intern();
	}

	public void runTest() throws Exception {
		numParsed = 0;
		numUnparsed = 0;

		startTime = System.currentTimeMillis();

		while (reader.hasNext()) {
			String[] raw = reader.next();
			ParsedData pd = parser.getBestParse(raw);
			if (pd != null) {
				numParsed++;
				handleParsed(raw);
			} else {
				numUnparsed++;
				handleUnparsed(raw);
			}
		}

		endTime = System.currentTimeMillis();
	}

	public long getExecutionTime() {
		return endTime - startTime;
	}

	public int getNumInstances() {
		return getNumUnparsedInstances() + getNumParsedInstances();
	}

	public int getNumUnparsedInstances() {
		return numUnparsed;
	}

	public int getNumParsedInstances() {
		return numParsed;
	}

	public void handleUnparsed(String[] raw) {
		if (type == PARSABILITY) {
			System.out.println( join(raw, " | ") );
		}
	}

	public void handleParsed(String[] raw) {
		// DO NOTHING
	}

	public void printResults() throws IOException {
		printResults(System.out);
	}

	public void printResults(String fileName) throws IOException {
		printResults(new FileOutputStream(fileName));
	}

	public void printResults(OutputStream os) throws IOException {
		printResults(new PrintStream(os));
	}

	public void printResults(PrintStream ps) throws IOException {
		ps.println();
		ps.println("Total Instances: " + getNumInstances());
		ps.println("Parsed Instances: " + getNumParsedInstances() + " (" +
			String.valueOf(100.0 * getNumParsedInstances() / getNumInstances()).substring(0, 4) + "%)");
		ps.println("Unparsed Instances: " + getNumUnparsedInstances() + " (" +
			String.valueOf(100.0 * getNumUnparsedInstances() / getNumInstances()).substring(0, 4) + "%)");
		ps.println("Total time: " + getExecutionTime() + "ms (" +
			String.valueOf(getExecutionTime() / (double) getNumInstances()).substring(0, 4) + "ms/instance)");
	}

	public static String join(String[] s, String delim) {
		StringBuffer buff = new StringBuffer();

		// 2014-04-24 rphall: Commented out unused local variable.
//		boolean lastNull = false;

		if (s.length == 0) {
			return "";
		} if (s[0] != null) {
			buff.append(s[0]);
		}
//		else {
//			lastNull = true;
//		}

		for (int i = 1; i < s.length; i++) {
			buff.append(delim);
			if (s[i] != null) {
				buff.append(s[i]);
			}
		}

		return buff.toString();
	}

}
