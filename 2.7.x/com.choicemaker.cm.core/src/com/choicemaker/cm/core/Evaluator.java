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
 * Matches records and computes matching probabilities. This class is extended
 * by the Evaluator's specific to machine learning techniques.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.3 $ $Date: 2010/09/02 00:05:53 $
 */
public abstract class Evaluator {
	/** The probability model. */
	protected IProbabilityModel model;
	/** The clue set. */
	protected ClueSet cs;
	/** The clue descriptors. */
	protected ClueDesc[] cd;
	/** Whether the model has any clues predicting hold. */
	protected boolean useHolds;
	
	/**
	 * Creates an evaluator for the specified <code>ProbabilityModel</code>.
	 *
	 * @param  model  The <code>ProbabilityModel</code> used for evaluation.
	 */
	protected Evaluator(IProbabilityModel model) {
		this.model = model;
		cs = model.getClueSet();
		cd = cs.getClueDesc();
		useHolds = model.getDecisionDomainSize() == 3;
	}

	/**
	 * Get a match result, regardless of the decision.
	 *
	 * @param   q  The query record.
	 * @param   m  The match record.
	 * @return  The <code>Match</code> containing the ID of <code>m</code>;
	 * never null.
	 */
	public Match getMatchNeverNull(Record q, Record m, float lt, float ut) {
		ActiveClues a = cs.getActiveClues(q, m, model.getCluesToEvaluate());
		float p = getProbability(a);
		Decision d = getDecision(a, p, lt, ut);
		return new Match(d, p, m.getId(), m, a);
	}

	/**
	 * Match two records.
	 *
	 * @param   q  The query record.
	 * @param   m  The match record.
	 * @return  The <code>Match</code> containing the ID of <code>m</code>
	 *            and the probability if the decision is match or differ or if a
	 *            clue/rule with modifier report is active. <code>null</code> otherwise. 
	 */
	public Match getMatch(Record q, Record m, float lt, float ut) {
		ActiveClues a = cs.getActiveClues(q, m, model.getCluesToEvaluate());
		float p = getProbability(a);
		Decision d = getDecision(a, p, lt, ut);
		if (d != Decision.DIFFER || a.isReport()) {
			return new Match(d, p, m.getId(), m, a);
		} else {
			return null;
		}
	}

	/**
	 * Computes the decision based on the active clues and rules, the match probability,
	 * and the thresholds.
	 *
	 * @param  a  The active clues.
	 * @param  p  The match probability.
	 * @param  lt  The lower threshold.
	 * @param  ut  The upper threshold.
	 * @return   The decision.
	 */
	public Decision getDecision(ActiveClues a, float p, float lt, float ut) {
		Decision d;
		if (p < lt) {
			d = Decision.DIFFER;
		} else if (p < ut) {
			d = Decision.HOLD;
		} else {
			d = Decision.MATCH;
		}
		int nf = a.sizeRules();
		if (nf != 0) {
			int fd = 7;
			for (int i = 0; i < nf; ++i) {
				int ruleNum = a.getRule(i);
				Decision td = cd[ruleNum].getDecision();
				if (td == Decision.DIFFER) {
					fd &= 1;
				} else if (td == Decision.HOLD) {
					fd &= 2;
				} else if (td == Decision.MATCH) {
					fd &= 4;
				} else if (td == ExtDecision.NODIFFER) {
					fd &= 6;
				} else if (td == ExtDecision.NOHOLD) {
					fd &= 5;
				} else if (td == ExtDecision.NOMATCH) {
					fd &= 3;
				}
			}
			switch (fd) {
				case 0 :
					return Decision.HOLD;
				case 1 :
					return Decision.DIFFER;
				case 2 :
					return Decision.HOLD;
				case 3 :
					return d == Decision.MATCH ? Decision.HOLD : d;
				case 4 :
					return Decision.MATCH;
				case 5 :
					return d == Decision.HOLD ? Decision.DIFFER : d;
				case 6 :
					return d == Decision.DIFFER ? Decision.HOLD : d;
				case 7 :
					return d;
			}
		}
		return d;
	}

	/**
	 * Returns the probability that two records match.
	 *
	 * @param   q  The query record.
	 * @param   m  The match record.
	 * @return  The probability that <code>m</code> and <code>q</code>
	 *            are the same entity.
	 */
	public float getMatchProbability(Record q, Record m) {
		ActiveClues a = cs.getActiveClues(q, m, model.getCluesToEvaluate());
		return getProbability(a);
	}

	/**
	 * Returns the match probability based on the active clues.
	 *
	 * @param   a  The active clues.
	 * @return  The match probability.
	 */
	public abstract float getProbability(ActiveClues a);
	
	/**
	 * Returns details used in calculating
	 * {@link #getProbability(ActiveClues) a ChoiceMaker probability score}
	 * that are specific to a particular machine learning technique.
	 * 
	 * @param   a  The active clues.
	 * @return  A string representing pertinent details.  For example,
	 * a Maximum Entropy classifier might return a string describing the
	 * classification probabiltiesfor match, hold and differ; whereas a
	 * Support Vector Machine might return a string representing a
	 * real-valued, type-dependent prediction for a match classification.
	 * <p>
	 * Subclasses should override the default, do-nothing method provided
	 * by this class.
	 * @author rphall
	 * @since 2010-08-11
	 */
	public String getProbabilityDetails(ActiveClues a) {
		return "";
	}

	/**
	 * Returns whether a rule has overridden the probabilistic decision.
	 *
	 * @param  p  The (evaluated) marked record pair.
	 * @param  t  The thresholds.
	 * @return   Whether a rule has overridden the probabilistic decision.
	 */
	public static boolean isBasedOnRule(MutableMarkedRecordPair p, Thresholds t) {
		Decision d;
		if (p.getProbability() < t.getDifferThreshold()) {
			d = Decision.DIFFER;
		} else if (p.getProbability() < t.getMatchThreshold()) {
			d = Decision.HOLD;
		} else {
			d = Decision.MATCH;
		}
		return p.getCmDecision() != d;
	}

	/**
	 * Returns whether a rule has overridden the probabilistic decision.
	 *
	 * @param  m  The match.
	 * @param  lowerThreshold  The lower thresholds.
	 * @param  upperThreshold  The upper thresholds.
	 * @return   Whether a rule has overridden the probabilistic decision.
	 */
	public static boolean isBasedOnRule(Match m, float lowerThreshold, float upperThreshold) {
		Decision d;
		if (m.probability < lowerThreshold) {
			d = Decision.DIFFER;
		} else if (m.probability < upperThreshold) {
			d = Decision.HOLD;
		} else {
			d = Decision.MATCH;
		}
		return m.decision != d;
	}

}
