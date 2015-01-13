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

	public ValidatorBase validator;

	/**
	 * An index used to split processing across a set of agents that are
	 * running in parallel.
	 */
	public int processingIndex;

	/**
	 * An index used to assign a Matcher to set of records within a chunk.
	 */
	public int treeIndex;

	// constructor
	public OabaJobMessage(long jobId) {
		this.jobID = jobId;
	}

	// copy constructor
	public OabaJobMessage(OabaJobMessage data) {
		this.jobID = data.jobID;
		this.processingIndex = data.processingIndex;
		this.treeIndex = data.treeIndex;
		this.validator = data.validator;
	}

	// create MatchWriterMessage from OabaJobMessage
	public OabaJobMessage(MatchWriterMessage data) {
		this.jobID = data.jobID;
		this.processingIndex = data.processingIndex;
		this.treeIndex = data.treeIndex;
	}

	@Override
	public String toString() {
		return "OabaJobMessage [jobID=" + jobID + "]";
	}

}
