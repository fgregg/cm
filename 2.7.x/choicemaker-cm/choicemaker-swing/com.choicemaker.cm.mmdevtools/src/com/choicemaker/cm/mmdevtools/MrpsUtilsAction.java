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
package com.choicemaker.cm.mmdevtools;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

import com.choicemaker.cm.mmdevtools.gui.MrpsFlattenDialog;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.menus.ToolsMenu.ToolAction;

/**
 * @author Adam Winkel
 */
public class MrpsUtilsAction extends AbstractAction {

	public MrpsUtilsAction() {
		super("Marked Record Pair Source Utils");
	}

	public void actionPerformed(ActionEvent e) { }

	public static class MrpsFlattenAction extends ToolAction {
		public MrpsFlattenAction() {
			super("Flatten...");
			setEnabled(false);
		}
		public void setModelMaker(final ModelMaker m) {
			m.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					setEnabled(m.haveSourceList());
				}
			});
			super.setModelMaker(m);
		}
		public void actionPerformed(ActionEvent e) {
			MrpsFlattenDialog.showMrpsFlattenDialog(modelMaker);
		}
	}
	
}
