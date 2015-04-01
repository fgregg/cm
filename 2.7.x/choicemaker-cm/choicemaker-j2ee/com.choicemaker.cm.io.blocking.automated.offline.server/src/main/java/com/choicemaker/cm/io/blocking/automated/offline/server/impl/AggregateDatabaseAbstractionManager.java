package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.io.db.base.DatabaseAbstraction;
import com.choicemaker.cm.io.db.base.DatabaseAbstractionManager;

/**
 * A hard-coded implementation of DatabaseAbstractionManager that aggregates
 * managers known at compile time.
 * <p>
 * FIXME replace with a plugin-based manager
 * 
 * @author rphall
 * @deprecated
 */
public class AggregateDatabaseAbstractionManager implements
		DatabaseAbstractionManager {

	private static final Logger logger = Logger
			.getLogger(AggregateDatabaseAbstractionManager.class.getName());

	protected static final String JBOSS_DBA_MANAGER =
			"com.choicemaker.cm.io.db.jboss.JBossDatabaseAbstractionManager";

	protected static final String ORACLE_DBA_MANAGER =
		"com.choicemaker.cm.io.db.oracle.blocking.OraDatabaseAbstractionManager";

	protected static final String SQLSERVER_DBA_MANAGER =
		"com.choicemaker.cm.io.db.sqlserver.blocking.SqlServerDatabaseAbstractionManager";

	protected static Class<? extends DatabaseAbstractionManager> resolveDatabaseAbstractionManagerClass(
			String className) throws ClassNotFoundException {
		@SuppressWarnings("unchecked")
		Class<? extends DatabaseAbstractionManager> retVal =
			(Class<? extends DatabaseAbstractionManager>) Class
					.forName(className);
		return retVal;
	}

	protected static DatabaseAbstractionManager newDatabaseAbstractionManager(
			String className) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class<? extends DatabaseAbstractionManager> c =
			resolveDatabaseAbstractionManagerClass(className);
		DatabaseAbstractionManager retVal = c.newInstance();
		return retVal;
	}

	private final Set<DatabaseAbstractionManager> managers =
		new LinkedHashSet<>();

	public AggregateDatabaseAbstractionManager() throws DatabaseException {
		try {
			DatabaseAbstractionManager mgr;
			mgr = newDatabaseAbstractionManager(ORACLE_DBA_MANAGER);
			logger.fine("Adding " + mgr.getClass().getName());
			managers.add(mgr);
			mgr = newDatabaseAbstractionManager(SQLSERVER_DBA_MANAGER);
			logger.fine("Adding " + mgr.getClass().getName());
			managers.add(mgr);
			mgr = newDatabaseAbstractionManager(JBOSS_DBA_MANAGER);
			logger.fine("Adding " + mgr.getClass().getName());
			managers.add(mgr);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			String msg =
				"Unable to create AggregateDatabaseAbstractionManager: "
						+ e.toString();
			throw new DatabaseException(msg);
		}
	}

	public AggregateDatabaseAbstractionManager(
			Set<DatabaseAbstractionManager> managers) {
		if (managers == null) {
			throw new IllegalArgumentException(
					"null set of database abstraction managers");
		}
		for (DatabaseAbstractionManager mgr : managers) {
			if (mgr == null) {
				throw new IllegalArgumentException(
						"null database abstraction manager");
			}
			logger.fine("Adding " + mgr.getClass().getName());
			this.managers.add(mgr);
		}
	}

	public DatabaseAbstraction lookupDatabaseAbstraction(DataSource ds)
			throws DatabaseException {

		// Precondition
		if (ds == null) {
			throw new IllegalArgumentException("null data source");
		}
		logger.info("Looking up database abstraction for "
				+ ds.getClass().getName());
	
		// Default value for unknown data sources
		DatabaseAbstraction retVal = null;

		for (DatabaseAbstractionManager mgr : managers) {

			DatabaseAbstraction dba = null;
			try {
				dba = mgr.lookupDatabaseAbstraction(ds);

			} catch (DatabaseException e) {
				// Expected: e.g. when mgr handles SqlServer but ds is Oracle
				String msg =
					"Ignoring DatabaseException from manager '"
							+ mgr.getClass().getName() + "' for data source '"
							+ ds.getClass().getName() + "': " + e.getMessage();
				logger.info(msg);

			} catch (Exception e) {
				String msg =
					"Unable to lookup database abstraction for '"
							+ ds.getClass().getName() + "': " + e.toString();
				throw new DatabaseException(msg, e);
			}

			// Check for conflicts
			if (retVal != null && dba != null && !dba.equals(retVal)) {
				assert retVal != null && !retVal.equals(dba);
				final String dbaName = dba.getClass().getName();
				final String retValName = retVal.getClass().getName() ;
				String msg =
					"Conflicting DatabaseAbstractions for '" + ds + "': '"
							+ dbaName + "', '" + retValName + "'";
				throw new DatabaseException(msg);

			// Continue if no assignment is required
			} else if (dba == null || dba.equals(retVal)) {
				continue;
			}
			
			// Assign to retVal if retVal is null and dba is not
			assert retVal == null && dba != null;
			retVal = dba;
		}

		// Error if unknown
		if (retVal == null) {
			String msg =
				"Unknown DataSource implementation: '"
						+ ds.getClass().getName() + "'";
			throw new DatabaseException(msg);
		}

		// Postcondition
		assert retVal != null;

		return retVal;
	}

}
