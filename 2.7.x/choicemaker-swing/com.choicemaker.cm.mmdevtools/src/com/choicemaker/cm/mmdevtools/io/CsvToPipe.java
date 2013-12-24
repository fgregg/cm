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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Converts a CSV file (fields optionally enclosed by double quotes)
 * to a pipe-delimited file
 *
 * @author    Adam Winkel
 * @version   
 */
public class CsvToPipe extends Reader {

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: CsvToPipe inFile outFile");
			System.exit(1);
		}
		
		Transducer transducer = new Transducer();
		
		FileReader fr = new FileReader(args[0]);
		BufferedReader reader = new BufferedReader(fr);
		
		FileWriter fw = new FileWriter(args[1]);
		PrintWriter writer = new PrintWriter(fw);
		
		int lineNum = 0;
		while (reader.ready()) {
			String line = reader.readLine();
			String out = transducer.transduce(line);
			writer.println(out);
		}
		
		writer.close();
		fw.close();
		
		reader.close();
		fr.close();
	}

	protected BufferedReader reader;
	protected Transducer transducer;
	protected char[] lineBuff;
	protected int lineLen, linePos;

	protected String lineSep;

	public CsvToPipe(Reader reader) {
		this(new BufferedReader(reader));
	}
	
	public CsvToPipe(BufferedReader reader) {
		this.reader = reader;
		this.transducer = new Transducer();
		
		lineSep = System.getProperty("line.separator");
	}

	public int read(char[] cbuf, int off, int len) throws IOException {
		if (linePos >= lineLen && !fill()) {
			return -1;
		}
				
		int available = lineLen - linePos;
		int toRead = len > available ? available : len;

		System.arraycopy(lineBuff, linePos, cbuf, off, toRead);
		linePos += toRead;

		return toRead;
	}

	public void close() throws IOException {
		reader.close();
	}
	
	private boolean fill() throws IOException {
		String s = reader.readLine();
		if (s != null) {
			String line = transducer.transduce(s);
			line += lineSep;
			lineBuff = (line + lineSep).toCharArray();
			lineLen = line.length();
			linePos = 0;
			
			return true;
		} else {
			return false;
		}
	}

	public static class Transducer {

		// states		
		public static final byte STATE_START           = 0;
		public static final byte STATE_REGFIELD        = 1;
		public static final byte STATE_QUOTEDFIELD     = 2;
		public static final byte STATE_QUOTEDFIELD_END = 3;

		// input alphabet (types)
		public static final byte COMMA = 0;
		public static final byte QUOTE = 1;
		public static final byte SPACE = 2;
		public static final byte OTHER = 3;
		
		// actions
		public static final byte OUTPUT_CHAR = -21;
		public static final byte END_FIELD   = -22;
		public static final byte END_LINE    = -23;
		public static final byte ERROR       = -24;
		public static final byte NONE        = -128;
				
		protected byte[][][] transitions = {
			{
				{END_FIELD, STATE_START},
				{NONE, STATE_QUOTEDFIELD},
				{NONE, STATE_START},
				{OUTPUT_CHAR, STATE_REGFIELD}
			}, 
			{
				{END_FIELD, STATE_START},
				{ERROR, 0},
				{OUTPUT_CHAR, STATE_REGFIELD},
				{OUTPUT_CHAR, STATE_REGFIELD}
			},
			{
				{OUTPUT_CHAR, STATE_QUOTEDFIELD},
				{NONE, STATE_QUOTEDFIELD_END},
				{OUTPUT_CHAR, STATE_QUOTEDFIELD},
				{OUTPUT_CHAR, STATE_QUOTEDFIELD}
			},
			{
				{END_FIELD, STATE_START},
				{ERROR, 1},
				{NONE, STATE_QUOTEDFIELD_END},
				{ERROR, 2}
			}
		};
		
		protected String[] errorMessages = {
			"Cannot have a quote in a regular field.",
			"Unbalanced quotes.",
			"Unexpected character outside of quotes in quoted field.",
			"Unexpected line end."  // not used above, but used below.
		};
		
		protected int lineNum;
		
		public Transducer() { 
			lineNum = 0;
		}
		
		public String transduce(String s) {
			lineNum++;

			byte state = STATE_START;
			
			StringBuffer buff = new StringBuffer(s.length());
			int numFields = 0;
			
			int len = s.length();
			for (int i = 0; i < len; i++) {
				char c = s.charAt(i);
				
				byte type = OTHER;
				if (c == '|') {
					buff.append(' ');
					System.err.println("Found pipe in line " + lineNum + ":\n\t" + s);
					continue;
				} else if (c == ',') {
					type = COMMA;
				} else if (c == '"') {
					type = QUOTE;
					if (state == STATE_QUOTEDFIELD && 
						i + 1 < len && 
						s.charAt(i+1) == '"') { // handle double quotes inside a quoted string.

						type = OTHER;
						i++;
					}
				} else if (Character.isWhitespace(c)) {
					c = ' ';
					type = SPACE;
				} else {
					type = OTHER;
				}
					
				byte[] trans = transitions[state][type];
				switch (trans[0]) {
					case ERROR:
						error(trans[1], s, lineNum, i);
						break;
					case OUTPUT_CHAR:
						buff.append(c);
						break;
					case END_FIELD:
						buff.append('|');
						break;
				};
				
				state = trans[1];
			}
			
			if (state != STATE_START) {
				byte[] trans = transitions[state][COMMA];
				if (trans[0] != END_FIELD) {  // just generate something that looks like an error...
					try {
						error(3, s, lineNum, len);
					} catch (RuntimeException ex) {
						System.err.println(ex.getMessage());
					}
					
					//padPipes(buff, 10);
				}
			}
			
			return buff.toString();
		}
	
		private void error(int msgIndex, String s, int lineNum, int column) {
			throw new RuntimeException(
				"Error at line " + lineNum + " column " + column + ": " + errorMessages[msgIndex] + 
				"\n\t" + s);
		}
		
		/*
		private void padPipes(StringBuffer buff, int numPipes) {
			int count = 0;
			for (int i = 0; i < buff.length(); i++) {
				count += buff.charAt(i) == '|' ? 1 : 0;
			}
			while (count < numPipes) {
				buff.append('|');
				count++;
			}
		}
		*/
		
	}

}
