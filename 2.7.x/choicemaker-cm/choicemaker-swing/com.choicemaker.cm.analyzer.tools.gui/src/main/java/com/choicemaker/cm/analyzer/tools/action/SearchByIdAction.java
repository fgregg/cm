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
package com.choicemaker.cm.analyzer.tools.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.choicemaker.cm.analyzer.tools.gui.SearchByIdDialog;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.menus.ToolsMenu.ToolAction;

/**
 * @author ajwinkel
 *
 */
public class SearchByIdAction extends ToolAction {

	private static final long serialVersionUID = 1L;

	public SearchByIdAction() {
		super("Search by ID...");
	}

	public void actionPerformed(ActionEvent e) {
		SearchByIdDialog.showSearchByIdDialog(modelMaker);
	}

	public void updateEnabled() {
		setEnabled(modelMaker.haveProbabilityModel() && modelMaker.isEvaluated());
	}
	
	public void setModelMaker(ModelMaker mm) {
		super.setModelMaker(mm);
		mm.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				updateEnabled();
			}
		});
		updateEnabled();
	}

}
