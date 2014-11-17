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
package com.choicemaker.cm.io.blocking.automated;

import com.choicemaker.cm.core.ImmutableProbabilityModel;

/**
 * Settings for the online/real-time ABA (Automated Batch Algorithm) are
 * matching parameters that change relatively infrequently. Prior to version 2.7
 * of ChoiceMaker, these settings were recorded as properties of the
 * {@link ImmutableProbabilityModel} interface, but in practice, separating them
 * in their interface makes it clear that their value rarely change and are
 * typically reused between many matching jobs.
 * 
 * @author rphall
 *
 */
public interface AutomatedBlockingSettings {

	/** Default id value for non-persistent settings */
	long NONPERSISTENT_ABA_SETTINGS_ID = 0;

	/**
	 * The persistence identifier for an instance. If the value is
	 * {@link #NONPERSISTENT_ABA_SETTINGS_ID}, then the settings are not
	 * persistent.
	 */
	long getId();

}
