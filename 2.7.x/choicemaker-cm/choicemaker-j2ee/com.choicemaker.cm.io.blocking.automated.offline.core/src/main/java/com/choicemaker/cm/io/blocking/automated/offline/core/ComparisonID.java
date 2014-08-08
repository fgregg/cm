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
 * This represents an ID.  It has an ID atrribute and an isStage attribute.
 * 
 * @author pcheung
 *
 */
public class ComparisonID<T extends Comparable<? super T>> implements Comparable<ComparisonID<T>>, Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -814074577290725346L;

	/**
	 * Record id of the first record.
	 */
	public T id1;
	
	
	/**
	 * This is true is id2 is a staging record.
	 */
	public boolean isStage;


	/** For testing only */
	int hashCode_00() {
		return id1.hashCode();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id1 == null) ? 0 : id1.hashCode());
		result = prime * result + (isStage ? 1231 : 1237);
		return result;
	}

	/** For testing only */
	boolean equals_00(Object o) {
		if (o instanceof ComparisonID) {
			@SuppressWarnings("rawtypes")
			ComparisonID cp = (ComparisonID) o;
			return ( this.id1.equals(cp.id1)
					&& this.isStage == cp.isStage );

		} else {
			return false;
		}
	}

	/**
	 * This is true if this Object is a MatchRecord and has the same id pair as
	 * the input MatchRecord.
	 * 
	 * @param o
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		ComparisonID other = (ComparisonID) obj;
		if (id1 == null) {
			if (other.id1 != null) {
				return false;
			}
		} else if (!id1.equals(other.id1)) {
			return false;
		}
		if (isStage != other.isStage) {
			return false;
		}
		return true;
	}

	/** This returns -1 if this object is less than input o,
	 * 0 is equals input 0,
	 * and 1 if it is greater than input o.
	 * 
	 */
	@Override
	public int compareTo(ComparisonID<T> p) {
		int ret = 0;
		if (id1.compareTo(p.id1) < 0) ret = -1;
		else if (id1.compareTo(p.id1) > 0) ret = 1;
		else if (id1.compareTo(p.id1) == 0) {
			if (isStage == true) ret = -1;
			else if (isStage == false) ret = 1;
		}
		return ret;
	}

	
}
