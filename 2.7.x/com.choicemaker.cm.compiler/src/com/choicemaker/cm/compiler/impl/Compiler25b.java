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

import java.util.Properties;

import com.choicemaker.cm.compiler.CompilationEnv;
import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Sourcecode;
import com.choicemaker.cm.core.compiler.ICompilerFeatureNames;

/**
 * ClueMaker compiler.
 *
 * @deprecated Use a class that extends CMCompiler, like Compiler26, class instead.
 * @author rphall
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:12:40 $
 */
public class Compiler25b extends Compiler {

	public final static String VERSION_VALUE = "2.5.2";

	public final static String OPTIMIZING_VALUE = "true";

	protected ICompilationUnit getCompilationUnit(
		CompilationEnv env,
		Sourcecode source) {
		// Preconditions
		if (env == null) {
			throw new IllegalArgumentException("null compilation environment");
		}
		if (source == null) {
			throw new IllegalArgumentException("null source code");
		}

		return new CompilationUnit25b(env, source);
	} // getCompilationUnit(CompilationEnv,Sourcecode)

	public Properties getFeatures() {
		Properties retVal = new Properties();
		retVal.setProperty(ICompilerFeatureNames.VERSION,VERSION_VALUE);
		retVal.setProperty(ICompilerFeatureNames.OPTIMIZING,OPTIMIZING_VALUE);
		return retVal;
	}

} // Compiler25a

