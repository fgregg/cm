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
 * Statistics for the online/real-time ABA (Automated Batch Algorithm) are
 * matching parameters that change relatively infrequently. Prior to version 2.7
 * of ChoiceMaker, these statistics were stored with each instance of a model
 * configuration. However, since statistics depend only on the schema of a
 * model, and since many model configurations can share the same schema, it
 * makes more sense to store ABA statistics in an object that is separate from
 * any particular model. In a J2EE environment, ABA statistics are cached in
 * memory by an EJB singleton service.
 * 
 * @see com.choicemaker.cm.io.blocking.automated.base.db.DefaultAbaStatisticsBean
 * 
 * @author rphall
 *
 */
public interface PersistentAbaStatistics extends Serializable {

	/** Default id value for non-persistent settings */
	long NONPERSISTENT_ABA_STATISTICS_ID = 0;

	/**
	 * The persistence identifier for an instance. If the value is
	 * {@link #NONPERSISTENT_ABA_STATISTICS_ID}, then the settings are not
	 * persistent.
	 */
	long getId();

	/**
	 * Statistics for the online Automated Blocking Algorithm. ABA statistics
	 * are specific to a particular database and blocking configuration of a
	 * given model
	 * 
	 * @param model
	 *            A configured model (loaded as a plugin)
	 * @param databaseConfigurationName
	 *            the name of a database configuration specified by the model
	 *            schema
	 * @param blockingConfigurationName
	 *            the name of a blocking configuration specified by the model
	 *            schema
	 * @return
	 */
	AbaStatistics getCountSource(ImmutableProbabilityModel model,
			String databaseConfigurationName, String blockingConfigurationName);

}
