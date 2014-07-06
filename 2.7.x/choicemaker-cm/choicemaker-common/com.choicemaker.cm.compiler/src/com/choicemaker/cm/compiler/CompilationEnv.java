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

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.compiler.CompilationArguments;
import com.choicemaker.cm.core.util.MessageUtil;

/**
 * Objects of that class represent a global compilation context; this
 * includes global settings, error handling etc.
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public class CompilationEnv {

	/** the tabulator size in the source code
	 */
	public int tabsize = 8;

	/** the source code encoding; if sourceEncoding == null, then the default
	 *  encoding is used
	 */
	public String sourceEncoding;

	/** the target code encoding; if sourceEncoding == null, then the default
	 *  encoding is used
	 */
	public String targetEncoding;

	/** the class path of this compilation environment
	 */
	public Classpath classPath;

	/** verbose mode?
	 */
	public boolean verbose;

	/** issue warnings?
	 */
	public boolean nowarn;

	/** issue debug messages?
	 */
	public boolean debug;

	/** prompt on error?
	 */
	public boolean prompt;

	/** the number of errors found so far
	 */
	public int errors;

	/** the number of warnings issued so far
	 */
	public int warnings;

	/** the class repository
	 */
	public ClassRepository repository;
	
	/**
	 *  packages  of source clue sets. will be created.
	 */
	public Set sourcePackages;
	
	private Writer w;

	public CompilationEnv(CompilationArguments arguments, String defaultPath, Writer w) {
		sourcePackages = new HashSet();
		this.w = w;
		nowarn = arguments.optionSet(CompilationArguments.NOWARN);
		verbose = arguments.optionSet(CompilationArguments.VERBOSE);
		debug = arguments.optionSet(CompilationArguments.DEBUG);
		prompt = arguments.optionSet(CompilationArguments.PROMPT);
		sourceEncoding = arguments.argumentVal(CompilationArguments.ENCODING);
		targetEncoding = arguments.argumentVal(CompilationArguments.TARGET_ENCODING);
		try {
			tabsize = Integer.parseInt(arguments.argumentVal(CompilationArguments.TABSIZE));
		} catch (NumberFormatException e) {
			error("illegal tabsize " + arguments.argumentVal(CompilationArguments.TABSIZE));
		}
		String primaryPath = arguments.argumentVal(CompilationArguments.CLASSPATH);
		classPath = new Classpath(defaultPath, primaryPath);
		repository = new ClassRepository(this);
	}

	/** issue a global error message and exit
	 */
	public void exit(String message) {
		error(message);
		System.exit(-1);
	}

	/** issue a global error message
	 */
	public void error(String message) {
		try {
			w.write("error: " + message + Constants.LINE_SEPARATOR);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		errors++;
	}

	/** issue an error for a specific source code file
	 */
	public void error(Sourcecode source, String message) {
		source.printMessage(Location.NOPOS, message);
		errors++;
	}

	/** issue an error for a specific line of a specific source code file
	 */
	public void error(Sourcecode source, int pos, String message) {
		if (source.printMessageIfNew(pos, message))
			errors++;
	}

	/** issue a global warning
	 */
	public void warning(String message) {
		if (!nowarn) {
			try {
				w.write("warning: " + message + Constants.LINE_SEPARATOR);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			warnings++;
		}
	}

	/** issue a warning for a specific source code file
	 */
	public void warning(Sourcecode source, String message) {
		if (!nowarn) {
			source.printMessage(Location.NOPOS, "(warning) " + message);
			warnings++;
		}
	}

	/** issue a warning for a specific line of a specific source code file
	 */
	public void warning(Sourcecode source, int pos, String message) {
		if (!nowarn) {
			source.printMessageIfNew(pos, "(warning) " + message);
			warnings++;
		}
	}

	/** issue a message if verbose is switched on
	 */
	public void message(String message) {
		if (verbose)
			try {
				w.write(message + Constants.LINE_SEPARATOR);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
	}

	/** issue a debug message
	 */
	public void debug(String message) {
		if (debug) {
			try {
				w.write("debug: " + message + Constants.LINE_SEPARATOR);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/** print out a conclusion message for the compilation process
	 */
	public boolean conclusion() {
		if ((errors + warnings) > 0) {
			System.out.println(
				MessageUtil.m.formatMessage("compiler.unit.conclusion", new Integer(errors), new Integer(warnings))
					+ Constants.LINE_SEPARATOR);
			if (errors > 0) {
				return false;
			}
		}
		return true;
	}
}
