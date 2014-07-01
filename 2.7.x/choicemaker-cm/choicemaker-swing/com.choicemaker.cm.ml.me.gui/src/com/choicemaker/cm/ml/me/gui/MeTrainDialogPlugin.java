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
package com.choicemaker.cm.ml.me.gui;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.ml.me.base.MaximumEntropy;
import com.choicemaker.cm.modelmaker.gui.dialogs.TrainDialog;
import com.choicemaker.cm.modelmaker.gui.hooks.TrainDialogPlugin;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:06 $
 */
public class MeTrainDialogPlugin extends TrainDialogPlugin {
	private static final long serialVersionUID = 1L;
	private JLabel trainingIterationsLabel;
	private JTextField trainingIterations;
	private MaximumEntropy me;
	private TrainDialog trainDialog;

	public MeTrainDialogPlugin(MaximumEntropy me) {
		this.me = me;
		setBorder(BorderFactory.createTitledBorder(MessageUtil.m.formatMessage("ml.me.train.label")));
		trainingIterationsLabel = new JLabel(MessageUtil.m.formatMessage("ml.me.train.iterations"));
		trainingIterations = new JTextField(5);
		trainingIterations.setText(String.valueOf(me.getTrainingIterations()));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(trainingIterationsLabel);
		add(trainingIterations);
	}
	
	public void setTrainDialog(TrainDialog trainDialog) {
		this.trainDialog = trainDialog;
		EnablednessGuard dl = new EnablednessGuard(trainDialog);
		trainingIterations.getDocument().addDocumentListener(dl);
	}
	
	public boolean isParametersValid() {
		try {
			return Integer.parseInt(trainingIterations.getText()) > 0;
		} catch (NumberFormatException ex) {
			// ignore
		}
		return false;
	}

	public void set() {
		me.setTrainingIterations(Integer.parseInt(trainingIterations.getText()));
	}
}
