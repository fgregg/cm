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
package com.choicemaker.demo.oaba0;

import java.io.Serializable;

import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;

/**
 * This is the data object that gets passed to the StartOABA message bean.
 * 
 * @author pcheung
 *
 */
public class StartData implements Serializable {

	static final long serialVersionUID = 271L;

	public long jobID;
	public int numBlockFields;
	public int numChunks;
	public int numRegularChunks;
	
	//If the number of staging records is below this threshold, single record match is used.
	public int maxCountSingle;
	
	//indicates the type of staging record id
	public int stageType;
	
	//indicates the type of master record id
	public int masterType;
	
	public float low;
	public float high;
	public ISerializableRecordSource staging;
	public ISerializableRecordSource master;
	public String stageModelName;
	public String masterModelName;

//	public String dbConf;
//	public String blockingConf;

	public ValidatorBase validator;
	
	//transitivity flag
	public boolean runTransitivity;

	/**
	 *  This tells matcher which chunk to process.
	 */
	public int ind;
	//the ith tree/array file of the chunk
	public int treeInd;
	
	
	//default constructor
	public StartData () {
	}
	
	
	//copying contructor
	public StartData (StartData data) {
		this.jobID = data.jobID;
		this.stageModelName = data.stageModelName;
		this.masterModelName = data.masterModelName;
		this.ind = data.ind;
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


	//create MatchWriterData from StartData
	public StartData (MatchWriterData data) {
		this.jobID = data.jobID;
		this.stageModelName = data.stageModelName;
		this.masterModelName = data.masterModelName;
		this.ind = data.ind;
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


	@Override
	public String toString() {
		return "StartData [jobID=" + jobID + ", runTransitivity="
				+ runTransitivity + "]";
	}

}
