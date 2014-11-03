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
package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import com.choicemaker.cm.core.SerialRecordSource;

/**
 * @author pcheung
 *
 */
public interface BatchParameters {

	String DEFAULT_EJB_REF_NAME = "ejb/BatchParameters";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	/** Default value when no jobId is assigned */
	public static final long INVALID_PARAMSID = 0;

	long getId();

	String getModelConfigurationName();

	String getStageModel();

	String getMasterModel();

	int getMaxSingle();

	float getLowThreshold();

	float getHighThreshold();

	SerialRecordSource getStageRs();

	SerialRecordSource getMasterRs();

	boolean getTransitivity();

}
