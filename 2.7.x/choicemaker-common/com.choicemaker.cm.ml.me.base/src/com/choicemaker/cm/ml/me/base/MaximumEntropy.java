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
package com.choicemaker.cm.ml.me.base;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.ClueSetType;
import com.choicemaker.cm.core.Evaluator;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ml.MachineLearner;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.xmlconf.MlModelConf;
import com.choicemaker.cm.ml.me.xmlconf.MeModelConf;

/**
 * .
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 23:18:01 $
 */
public class MaximumEntropy implements MachineLearner {

	private static Logger logger = Logger.getLogger(MaximumEntropy.class);

	private float[] weights;
	private int trainingIterations = 4000;
	private IProbabilityModel model;

	/**
	 * @see com.choicemaker.cm.ml.MachineLearner#getEvaluator()
	 */
	public Evaluator getEvaluator() {
		return new MeEvaluator(model, weights);
	}

	/**
	 * @see com.choicemaker.cm.ml.MachineLearner#setProbabilityModel(com.choicemaker.cm.core.ProbabilityModel)
	 */
	public void setProbabilityModel(IProbabilityModel model) {
		this.model = model;
	}

	public void changedAccessor(Accessor oldAccessor, Accessor newAccessor, int[] oldClueNums) {
		int size = oldClueNums.length;
		float[] newWeights = new float[size];
		for (int i = 0; i < size; ++i) {
			int clueNum = oldClueNums[i];
			if (clueNum != -1 && weights != null) {
				newWeights[i] = weights[clueNum];
			} else {
				newWeights[i] = 1;
			}
		}
		weights = newWeights;
	}

	/**
	 * Returns the weights.
	 *
	 * @return   The weights.
	 */
	public float[] getWeights() {
		return weights;
	}

	/**
	 * Sets the weights.
	 *
	 * @param   weights  The weights.
	 */
	public void setWeights(float[] weights) throws IllegalArgumentException {
		if (model != null) {
			ClueSet clueSet = model.getClueSet();
			if (clueSet != null) {
				if (weights == null || weights.length != clueSet.size()) {
					throw new IllegalArgumentException("Illegal weights.");
				}
			} else if (weights != null) {
				throw new IllegalArgumentException("Illegal weights.");
			}
		}
		float[] oldWeights = this.weights;
		this.weights = weights;
		if (model != null) {
			model.machineLearnerChanged(oldWeights, weights);
		}
	}

	public void resetWeights() {
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 1.0f;
		}
		model.machineLearnerChanged(null, weights);
	}

	/**
	 * Get the value of trainingIterations.
	 * @return value of trainingIterations.
	 */
	public int getTrainingIterations() {
		return trainingIterations;
	}

	/**
	 * Set the value of trainingIterations.
	 * @param v  Value to assign to trainingIterations.
	 */
	public void setTrainingIterations(int v) {
		this.trainingIterations = v;
	}

	/**
	 * @see com.choicemaker.cm.ml.MachineLearner#train(java.util.Collection, double)
	 */
	public Object train(Collection src, double[] firingPercentages) {
		try {
			MeEstimator estimator = new MeEstimator(model, src, firingPercentages, trainingIterations);
			estimator.run();
			setWeights(estimator.getWeights());
			return estimator.getWarning();
		} catch (Exception ex) {
			logger.error(new LoggingObject("CM-010001"), ex);
			return null;
		}
	}

	/**
	 * @see com.choicemaker.cm.ml.MachineLearner#getModelConf()
	 */
	public MlModelConf getModelConf() {
		return new MeModelConf(this);
	}

	/**
	 * @see com.choicemaker.cm.ml.MachineLearner#canEvaluate()
	 */
	public boolean canEvaluate() {
		return true;
	}

	/**
	 * @see com.choicemaker.cm.ml.MachineLearner#canTrain()
	 */
	public boolean canTrain() {
		return true;
	}

	/**
	 * @see com.choicemaker.cm.ml.MachineLearner#canUse(com.choicemaker.cm.core.ClueSet)
	 */
	public boolean canUse(ClueSet cs) {
		return cs.hasDecision() && cs.getType() == ClueSetType.BOOLEAN;
	}

	/**
	 * @see com.choicemaker.cm.ml.MachineLearner#isRegression()
	 */
	public boolean isRegression() {
		return true;
	}

	public String getModelInfo() {
		return "Training iterations: " + trainingIterations;
	}
}
