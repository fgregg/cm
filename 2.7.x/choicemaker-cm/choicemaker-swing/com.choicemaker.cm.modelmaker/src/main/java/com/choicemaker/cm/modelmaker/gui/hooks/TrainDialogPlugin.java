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
package com.choicemaker.cm.modelmaker.gui.hooks;

import javax.swing.JPanel;

import com.choicemaker.cm.modelmaker.gui.dialogs.TrainDialog;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:08 $
 */
public abstract class TrainDialogPlugin extends JPanel {
	private static final long serialVersionUID = 1L;

	public abstract boolean isParametersValid();

	public abstract void set();
	
	public abstract void setTrainDialog(TrainDialog d);
}
