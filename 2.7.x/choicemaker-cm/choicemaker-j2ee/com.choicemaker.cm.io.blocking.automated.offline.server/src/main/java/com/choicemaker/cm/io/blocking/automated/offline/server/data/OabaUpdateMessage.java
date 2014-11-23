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

/**
 * This is the data object that gets passed to the UpdateStatus message bean.
 * 
 * @author pcheung
 *
 */
public class OabaUpdateMessage implements Serializable {

	static final long serialVersionUID = 271;

	private final long jobID;
	private final int percentComplete;
	
	public OabaUpdateMessage(long jobId, int percentComplete) {
		this.jobID = jobId;
		this.percentComplete = percentComplete;
	}

	public long getJobID() {
		return jobID;
	}

	public int getPercentComplete() {
		return percentComplete;
	}

}
