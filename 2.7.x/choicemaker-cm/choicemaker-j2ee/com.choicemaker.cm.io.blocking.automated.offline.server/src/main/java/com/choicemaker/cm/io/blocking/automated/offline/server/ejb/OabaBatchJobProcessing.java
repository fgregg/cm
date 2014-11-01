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

import java.util.Date;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;

/**
 * Extends the OabaProcessing interface by adding:<ul>
 * <li>A {@link #getId() persistence identifier}</li>
 * <li>The {@link #getJobId() persistence identifier} of the batch job associated with a status entry</li>
 * <li>A {@link #getTimestamp() timestamp} for when the status entry was recorded</li>
 * <li>An unused and deprecated {@link #getVersion() class version identifier}</li> 
 * </ul>
 * @author pcheung
 *
 */
public interface OabaBatchJobProcessing extends OabaProcessing {
	
	String DEFAULT_EJB_REF_NAME = "ejb/OabaBatchJobProcessing";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME ;
	
	long INVALID_ID = 0;

	long VERSION = 0;
	
	long getId();

	long getJobId();
	
	Date getTimestamp();

	@Deprecated
	long getVersion();
}
