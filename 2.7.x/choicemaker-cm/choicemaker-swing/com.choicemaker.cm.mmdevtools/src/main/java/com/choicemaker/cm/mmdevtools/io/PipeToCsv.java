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
import java.io.PrintWriter;

/**
 *
 * @author    Adam Winkel
 * @version   
 */
public class PipeToCsv {

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: PipeToCsv inFile outFile");
			System.exit(1);
		}
				
		FileReader fr = new FileReader(args[0]);
		BufferedReader reader = new BufferedReader(fr);
		
		FileWriter fw = new FileWriter(args[1]);
		PrintWriter writer = new PrintWriter(fw);
		
		int lineNum = 0;
		while (reader.ready()) {
			String line = reader.readLine();
			lineNum++;
			
			String out = transform(line);
			writer.println(out);
			
			if (lineNum % 100000 == 0) {
				System.out.println(lineNum);
			}
		}
		
		writer.close();
		fw.close();
		
		reader.close();
		fr.close();
	}

	private static String transform(String s) {
		int len = s.length();
		StringBuffer buff = new StringBuffer(len);

		StringBuffer field = new StringBuffer();
		boolean useQuotes = false;
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c == '|') {
				if (useQuotes) {
					buff.append('\"');
					buff.append(field);
					buff.append('\"');
				} else {
					buff.append(field);
				}
				buff.append(',');

				field.setLength(0);
				useQuotes = false;
			} else {
				if (c == ',') {
					useQuotes = true;
					field.append(c);
				} else if (c == '\"') {
					useQuotes = true;
					field.append("\"\"");
				} else {
					field.append(c);
				}
			}
		}
		
		// flush the last field.
		if (useQuotes) {
			buff.append('\"');
			buff.append(field);
			buff.append('\"');
		} else {
			buff.append(field);
		}
		
		return buff.toString();
	}

}
