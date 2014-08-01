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
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ProbabilityModelSpecification;

/**
 * Interface used for testing compiler implementations.
 * @author rphall
 */
public interface ICompiler {

	/** Integer returned by the "Modern" jdk1.3 compiler to indicate success. */
	int MODERN_COMPILER_SUCCESS = 0;

	/** Returns the number of ClueMaker compilation errors */
	int generateJavaCode(CompilationArguments arguments, Writer statusOutput)
			throws CompilerException;

	String compile(CompilationArguments arguments, Writer statusOutput)
		throws CompilerException;

	boolean compile(IProbabilityModel model, Writer statusOutput)
		throws CompilerException;

	ImmutableProbabilityModel compile(ProbabilityModelSpecification model, Writer statusOutput)
			throws CompilerException;

	Properties getFeatures();

}

