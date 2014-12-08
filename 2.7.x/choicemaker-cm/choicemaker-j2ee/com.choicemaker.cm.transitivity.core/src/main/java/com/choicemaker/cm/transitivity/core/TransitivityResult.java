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
package com.choicemaker.cm.transitivity.core;

import java.util.Iterator;

/**
 * This is the return object of the Transitivity Engine.  It contains information
 * about the model, threshold, and clusters (CompositeEntity).
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies Inc.
 */
@SuppressWarnings("rawtypes")
public class TransitivityResult {

	/**	
	* This field is for information purposes only, since transivity doesn't depend on a model directly, only
	* on how composite entities are iterated.
	*/
	private String model;
	
	private float differ;
	
	private float match;

	private Iterator nodeIterator;	

	/**
	 * Creates an object that encapsulates the results of a
	 * transitivity calculation.
	 * @param modelName for information purposes only,
	 * Transivity doesn't depend on a model directly, only
	 * on how composite entities are iterated.
	 * @param differ the differ threshold
	 * @param match the match threshold
	 * @param compositeEntityIterator the clustered records
	 */		
	public TransitivityResult (String modelName, float differ, float match,
		Iterator compositeEntityIterator) {
		this.model = modelName;
		this.differ = differ;
		this.match = match;
		this.nodeIterator = compositeEntityIterator;
	}
		
	/**
	 * This method returns the name of the Probability Model.
	 * @return String (may be null, since transitivity doesn't
	 * depend on a model directly)
	 */
	public String getModelName () {
		return model;
	}
	
	
	/**
	 * This method returns the differ threshold of this run.
	 * @return float
	 */
	public float getDifferThreshold () {
		return differ;
	}
	
	
	/**
	 * This method returns the match threshold of this run.
	 * @return float
	 */
	public float getMatchThreshold () {
		return match;
	}
	
	
	/**
	 * This method returns an Iterator of INode representing the match clusters.
	 * @return Iterator of INode
	 */
	public Iterator getNodes () {
		return nodeIterator;
	}
		
}
