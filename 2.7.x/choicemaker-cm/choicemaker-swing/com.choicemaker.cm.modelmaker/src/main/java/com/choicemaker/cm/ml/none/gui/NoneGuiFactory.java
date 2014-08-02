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

import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.base.DoNothingMachineLearning;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.hooks.TrainDialogPlugin;
import com.choicemaker.cm.modelmaker.gui.ml.MlGuiFactory;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:08 $
 */
public class NoneGuiFactory extends MlGuiFactory {
	/**
	 * @see com.choicemaker.cm.ml.gui.MlGuiFactory#getTrainDialogPlugin(com.choicemaker.cm.core.MachineLearner)
	 */
	public TrainDialogPlugin getTrainDialogPlugin(MachineLearner learner) {
		return new NoneTrainDialogPlugin();
	}
	/**
	 * @see com.choicemaker.cm.core.base.DynamicDispatchHandler#getHandler()
	 */
	public Object getHandler() {
		return this;
	}
	/**
	 * @see com.choicemaker.cm.core.base.DynamicDispatchHandler#getHandledType()
	 */
	public Class getHandledType() {
		return DoNothingMachineLearning.class;
	}
	
	public String toString() {
		return ChoiceMakerCoreMessages.m.formatMessage("ml.none.label");
	}
	/**
	 * @see com.choicemaker.cm.ml.gui.MlGuiFactory#getMlInstance()
	 */
	public MachineLearner getMlInstance() {
		return new DoNothingMachineLearning();
	}
}
