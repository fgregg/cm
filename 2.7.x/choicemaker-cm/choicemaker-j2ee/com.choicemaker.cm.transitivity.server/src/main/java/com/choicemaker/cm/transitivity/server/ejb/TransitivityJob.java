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

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

/**
 * @author pcheung
 *
 */
public interface TransitivityJob extends OabaJob {
	
	// -- Deprecated constants

	String DEFAULT_EJB_REF_NAME = "ejb/TransitivityJob";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	@Deprecated
	String getModel();
	@Deprecated
	float getDiffer();
	@Deprecated
	float getMatch();
	@Deprecated
	void setModel(String stageModelName);
	@Deprecated
	void setMatch(float high);
	@Deprecated
	void setDiffer(float low);

}
