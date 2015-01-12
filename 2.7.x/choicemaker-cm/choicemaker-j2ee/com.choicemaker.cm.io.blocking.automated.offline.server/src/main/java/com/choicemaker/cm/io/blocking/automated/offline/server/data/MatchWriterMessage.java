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
package com.choicemaker.cm.io.blocking.automated.offline.server.data;

import java.io.Serializable;
import java.util.List;

import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * @author pcheung
 */
@SuppressWarnings("rawtypes")
public class MatchWriterMessage implements Serializable {

	static final long serialVersionUID = 271;

	public final long jobID;

	/** The chunk id */
	public int ind;

	/** The ith tree/array file of the chunk */
	public int treeInd;

	public int numRegularChunks;

	/** Indicates the type of staging record id */
	public RECORD_ID_TYPE stageType;

	/** Indicates the type of master record id */
	public RECORD_ID_TYPE masterType;

	// keep track of total comparison and matches
	public boolean doneMatch;
	public int numCompares;
	public int numMatches;

	// time keepers
	public long timeWriting;
	public long inLookup;
	public long inCompare;

	/** Matches to be written */
	public List<MatchRecord2> matches;

	public MatchWriterMessage(long jobId) {
		this.jobID = jobId;
	}

	/** Constructs MatchWriterMessage from OabaJobMessage */
	public MatchWriterMessage(OabaJobMessage data) {
		this.jobID = data.jobID;
		this.ind = data.ind;
		this.stageType = data.stageType;
		this.masterType = data.masterType;
		this.treeInd = data.treeInd;
		this.numRegularChunks = data.numRegularChunks;
	}

}
