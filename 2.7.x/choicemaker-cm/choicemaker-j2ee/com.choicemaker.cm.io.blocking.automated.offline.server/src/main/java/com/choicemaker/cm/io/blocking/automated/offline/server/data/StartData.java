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

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.data.Status;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;

/**
 * This is the data object that gets passed to the StartOABA message bean.
 * 
 * @author pcheung
 *
 */
public class StartData implements Serializable {

	static final long serialVersionUID = 271;

	public final long jobID;

	public int numBlockFields;
	public int numChunks;
	public int numRegularChunks;

	// indicates the type of staging record id
	public int stageType;

	// indicates the type of master record id
	public int masterType;

	public ValidatorBase validator;

	/**
	 * This tells matcher which chunk to process.
	 */
	public int ind;
	// the ith tree/array file of the chunk
	public int treeInd;

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

	// constructor
	public StartData(long jobId) {
		this.jobID = jobId;
		this.maxCountSingle = -1;
		this.low = -1f;
		this.high = -1f;
		this.staging = null;
		this.master = null;
		this.modelConfigurationName = null;
		this.runTransitivity = false;
	}

	// copy constructor
	public StartData(StartData data) {
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
		this.runTransitivity = data.runTransitivity;
		this.maxCountSingle = data.maxCountSingle;
		this.validator = data.validator;
		this.staging = data.staging;
		this.master = data.master;
	}

	// create MatchWriterData from StartData
	public StartData(MatchWriterData data) {
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
		this.maxCountSingle = -1;
	}

}
