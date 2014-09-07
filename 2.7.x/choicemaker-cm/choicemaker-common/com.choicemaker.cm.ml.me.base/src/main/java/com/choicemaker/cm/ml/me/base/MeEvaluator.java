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
package com.choicemaker.cm.ml.me.base;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.BooleanActiveClues;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.util.Signature;

/**
 * Matches records and computes matching probabilities.
 *
 * @author Martin Buechi
 * @version $Revision: 1.3 $ $Date: 2010/09/02 00:05:52 $
 */
public class MeEvaluator extends Evaluator {

	private final float[] weights;

	/**
	 * Creates an evaluator for the specified <code>ProbabilityModel</code>.
	 *
	 * @param model
	 *            a non-null <code>ProbabilityModel</code> used for evaluation.
	 * @param weights
	 *            a non-null, ordered list of weights corresponding to the clues
	 *            used by this model.
	 */
	MeEvaluator(IProbabilityModel model, float[] weights) {
		super(model);
		if (weights == null) {
			throw new IllegalArgumentException("null weights");
		}
		this.weights = weights;
	}

	/**
	 * The index for differ classifications (0) in the array returned by
	 * {@link #getClassificationProbabilities(ActiveClues)}.
	 */
	public final static int IDX_DIFFER = 0;

	/**
	 * The index for match classifications (1) in the array returned by
	 * {@link #getClassificationProbabilities(ActiveClues)}.
	 */
	public final static int IDX_MATCH = 1;

	/**
	 * The index for hold classifications (2, if present) in the array returned
	 * by {@link #getClassificationProbabilities(ActiveClues)}.
	 */
	public final static int IDX_HOLD = 2;

	/**
	 * Returns an array of classification probabilities. The array size is equal
	 * to
	 * {@link com.choicemaker.cm.core.base.ImmutableProbabilityModel#getDecisionDomainSize()
	 * the decision domain size}, either 2 (differs and matches) or 3 (differs,
	 * matches and holds). The array elements represent differs (element 0),
	 * matches (element 1) and possibly holds (element 2, if present).
	 */
	public float[] getClassificationProbabilities(ActiveClues a) {
		int domainSize = model.getDecisionDomainSize();
		float[] retVal = new float[domainSize];
		BooleanActiveClues bac = (BooleanActiveClues) a;
		float match = 1.0f;
		float differ = 1.0f;
		float hold = 1.0f;
		int noClues = bac.size();
		for (int i = 0; i < noClues; ++i) {
			int clueNum = bac.get(i);
			Decision d = cd[clueNum].decision;
			if (d == Decision.MATCH) {
				match *= weights[clueNum];
			} else if (d == Decision.DIFFER) {
				differ *= weights[clueNum];
			} else if (d == Decision.HOLD) {
				hold *= weights[clueNum];
			}
		}
		float norm;
		if (retVal.length > IDX_HOLD) {
			norm = match + differ + hold;
			retVal[IDX_HOLD] = hold / norm;
		} else {
			norm = match + differ;
		}
		retVal[IDX_DIFFER] = differ / norm;
		retVal[IDX_MATCH] = match / norm;
		return retVal;
	}

	/**
	 * Returns the ChoiceMaker probability score for a set of active clues
	 * (a.k.a. features).
	 */
	public float getProbability(ActiveClues a) {
		float retVal;
		float[] outcomes = getClassificationProbabilities(a);
		if (outcomes.length > IDX_HOLD) {
			retVal = outcomes[IDX_MATCH] + 0.5f * outcomes[IDX_HOLD];
		} else {
			retVal = outcomes[IDX_MATCH];
		}
		return retVal;
	}

	/**
	 * Returns details used in calculating {@link #getProbability(ActiveClues) a
	 * ChoiceMaker probability score} that are specific to the maximum entropy
	 * machine learning technique.
	 * 
	 * @param a
	 *            The active clues.
	 * @return An XML fragment representing classification probabilities.
	 * @author rphall
	 * @since 2010-08-11
	 */
	public String getProbabilityDetails(ActiveClues a) {
		float[] outcomes = getClassificationProbabilities(a);
		StringBuffer sb = new StringBuffer("<maxEntClasses>");
		sb.append("<outcome class=\"differ\" probability=\"")
				.append(outcomes[IDX_DIFFER]).append("\"/>");
		sb.append("<outcome class=\"match\" probability=\"")
				.append(outcomes[IDX_MATCH]).append("\"/>");
		if (outcomes.length > IDX_HOLD) {
			sb.append("<outcome class=\"hold\" probability=\"")
					.append(outcomes[IDX_HOLD]).append("\"/>");
		}
		sb.append("</maxEntClasses>");
		String retVal = sb.toString();
		return retVal;
	}

	/**
	 * Returns a signature based on the class of this instance and its weights.
	 */
	public String getSignature() {
		String retVal = Signature.calculateSignature(getClass());
		StringBuilder sb = new StringBuilder(retVal);
		for (int i = 0; i < weights.length; i++) {
			sb.append(Signature.calculateSignature(weights[i]));
		}
		return retVal;
	}

}
