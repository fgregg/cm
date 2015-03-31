package com.choicemaker.cm.io.db.sqlserver.blocking;

import javax.sql.DataSource;

import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.io.db.base.DatabaseAbstraction;
import com.choicemaker.cm.io.db.base.DatabaseAbstractionManager;

/**
 * A hard-coded implementation of DatabaseAbstractionManager that handles only
 * SqlServer DataSources.
 * <p>
 * FIXME replace with a plugin-based manager
 * 
 * @author rphall
 * @deprecated
 */
public class SqlServerDatabaseAbstractionManager implements
		DatabaseAbstractionManager {

	public SqlServerDatabaseAbstractionManager() {
		// TODO Auto-generated constructor stub
	}

	public DatabaseAbstraction lookupDatabaseAbstraction(DataSource ds)
			throws DatabaseException {

		// Precondition
		if (ds == null) {
			throw new IllegalArgumentException("null data source");
		}

		// Default value for unknown data sources
		DatabaseAbstraction retVal = null;

		// Microsoft SqlServer DataSource implementations
		try {
			if (ds.isWrapperFor(Class
					.forName("com.microsoft.sqlserver.jdbc.CommonDataSource"))) {
				retVal = new SqlDatabaseAbstraction();

			} else if (ds
					.isWrapperFor(Class
							.forName("com.microsoft.sqlserver.jdbc.ISQLServerDataSource"))) {
				retVal = new SqlDatabaseAbstraction();

			} else if (ds
					.isWrapperFor(Class
							.forName("com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource"))) {
				retVal = new SqlDatabaseAbstraction();

			} else if (ds
					.isWrapperFor(Class
							.forName("com.microsoft.sqlserver.jdbc.SQLServerDataSource"))) {
				retVal = new SqlDatabaseAbstraction();

			} else if (ds
					.isWrapperFor(Class
							.forName("com.microsoft.sqlserver.jdbc.SQLServerXADataSource"))) {
				retVal = new SqlDatabaseAbstraction();
			}

			// Error if unknown
			else {
				assert retVal == null;
				String msg =
					"Unknown DataSource implementation: '"
							+ ds.getClass().getName() + "'";
				throw new DatabaseException(msg);
			}

		} catch (Exception e) {
			String msg =
				"Unable to lookup database abstraction for '"
						+ ds.getClass().getName() + "': " + e.toString();
			;
			throw new DatabaseException(msg, e);
		}

		// Postcondition
		assert retVal != null;

		return retVal;
	}

}
