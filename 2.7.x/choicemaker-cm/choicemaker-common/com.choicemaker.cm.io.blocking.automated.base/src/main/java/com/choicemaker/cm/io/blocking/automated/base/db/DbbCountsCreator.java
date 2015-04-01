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
package com.choicemaker.cm.io.blocking.automated.base.db;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.AbaStatisticsCache;
import com.choicemaker.cm.io.blocking.automated.BlockingAccessor;
import com.choicemaker.cm.io.blocking.automated.IBlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.ICountField;
import com.choicemaker.cm.io.blocking.automated.IDbField;
import com.choicemaker.cm.io.blocking.automated.IDbTable;
import com.choicemaker.cm.io.blocking.automated.base.CountField;
import com.choicemaker.cm.io.blocking.automated.base.DbTable;
import com.choicemaker.cm.io.blocking.automated.cachecount.AbaStatisticsImpl;
import com.choicemaker.cm.io.db.base.DatabaseAbstraction;

/**
 * Database Blocking Counts Creator
 * 
 * @author mbuechi
 */
public class DbbCountsCreator {

	private static Logger logger = Logger.getLogger(DbbCountsCreator.class
			.getName());

	// private final DatabaseAbstractionManager databaseAbstractionManager;
	//
	// public DbbCountsCreator(DatabaseAbstractionManager mgr) {
	// if (mgr == null) {
	// throw new IllegalArgumentException("null database abstraction manager");
	// }
	// this.databaseAbstractionManager = mgr;
	// }
	//
	// public DatabaseAbstractionManager getDatabaseAbstractionManager() {
	// return databaseAbstractionManager;
	// }

