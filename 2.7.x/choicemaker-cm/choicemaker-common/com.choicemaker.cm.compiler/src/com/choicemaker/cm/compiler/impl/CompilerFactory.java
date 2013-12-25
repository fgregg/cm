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

import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.compiler.ICompilerFactory;
import com.choicemaker.cm.core.compiler.UnavailableCompilerFeaturesException;

/**
 * This object returns the appropiate compiler to the invoker.
 * 
 * @author pcheung
 *
 */
public class CompilerFactory implements ICompilerFactory {
	
	public static final String C25 = "25";
	public static final String C24 = "24";
	public static final String C25a = "25a";
	public static final String C25b = "25b";
	public static final String C26 = "26";
	
	private static CompilerFactory factory = null;
	
	
	private CompilerFactory () {
	}
	
	
	public static CompilerFactory getInstance () {
		if (factory == null) factory = new CompilerFactory ();
		return factory;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.compiler.ICompilerFactory#getCompiler(java.util.Properties)
	 */
	public ICompiler getCompiler(Properties features) throws UnavailableCompilerFeaturesException {
		// TODO add more checks to features
		
		String name = features.getProperty("Name");
		
		return getCompiler (name);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.compiler.ICompilerFactory#getCompiler(java.lang.String)
	 */
	public ICompiler getCompiler(String name) throws UnavailableCompilerFeaturesException {
		
		if (name == null || name.equals("")) 
			throw new UnavailableCompilerFeaturesException ("Must specify a compiler name.");
		
		if (name.equals(C24)) return new Compiler24();
		else if (name.equals(C25)) return new Compiler25();
		else if (name.equals(C25a)) return new Compiler25a();
		else if (name.equals(C25b)) return new Compiler25b();
		else if (name.equals(C26)) return new Compiler26();
		else throw new UnavailableCompilerFeaturesException ("Compiler " + name + " not found.");

	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.compiler.ICompilerFactory#getDefaultCompiler()
	 */
	public ICompiler getDefaultCompiler() {
		return new Compiler26();
	}

}
