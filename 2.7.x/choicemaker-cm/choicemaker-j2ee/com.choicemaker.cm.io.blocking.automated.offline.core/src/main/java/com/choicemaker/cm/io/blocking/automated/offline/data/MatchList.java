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
package com.choicemaker.cm.io.blocking.automated.offline.data;

import java.io.Serializable;

import com.choicemaker.cm.core.base.MatchCandidate;

/**
 * This object holds a record ID and a list of MatchCandidate objects
 * 
 * @author pcheung
 *
 */
public class MatchList implements Serializable{

	/* As of 2010-03-10 */
	static final long serialVersionUID = 5330826815562783479L;

	private long recordID;
	private MatchCandidate [] candidates;
	
	public MatchList (long id, MatchCandidate [] candidates) {
		this.recordID = id;
		this.candidates = candidates;
	}
	
	public long getRecordID () { return recordID; }

	public MatchCandidate [] getMatchCandidates () { return candidates; }

}
