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

import java.util.Date;

import com.choicemaker.cm.core.ml.MachineLearner;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:00:57 $
 */
public interface IProbabilityModel extends AccessProvider, ImmutableProbabilityModel {
	
	public abstract void beginMultiPropertyChange();
	public abstract void endMultiPropertyChange();
	/**
	 * Sets the translator accessors.
	 *
	 * @param   newAcc  The translator accessors.
	 */
	public abstract void setAccessor(Accessor newAcc);
	/**
	 * Set the value of antCommand.
	 * @param v  Value to assign to antCommand.
	 */
	public abstract void setAntCommand(String v);
	/**
	 * Sets the clues to evaluate.
	 *
	 * @param   cluesToEvaluate  The clues to evaluate.
	 */
	public abstract void setCluesToEvaluate(boolean[] cluesToEvaluate)
		throws IllegalArgumentException;
	/**
	 * Set the value of enableAllCluesBeforeTraining.
	 * @param v  Value to assign to enableAllCluesBeforeTraining.
	 */
	public abstract void setEnableAllCluesBeforeTraining(boolean v);
	/**
	 * Set the value of enableAllRulesBeforeTraining.
	 * @param v  Value to assign to enableAllRulesBeforeTraining.
	 */
	public abstract void setEnableAllRulesBeforeTraining(boolean v);
	/**
	 * Sets the name of the probability model.
	 *
	 * If this model is in the collection of probability models, the
	 * name that it is associated with in the collection does not get
	 * changed.
	 *
	 * @param   fileName  The new name.
	 */
	public abstract void setFileName(String fileName);
	/**
	 * Set the value of firingThreshold.
	 * @param v  Value to assign to firingThreshold.
	 */
	public abstract void setFiringThreshold(int v);
	/**
	 * Set the value of lastTrainingDate.
	 * @param v  Value to assign to lastTrainingDate.
	 */
	public abstract void setLastTrainingDate(Date v);
	public abstract void setMachineLearner(MachineLearner ml);
	public abstract void setName(String name);
	public abstract void setRawClueFileName(String fn);
	public abstract void setTrainedWithHolds(boolean b);
	/**
	 * Set the value of trainingSource.
	 * @param v  Value to assign to trainingSource.
	 */
	public abstract void setTrainingSource(String v);
	/**
	 * Set the value of useAnt.
	 * @param v  Value to assign to useAnt.
	 */
	public abstract void setUseAnt(boolean v);
	/**
	 * Set the value of userName.
	 * @param v  Value to assign to userName.
	 */
	public abstract void setUserName(String v);
}
