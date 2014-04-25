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

/**
 * @author rphall
 * @version $Revision: 1.2 $  $Date: 2010/03/24 16:59:02 $
 */
public class UnavailableCompilerFeaturesException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnavailableCompilerFeaturesException() {
		super();
	}

	public UnavailableCompilerFeaturesException(String msg) {
		super(msg);
	}

	public UnavailableCompilerFeaturesException(String msg, Throwable rootCause) {
		super(msg, rootCause);
	}

	public UnavailableCompilerFeaturesException(Throwable rootCause) {
		super(rootCause);
	}

} // UnavailableCompilerFeaturesException


