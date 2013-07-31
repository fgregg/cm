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

import java.util.Arrays;

/**
 * A match consisting of the ID of the matching record and the
 * match probability.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:35:15 $
 */
public class Match implements /* ImmutableUnsafeMatch, */ Comparable {
	
	public final static double PROBABILITY_TOLERANCE = .000001;
	
	/** The decision */
	public final Decision decision;

	/** The match probability. */
	public final float probability;

	/** The id of the matching record. */
	public final Comparable id;

	/** The match record */
	public final Record m;

	/** The active clues */
	public final ActiveClues ac;

	/**
	 * Creates a <code>Match<code> with the specified
	 * probability and ID.
	 *
	 * @param   probability  The match probability.
	 * @param   id  The ID of the matching record.
	 */
	public Match(
		Decision decision,
		float probability,
		Comparable id,
		Record m,
		ActiveClues ac) {
		this.decision = decision;
		this.probability = probability;
		this.id = id;
		this.m = m;
		this.ac = ac;
	}

	/** Match before differ, differ before hold. Then by probability descending */
	public int compareTo(Object o) {
		Match m = (Match) o;
		int d = decision.compareTo(m.decision);
		if (d != 0) {
			return d;
		} else if (m.probability < probability) {
			return -1;
		} else if (m.probability > probability) {
			return 1;
		} else {
			return id.compareTo(m.id);
		}
	}

	public boolean equals(Object o) {
		if (o instanceof Match) {
			Match that = (Match) o;
			boolean bProb =
				(Math.abs(that.probability - this.probability) < PROBABILITY_TOLERANCE);
			boolean bId = (that.id.equals(this.id));
			return (bProb && bId);
		} else {
			return false;
		}
	}

	public boolean equalsIgnoreRecord(Object o) {
		boolean retVal = false;
		if (o instanceof Match) {
			Match that = (Match) o;

			boolean bDecision = false;
			if (that.decision != null && this.decision != null) {
				bDecision = that.decision.toInt() == this.decision.toInt();
			}

			boolean bProb =
				(Math.abs(that.probability - this.probability) < PROBABILITY_TOLERANCE);

			boolean bAC = false;
			if (that.ac != null && this.ac != null) {
				int[] ar1 = this.ac.getRules();
				if (this.ac instanceof BooleanActiveClues) {
					ar1 = ((BooleanActiveClues) this.ac).getCluesAndRules();
				}
				int[] ar2 = that.ac.getRules();
				if (that.ac instanceof BooleanActiveClues) {
					ar2 = ((BooleanActiveClues) that.ac).getCluesAndRules();
				}
				Arrays.sort(ar1);
				Arrays.sort(ar2);
				bAC = Arrays.equals(ar1, ar2);
			}

			retVal = bDecision && bProb && bAC;
		}
		return retVal;
	}

	public boolean strictEquals(Object o) {
		boolean retVal = false;
		if (o instanceof Match) {
			Match that = (Match) o;

			boolean b0 = equalsIgnoreRecord(that);

			boolean bId = false;
			if (that.id != null) {
				bId = (that.id.equals(this.id));
			}

			// Usually a redundant test, but there is no
			// guarantee from  the contract of the Match
			// interface that Match.id == Match.m.getId()
			//
			boolean bRecord = false;
			if (that.m != null && that.m.getId() != null && this.m != null) {
				bRecord = that.m.getId().equals(this.m.getId());
			}

			retVal = b0 && bId && bRecord;
		}
		return retVal;
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		StringBuffer b = new StringBuffer("Match[decision:");
		if (decision != null)
			b.append(decision == null ? "null" : decision.toSingleCharString());
		b.append(",probability:");
		b.append(probability);
		b.append(",id:");
		b.append(id == null ? "null" : id.toString());
		b.append(",record:");
		b.append(m == null ? "null" : m.getId());
		b.append(",");
		b.append(toString(ac));
		b.append("]");
		String retVal = b.toString();
		return retVal;
	}

	public static String toString(ActiveClues ac) {
		StringBuffer b = new StringBuffer("active clues[");
		if (ac != null) {
			int[] cluesOrRules = ac.getRules();
			if (ac instanceof BooleanActiveClues) {
				BooleanActiveClues bac = (BooleanActiveClues) ac;
				cluesOrRules = bac.getCluesAndRules();
			}
			if (cluesOrRules != null) {
				for (int i = 0; i < cluesOrRules.length - 1; i++) {
					b.append(cluesOrRules[i]);
					b.append(",");
				}
				if (cluesOrRules.length > 0) {
					b.append(cluesOrRules[cluesOrRules.length - 1]);
				}
			} else {
				b.append("null");
			}
		} else {
			b.append("null");
		}
		b.append("]");
		String retVal = b.toString();
		return retVal;
	}

}

