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

import com.choicemaker.cm.core.base.IProbabilityModel;
import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
import com.choicemaker.cm.core.util.OperationFailedException;
import com.choicemaker.cm.modelmaker.gui.listeners.EventMultiplexer;

/**
 * @author rphall
 *
 */
public interface IModelControl {
	
	public abstract EventMultiplexer getProbabilityModelEventMultiplexer();
	public abstract boolean haveProbabilityModel();
	public abstract void reloadProbabilityModel();
	/**
	 * Gets the model using the model name, then sets the model.
	 * If a model by the passed name can not be retrieved, an 
	 * error is posted and the previously set model is kept as 
	 * the active model.
	 * 
	 * @param modelName
	 */
	public abstract void setProbabilityModel(String modelName, boolean reload)
		throws OperationFailedException;
	/**
	 * Sets the probability model.  Nulls out the source list and calls
	 * resetEvaluationStatistics on the trainingPanel so that the proper clue 
	 * set is displayed.  Sends a modelChanged message to any listeners.
	 * 
	 * @param pm     A reference to a PMManager.
	 */
	public abstract void setProbabilityModel(IProbabilityModel pm);
	/**
	 * 
	 * @return A reference to the active PMManager.
	 */
	public abstract ImmutableProbabilityModel getProbabilityModel();
	public abstract ImmutableProbabilityModel getProbabilityModel(String modelName)
		throws OperationFailedException;
	/**
	 * Saves the probability model to disk.
	 * 
	 * @param pm
	 */
	public abstract void saveProbabilityModel(IProbabilityModel pm)
		throws OperationFailedException;
	/**
	 * Saves the active probability model to disk.
	 */
	public abstract void saveActiveModel();
	public abstract boolean buildProbabilityModel(IProbabilityModel pm);

}
