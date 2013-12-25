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
package com.choicemaker.cm.compiler.impl;

import com.choicemaker.cm.compiler.CompilationEnv;
import com.choicemaker.cm.compiler.Sourcecode;
import com.choicemaker.cm.compiler.backend.TargetPrinter25;
import com.choicemaker.cm.compiler.backend.Translator25a;
import com.choicemaker.cm.core.compiler.CompilerException;

/**
 * @author rphall
 *
 */
public class CompilationUnit25a extends CompilationUnit {

	/**
	 * @param env
	 * @param source
	 */
	public CompilationUnit25a(CompilationEnv env, Sourcecode source) {
		super(env, source);
	}

	public void codeGeneration() throws CompilerException {
		if (getErrors() == 0 && getCompilationEnv().errors == 0) {
			try {
				new Translator25a(
					new TargetPrinter25(this.getClueSetFileName()),
					this)
					.translate();
			} catch (Exception x) {
				throw new CompilerException(x);
			}
		}
	}

}
