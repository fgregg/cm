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
package com.choicemaker.cm.module.swing;

import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.Icon;

import com.choicemaker.cm.module.IModule;
import com.choicemaker.cm.module.IModule.IOperationModel;
import com.choicemaker.cm.module.IModuleController;


public abstract class ModuleAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private IModuleController module;
	private IOperationModel operation; // cached

	public ModuleAction() {
	}

	public ModuleAction(String name) {
		super(name);
	}

	public ModuleAction(String name, Icon icon) {
		super(name, icon);
	}

	/**
	 * Invoked after the method for {@link #setModule(IModule) setModule}.
	 * Subclasses should override this method to set up the appearance of the
	 * buttons used to invoke them.
	 */	
	public abstract void initializeButtonModel(ButtonModel model);

	public final void setModule(IModuleController module) {
		if (module == null) {
			throw new IllegalArgumentException("null module controller");
		}
		this.module = module;
		this.addListeners();
	}

	/**
	 * Invoked after a module has been set for this action.
	 * Subclasses should override this method to set up
	 * appropriates listeners to the module (which should
	 * be accessed through <code>getModule()</code>).
	 */	
	protected abstract void addListeners();

	public final IModuleController getModuleController() {
		if (this.module == null) {
			throw new IllegalStateException("module is not set");
		}
		return this.module;
	}

	public final IOperationModel getOperationModel() {
		if (this.operation == null) {
			this.operation = getModuleController().getOperationModel();
		}
		if (this.operation == null) {
			throw new IllegalStateException("module controller is not set");
		}
		return this.operation;
	}

}

