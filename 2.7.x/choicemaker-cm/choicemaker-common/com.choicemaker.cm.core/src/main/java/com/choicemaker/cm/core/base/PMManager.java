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

import java.util.Map;
import java.util.logging.Logger;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.IProbabilityModelManager;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.report.Reporter;

/**
 * Creates and manages a collection of IProbabilityModel instances.
 * @author Martin Buechi
 * @author S. Yoakum-Stover
 * @author rphall (Split ProbabilityModel into separate instance and manager types)
 * @version $Revision: 1.2 $ $Date: 2013/02/23 19:57:50 $
 */
public class PMManager {
	
	private static final Logger logger = Logger.getLogger(PMManager.class.getName());
	
	// FIXME eventually, the default probability model manager should be configurable
	private static IProbabilityModelManager defaultManager = DefaultProbabilityModelManager.getInstance();
	
	public static Map models() {
		return defaultManager.models();
	}

	public static IProbabilityModel[] getModels() {
		return defaultManager.getModels();
	}

	public static void addModel(IProbabilityModel model) {
		defaultManager.addModel(model);
	}

	public static Accessor createAccessor(String className, ClassLoader cl) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return defaultManager.createAccessor(className,cl);
	}

	public static IProbabilityModel getModelInstance(String name) {
		IProbabilityModel retVal = defaultManager.getModelInstance(name);
		if (retVal == null) {
			String msg = "No model corresponding to '" + name + "'";
			logger.warning(msg);
		}
		return retVal;
	}

	public static ImmutableProbabilityModel getImmutableModelInstance(String name) {
		return defaultManager.getImmutableModelInstance(name);
	}

	public static void setGlobalReporters(Reporter[] rs) {
		defaultManager.setGlobalReporters(rs);
	}

	public static Reporter[] getGlobalReporters() {
		return defaultManager.getGlobalReporters();
	}

}

