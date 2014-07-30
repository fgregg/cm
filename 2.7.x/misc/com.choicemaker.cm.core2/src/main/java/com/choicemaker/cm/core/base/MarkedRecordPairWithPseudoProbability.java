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
package com.choicemaker.cm.core.base;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.Record;

/** Deprecated */
public class MarkedRecordPairWithPseudoProbability extends MutableRecordPair {
	/** The <code>Decision</code> whether this pair matches or not. */
	public Decision decision;

	/** The Pseudo Probability of the pair. */
	public float pseudoProbability;

	/** The Rule Decision of the pair. */
	public String ruleDecision;

	public MarkedRecordPairWithPseudoProbability() {
	}

	/**
	 * Constructor for a marked record pair with pseudo probability.
	 *
	 * @param   pseudoProbability  The Pseudo Probability of the pair.
	 * @param   ruleDecision  The Rule Decision of the pair.
	 */
	public MarkedRecordPairWithPseudoProbability(
		Record q,
		Record m,
		Decision decision,
		float pseudoProbability,
		String ruleDecision) {
			super(q, m);
			this.decision = decision;
			this.pseudoProbability = pseudoProbability;
			this.ruleDecision = ruleDecision;
	}

}
