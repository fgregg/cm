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
package com.choicemaker.cm.reviewmaker.base;

import java.io.Serializable;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.Record;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/04/15 20:51:55 $
 */
public class MatchRecord implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -8148681366848784045L;

	private Decision choiceMakerDecision;
	private float probability;
	private Record record;

	public MatchRecord(Decision choiceMakerDecision, float probability, Record record) {
		this.choiceMakerDecision = choiceMakerDecision;
		this.probability = probability;
		this.record = record;
	}

	/**
	 * Returns the choiceMakerDecision.
	 * @return Decision
	 */
	public Decision getChoiceMakerDecision() {
		return choiceMakerDecision;
	}

	/**
	 * Returns the probability.
	 * @return float
	 */
	public float getProbability() {
		return probability;
	}

	/**
	 * Returns the record.
	 * @return Record
	 */
	public Record getRecord() {
		return record;
	}

}
