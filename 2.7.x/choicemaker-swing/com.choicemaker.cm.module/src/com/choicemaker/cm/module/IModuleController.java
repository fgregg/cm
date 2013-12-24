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
package com.choicemaker.cm.module;


/**
 * Defines access to user interface components of a module.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:56 $
 */
public interface IModuleController extends IModule {
	
	// -- User interaction
	
	/** Tagging interface for the user interface to module operations */
	interface IUserInterface {}
	
	/** Tagging interface for module status messages */
	interface IStatusModel {}
	
	// -- Controller access

	/** Returns the interface through which the user interacts with the module */
	IUserInterface getUserInterface();

	/** The means by which operational status is reported to the user */
	IStatusModel getStatusModel();
	
}

