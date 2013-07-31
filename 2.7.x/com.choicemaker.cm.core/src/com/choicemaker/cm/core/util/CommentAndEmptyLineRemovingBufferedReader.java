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
package com.choicemaker.cm.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author ajwinkel
 *
 */
public class CommentAndEmptyLineRemovingBufferedReader {

	private BufferedReader reader;

	public CommentAndEmptyLineRemovingBufferedReader(String fileName) throws IOException {
		this(new File(fileName));
	}

	public CommentAndEmptyLineRemovingBufferedReader(File f) throws IOException {
		this(new FileReader(f));
	}

	public CommentAndEmptyLineRemovingBufferedReader(Reader in) {
		if (in instanceof BufferedReader) {
			this.reader = (BufferedReader)in;
		} else {
			this.reader = new BufferedReader(in);
		}
	}
	
	public String readLine() throws IOException {
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				return null;
			} else {
				int commentIndex = line.indexOf("//");
				if (commentIndex >= 0) {
					line = line.substring(0, commentIndex);
				}
				
				if (line.length() > 0) {
					return line;
				}
			}
		}		
	}
	
	public void close() throws IOException {
		reader.close();
		reader = null;
	}

}
