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
package com.choicemaker.cm.mmdevtools.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.choicemaker.cm.core.util.XmlWriter;

/**
 * @author ajwinkel
 *
 */
public abstract class FlatFileToXml {

	protected File input;
	protected File output;

	protected boolean hasHeader;
	protected boolean autoCreateFormat;

	public FlatFileToXml(File input, File output, boolean hasHeader, boolean autoCreateFormat) {
		this.input = input;
		this.output = output;
		
		this.hasHeader = hasHeader;
		this.autoCreateFormat = autoCreateFormat;
	}

	public void transform() throws IOException {
		BufferedReader reader = createReader(input);
		Format format = createFormat();
		XmlWriter writer = createWriter(output);

		if (hasHeader) {
			String s = reader.readLine();
			if (autoCreateFormat) {
				autoCreateFormatFromHeader(format, s);
			}
			Line line = format.parseLine(s);
			processHeader(line);
		}

		writeHeader(writer);
		
		String s = reader.readLine();
		while (s != null) {
			Line line = format.parseLine(s);
			writeRecord(line, writer);
			
			s = reader.readLine();
		}
		
		writeFooter(writer);
		
		writer.close();
		reader.close();
	}

	public BufferedReader createReader(File input) throws IOException {
		return new BufferedReader(new FileReader(input));
	}

	public abstract Format createFormat();

	protected void autoCreateFormatFromHeader(Format f, String s) {
		String[] line = f.tokenizeLine(s);
		for (int i = 0; i < line.length; i++) {
			f.addColumn(new Column(line[i]));
		}
	}

	public XmlWriter createWriter(File output) throws IOException {
		return new XmlWriter(new FileWriter(output), "UTF-8");
	}
	
	public void processHeader(Line line) {
		// by default, do nothing.
	}
	
	public void writeHeader(XmlWriter w) throws IOException {
		w.beginElement("ChoiceMakerRecords");
	}
	
	public abstract void writeRecord(Line line, XmlWriter w) throws IOException;

	public void writeFooter(XmlWriter w) throws IOException {
		w.endElement();
	}

	public static interface Format {
		public void addColumn(Column c);
		public void addColumn(Column c, int index);
		public Column getColumn(int index);
		public int getNumColumns();
		public int columnNameToIndex(String name);
		public Line parseLine(String line);
		public String[] tokenizeLine(String line);
	}

	public static abstract class AbstractFormat implements Format {
		private List columns = new ArrayList();
		private Map namesToIndices = new HashMap();
		public void addColumn(Column c) {
			addColumn(c, columns.size());
		}
		public void addColumn(Column c, int index) {
			while (index >= getNumColumns()) {
				columns.add(null);
			}
			columns.set(index, c);
			namesToIndices.put(c.getName(), new Integer(index));
		}
		public Column getColumn(int i) {
			return (Column) columns.get(i);
		}
		public int getNumColumns() {
			return columns.size();
		}
		public int columnNameToIndex(String name) {
			return ((Integer) namesToIndices.get(name)).intValue();
		}
	}

	public static class FixedWidthFormat extends AbstractFormat {
		public Line parseLine(String line) {
			String[] cols = new String[getNumColumns()];
			for (int i = 0; i < cols.length; i++) {
				Column c = getColumn(i);
				if (c != null) {
					cols[i] = line.substring(c.beginIndex(), c.endIndex());
				}
			}
			return new Line(cols, this);
		}
		public String[] tokenizeLine(String line) {
			throw new UnsupportedOperationException();
		}
	}

	public static class CharacterDelimitedFormat extends AbstractFormat {
		private char delim;

		public CharacterDelimitedFormat(char delim) {
			this.delim = delim;
		}

		public Line parseLine(String line) {
			String[] tokens = tokenizeLine(line);
			String[] cols = new String[getNumColumns()];
			for (int i = 0; i < tokens.length; i++) {
				cols[i] = tokens[i];
			}
			return new Line(cols, this);
		}

		public String[] tokenizeLine(String line) {
			List tokens = new ArrayList();

			int len = line.length();
			int last = 0;
			int index;
			while (true) {
				index = line.indexOf(delim, last);
				if (index == -1) {
					tokens.add(line.substring(last));
					break;
				} else if (index == len - 1) {
					tokens.add(line.substring(last, index));
					tokens.add("");
					break;
				} else {
					tokens.add(line.substring(last, index));
				}

				last = index + 1;
			}

			String[] toks = new String[tokens.size()];
			for (int i = 0; i < toks.length; i++) {
				toks[i] = (String) tokens.get(i);
			}

			return toks;
		}
	}

	public static class WhitespaceDelimitedFormat extends AbstractFormat {
		public Line parseLine(String line) {
			StringTokenizer tokens = new StringTokenizer(line);
			String[] cols = new String[getNumColumns()];
			int colIndex = -1;
			while (tokens.hasMoreTokens() && ++colIndex < cols.length) {
				Column c = getColumn(colIndex);
				String s = tokens.nextToken();
				if (c != null) {
					cols[colIndex] = s;
				}
			}
			return new Line(cols, this);
		}
		public String[] tokenizeLine(String line) {
			throw new UnsupportedOperationException();
		}
	}

	public static class Column {
		private String name;
		private int begin;
		private int end;
		public Column(String name) {
			this(name, -1, -1);
		}
		public Column(String name, int begin, int end) {
			this.name = name;
			this.begin = begin;
			this.end = end;
		}
		public String getName() {
			return name;
		}
		public int beginIndex() {
			return begin;
		}
		public int endIndex() {
			return end;
		}
	}

	public static class Line {
		private String[] values;
		private Format f;
		public Line(Format f) {
			this(new String[f.getNumColumns()], f);
		}
		public Line(String[] values, Format f) {
			this.values = values;
			this.f = f;
		}
		public String getColumnValue(int i) {
			return values[i];
		}
		public String getColumnValue(String name) {
			return values[f.columnNameToIndex(name)];
		}
		public void setColumnValue(int i, String value) {
			values[i] = value;
		}
		public void setColumnValue(String name, String value) {
			values[f.columnNameToIndex(name)] = value;
		}
	}

	public static class Sequence {
		protected int seq;
		public Sequence() {
			seq = 0;
		}
		public int peek() {
			return seq;
		}
		public int next() {
			return seq++;
		}
		public void setNext(int next) {
			seq = next;
		}
	}

}
