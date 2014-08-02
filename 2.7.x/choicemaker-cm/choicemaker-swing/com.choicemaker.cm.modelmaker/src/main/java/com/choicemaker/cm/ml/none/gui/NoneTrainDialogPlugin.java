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
package com.choicemaker.cm.ml.none.gui;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.dialogs.TrainDialog;
import com.choicemaker.cm.modelmaker.gui.hooks.TrainDialogPlugin;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:08 $
 */
public class NoneTrainDialogPlugin extends TrainDialogPlugin {
	private static final long serialVersionUID = 1L;

	public NoneTrainDialogPlugin() {
		setBorder(BorderFactory.createTitledBorder(ChoiceMakerCoreMessages.m.formatMessage("ml.none.train.label")));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel(ChoiceMakerCoreMessages.m.formatMessage("ml.none.train.notpossible")));
	}


	/**
	 * @see java.awt.Component#isValid()
	 */
	public boolean isParametersValid() {
		return false;
	}

	/**
	 * @see com.choicemaker.cm.train.gui.hooks.TrainDialogPlugin#set()
	 */
	public void set() {
	}
	
	public void setTrainDialog(TrainDialog trainDialog) {
	}
}
