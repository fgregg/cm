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

import java.io.IOException;

import com.choicemaker.cm.core.report.Reporter;

/**
 * Manages a collection of IProbabilityModel instances.
 * @author rphall (Based on earlier PMManager and ProbabilityModel classes)
 * @version $Revision: 1.2 $ $Date: 2013/02/23 19:57:50 $
 */
public interface IProbabilityModelManager {

	/**
	 * Load models from the plugin registry into this manager
	 * @return the number of plugins loaded
	 */
	int loadModelPlugins() throws ModelConfigurationException, IOException;

	/**
	 * Adds a probability model to the collection of configured models.
	 *
	 * @param   model  The probability model.
	 */
	void addModel(IProbabilityModel model);

	Accessor createAccessor(String className, ClassLoader cl)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException;

	Reporter[] getGlobalReporters();

	/**
	 * Returns the specified probability model.
	 *
	 * @return  The specified probability model.
	 */
	ImmutableProbabilityModel getImmutableModelInstance(String name);

	/**
	 * Returns the specified probability model.
	 *
	 * @return  The specified probability model.
	 */
	IProbabilityModel getModelInstance(String name);

	IProbabilityModel[] getModels();

//	/** Returns an unmodifiable map of names to models */
//	Map models();

	void setGlobalReporters(Reporter[] rs);

}
