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
 * A placeholder for objects that require, but don't use, a compiler.
 * (There's a lot of these objects in the J2EE projects.) Both
 * <code>compile</code> methods throw a RuntimeException ("not implemented").
 * 
 * @author rphall
 */
public final class DoNothingCompiler implements ICompiler {
	
	public static final ICompiler instance = new DoNothingCompiler();
	
	private DoNothingCompiler() {}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.compiler.ICompiler#compile(com.choicemaker.cm.core.base.compiler.CompilationArguments, java.io.Writer)
	 */
	public String compile(CompilationArguments arguments, Writer statusOutput)
		throws CompilerException {
		throw new RuntimeException("Compiler method 'compile' not implemented");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.compiler.ICompiler#compile(com.choicemaker.cm.core.base.ProbabilityModel, java.io.Writer)
	 */
	public boolean compile(IProbabilityModel model, Writer statusOutput)
		throws CompilerException {
			throw new RuntimeException("Compiler method 'compile' not implemented");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.compiler.ICompiler#getFeatures()
	 */
	public Properties getFeatures() {
		return new Properties();
	}

}