	public void install(DataSource ds) throws SQLException {
		final String METHOD = "DbbCountsCreator.install: ";
		if (ds == null) {
			throw new IllegalArgumentException(METHOD + "null data source");
		}
		logger.info("DEBUG " + METHOD + "entering");

		Connection connection = null;
		try {
			connection = ds.getConnection();
			setConfigFields(connection);
			setMainFields(connection);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e1) {
					logger.severe(METHOD + e1.toString());
				}
			}
		}
		logger.info("DEBUG " + METHOD + "exiting");
	}

	private void setConfigFields(final Connection connection)
			throws SQLException {
		final String METHOD = "DbbCountsCreator.setConfigFields: ";
		assert connection != null;
		logger.info("DEBUG " + METHOD + "entering");
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ImmutableProbabilityModel[] models = PMManager.getModels();
			if (logger.isLoggable(Level.FINE)) {
				if (models == null) {
					logger.warning(METHOD + "null models");
				} else {
					final int mcount = models == null ? 0 : models.length;
					logger.info("DEBUG " + "model count: " + mcount);
					for (ImmutableProbabilityModel m : models) {
						logger.info("DEBUG " + METHOD + m.getModelName());
					}
				}
			}
			IBlockingConfiguration[] bcs = getBlockingConfigurations(models);
			if (logger.isLoggable(Level.FINE)) {
				if (bcs == null) {
					logger.warning(METHOD + "null bcs");
				} else {
					final int bcount = bcs == null ? 0 : bcs.length;
					logger.info("DEBUG " + "blocking config count: " + bcount);
				}
			}
			for (int i = 0; i < bcs.length; ++i) {
				IBlockingConfiguration bc = bcs[i];
				logger.info("DEBUG " + METHOD + bc.getName());
				String name = bc.getName();
				String query =
					"DELETE FROM TB_CMT_COUNT_CONFIG_FIELDS WHERE config = \'"
							+ name + "\'";
				logger.info("DEBUG " + query);
				stmt.execute(query);
				for (int j = 0; j < bc.getDbFields().length; ++j) {
					IDbField df = bc.getDbFields()[j];
					query =
						"INSERT INTO TB_CMT_COUNT_CONFIG_FIELDS VALUES(" + "'"
								+ name + "'," + "'" + df.getTable().getName()
								+ "'," + "'" + df.getName() + "'," + "'"
								+ df.getTable().getUniqueId() + "',"
								+ df.getDefaultCount() + ")";
					logger.info("DEBUG " + query);
					stmt.execute(query);
				}
				for (int j = 0; j < bc.getDbTables().length; ++j) {
					IDbTable dt = bc.getDbTables()[j];
					query =
						"INSERT INTO TB_CMT_COUNT_CONFIG_FIELDS VALUES(" + "'"
								+ name + "'," + "'" + dt.getName() + "',"
								+ "null," + "'" + dt.getUniqueId() + "',"
								+ "null)";
					logger.info("DEBUG " + query);
					stmt.execute(query);
				}
			}
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.severe(METHOD + e.toString());
				}
			}
		}
		logger.info("DEBUG " + METHOD + "exiting");
	}

	private void setMainFields(final Connection connection) throws SQLException {
		final String METHOD = "DbbCountsCreator.setMainFields: ";
		assert connection != null;
		logger.info("DEBUG " + METHOD + "entering");
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			int maxId = -1;
			String query = "SELECT MAX(FieldId) FROM TB_CMT_COUNT_FIELDS";
			logger.info("DEBUG " + query);
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				maxId = rs.getInt(1);
			}
			rs.close();
			query =
				"SELECT ViewName, ColumnName, MasterId, MIN(MinCount) "
						+ "FROM TB_CMT_COUNT_CONFIG_FIELDS t1 "
						+ "WHERE ColumnName IS NOT NULL AND NOT EXISTS ("
						+ "SELECT * FROM TB_CMT_COUNT_FIELDS t2 "
						+ "WHERE t1.ViewName = t2.ViewName AND "
						+ "t1.ColumnName = t2.ColumnName AND "
						+ "t1.MasterId = t2.MasterId) "
						+ "GROUP BY ViewName, ColumnName, MasterId";
			logger.info("DEBUG " + query);
			rs = stmt.executeQuery(query);
			// Some JDBC drivers don't support multiple statements or result
			// sets on a single connection.
			ArrayList<String> l = new ArrayList<>();
			while (rs.next()) {
				for (int i = 1; i <= 4; ++i) {
					l.add(rs.getString(i));
				}
			}
			rs.close();
			Iterator<String> iL = l.iterator();
			while (iL.hasNext()) {

				query =
					"INSERT INTO TB_CMT_COUNT_FIELDS VALUES(" + (++maxId)
							+ ", '" + iL.next() + "','" + iL.next() + "','"
							+ iL.next() + "'," + iL.next() + ", null)";
				logger.info("DEBUG " + query);
				stmt.execute(query);
			}
			l.clear();
			query =
				"SELECT DISTINCT ViewName, MasterId FROM TB_CMT_COUNT_CONFIG_FIELDS t1 WHERE ColumnName IS NULL AND NOT EXISTS "
						+ "(SELECT * FROM TB_CMT_COUNT_FIELDS t2 WHERE t1.ViewName = t2.ViewName AND t2.ColumnName IS NULL "
						+ "AND t1.MasterId = t2.MasterId)";
			logger.info("DEBUG " + query);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				for (int i = 1; i <= 2; ++i) {
					l.add(rs.getString(i));
				}
			}
			rs.close();
			iL = l.iterator();
			while (iL.hasNext()) {
				query =
					"INSERT INTO TB_CMT_COUNT_FIELDS VALUES(" + (++maxId)
							+ ", '" + iL.next() + "', null, '" + iL.next()
							+ "', null, null)";
				logger.info("DEBUG " + query);
				stmt.execute(query);
			}
			query =
				"DELETE FROM TB_CMT_COUNTS WHERE fieldId NOT IN ("
						+ "SELECT fieldId FROM TB_CMT_COUNT_FIELDS f, "
						+ "TB_CMT_COUNT_CONFIG_FIELDS k "
						+ "WHERE f.ViewName = k.ViewName AND ("
						+ "(f.ColumnName IS NULL AND k.ColumnName IS NULL) OR "
						+ "(f.ColumnName = k.ColumnName)" + ") )";
			logger.info("DEBUG " + query);
			stmt.execute(query);
			query =
				"DELETE FROM TB_CMT_COUNT_FIELDS "
						+ "WHERE ColumnName IS NOT NULL AND  NOT EXISTS ("
						+ "SELECT * FROM TB_CMT_COUNT_CONFIG_FIELDS t2 "
						+ "WHERE TB_CMT_COUNT_FIELDS.ViewName = t2.ViewName AND "
						+ "TB_CMT_COUNT_FIELDS.ColumnName = t2.ColumnName AND "
						+ "TB_CMT_COUNT_FIELDS.MasterId = t2.MasterId)";
			logger.info("DEBUG " + query);
			stmt.execute(query);
			query =
				"DELETE FROM TB_CMT_COUNT_FIELDS WHERE ColumnName IS NULL AND "
						+ "NOT EXISTS (SELECT * FROM TB_CMT_COUNT_CONFIG_FIELDS t2 WHERE TB_CMT_COUNT_FIELDS.ViewName = t2.ViewName AND TB_CMT_COUNT_FIELDS.MasterId = t2.MasterId "
						+ "AND t2.ColumnName IS NULL)";
			logger.info("DEBUG " + query);
			stmt.execute(query);

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.severe(METHOD + e.toString());
				}
			}
		}

		logger.info("DEBUG " + METHOD + "exiting");
	}

	// public void create(DataSource ds, boolean neverComputedOnly)
	// throws SQLException {
	// DatabaseAbstraction dba =
	// getDatabaseAbstractionManager().lookupDatabaseAbstraction(ds);
	// create(ds, dba, neverComputedOnly);
	// }

	public void create(DataSource ds, DatabaseAbstraction databaseAbstraction,
			boolean neverComputedOnly) throws SQLException {
		create(ds, databaseAbstraction, neverComputedOnly, true);
	}

	public void create(DataSource ds, DatabaseAbstraction databaseAbstraction,
			boolean neverComputedOnly, boolean commitChanges)
			throws SQLException {
		final String METHOD = "DbbCountsCreator.create: ";
		if (ds == null) {
			throw new IllegalArgumentException(METHOD + "null data source");
		}
		if (databaseAbstraction == null) {
			throw new IllegalArgumentException(METHOD
					+ "null database abstraction");
		}
		logger.info("DEBUG " + METHOD + "entering");

		// Debugging
		String _latest_query = null;

		// BUG 2009-08-21 rphall
		// This method may fail if two CM Server instances use the same database
		// simultaneously.
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = ds.getConnection();
			stmt = connection.createStatement();
			String q0 = databaseAbstraction.getSetDateFormatExpression();
			_latest_query = q0;
			stmt.execute(q0);
			String delete;
			String query;
			if (neverComputedOnly) {
				delete =
					"DELETE FROM TB_CMT_COUNTS WHERE NOT EXISTS "
							+ "(SELECT * FROM TB_CMT_COUNT_FIELDS f "
							+ "WHERE TB_CMT_COUNTS.FieldId = f.FieldId "
							+ "AND f.lastUpdate IS NOT NULL)";
				query =
					"SELECT * FROM TB_CMT_COUNT_FIELDS WHERE LastUpdate IS NULL";

			} else {

				// DESIGN BUG 2009-08-21 rphall
				// Some databases should use TRUNCATE rather than
				// DELETE for performance reasons. Other databases
				// don't support TRUNCATE. So there should be a mechanism
				// to invoke DELETE or TRUNCATE depending on the DB flavor
				// (something better than the flawed DatabaseAbstraction
				// design, which is tied to models rather than connections).
				delete = "DELETE FROM TB_CMT_COUNTS";

				// truncate is not supported in DB2
				// delete = "TRUNCATE TABLE TB_CMT_COUNTS";

				query = "SELECT * FROM TB_CMT_COUNT_FIELDS";
				// END DESIGN BUG
			}
			logger.info("DEBUG " + delete);
			_latest_query = delete;
			stmt.execute(delete);
			logger.info("DEBUG " + query);
			_latest_query = query;
			ResultSet rs = stmt.executeQuery(query);
			List<String> l = new ArrayList<>();
			while (rs.next()) {
				for (int i = 1; i <= 5; ++i) {
					l.add(rs.getString(i));
				}
			}
			rs.close();
			Iterator<String> iL = l.iterator();
			while (iL.hasNext()) {
				String fieldId = (String) iL.next();
				String table = (String) iL.next();
				String column = (String) iL.next();
				String uniqueId = (String) iL.next();
				String minCount = (String) iL.next();
				if (column == null || column.length() == 0) {
					// table
					query =
						"INSERT INTO TB_CMT_COUNTS SELECT " + fieldId
								+ ", 'table', COUNT(DISTINCT " + uniqueId
								+ ") FROM " + table;
					logger.info("DEBUG " + query);
					_latest_query = query;
					stmt.execute(query);

				} else {
					// field
					query =
						"SELECT " + column + " FROM " + table + " WHERE 0 = 1";
					logger.info("DEBUG " + query);
					_latest_query = query;
					ResultSet tmpRs = stmt.executeQuery(query);
					int columnType = tmpRs.getMetaData().getColumnType(1);
					boolean isDate =
						columnType == Types.DATE
								|| columnType == Types.TIMESTAMP;
					tmpRs.close();

					query =
						"INSERT INTO TB_CMT_COUNTS SELECT "
								+ fieldId
								+ ","
								+ (isDate ? databaseAbstraction
										.getDateFieldExpression(column)
										: column) + ", COUNT(" + uniqueId
								+ ") FROM " + table + " WHERE " + column
								+ " IS NOT NULL " + "GROUP BY " + column
								+ " HAVING COUNT(" + uniqueId + ") > "
								+ minCount;
					logger.info("DEBUG " + query);
					_latest_query = query;
					stmt.execute(query);
				}

				query =
					"UPDATE TB_CMT_COUNT_FIELDS SET LastUpdate = "
							+ databaseAbstraction.getSysdateExpression()
							+ " WHERE FieldId = " + fieldId;
				logger.info("DEBUG " + query);
				_latest_query = query;
				stmt.execute(query);

				if (commitChanges) {
					logger.info(METHOD + "commiting ABA statistics to DB");
					connection.commit();
				} else {
					String msg =
						"skipping commit of ABA statistics to DB "
								+ "-- assuming a managed connection will "
								+ "automagically commit them instead.";
					logger.info(METHOD + msg);
				}
			}

		} catch (SQLException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			String msg =
				METHOD + "Unable to create ABA statistics: " + e.toString();
			pw.println(msg);
			pw.println("   LATEST QUERY: " + _latest_query);
			msg = sw.toString();
			logger.severe(msg);
			throw e;

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.severe(METHOD + e.toString());
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e1) {
					logger.severe(METHOD + e1.toString());
				}
			}
		}
	}

	public void setCacheCountSources(DataSource ds,
			DatabaseAbstraction databaseAbstraction, AbaStatisticsCache cache)
			throws SQLException {
		final String METHOD = "DbbCountsCreator.setCacheCountSources: ";
		if (ds == null) {
			throw new IllegalArgumentException(METHOD + "null data source");
		}
		if (cache == null) {
			throw new IllegalArgumentException(METHOD + "null statistics cache");
		}
		logger.info("DEBUG " + METHOD + "entering");

		// BUG 2009-08-21 rphall
		// The "models" instance data can be null (because of
		// a flawed constructor) and if so, this method fails quietly
		ImmutableProbabilityModel[] models = PMManager.getModels();
		if (models != null) {

			Connection connection = null;
			Statement stmt = null;
			try {
				connection = ds.getConnection();

				stmt = connection.createStatement();
				List<CountField> countFields = new ArrayList<>();
				Map<DbTable, Integer> tableSizes = readTableSizes(stmt);

				IBlockingConfiguration[] blockingConfigurations =
					getBlockingConfigurations(models);
				AbaStatisticsImpl[] ccs =
					new AbaStatisticsImpl[blockingConfigurations.length];
				for (int i = 0; i < blockingConfigurations.length; ++i) {
					IBlockingConfiguration bc = blockingConfigurations[i];
					ICountField[] bcCountFields =
						new ICountField[bc.getDbFields().length];
					for (int j = 0; j < bc.getDbFields().length; ++j) {
						IDbField dbf = bc.getDbFields()[j];
						CountField f = find(countFields, dbf);
						if (f == null) { // read in
							String column = dbf.getName();
							String view = dbf.getTable().getName();
							String uniqueId = dbf.getTable().getUniqueId();
							int tableSize =
								getTableSize(tableSizes, dbf.getTable());
							f =
								new CountField(100, dbf.getDefaultCount(),
										tableSize, column, view, uniqueId);
							countFields.add(f);

							String query =
								"SELECT FieldId FROM TB_CMT_COUNT_FIELDS "
										+ "WHERE ViewName = '" + view
										+ "' AND ColumnName = '" + column
										+ "' AND MasterId = '" + uniqueId + "'";
							logger.info("DEBUG " + query);
							ResultSet rs = stmt.executeQuery(query);
							if (rs.next()) {
								int fieldId = rs.getInt(1);
								rs.close();

								query =
									"SELECT Value, Count FROM TB_CMT_COUNTS "
											+ "WHERE FieldId = " + fieldId;
								logger.info("DEBUG " + query);
								rs = stmt.executeQuery(query);
								while (rs.next()) {
									f.putValueCount(rs.getString(1),
											CountField.getInteger(rs.getInt(2)));
								}
								rs.close();

							} else {
								rs.close();
							}
						}
						bcCountFields[j] = f;
					}
					ccs[i] =
						new AbaStatisticsImpl(getTableSize(tableSizes,
								bc.getDbTables()[0]), bcCountFields);
				}
				for (ImmutableProbabilityModel model : models) {
					final String bcName = model.getBlockingConfigurationName();
					final String dn = model.getDatabaseConfigurationName();
					logger.info("DEBUG " + "Using blocking configuration: "
							+ bcName);
					IBlockingConfiguration bc =
						((BlockingAccessor) model.getAccessor())
								.getBlockingConfiguration(bcName, dn);
					final String bcClassName = bc.getClass().getName();

					// This would be simpler if the collection of
					// AbaStatisticsImpl instances were not an array, but rather
					// a Map. The key could remain as className, but it would be
					// more resilient and less dependent on implementation if
					// the key were a concatenation of the names of a model and
					// a blocking configuration.
					int j = 0;
					while (!blockingConfigurations[j].getClass().getName()
							.equals(bcClassName)) {
						++j;
					}

					// DEPRECATED 2014-11-18 rphall
					// model.properties().put("countSource", ccs[j]);
					// END DEPRECATED
					cache.putStatistics(model, ccs[j]);
				}
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.severe(METHOD + e.toString());
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e1) {
						logger.severe(METHOD + e1.toString());
					}
				}
			}

		}
	}

	private Map<DbTable, Integer> readTableSizes(Statement stmt)
			throws SQLException {
		logger.info("DEBUG " + "readTableSizes...");
		Map<DbTable, Integer> l = new HashMap<>();
		String query =
			"SELECT ViewName, MasterId, Count "
					+ "FROM TB_CMT_COUNT_FIELDS f, TB_CMT_COUNTS c "
					+ "WHERE f.FieldId = c.FieldId AND f.ColumnName IS NULL";
		logger.info("DEBUG " + query);
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			l.put(new DbTable(rs.getString(1), 0, rs.getString(2)),
					new Integer(Math.max(1, rs.getInt(3))));
		}
		if (l.size() == 0) {
			String msg =
				"Required views for automated blocking were not found. "
						+ "Automated blocking will not work without them. "
						+ "Use CM-Analyzer to produce a script that will create them, "
						+ "then run the script to add them to the database.";
			logger.warning(msg);
		}
		logger.info("DEBUG " + "...readTableSizes");
		return l;
	}

	private int getTableSize(Map<DbTable, Integer> tableSizes, IDbTable dbt) {
		int retVal = 0;
		Integer s = (Integer) tableSizes.get(dbt);
		if (s == null) {
			logger.warning("Table size is null. Have ABA statistics been computed?");
		}
		return retVal;
	}

	private CountField find(List<CountField> countFields, IDbField dbf) {
		String column = dbf.getName();
		String view = dbf.getTable().getName();
		String uniqueId = dbf.getTable().getUniqueId();
		Iterator<CountField> iCountFields = countFields.iterator();
		while (iCountFields.hasNext()) {
			CountField cf = (CountField) iCountFields.next();
			if (column.equals(cf.getColumn()) && view.equals(cf.getView())
					&& uniqueId.equals(cf.getUniqueId())) {
				return cf;
			}
		}
		return null;
	}

	private static IBlockingConfiguration[] getBlockingConfigurations(
			ImmutableProbabilityModel[] models) {
		int len = models.length;
		IBlockingConfiguration[] bcs = new IBlockingConfiguration[len];
		int numConfigurations = 0;
		for (int i = 0; i < len; ++i) {
			ImmutableProbabilityModel model = models[i];
			String bcName = model.getBlockingConfigurationName();
			String dn = model.getDatabaseConfigurationName();
			IBlockingConfiguration bc =
				((BlockingAccessor) model.getAccessor())
						.getBlockingConfiguration(bcName, dn);
			int j = 0;
			// AWKWARD, BRITTLE CODE 2009-08-21 rphall
			// Just use a Map of models to blockingConfigurations!
			while (j < numConfigurations
					&& !bc.getClass().getName()
							.equals(bcs[j].getClass().getName())) {
				++j;
			}
			if (j == numConfigurations) {
				bcs[numConfigurations++] = bc;
			}
			// END AWKWARD, BRITTLE CODE
		}
		IBlockingConfiguration[] res =
			new IBlockingConfiguration[numConfigurations];
		System.arraycopy(bcs, 0, res, 0, numConfigurations);
		return res;
	}

	// private static DatabaseAbstraction[] getDatabaseAbstractions(
	// ImmutableProbabilityModel[] models) throws E2Exception {
	// Set<String> dbaNames = new HashSet<>();
	// for (ImmutableProbabilityModel model : models) {
	// final String dbaName = model.getDatabaseAbstractionName();
	// if (dbaName == null) {
	// String msg =
	// "Missing name of database abstraction for model '"
	// + model.getModelName() + "'";
	// logger.warning(msg);
	// }
	// dbaNames.add(dbaName);
	// }
	// if (dbaNames.isEmpty()) {
	// String msg = "No database abstractions configured for any model.";
	// logger.severe(msg);
	// throw new IllegalStateException(msg);
	// }
	// List<DatabaseAbstraction> dbas = new ArrayList<>();
	// for (String dbaName : dbaNames) {
	// DatabaseAbstraction dba = getDatabaseAbtraction(dbaName);
	// if (dba == null) {
	// String msg = "Missing database abstraction '" + dbaName + "'";
	// logger.severe(msg);
	// throw new IllegalStateException(msg);
	// }
	// dbas.add(dba);
	// }
	// DatabaseAbstraction[] retVal =
	// dbas.toArray(new DatabaseAbstraction[dbas.size()]);
	// return retVal;
	// }
	//
	// private static DatabaseAbstraction getDatabaseAbtraction(String das)
	// throws E2Exception {
	// assert das != null && !das.isEmpty();
	// CMExtension dbExtension =
	// CMPlatformUtils.getExtension(DatabaseAbstraction.EXTENSION_POINT,
	// das);
	// CMConfigurationElement[] configurationElements =
	// dbExtension.getConfigurationElements();
	// CMConfigurationElement classConfiguration = configurationElements[0];
	// DatabaseAbstraction databaseAbstraction =
	// (DatabaseAbstraction) classConfiguration
	// .createExecutableExtension("class");
	// return databaseAbstraction;
	// }

}
