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

import java.io.Serializable;

import com.choicemaker.cm.core.ImmutableProbabilityModel;

/**
 * Settings for the online/real-time ABA (Automated Batch Algorithm) are
 * matching parameters that change relatively infrequently. Prior to version 2.7
 * of ChoiceMaker, these settings were recorded as properties of the
 * {@link ImmutableProbabilityModel} interface, but in practice, separating them
 * in their interface makes it clear that their value rarely change and are
 * typically reused between many matching jobs.
 * 
 * @see com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultAbaSettingsBean
 * 
 * @author rphall
 *
 */
public interface AbaSettings extends Serializable {

	int DEFAULT_LIMIT_PER_BLOCKING_SET = 50;
	int DEFAULT_LIMIT_SINGLE_BLOCKING_SET = 100;
	int DEFAULT_SINGLE_TABLE_GRACE_LIMIT = 200;

	/** Default id value for non-persistent settings */
	long NONPERSISTENT_ABA_SETTINGS_ID = 0;

	/**
	 * The persistence identifier for an instance. If the value is
	 * {@link #NONPERSISTENT_ABA_SETTINGS_ID}, then the settings are not
	 * persistent.
	 */
	long getId();

	/**
	 * The maximum of size of a blocking set before it must be refined by
	 * qualifying it with additional blocking values.
	 */
	int getLimitPerBlockingSet();

	/**
	 * A special exemption to the {@link #getLimitPerBlockingSet() general limit
	 * on blocking set size}
	 */
	int getLimitSingleBlockingSet();

	/**
	 * Another special exemption to the {@link #getLimitPerBlockingSet() general
	 * limit on blocking set size}
	 */
	int getSingleTableBlockingSetGraceLimit();

}
