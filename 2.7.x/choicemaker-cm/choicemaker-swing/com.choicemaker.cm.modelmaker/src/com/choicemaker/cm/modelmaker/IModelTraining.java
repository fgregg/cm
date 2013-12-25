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
package com.choicemaker.cm.modelmaker;

import com.choicemaker.cm.core.Repository;
import com.choicemaker.cm.core.train.Trainer;
import com.choicemaker.cm.modelmaker.stats.Statistics;

/**
 * @author rphall
 *
 */
public interface IModelTraining {

	public void setAllCluesOrRules(int what, boolean value);

	/**
	 * Resets the weights in the probability model all to 1.
	 */
	public void resetWeights();

	/**
	 * Evaluates the clues on the source to get and display
	 * the counts.
	 */
	public void evaluateClues();

	/**
	 * Starts the training.  When training is done, updates 
	 * the ProbabilityModelChangeListeners and TrainerChangeListeners
	 * so that they can update their data.
	 */
	public boolean train(
		boolean recompile,
		boolean enableAllClues,
		boolean enableAllRules,
		int firingThreshold,
		boolean andTest);

	public Trainer getTrainer();

	public Statistics getStatistics();

	public Repository getRepository();

}
