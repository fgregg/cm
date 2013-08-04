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
package com.choicemaker.cm.urm.base;

/**
 * A matching score between a single and a composite record that consists of the
 * scores between the single record and records included in the composite record. 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 12:26:02 PM
 * @see
 */
public class CompositeMatchScore implements IMatchScore {
	
	
	protected MatchScore[] innerScores;
	
	public CompositeMatchScore() {
		super();
	}

	public CompositeMatchScore(MatchScore[] is) {
		this.innerScores = is;
	}

	public MatchScore[] getInnerScores() {
		return innerScores;
	}

	public Decision3 getConservativeDecision() {
		//TODO:impl
		return null;
	}

	public float getAverageProbability(){
		float avProb = 0;
		for(int n=0; n<this.innerScores.length; n++ ){
			avProb += this.innerScores[n].probability;
		}
		return  avProb/this.innerScores.length; 
	}

	public Decision3 getDecision() {
		return getConservativeDecision(); 
	}


	public float getProbability() {
		return getAverageProbability();
	}

}
