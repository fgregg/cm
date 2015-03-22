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

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.io.blocking.automated.cachecount.CacheCountSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SqlRecordSourceController;
import com.choicemaker.cm.server.util.CountsUpdate;

@Singleton
public class AbaStatisticsSingleton implements Serializable {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger
			.getLogger(MatchSchedulerSingleton.class.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatchSchedulerSingleton.class.getName());

	// -- Injected data

	@EJB
	private OabaJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private SqlRecordSourceController sqlRSController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OperationalPropertyController propertyController;

	@EJB
	private ProcessingController processingController;

	@EJB
	private RecordSourceController rsController;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupQueue")
	private Queue matchDedupQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/matcherQueue")
	private Queue matcherQueue;

	@Inject
	private JMSContext jmsContext;

	// -- Callbacks

	protected OabaJobController getJobController() {
		return jobController;
	}

	protected OabaParametersController getOabaParametersController() {
		return paramsController;
	}

	protected final RecordSourceController getRecordSourceController() {
		return rsController;
	}

	protected final SqlRecordSourceController getSqlRecordSourceController() {
		return sqlRSController;
	}

	protected ServerConfigurationController getServerController() {
		return serverController;
	}

	protected OabaSettingsController getSettingsController() {
		return oabaSettingsController;
	}

	protected OperationalPropertyController getPropertyController() {
		return propertyController;
	}

	protected ProcessingController getProcessingController() {
		return processingController;
	}

	protected Logger getLogger() {
		return log;
	}

	protected Logger getJMSTrace() {
		return jmsTrace;
	}
	
	protected static class StatKey {
		private final String modelName;
		private final String databaseConfiguration;
		private final String blockingConfiguration;
		private final RECORD_SOURCE_ROLE role;
	}
	
	/** Map of model configuration names to ABA statistics */
	private Map<String, CacheCountSource[]> cachedStats = new Hashtable<>();

	public DataSource upateMasterAbaStatistics(OabaParameters params) throws BlockingException {
	}

	public DataSource upateMasterAbaStatistics(OabaParameters params) throws BlockingException {
		
	// Get the data sources for ABA queries
	DataSource stageDS = null;
	try {
		stageDS = getSqlRecordSourceController().getStageDataSource(params);
	} catch (BlockingException e) {
		String msg = "Unable to acquire data source: " + e;
		log.severe(msg);
		throw e;
	}
	assert stageDS != null;

	// Cache ABA statistics for field-value counts
	log.info("Caching ABA statistic for staging records..");
	try {
		CountsUpdate update = new CountsUpdate();
		update.cacheCounts(stageDS);
	} catch (DatabaseException e) {
		String msg = "Unable to cache ABA statistics: " + e;
		log.severe(msg);
		throw new BlockingException(msg);
	}
	log.info("... finished caching ABA statistics for staging records.");

	DataSource masterDS = null;
	try {
		masterDS =
			getSqlRecordSourceController().getMasterDataSource(params);
	} catch (BlockingException e) {
		String msg = "Unable to acquire data source: " + e;
		log.severe(msg);
		throw e;
	}
	assert masterDS != null;
	
	return masterDS;
	}

	public DataSource getMasterDataSource(OabaParameters params) throws BlockingException {
		
	// Get the data sources for ABA queries
	DataSource stageDS = null;
	try {
		stageDS = getSqlRecordSourceController().getStageDataSource(params);
	} catch (BlockingException e) {
		String msg = "Unable to acquire data source: " + e;
		log.severe(msg);
		throw e;
	}
	assert stageDS != null;

	// Cache ABA statistics for field-value counts
	log.info("Caching ABA statistic for staging records..");
	try {
		CountsUpdate update = new CountsUpdate();
		update.cacheCounts(stageDS);
	} catch (DatabaseException e) {
		String msg = "Unable to cache ABA statistics: " + e;
		log.severe(msg);
		throw new BlockingException(msg);
	}
	log.info("... finished caching ABA statistics for staging records.");

	DataSource masterDS = null;
	try {
		masterDS =
			getSqlRecordSourceController().getMasterDataSource(params);
	} catch (BlockingException e) {
		String msg = "Unable to acquire data source: " + e;
		log.severe(msg);
		throw e;
	}
	assert masterDS != null;
	
	return masterDS;
	}

	public void updateCounts(String probabilityModel) {
		DataSource ds = null;
		try {
			log.fine("url" + urlString);
			if (urlString == null || urlString.length() == 0)
				throw new RecordCollectionException("empty url");
			Context ctx = new InitialContext();
			Object o = ctx.lookup(urlString);
			ds = (DataSource) o;
			// <BUGFIX>
			// 2008-12-04 rphall
			// This public method should force updates on all
			// counts, since that's what a user probably intends.
			// See the old com.choicemaker.cm.server.ejb.impl.AdminServiceBean
			// which forces an update on all counts in the updateCounts method.
			// NOTE: This fix has not been implemented yet, even though it is simple.
			//
			//new CountsUpdate().updateCounts(ds, true);
			// </BUGFIX>
			new CountsUpdate().updateCounts(ds, false);
		} catch (NamingException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		} catch (DatabaseException e) {
			log.severe(e.toString());
			throw new RecordCollectionException(e.toString());
		}
	}

}
