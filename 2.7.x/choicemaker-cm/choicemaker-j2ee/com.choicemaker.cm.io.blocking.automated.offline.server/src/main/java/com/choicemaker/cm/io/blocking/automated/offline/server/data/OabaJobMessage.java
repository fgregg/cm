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


import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
//import com.choicemaker.cm.core.SerializableRecordSource;
//import com.choicemaker.cm.io.blocking.automated.offline.data.Status;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;

/**
 * This is the data object that gets passed to the StartOabaMDB message bean.
 * 
 * @author pcheung
 *
 */
public class OabaJobMessage implements Serializable {

	static final long serialVersionUID = 271;

	public final long jobID;

	public int numBlockFields;
	public int numChunks;
	public int numRegularChunks;

	// indicates the type of staging record id
	public RECORD_ID_TYPE stageType;

	// indicates the type of master record id
	public RECORD_ID_TYPE masterType;

	public ValidatorBase validator;

	/**
	 * This tells matcher which chunk to process.
	 */
	public int ind;
	// the ith tree/array file of the chunk
	public int treeInd;

	// constructor
	public OabaJobMessage(long jobId) {
		this.jobID = jobId;
	}

	// copy constructor
	public OabaJobMessage(OabaJobMessage data) {
		this.jobID = data.jobID;
		this.ind = data.ind;
		this.stageType = data.stageType;
		this.masterType = data.masterType;
		this.numChunks = data.numChunks;
		this.treeInd = data.treeInd;
		this.numRegularChunks = data.numRegularChunks;
		this.validator = data.validator;
	}

	// create MatchWriterMessage from OabaJobMessage
	public OabaJobMessage(MatchWriterMessage data) {
		this.jobID = data.jobID;
		this.ind = data.ind;
		this.stageType = data.stageType;
		this.masterType = data.masterType;
		this.numChunks = data.numChunks;
		this.treeInd = data.treeInd;
		this.numRegularChunks = data.numRegularChunks;
	}

	@Override
	public String toString() {
		return "OabaJobMessage [jobID=" + jobID + "]";
	}

}
