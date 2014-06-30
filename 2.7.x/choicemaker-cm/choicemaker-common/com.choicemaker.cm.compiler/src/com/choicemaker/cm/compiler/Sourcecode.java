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
package com.choicemaker.cm.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;

import com.choicemaker.cm.core.base.Constants;

/**
 * This class represents a single source code file
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
public final class Sourcecode implements Characters {

	/** the filename
	 */
	public String filename;
	public String shortname;

	/** a log of all issued errors; this is used to avoid printing an
	 *  error message or related error messages more than once
	 */
	protected HashMap issued = new HashMap();

	/** the buffer containing the file that is currently translated
	 */
	protected char[] buf;

	protected Writer w;

	/** the last error position
	 */
	protected int newPos = 0;
	protected int lastLine = 0;
	protected int lastPos = 0;
	protected int lineEnd = 0;

	/** create unknown SourceCode object
	 */
	public Sourcecode() {
		this.filename = "(sourcefile not available)";
		this.shortname = "(unavailable)";
		buf = new char[] { FE };
	}

	/** create SourceCode object for the default encoding
	 */
	public Sourcecode(String filename) throws IOException {
		this(filename, null, null);
	}

	/** create SourceCode object for a given encoding
	 */
	public Sourcecode(String filename, String encoding, Writer w) throws IOException {
		this.w = w;
		File f = new File(filename).getAbsoluteFile();
		this.filename = filename;
		this.shortname = f.getName();
		InputStreamReader in =
			(encoding == null)
				? new InputStreamReader(new FileInputStream(new File(filename).getAbsoluteFile()))
				: new InputStreamReader(new FileInputStream(new File(filename).getAbsoluteFile()), encoding);
		buf = new char[(int) (f.length() + 1)];
		buf[in.read(buf)] = FE;
		buf = Names.toInternal(buf, 0, buf.length);
		in.close();
	}

	/** return filename as a string
	 */
	public String toString() {
		return filename;
	}

	public String getShortName() {
		return new File(filename).getName();
	}

	/** return the source buffer of this file
	 */
	public char[] getBuffer() {
		return buf;
	}

	/** number of logged entries
	 */
	public int logged() {
		return issued.size();
	}

	/** is there already an entry at position 'pos'
	 */
	public boolean isLogged(int pos) {
		return (issued.get(new Integer(pos)) != null);
	}

	/** enter entry into log table
	 */
	public void log(int pos, String message) {
		issued.put(new Integer(pos), message);
	}

	/** print message, if there is no entry for this position
	 *  and enter message into log
	 */
	public boolean printMessageIfNew(int pos, String message) {
		if (pos == Location.NOPOS || !isLogged(pos)) {
			log(pos, message);
			printMessage(pos, message);
			return true;
		}
		return false;
	}

	/** print message and line in sourcefile
	 */
	public void printMessage(int pos, String message) {
		try {
			if (pos == Location.NOPOS) {
				w.write(filename + ": " + message + Constants.LINE_SEPARATOR);
			} else {
				int line = Location.line(pos);
				int col = Location.column(pos);
				w.write(filename + ":" + line + ": ");
				w.write(message + Constants.LINE_SEPARATOR);
				printLine(line, col);
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/** print source line
	 */
	public void printLine(int line, int col) {
		int pos = 0;
		if (lastLine > line)
			lastLine = 0;
		else
			pos = newPos;
		while ((pos < buf.length) && (lastLine < line)) {
			lastPos = pos;
			while ((pos < buf.length) && (buf[pos] != CR) && (buf[pos] != LF) && (buf[pos] != FF))
				pos++;
			lineEnd = pos;
			if (pos < buf.length)
				pos++;
			if ((pos < buf.length) && (buf[pos - 1] == CR) && (buf[pos] == LF))
				pos++;
			lastLine++;
		}
		newPos = pos;
		try {
			w.write(new String(buf, lastPos, lineEnd - lastPos) + Constants.LINE_SEPARATOR);
			char[] ptr = new char[col];
			for (int i = col - 2; i >= 0; i--)
				ptr[i] = ' ';
			ptr[col - 1] = '^';
			w.write(new String(ptr) + Constants.LINE_SEPARATOR);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
}
