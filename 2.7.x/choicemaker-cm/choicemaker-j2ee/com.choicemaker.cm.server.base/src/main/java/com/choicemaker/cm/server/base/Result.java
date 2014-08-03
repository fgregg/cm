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
package com.choicemaker.cm.server.base;

import java.io.Serializable;

import com.choicemaker.cm.core.base.MatchCandidate;

/**
 * The result of a match query.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:54:40 $
 */
public class Result implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -482416310186293156L;

	private MatchCandidate[] matchCandidates;
	
	/**
	 * Constructs an empty result.
	 */
	public Result() { }
	
	/**
	 * Constructs a <code>Result</code> holding the specified match candidates.
	 * Note that the match candidates are not cloned by this constructor.
	 * 
	 * @param   matchCandidates  The match candidates, i.e., the records that match the query record.
	 */
	public Result(MatchCandidate[] matchCandidates) {
		this.matchCandidates = matchCandidates;
	}
	
	/**
	 * Returns the match candidates.
	 * 
	 * @return  The match candidates.
	 */
	public MatchCandidate[] getMatchCandidates() {
		return matchCandidates;
	}
	
	/**
	 * Sets the match candidates.
	 * 
	 * @param matchCandidates  the records that match the query
	 */
	public void setMatchCandidates(MatchCandidate[] matchCandidates) {
		this.matchCandidates = matchCandidates;
	}
}
