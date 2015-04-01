package com.choicemaker.cm.io.db.jboss;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.io.db.base.DatabaseAbstraction;
import com.choicemaker.cm.io.db.base.DatabaseAbstractionManager;
import com.choicemaker.cm.io.db.oracle.blocking.OraDatabaseAbstraction;
import com.choicemaker.cm.io.db.sqlserver.blocking.SqlDatabaseAbstraction;

/**
 * A hard-coded implementation of DatabaseAbstractionManager that handles only
 * SqlServer DataSources.
 * <p>
 * FIXME replace with a plugin-based manager
 * 
 * @author rphall
 * @deprecated
 */
public class JBossDatabaseAbstractionManager implements
		DatabaseAbstractionManager {

	private static final Logger logger = Logger
			.getLogger(JBossDatabaseAbstractionManager.class.getName());

	public static final String JBOSS_WRAPPER_METHOD = "getUnderlyingConnection";

	public static final String ORACLE_SIGNATURE = "oracle";

	public static final String SQLSERVER_SIGNATURE = "sqlserver";

	public DatabaseAbstraction lookupDatabaseAbstraction(DataSource ds)
			throws DatabaseException {

		// Precondition
		if (ds == null) {
			throw new IllegalArgumentException("null data source");
		}

		// Default value for unknown data sources
		DatabaseAbstraction retVal = null;
		try {
			Connection conn = null;
			try {
				conn = ds.getConnection();
				Class c = conn.getClass();
				String msg = "DS type: " + c.getName();
				logger.fine(msg);

				Method m = c.getMethod(JBOSS_WRAPPER_METHOD, (Class[]) null);
				Object o = m.invoke(conn, (Object[]) null);
				c = o.getClass();
				String className = c.getName();
				logger.fine("Underlying connection: " + className);

				if (className.toLowerCase().contains("sqlserver")) {
					logger.fine("SqlDatabaseAbstraction");
					retVal = new SqlDatabaseAbstraction();

				} else if (className.toLowerCase().contains("oracle")) {
					logger.fine("OraDatabaseAbstraction");
					retVal = new OraDatabaseAbstraction();

				} else {
					String s = ds.getClass().getName();
					msg = "Unknown DataSource implementation: '" + s + "'";
					throw new DatabaseException(msg);
				}

			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						logger.warning(e.toString());
					}
				}
			}

		} catch (Exception e) {
			String msg =
				"Unable to lookup database abstraction for '"
						+ ds.getClass().getName() + "': " + e.toString();
			throw new DatabaseException(msg, e);
		}

		// Postcondition
		assert retVal != null;

		return retVal;
	}

}
