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
package com.choicemaker.cm.transitivity.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;

/**
 * This session bean allows the user to start, query, and get result from the
 * TE. It is to be used with the OABA.
 * 
 * @author pcheung
 *
 */
@Local
public interface TransitivityService {

	String DEFAULT_EJB_REF_NAME = "ejb/TransitivityService";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	/**
	 * This method starts transitivity analysis of the specified OABA job. The
	 * OABA job must have completed successfully.
	 * 
	 * @throws ServerConfigurationException
	 */
	public long startTransitivity(String externalID,
			TransitivityParameters batchParams, OabaJob oabaJob,
			ServerConfiguration serverConfiguration)
			throws ServerConfigurationException;

	/**
	 * This method queries the current status of a transitivity job.
	 * 
	 * @param jobID
	 *            the id of the transitivity job
	 * @return BatchJobStatus
	 */
	public BatchJobStatus getStatus(long jobID);

}
