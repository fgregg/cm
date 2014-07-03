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

import com.choicemaker.cm.mmdevtools.gui.ParserTestDialog;
import com.choicemaker.cm.modelmaker.gui.menus.ToolsMenu.ToolAction;

/**
 * @author ajwinkel
 *
 */
public class ParserUtilsAction extends ToolAction {

	private static final long serialVersionUID = 1L;

	public ParserUtilsAction() {
		super("Parser Utils");
	}

	public void actionPerformed(ActionEvent e) {
		// do nothing		
	}
	
	public static class ParserTestAction extends ToolAction {

		private static final long serialVersionUID = 1L;

		public ParserTestAction() {
			super("Parser Test...");
		}
	
		public void actionPerformed(ActionEvent e) {
			new ParserTestDialog(modelMaker).setVisible(true);
		}

	}


}
