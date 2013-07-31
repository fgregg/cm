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
 * A replacement for the deprecated RecordPair class, which
 * allows direct access to field members.
 * @author rphall
 * @version   $Revision: 1.1 $ $Date: 2010/03/24 18:02:30 $
 */
public class MutableRecordPair extends RecordData implements IRecordPair {

	/**
	 * The clues that fired on a pair.
	 * @deprecated use get/setProbability instead. This is field
	 * is still used by generated code, but shouldn't used elsewhere.
	 */
	private ActiveClues af;

	/**
	 * The decision assigned by ChoiceMaker.
	 * @deprecated use get/setCmDecision instead. This is field
	 * is still used by generated code, but shouldn't used elsewhere.
	 */
	private Decision cmDecision;

	/**
	 * The other record, usually called the match record.
	 * @deprecated use get/setMatchRecord() instead. This is field
	 * is still used by generated code, but shouldn't used elsewhere.
	 */
	private Record m;

	/**
	 * The probability assigned by ChoiceMaker.
	 * @deprecated use get/setProbability instead. This is field
	 * is still used by generated code, but shouldn't used elsewhere.
	 */
	private float probability;

	/**
	 * One of the records, usually called the query record.
	 * @deprecated use get/setQueryRecord() instead. This is field
	 * is still used by generated code, but shouldn't used elsewhere.
	 */
	private Record q;

	public MutableRecordPair() {
	}

	/**
	 * Constructor.
	 *
	 * @param   q  One of the records.
	 * @param   m  The other record.
	 */
	public MutableRecordPair(Record q, Record m) {
		setQueryRecord(q);
		setMatchRecord(m);
	}

	public ActiveClues getActiveClues() {
		return af;
	}

	public Decision getCmDecision() {
		return cmDecision;
	}

	/**
	 * @see com.choicemaker.cm.core.RecordData#getFirstRecord()
	 */
	public Record getFirstRecord() {
		return getQueryRecord();
	}

	public Record getMatchRecord() {
		return m;
	}

	public float getProbability() {
		return probability;
	}

	public Record getQueryRecord() {
		return q;
	}

	/**
	 * @see com.choicemaker.cm.core.RecordData#getSecondRecord()
	 */
	public Record getSecondRecord() {
		return getMatchRecord();
	}

	public void setActiveClues(ActiveClues af) {
		this.af = af;
	}

	public void setCmDecision(Decision cmDecision) {
		this.cmDecision = cmDecision;
	}

	public void setMatchRecord(Record m) {
		this.m = m;
	}

	public void setProbability(float probability) {
		this.probability = probability;
	}

	public void setQueryRecord(Record q) {
		this.q = q;
	}

}

