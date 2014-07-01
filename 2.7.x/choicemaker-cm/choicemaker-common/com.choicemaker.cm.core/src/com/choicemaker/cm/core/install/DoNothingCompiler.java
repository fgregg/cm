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
package com.choicemaker.cm.core.install;

import java.io.Writer;
import java.util.Properties;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ProbabilityModelSpecification;
import com.choicemaker.cm.core.compiler.CompilationArguments;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.compiler.ICompiler;

/**
 * A placeholder for objects that require, but don't use, a compiler.
 * (There's a lot of these objects in the J2EE projects.) Both
 * <code>compile</code> methods throw a RuntimeException ("not implemented").
 *
 * @author rphall
 */
final class DoNothingCompiler implements ICompiler {

	static final ICompiler instance = new DoNothingCompiler();

	private DoNothingCompiler() {}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.compiler.Compiler#compile(com.choicemaker.cm.core.compiler.DefaultCompilationArguments, java.io.Writer)
	 */
	public String compile(CompilationArguments arguments, Writer statusOutput)
		throws CompilerException {
		throw new CompilerException("Compiler method 'compile' not implemented");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.compiler.Compiler#compile(com.choicemaker.cm.core.base.ImmutableProbabilityModel, java.io.Writer)
	 */
	public ImmutableProbabilityModel compile(ProbabilityModelSpecification model, Writer statusOutput)
		throws CompilerException {
			throw new CompilerException("Compiler method 'compile' not implemented");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.compiler.Compiler#getFeatures()
	 */
	public Properties getFeatures() {
		return new Properties();
	}

	public boolean compile(IProbabilityModel model, Writer statusOutput)
			throws CompilerException {
		// TODO Auto-generated method stub
		return false;
	}

}
