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
package com.choicemaker.cm.core.compiler;

import com.choicemaker.util.Arguments;

/**
 * Command line arguments for compiler.
 * 
 * @author Matthias Zenger
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:06 $
 */
public class CompilationArguments extends Arguments {

	public static final String VERBOSE = "-verbose";
	public static final String NOWARN = "-nowarn";
	public static final String DEBUG = "-debug";
	public static final String PROMPT = "-prompt";
	public static final String TABSIZE = "-tabsize";
	public static final String ENCODING = "-encoding";
	public static final String TARGET_ENCODING = "-targetencoding";
	public static final String CLASSPATH = "-classpath";
	public static final String OUTPUT_DIRECTORY = "-d";
	public static final String CHOICEMAKER_CONFIGURATION_FILE = "-config";
	public static final String LOGGING_CONFIGURATION_NAME = "-log";

	public static final int DEFAULT_TABSIZE = 8;
	public static final String DEFAULT_OUTPUT_DIRECTORY = ".";

	public CompilationArguments() {
		addOption(VERBOSE);
		addOption(NOWARN);
		addOption(DEBUG);
		addOption(PROMPT);
		addArgument(TABSIZE, DEFAULT_TABSIZE);
		addArgument(ENCODING);
		addArgument(TARGET_ENCODING);
		addArgument(CompilationArguments.CLASSPATH);
		addArgument(CompilationArguments.OUTPUT_DIRECTORY,
				DEFAULT_OUTPUT_DIRECTORY);
		addArgument(CHOICEMAKER_CONFIGURATION_FILE);
		addArgument(LOGGING_CONFIGURATION_NAME);
	}
}
