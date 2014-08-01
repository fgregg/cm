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

/**
 * The result of evaluating whether a candidate record is a match to a reference
 * record.
 *
 * @author Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/27 21:35:15 $
 */
public class PairEvaluation implements Comparable {

	// public final static double PROBABILITY_TOLERANCE = .0000001;

	/** The reference record */
	public final Record q;

	/** The candidate record */
	public final Record m;

	/** The decision */
	public final Decision decision;

	/** The match probability. */
	public final float probability;

	/** The active clues */
	public final Firings ac;

	/**
	 * Creates a <code>PairEvaluation<code> with the specified
	 * probability and ID.
	 *
	 * @param   p  The match probability.
	 * @param   mid  The ID of the matching record.
	 */
	public PairEvaluation(Record q, Record m, Decision d, float p,
			Firings ac) {
		if (q == null || m == null || d == null || ac == null) {
			throw new IllegalArgumentException("null parameter");
		}
		this.q = q;
		this.m = m;
		this.decision = d;
		this.probability = p;
		this.ac = ac;
	}

//	/**
//	 * Creates a <code>PairEvaluation<code> with the specified
//	 * probability and ID.
//	 *
//	 * @param p
//	 *            The match probability.
//	 * @param mid
//	 *            The ID of the matching record.
//	 */
//	public PairEvaluation(Record q, Record m, Decision d, float p) {
//		if (q == null || m == null || d == null) {
//			throw new IllegalArgumentException("null parameter");
//		}
//		this.q = q;
//		this.m = m;
//		this.decision = d;
//		this.probability = p;
//	}
//
	/**
	 * Compare decisions: match before differ and differ before hold. Next,
	 * compare probability descending. Next, compare records ids, first
	 * reference id, then candidate id.
	 */
	public int compareTo(Object o) {
		if (o == null) {
			throw new IllegalArgumentException("null pair evaluation");
		}
		PairEvaluation other = (PairEvaluation) o;
		// Compare decisions
		int retVal = decision.compareTo(other.decision);

		// Compare probabilities (if necessary)
		if (retVal == 0) {
			float diff = this.probability - other.probability;
			if (diff != 0f) {
				double d = diff / Math.abs(diff);
				retVal = (int) d;
			}
		}

		// Compare reference ids (if necessary and feasible)
		if (retVal == 0) {
			Comparable thisId = this.q.getId();
			Comparable otherId = other.q.getId();
			if (thisId != null && otherId != null) {
				retVal = thisId.compareTo(otherId);
			}
		}

		// Compare candidate ids (if necessary and feasible)
		if (retVal == 0) {
			Comparable thisId = this.m.getId();
			Comparable otherId = other.m.getId();
			if (thisId != null && otherId != null) {
				retVal = thisId.compareTo(otherId);
			}
		}

		return retVal;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((decision == null) ? 0 : decision.hashCode());
		result = prime * result + ((m == null) ? 0 : m.hashCode());
		result = prime * result + Float.floatToIntBits(probability);
		result = prime * result + ((q == null) ? 0 : q.hashCode());
		return result;
	}

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
		PairEvaluation other = (PairEvaluation) obj;
		if (q == null) {
			if (other.q != null) {
				return false;
			}
		} else if (!q.equals(other.q)) {
			return false;
		}
		if (m == null) {
			if (other.m != null) {
				return false;
			}
		} else if (!m.equals(other.m)) {
			return false;
		}
		if (decision == null) {
			if (other.decision != null) {
				return false;
			}
		} else if (!decision.equals(other.decision)) {
			return false;
		}
		if (Float.floatToIntBits(probability) != Float
				.floatToIntBits(other.probability)) {
			return false;
		}
		return true;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("PairEvaluation [");
		if (q != null) {
			buffer.append("q=");
			buffer.append(q);
			buffer.append(", ");
		}
		if (m != null) {
			buffer.append("m=");
			buffer.append(m);
			buffer.append(", ");
		}
		if (decision != null) {
			buffer.append("decision=");
			buffer.append(decision);
			buffer.append(", ");
		}
		buffer.append("probability=");
		buffer.append(probability);
		buffer.append("]");
		return buffer.toString();
	}

}
