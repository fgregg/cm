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
package com.choicemaker.cm.core.base;

import java.util.Collection;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.xmlconf.MlModelConf;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/24 18:24:05 $
 */
public class DoNothingMachineLearning implements MachineLearner {

	/**
	 * @see com.choicemaker.cm.core.MachineLearner#getEvaluator()
	 */
	public Evaluator getEvaluator() {
		return null;
	}

	/**
	 * @see com.choicemaker.cm.core.MachineLearner#setProbabilityModel(com.choicemaker.cm.core.base.ProbabilityModel)
	 */
	public void setProbabilityModel(ImmutableProbabilityModel model) {
	}

	/**
	 * @see com.choicemaker.cm.core.MachineLearner#changedAccessor(com.choicemaker.cm.core.base.Accessor, com.choicemaker.cm.core.base.Accessor, int)
	 */
	public void changedAccessor(Accessor oldAccessor, Accessor newAccessor, int[] oldClueNums) {
	}

	/**
	 * @see com.choicemaker.cm.core.MachineLearner#train(java.util.Collection, double)
	 */
	public Object train(Collection src, double[] firingPercentages) {
		return null;
	}

	/**
	 * @see com.choicemaker.cm.core.MachineLearner#getModelConf()
	 */
	public MlModelConf getModelConf() {
		return DoNothingMachineLearningPersistance.instance;
	}

	/**
	 * @see com.choicemaker.cm.core.MachineLearner#canEvaluate()
	 */
	public boolean canEvaluate() {
		return false;
	}

	/**
	 * @see com.choicemaker.cm.core.MachineLearner#canTrain()
	 */
	public boolean canTrain() {
		return false;
	}

	/**
	 * @see com.choicemaker.cm.core.MachineLearner#canUse(com.choicemaker.cm.core.base.ClueSet)
	 */
	public boolean canUse(ClueSet clueset) {
		return true;
	}

	public boolean isRegression() {
		return true;
	}
	
	public String getModelInfo() {
		return null;
	}
}
