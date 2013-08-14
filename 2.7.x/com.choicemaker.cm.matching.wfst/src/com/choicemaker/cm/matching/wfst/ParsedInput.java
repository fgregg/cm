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
package com.choicemaker.cm.matching.wfst;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A pair of objects, consisting of an input String and a List of Maps,
 * each Map representing a possible parse of the input.
 * @author rphall
 * @see AmbiguousParser
 */
public class ParsedInput {

	public final String input;
	public final List output;

	public ParsedInput(String input, List output) {
		this.input = input;
		this.output = output;
		checkThis(this);
	} // ctor(String,List)

	public ParsedInput(String line) {
		Object[] o = parseLine(line);
		this.input = (String) o[0];
		this.output = (List) o[1];
		checkThis(this);
	}

	private static void checkThis(ParsedInput pi) {
		if (pi.input == null) {
			throw new IllegalArgumentException("null input");
		}
		if (pi.output == null) {
			throw new IllegalArgumentException("null output");
		}
		for (int i = 0; i < pi.output.size(); i++) {
			if (!(pi.output.get(i) instanceof Map)) {
				throw new IllegalArgumentException(
					"element '" + i + "' of output is not a Map");
			}
		}
	}

	private static Object[] parseLine(String line) {
		if (line == null || line.length() == 0) {
			throw new IllegalArgumentException("null or blank line");
		}
		Object[] retVal = new Object[2];
		int colon = line.indexOf(':');
		int leftBracket = line.indexOf('[');
		int rightBracket = line.indexOf(']');

		if (colon == -1 || colon >= leftBracket) {
			throw new RuntimeException("Bad colon position: " + colon);
		} else if (leftBracket == -1 || leftBracket >= rightBracket) {
			throw new RuntimeException(
				"Bad left bracket position: " + leftBracket);
		} else if (rightBracket == -1 || rightBracket > line.length()) {
			throw new RuntimeException(
				"Bad right bracket position: " + rightBracket);
		}

		String input = line.substring(0, colon);
		String list =
			line.substring(leftBracket + 1, rightBracket);
		List maps = parseMaps(list);

		retVal[0] = input;
		retVal[1] = maps;

		return retVal;
	} // parseLine(String)

	private static List parseMaps(String s) {
		List retVal = new ArrayList();
		int leftBracket = s.indexOf('{');
		while (leftBracket > -1) {
			int start = leftBracket + 1;
			int end = s.indexOf('}', start);
			if (end == -1 || end <= start) {
				throw new RuntimeException(
					"Bad right bracket position: " + end);
			}
			String map = s.substring(start,end);
			Map m = parseMap(map);
			retVal.add(m);
			leftBracket = s.indexOf('{', leftBracket + 1);
		}

		return retVal;
	} // parseMaps(String)

	private static Map parseMap(String s) {
		Map retVal = new HashMap();
		int start = 0;
		while (start < s.length()) {
			int end = s.indexOf(',', start);
			if (end == -1) {
				end = s.length();
			}
			String keyValue = s.substring(start,end);
			int equals = keyValue.indexOf('=');

			if (equals > -1) {
				String key = keyValue.substring(0, equals);
				key = key.trim();
				String value = "";
				if (equals < keyValue.length()) {
					value = keyValue.substring(equals + 1);
				}
				retVal.put(key, value);
			}

			start = end + 1;
		} // while

		return retVal;
	} // parseMap(String)

	/**
	 * Returns a String representation of a ParsedInput instance.
	 */
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);
		print(w);
		String retVal = sw.toString();
		return retVal;
	}

	/**
	 * Reads ParsedInput instances from a Reader.
	 * @param r a non-null Reader
	 * @return a valid ParsedInput instance until the end of the input
	 * has been reached, at which point this method returns null
	 * @throws IOException
	 */
	public static ParsedInput read(BufferedReader reader) throws IOException {
		if (reader == null) {
			throw new IllegalArgumentException("null reader");
		}
		ParsedInput retVal = null;
		String line = reader.readLine();
		if (line != null) {
			retVal = new ParsedInput(line);
		}
		return retVal;
	}

	public void print(PrintWriter w) {
		w.print(input + ": ");
		printOutput(w);
		return;
	}

	private void printOutput(PrintWriter w) {
		w.print("[");
		for (int i = 0; i < output.size(); i++) {
			w.print("{");
			Map m = (Map) output.get(i);
			printMap(m, w);
			w.print("}");
			if (i < output.size() - 1) {
				w.print(", ");
			}
		}
		w.print("]");
		return;
	}

	private static void printMap(Map m, PrintWriter w) {
		Set keys = m.keySet();
		int count = 0;
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = (String) m.get(key);
			w.print(key.trim() + "=" + value.trim());
			if (count < keys.size() - 1) {
				w.print(", ");
			}
			++count;
		}
		return;
	} // printMap(Map,PrintWriter)

} // ParsedInput

