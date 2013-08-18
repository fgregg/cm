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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.io.Serializable;

/**
 * This represents a pair for matching comparison.
 * 
 * @author pcheung
 *
 */
public class ComparisonPair implements Comparable, Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -8367155014018115222L;

	/**
	 * Record id of the first record.
	 */
	public Comparable id1;
	
	
	/**
	 * Record if of the second record.
	 */
	public Comparable id2;
	
	
	/**
	 * This is true is id2 is a staging record.
	 */
	public boolean isStage;


	/** This is true if this Object is a MatchRecord and has the same id pair as the input MatchRecord.
	 * 
	 * @param o
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	public boolean equals (Object o) {
		boolean ret = false;
		
		if (o.getClass() == ComparisonPair.class) {
			equals ((ComparisonPair) o);
		}
		return ret;
	}


	/** This is true if this MatchRecord has the same id pair as the input MatchRecord.
	 * 
	 * @param p
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	public boolean equals (ComparisonPair p) {
		boolean ret = false;
		if (this.id1.equals(p.id1) && this.id2.equals(p.id2) &&
			this.isStage == p.isStage) ret = true;
		return ret;
	}



	/** This returns -1 if this object is less than input o,
	 * 0 is equals input 0,
	 * and 1 if it is greater than input o.
	 * 
	 */
	public int compareTo(Object o) {
		int ret = 0;
		ComparisonPair p = (ComparisonPair) o;
		
		if (id1.compareTo(p.id1) < 0) ret = -1;
		else if (id1.compareTo(p.id1) > 0) ret = 1;
		else if (id1.compareTo(p.id1) == 0) {
			if (id2.compareTo(p.id2) < 0) ret = -1;
			else if (id2.compareTo(p.id2) > 0) ret = 1;
			else if (id2.compareTo(p.id2) == 0) {
				if (isStage == p.isStage) ret = 0;
				else if (isStage == true) ret = -1;
				else if (isStage == false) ret = 1;
			} 
		}
		return ret;
	}

	
}
