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
 * A record with an assigned match score. Match score is assigned based on the comparison with the query
 * record that is provided by the client application. The query record is not the part of this class and it is
 * assumed to be known to the client application.
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 11:21:17 AM
 * @see
 */
public class EvaluatedRecord implements Serializable{

	/* As of 2010-03-10 */
	static final long serialVersionUID = 3355890340990553896L;

	protected IRecord record;
	protected IMatchScore score;

	/**
	 * @param record
	 * @param score
	 */
	public EvaluatedRecord(IRecord record, IMatchScore score) {
		super();
		this.record = record;
		this.score = score;
	}

	/**
	 * <p>
	 * 
	 * @return
	 */
	public IRecord getRecord() {
		return record;
	}

	/**
	 * <p>
	 * 
	 * @return
	 */
	public IMatchScore getScore() {
		return score;
	}

	/**
	 * <p>
	 * 
	 * @param record
	 */
	public void setRecord(IRecord record) {
		this.record = record;
	}

	/**
	 * <p>
	 * 
	 * @param score
	 */
	public void setScore(IMatchScore score) {
		this.score = score;
	}

}
