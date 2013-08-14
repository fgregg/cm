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
 * Defines access to the functional domain components of a module.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:56 $
 */
public interface IModule {
	
	// --- Controller capabilities

	/** Tagging interface for module operations */
	interface IOperationModel {}
	
	/** Tagging interface for module state */
	interface IStateModel {}
	
	/** Tagging interface for module events */
	interface IEventModel {}
	
	/** Tagging interface for module configuration */
	interface IConfigurationModel {}
	
	// -- Module access

	/** Returns the operations that are available through the module. */
	IOperationModel getOperationModel();
	
	/** Returns the events that the module operations will produce */	
	IEventModel getEventModel();
	
	/** Returns the states through which the module will transition in response to module events */
	IStateModel getStateModel();
	
	/** Reports, and possibly provides means of changing, the module configuration */
	IConfigurationModel getConfigurationModel();
	
	/** Support for retrieving user messages from module-specific resource bundles */
	public IMessageSupport getMessageSupport();

}

