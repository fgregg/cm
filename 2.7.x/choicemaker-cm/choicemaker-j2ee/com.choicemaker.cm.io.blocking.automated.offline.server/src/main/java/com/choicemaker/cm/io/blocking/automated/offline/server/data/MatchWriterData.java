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

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.Status;

/**
 * @author pcheung
 * 
 *
 */
@SuppressWarnings("rawtypes")
public class MatchWriterData implements Serializable {

	static final long serialVersionUID = 271;

	public final long jobID;

	/** The chunk id */
	public int ind;

	/** The ith tree/array file of the chunk */
	public int treeInd;
	
	public int numChunks;
	public int numRegularChunks;
	
	/** Indicates the type of staging record id */
	public int stageType;
	
	/** Indicates the type of master record id */
	public int masterType;
	
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
	
	@Deprecated
	public final int maxCountSingle;
	@Deprecated
	public final float low;
	@Deprecated
	public final float high;
	@Deprecated
	public final SerializableRecordSource staging;
	@Deprecated
	public final SerializableRecordSource master;
	@Deprecated
	public final String modelConfigurationName;
	@Deprecated
	public Status status;
	@Deprecated
	public final boolean runTransitivity;

	public MatchWriterData (long jobId) {
		this.jobID = jobId;
		this.maxCountSingle = -1;
		this.low = -1f;
		this.high = -1f;
		this.staging = null;
		this.master = null;
		this.modelConfigurationName = null;
		this.runTransitivity = false;
	}
	
	/** Constructs MatchWriterData from StartData */
	public MatchWriterData (StartData data) {
		this.jobID = data.jobID;
		this.modelConfigurationName = data.modelConfigurationName;
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
		this.maxCountSingle = data.maxCountSingle;
	}
	
}
