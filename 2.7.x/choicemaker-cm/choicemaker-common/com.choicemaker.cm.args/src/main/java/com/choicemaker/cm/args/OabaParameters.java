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
package com.choicemaker.cm.args;

import java.io.Serializable;

/**
 * @author pcheung
 *
 */
public interface OabaParameters {

	String DEFAULT_EJB_REF_NAME = "ejb/OabaParameters";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	long getId();

	String getModelConfigurationName();
	
	OabaJobType getOabaJobType();

	String getStageModel();

	String getMasterModel();

	float getLowThreshold();

	float getHighThreshold();

	/** Returns a <code>SerializableRecordSource</code> for staging records */
	Serializable getStageRs();

	/** Returns a <code>SerializableRecordSource</code> for master records */
	Serializable getMasterRs();
	
}
