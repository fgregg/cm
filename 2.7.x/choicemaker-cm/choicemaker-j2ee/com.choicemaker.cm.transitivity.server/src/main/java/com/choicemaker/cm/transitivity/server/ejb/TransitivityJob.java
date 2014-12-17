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

import com.choicemaker.cm.batch.BatchJob;

/**
 * @author rphall
 */
public interface TransitivityJob extends BatchJob {

	String DEFAULT_EJB_REF_NAME = "ejb/TransitivityJob";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	// -- Accessors

	long getTransitivityParametersId();

	long getTransitivitySettingsId();

	long getServerId();

	long getUrmId();

}
