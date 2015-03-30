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

import java.util.Date;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:00:57 $
 */
public interface IProbabilityModel extends AccessProvider, ImmutableProbabilityModel {
	
	void beginMultiPropertyChange();
	void endMultiPropertyChange();
	/**
	 * Sets the translator accessors.
	 *
	 * @param   newAcc  The translator accessors.
	 * @throws ModelConfigurationException 
	 */
	void setAccessor(Accessor newAcc) throws ModelConfigurationException;
	/**
	 * Set the value of antCommand.
	 * @param v  Value to assign to antCommand.
	 */
	void setAntCommand(String v);
	/**
	 * Sets the clues to evaluate.
	 *
	 * @param   cluesToEvaluate  The clues to evaluate.
	 */
	void setCluesToEvaluate(boolean[] cluesToEvaluate)
		throws IllegalArgumentException;
	/**
	 * Set the value of enableAllCluesBeforeTraining.
	 * @param v  Value to assign to enableAllCluesBeforeTraining.
	 */
	void setEnableAllCluesBeforeTraining(boolean v);
	/**
	 * Set the value of enableAllRulesBeforeTraining.
	 * @param v  Value to assign to enableAllRulesBeforeTraining.
	 */
	void setEnableAllRulesBeforeTraining(boolean v);
	/**
	 * Sets the path to the probability model weights file (*.model)
	 * 
	 * If this model is in the collection of probability models, the
	 * {@link #getModelName() name} that it is associated with in the collection does
	 * not get changed.
	 * 
	 * @param filePath
	 *            The new name.
	 */
	void setModelFilePath(String fileName);

	/**
	 * Set the value of firingThreshold.
	 * @param v  Value to assign to firingThreshold.
	 */
	void setFiringThreshold(int v);
	/**
	 * Set the value of lastTrainingDate.
	 * @param v  Value to assign to lastTrainingDate.
	 */
	void setLastTrainingDate(Date v);

	void setMachineLearner(MachineLearner ml);
	
	void setClueFilePath(String fn);
	
	void setTrainedWithHolds(boolean b);

	/**
	 * Set the value of trainingSource.
	 * @param v  Value to assign to trainingSource.
	 */
	void setTrainingSource(String v);
	/**
	 * Set the value of useAnt.
	 * @param v  Value to assign to useAnt.
	 */
	void setUseAnt(boolean v);
	/**
	 * Set the value of userName.
	 * @param v  Value to assign to userName.
	 */
	void setUserName(String v);

}
