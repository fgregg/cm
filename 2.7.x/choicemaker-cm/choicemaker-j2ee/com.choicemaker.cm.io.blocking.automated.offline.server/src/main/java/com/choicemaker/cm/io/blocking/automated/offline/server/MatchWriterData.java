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
package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.io.Serializable;
import java.util.List;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.Status;

/**
 * @author pcheung
 * 
 *
 */
public class MatchWriterData implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 9096483901953216037L;

	public long jobID;

	public String stageModelName;
	public String masterModelName;
	
	public SerialRecordSource staging;
	public SerialRecordSource master;

	/** The chunk id */
	public int ind;

	/** The ith tree/array file of the chunk */
	public int treeInd;
	
	public int numChunks;
	public int numRegularChunks;
	
	public Status status;
	
	/** Indicates the type of staging record id */
	public int stageType;
	
	/** Indicates the type of master record id */
	public int masterType;
	
	public float low;
	public float high;
	
	/** Transitivity flag */
	public boolean runTransitivity;

	//keep track of total comparison and matches
	public boolean doneMatch;
	public int numCompares;
	public int numMatches;
	
	//time keepers
	public long timeWriting;
	public long inLookup;
	public long inCompare;
	
	/** Matches to be written */
	public List<MatchRecord2> matches;
	
	public MatchWriterData () {
	}
	
	/** Constructs MatchWriterData from StartData */
	public MatchWriterData (StartData data) {
		this.jobID = data.jobID;
		this.stageModelName = data.stageModelName;
		this.masterModelName = data.masterModelName;
		this.ind = data.ind;
		this.status = data.status;
		this.stageType = data.stageType;
		this.masterType = data.masterType;
		this.high = data.high;
		this.low = data.low;
		this.numChunks = data.numChunks;
		this.treeInd = data.treeInd;
		this.numRegularChunks = data.numRegularChunks;
		this.staging = data.staging;
		this.master = data.master;
		this.runTransitivity = data.runTransitivity;
	}
	
}
