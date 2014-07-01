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

import java.io.Writer;
import java.util.Properties;

import com.choicemaker.cm.core.IProbabilityModel;

/**
 * Interface used for testing compiler implementations.
 * @author rphall
 */
public interface ICompiler {

	/** Integer returned by the "Modern" jdk1.3 compiler to indicate success. */
	public static final int MODERN_COMPILER_SUCCESS = 0;

	public abstract String compile(CompilationArguments arguments, Writer statusOutput)
		throws CompilerException;

	public abstract boolean compile(IProbabilityModel model, Writer statusOutput)
		throws CompilerException;
		
	public abstract Properties getFeatures();

}

