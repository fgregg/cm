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
package com.choicemaker.cm.transitivity.core;

/**
 * This is the exception thrown by the Transitivity Engine.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies Inc.
 */
public class TransitivityException extends Exception {

	public TransitivityException () {
		super ();
	}

	public TransitivityException (String message) {
		super (message);
	}

}