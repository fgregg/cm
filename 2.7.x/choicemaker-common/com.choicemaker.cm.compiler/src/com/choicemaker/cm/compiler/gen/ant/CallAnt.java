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
package com.choicemaker.cm.compiler.gen.ant;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.compiler.impl.CMCompiler;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:05:46 $
 */
public class CallAnt {

	/**
	 * @deprecated
	 * @return
	 */
	public static ClassLoader getJavacClassLoader() {
		/*
		StringBuffer sb = new StringBuffer();
		File f = new File(System.getProperty("java.home"));
		String temp = f.getAbsoluteFile().getParent();
		sb.append(temp).append(File.separator).append("lib").append(File.separator).append("tools.jar");
		String tools = sb.toString();
		try {
			return new URLClassLoader(new URL[] {new File(tools).toURL() },	Compiler.class.getClassLoader());
		} catch (MalformedURLException ex) {
			return ICompiler.class.getClassLoader();
		}
		*/
		throw new RuntimeException("This method should no longer be used.");
	}

	public static boolean callAnt(String arg, PrintStream ps) {
		if (System.getProperty("ant.home") == null) {
			System.setProperty("ant.home", "c:\\%ProgramFiles%\\ant");
		}
		String[] args = argToArgs(arg);
		PrintStream out = System.out;
		PrintStream err = System.err;
		System.setErr(ps);
		System.setOut(ps);
		boolean success = Main.start(args, null, CMCompiler.getJavacClassLoader());
		System.setErr(err);
		System.setOut(out);
		return success;
	}

	private static String[] argToArgs(String arg) {
		boolean inQuotes = false;
		List l = new ArrayList();
		StringBuffer b = new StringBuffer();
		int i = 0;
		int len = arg.length();
		while (i < len && Character.isWhitespace(arg.charAt(i))) {
			++i;
		}
		while (i < len) {
			char c = arg.charAt(i);
			if (!inQuotes && Character.isWhitespace(c)) {
				l.add(b.toString());
				b = new StringBuffer();
			} else if (c == '"') {
				if (inQuotes) {
					l.add(b.toString());
					b = new StringBuffer();
				} else {
					inQuotes = true;
				}
			} else if (c == '\\') {
				if (i + 1 < len) {
					b.append(arg.charAt(++i));
				}
			} else {
				b.append(c);
			}
			++i;
		}
		if (b.length() != 0) {
			l.add(b.toString());
		}
		return (String[]) l.toArray(new String[l.size()]);
	}
}
