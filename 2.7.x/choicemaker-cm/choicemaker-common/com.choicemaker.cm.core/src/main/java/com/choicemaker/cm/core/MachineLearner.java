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
package com.choicemaker.cm.core;

import java.util.Collection;

import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.xmlconf.MlModelConf;

/**
 * The base interface for all machine learning techniques. The classes
 * that implement this interface provide access to the parts that make up
 * a machine learner and support training and evaluation.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 18:16:14 $
 */
public interface MachineLearner {
	/**
	 * Returns an Evaluator for this machine learning technique.
	 *
	 * @return   An Evaluator for this machine learning technique.
	 */
	Evaluator getEvaluator();

	/**
	 * Sets the probability model. Most other methods requre a non-null
	 * probability model.
	 *
	 * @param  model  The probability model.
	 */
	void setProbabilityModel(IProbabilityModel model);

	/**
	 * Notify the machine learner that the accessor and, therefore, the
	 * clue set has changed. This method tries to carry over as much as
	 * possible of the model, e.g., weights, as possible.
	 *
	 * @param  oldAccessor  The old accessor.
	 * @param  newAccessor  The new accessor.
	 * @param  oldClueNums  Cross reference of the old clue numbers. For each
	 *         new clue this indicates the index of the clue with the same name
	 *         in the old clue set, or -1 for new clues.
	 */
	void changedAccessor(Accessor oldAccessor, Accessor newAccessor, int[] oldClueNums);

	/**
	 * Train the model using this machine learning technique.
	 *
	 * @param  src  The collection of marked record pairs to train on.
	 * @param  firingPercentages  The firing percentages as computed by the Trainer.
	 */
	Object train(Collection src, double[] firingPercentages);

	/**
	 * Returns the XML model configurator for this machine learning technique.
	 *
	 * @return   The XML model configurator for this machine learning technique.
	 */
	MlModelConf getModelConf();
	
	/**
	 * Returns whether the machine learner is in a state in which it
	 * can compute decisions on record pairs.
	 *
	 * @return   Whether the machine learner is in a state in which it
	 *           can compute decisions on record pairs.
	 */
	boolean canEvaluate();

	/**
	 * Returns whether the machine learner is in a state in which it
	 * can train.
	 *
	 * @return   Whether the machine learner is in a state in which it
	 *           can train.
	 */
	boolean canTrain();

	/**
	 * Returns whether the machine learner can be used on the specified clue
	 * set.
	 *
	 * @param    The clueset to test.
	 * @return   Whether the machine learner can be used on the specified clue
	 * set
	 */
	boolean canUse(ClueSet clueset);

	/**
	 * Returns whether regression or classification is done.
	 * 
	 * @return  Whether regression or classification is done.
	 */
	boolean isRegression();

	String getModelInfo();
	/**
	 * Returns the factory of this machine learning technique.
	 *
	 * @return  The factory of this machine learning technique.
	 */
//	MachineLearnerFactory getFactory();	
}
