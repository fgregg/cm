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
package com.choicemaker.cm.urm.base;

import java.io.Serializable;


/**
 * A pair of records with their match score.
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:28:34 PM
 * @see
 */
public class EvaluatedRecordPair implements Serializable{

	/* As of 2010-03-10 */
	static final long serialVersionUID = -4468461856258743939L;

	IRecord			record1;
	IRecord			record2;
	IMatchScore		score;
	
//	public EvaluatedRecordPair() {
//		super();
//	}
		
	public EvaluatedRecordPair(IRecord r1, IRecord r2, IMatchScore	score) {
		super();
		this.record1 = r1;
		this.record2 = r2;
		this.score = score;
	}

	public IRecord getRecord1() {
		return record1;
	}

	public IMatchScore getScore() {
		return score;
	}

	public IRecord getRecord2() {
		return record2;
	}

	public void setRecord1(IRecord record) {
		record1 = record;
	}

	public void setRecord2(IRecord record) {
		record2 = record;
	}

	public void setScore(IMatchScore score) {
		this.score = score;
	}

}
