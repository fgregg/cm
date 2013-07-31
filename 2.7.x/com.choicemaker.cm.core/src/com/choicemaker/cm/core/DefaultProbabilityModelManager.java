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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.choicemaker.cm.core.report.Reporter;
import com.choicemaker.cm.core.util.Precondition;

/**
 * Creates and manages a collection of IProbabilityModel instances.
 * @author Martin Buechi (initial implementation of ProbabilityModel)
 * @author S. Yoakum-Stover (initial implementation of ProbabilityModel)
 * @author rphall (Refactored from PMManager and previously ProbabilityModel)
 * @version $Revision: 1.2 $ $Date: 2013/02/23 19:57:50 $
 */
public class DefaultProbabilityModelManager implements IProbabilityModelManager {

	private DefaultProbabilityModelManager() {}
	
	private static final Object _instanceSynch = new Object();
	
	private static DefaultProbabilityModelManager _instance = null;
	
	public static IProbabilityModelManager getInstance() {
		if (_instance == null) {
			synchronized(_instanceSynch) {
				if (_instance == null) {
					_instance = new DefaultProbabilityModelManager();
				}
			}
		}
		return _instance;
	}
	
	private final Map models = new HashMap();
	private Reporter[] reporters = new Reporter[0];

	private static final String CLUES_TO_EVALUATE = ImmutableProbabilityModel.CLUES_TO_EVALUATE;

	private static final String MACHINE_LEARNER = ImmutableProbabilityModel.MACHINE_LEARNER;

	private static final String MACHINE_LEARNER_PROPERTY =
		ImmutableProbabilityModel.MACHINE_LEARNER_PROPERTY;

	private static final String NAME = ImmutableProbabilityModel.NAME;

	/**
	 * Adds a probability model to the collection of configured models.
	 *
	 * @param   model  The probability model.
	 */
	public void addModel(IProbabilityModel model) {
		models.put(model.getName(), model);
	}

	public Accessor createAccessor(String className, ClassLoader cl) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class accessorClass = Class.forName(className, true, cl);
		return (Accessor) accessorClass.newInstance();
	}

	/**
	 * Returns the specified probability model.
	 *
	 * @return  The specified probability model.
	 */
	public IProbabilityModel getModelInstance(String name) {
		return (IProbabilityModel) models.get(name);
	}

	/**
	 * Returns the specified probability model.
	 *
	 * @return  The specified probability model.
	 */
	public ImmutableProbabilityModel getImmutableModelInstance(String name) {
		return (ImmutableProbabilityModel) models.get(name);
	}
	
	public Map models() {
		Map retVal = Collections.unmodifiableMap(models);
		return retVal;
	}

	public IProbabilityModel[] getModels() {
		Collection coll = models.values();
		return (IProbabilityModel[]) coll.toArray(new IProbabilityModel[coll.size()]);
	}

	public void setGlobalReporters(Reporter[] rs) {
		Precondition.assertNonNullArgument("null reporter array",rs);
		for (int i=0; i<rs.length; i++) {
			Precondition.assertNonNullArgument("null reporter[" + i + "]",rs[i]);
		}
		Reporter[] copy = new Reporter[rs.length];
		System.arraycopy(rs,0,copy,0,rs.length);
		reporters = copy;
	}

	public Reporter[] getGlobalReporters() {
		Reporter[] retVal = new Reporter[this.reporters.length];
		System.arraycopy(this.reporters,0,retVal,0,this.reporters.length);
		return retVal;
	}

}

