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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (jobID ^ (jobID >>> 32));
		result = prime * result + percentComplete;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OabaUpdateMessage other = (OabaUpdateMessage) obj;
		if (jobID != other.jobID) {
			return false;
		}
		if (percentComplete != other.percentComplete) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OabaUpdateMessage [jobID=" + jobID + ", percentComplete="
				+ percentComplete + "]";
	}

}
