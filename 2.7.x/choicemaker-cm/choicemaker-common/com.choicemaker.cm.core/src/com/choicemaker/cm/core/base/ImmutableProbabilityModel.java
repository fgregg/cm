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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

import com.choicemaker.cm.core.ml.MachineLearner;
import com.choicemaker.cm.core.report.Report;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:00:57 $
 */
public interface ImmutableProbabilityModel {
	public static final String CLUES_TO_EVALUATE = "cluesToEvaluate";

	public static final String MACHINE_LEARNER = "machineLearner";

	public static final String MACHINE_LEARNER_PROPERTY =
		"machineLearnerProperty";

	public static final String NAME = "name";

	public Hashtable properties();

	/**
	 * Returns the number of active clues in this <code>ClueSet</code>.
	 *
	 * @return  The number of active clues in this <code>ProbabilityModel</code>.
	 */
	public abstract int activeSize();

	/**
	 * Returns the number of clues predicting <code>Decision<code>
	 * <code>d</code> in this <code>ProbabilityModel</code>.
	 *
	 * @return  The number of clues predicting <code>Decision</code>
	 *            <code>d</code> in this <code>ProbabilityModel</code>.
	 */
	public abstract int activeSize(Decision d);

	public abstract void addPropertyChangeListener(PropertyChangeListener l);

	public abstract boolean canEvaluate();

	public abstract void changedCluesToEvaluate();

	/**
	 * Returns the translator accessors.
	 *
	 * @return  The translator accessors.
	 */
	public abstract Accessor getAccessor();

	/**
	 * Returns the name of the Accessor class.
	 * 
	 * Note: this is not the same as getAccessor().getClass().getName() 
	 * because getAccessor() returns a dynamic proxy, so the class name
	 * is something like $Proxy0.
	 * 
	 * @return The name of the accessor class.
	 */
	public abstract String getAccessorClassName();

	/**
	 * Get the value of antCommand.
	 * @return value of antCommand.
	 */
	public abstract String getAntCommand();

	public abstract String getClueFileName();

	/**
	 * Returns an instance of the clue set.
	 *
	 * @return   An instance of the clue set.
	 */
	public abstract ClueSet getClueSet();

	/**
	 * Returns the list of clues to evaluate.
	 *
	 * @return  The list of clues to evaluate.
	 */
	public abstract boolean[] getCluesToEvaluate();

	public abstract String getClueText(int clueNum) throws IOException;

	public abstract int getDecisionDomainSize();

	public abstract Evaluator getEvaluator();

	/**
	 * Returns the file name of the probability model.
	 *
	 * @return   The file name of the probability model.
	 */
	public abstract String getFileName();

	/**
	 * Get the value of firingThreshold.
	 * @return value of firingThreshold.
	 */
	public abstract int getFiringThreshold();

	/**
	 * Get the value of lastTrainingDate.
	 * @return value of lastTrainingDate.
	 */
	public abstract Date getLastTrainingDate();

	public abstract MachineLearner getMachineLearner();

	/**
	 * Returns the name of the probability model.
	 *
	 * @return   The name of the probability model.
	 */
	public abstract String getName();

	public abstract String getRawClueFileName();

	public abstract boolean[] getTrainCluesToEvaluate();

	/**
	 * Get the value of trainingSource.
	 * @return value of trainingSource.
	 */
	public abstract String getTrainingSource();

	/**
	 * Get the value of userName.
	 * @return value of userName.
	 */
	public abstract String getUserName();

	/**
	 * Get the value of enableAllCluesBeforeTraining.
	 * @return value of enableAllCluesBeforeTraining.
	 */
	public abstract boolean isEnableAllCluesBeforeTraining();

	/**
	 * Get the value of enableAllRulesBeforeTraining.
	 * @return value of enableAllRulesBeforeTraining.
	 */
	public abstract boolean isEnableAllRulesBeforeTraining();

	public abstract boolean isTrainedWithHolds();

	/**
	 * Get the value of useAnt.
	 * @return value of useAnt.
	 */
	public abstract boolean isUseAnt();

	public abstract void machineLearnerChanged(
		Object oldValue,
		Object newValue);

	public abstract boolean needsRecompilation();

	public abstract int numTrainCluesToEvaluate();

	public abstract void removePropertyChangeListener(PropertyChangeListener l);

	public abstract void report(Report report) throws IOException;
}
