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
 * This represents an ID. It has an ID atrribute and an isStage attribute.
 * 
 * @author pcheung
 *
 */
public class ComparisonID<T extends Comparable<T>> implements
		Comparable<ComparisonID<T>>, Serializable {

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

	/**
	 * This is true if this Object is a MatchRecord and has the same id pair as
	 * the input MatchRecord.
	 * 
	 * @param o
	 * @return boolean - true if the ids from both MatchRecords match.
	 */
	public boolean equals(ComparisonID<T> cp) {
		if (cp != null && this.id1 != null) {
			return (this.id1.equals(cp.id1) && this.isStage == cp.isStage);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id1 == null ? 0 : id1.hashCode();
	}

	/**
	 * This returns -1 if this object is less than input o, 0 is equals input 0,
	 * and 1 if it is greater than input o.
	 * 
	 */
	@Override
	public int compareTo(ComparisonID<T> p) {
		int ret = 0;

		if (id1.compareTo(p.id1) < 0)
			ret = -1;
		else if (id1.compareTo(p.id1) > 0)
			ret = 1;
		else if (id1.compareTo(p.id1) == 0) {
			if (isStage == true)
				ret = -1;
			else if (isStage == false)
				ret = 1;
		}
		return ret;
	}

}
