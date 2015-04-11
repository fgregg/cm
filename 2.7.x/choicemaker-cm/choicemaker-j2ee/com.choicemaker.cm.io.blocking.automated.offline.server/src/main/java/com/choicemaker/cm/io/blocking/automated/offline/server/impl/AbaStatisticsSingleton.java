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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.PersistableSqlRecordSource;
import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.AbaStatistics;
import com.choicemaker.cm.io.blocking.automated.base.db.DbbCountsCreator;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.AbaStatisticsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SqlRecordSourceController;
import com.choicemaker.cm.io.db.base.DatabaseAbstraction;
import com.choicemaker.cm.io.db.base.DatabaseAbstractionManager;

@Singleton
public class AbaStatisticsSingleton implements AbaStatisticsController {

	private static final Logger log = Logger
			.getLogger(MatchSchedulerSingleton.class.getName());

	// -- Injected data

	@EJB
	private SqlRecordSourceController sqlRSController;

	// -- Accessors

	protected final SqlRecordSourceController getSqlRecordSourceController() {
		return sqlRSController;
	}

	/** Map of model configuration names to ABA statistics */
	private Map<String, AbaStatistics> cachedStats = new Hashtable<>();

	@Override
	public void updateMasterAbaStatistics(OabaParameters params)
			throws DatabaseException {
		final long rsId = params.getReferenceRsId();
		final String type = PersistableSqlRecordSource.TYPE;
		PersistableSqlRecordSource rs =
			this.getSqlRecordSourceController().find(rsId, type);
		String dsJndiUrl = rs.getDataSource();
		updateAbaStatistics(dsJndiUrl);
	}

	@Override
	public void updateAbaStatistics(String urlString) throws DatabaseException {
		DataSource ds = null;
		log.fine("url" + urlString);
		if (urlString == null || urlString.length() == 0)
			throw new IllegalArgumentException("empty DataSource url");
		Context ctx;
		try {
			ctx = new InitialContext();
			Object o = ctx.lookup(urlString);
			ds = (DataSource) o;
		} catch (NamingException e) {
			String msg =
				"Unable to acquire DataSource from JNDI URL '" + urlString
						+ "': " + e;
			log.severe(msg);
			throw new DatabaseException(msg, e);
		}
		DatabaseAbstractionManager mgr =
			new AggregateDatabaseAbstractionManager();
		DatabaseAbstraction dba = mgr.lookupDatabaseAbstraction(ds);
		DbbCountsCreator countsCreator = new DbbCountsCreator();
		try {
			countsCreator.install(ds);
			final boolean neverComputeOnly = false;
			final boolean commitChanges = false;
			countsCreator.create(ds, dba, neverComputeOnly, commitChanges);
			countsCreator.setCacheCountSources(ds, dba, this);
		} catch (SQLException e) {
			String msg =
				"Unable to compute ABA statistics for '" + urlString + "': "
						+ e;
			log.severe(msg);
			throw new DatabaseException(msg, e);
		}
	}

	@Override
	public void putStatistics(ImmutableProbabilityModel model,
			AbaStatistics counts) {
		final String METHOD = "AbaStatisticsSingleton.putStatistics: ";
		if (model == null || counts == null) {
			String msg = METHOD + "null constructor argument";
			throw new IllegalArgumentException(msg);
		}
		String name = model.getModelName();
		assert name != null && name.equals(name.trim()) && !name.isEmpty();
		this.cachedStats.put(name, counts);
	}

	@Override
	public AbaStatistics getStatistics(ImmutableProbabilityModel model) {
		final String METHOD = "AbaStatisticsSingleton.getStatistics: ";
		if (model == null) {
			String msg = METHOD + "null model";
			throw new IllegalArgumentException(msg);
		}
		String name = model.getModelName();
		assert name != null && name.equals(name.trim()) && !name.isEmpty();
		AbaStatistics retVal = this.cachedStats.get(name);
		assert retVal != null;
		return retVal;
	}

}
