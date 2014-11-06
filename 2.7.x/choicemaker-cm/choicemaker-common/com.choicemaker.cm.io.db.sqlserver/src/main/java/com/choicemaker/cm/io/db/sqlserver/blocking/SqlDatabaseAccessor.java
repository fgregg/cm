/*
 * @(#)$RCSfile: SqlDatabaseAccessor.java,v $        $Revision: 1.9.88.1 $ $Date: 2009/11/18 01:00:11 $
 *
 * Copyright (c) 2002 ChoiceMaker Technologies, Inc.
 * 41 East 11th Street, New York, NY 10003
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */

package com.choicemaker.cm.io.db.sqlserver.blocking;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.io.blocking.automated.base.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.base.BlockingField;
import com.choicemaker.cm.io.blocking.automated.base.BlockingSet;
import com.choicemaker.cm.io.blocking.automated.base.BlockingValue;
import com.choicemaker.cm.io.blocking.automated.base.DatabaseAccessor;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderSequential;
import com.choicemaker.cm.io.db.sqlserver.dbom.SqlDbObjectMaker;
import com.choicemaker.util.StringUtils;

/**
 *
 * @author    
 * @version   $Revision: 1.9.88.1 $ $Date: 2009/11/18 01:00:11 $
 */
public class SqlDatabaseAccessor implements DatabaseAccessor {
	private static Logger logger = Logger.getLogger(SqlDatabaseAccessor.class.getName());

	private DataSource ds;
	private Connection connection;
	private DbReaderSequential dbr;
	private Statement stmt;
	private String condition;

	public SqlDatabaseAccessor() { }

	public SqlDatabaseAccessor(DataSource ds, String condition) {
		setDataSource(ds);
		setCondition(condition);
	}
	
	public void setDataSource(DataSource dataSource) {
		this.ds = dataSource;
	}
	
	public void setCondition(Object condition) {
		this.condition = (String)condition;
	}

	public DatabaseAccessor cloneWithNewConnection()
		throws CloneNotSupportedException {
		throw new CloneNotSupportedException("not yet implemented");
	}

	public void open(AutomatedBlocker blocker) throws IOException {
		Accessor acc = blocker.getModel().getAccessor();
		String dbrName = (String) blocker.getModel().properties().get("dbConfiguration");
		dbr = ((DbAccessor) acc).getDbReaderSequential(dbrName);
		String query = null;
		try {
			query = getQuery(blocker, dbr);
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(100);
			logger.fine(query);
			ResultSet rs = stmt.executeQuery(query);
			rs.setFetchSize(100);
			dbr.open(rs, stmt);
		} catch (SQLException ex) {
			logger.severe("Opening blocking data: " + query + ": " + ex.toString());
			throw new IOException(ex.toString());
		}
	}
	
	public void close() throws IOException {
		Exception ex = null;
		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		} catch (java.sql.SQLException e) {
			ex = e;
			logger.severe("Closing statement: " + e.toString());
		}
		if (connection != null) {
			try {
				connection.commit();
			} catch (java.sql.SQLException e) {
				ex = e;
				logger.severe("Commiting: " + e.toString());
			}
			try {
				connection.close();
				connection = null;
			} catch (java.sql.SQLException e) {
				ex = e;
				logger.severe("Closing connection: " + e.toString());
			}
		}
		if (ex != null) {
			throw new IOException(ex.toString());
		}
	}

	public boolean hasNext() {
		return dbr.hasNext();
	}

	public Record getNext() throws IOException {
		return dbr.getNext();
	}

	private String getQuery(AutomatedBlocker blocker, DbReaderSequential dbr) {
		StringBuffer b = new StringBuffer(16000);
		String id = dbr.getMasterId();
		b.append("DECLARE @ids TABLE (id " + dbr.getMasterIdType() + ")" + Constants.LINE_SEPARATOR + "INSERT INTO @ids");
		if (StringUtils.nonEmptyString(condition)) {
			b.append(" SELECT b.");
			b.append(id);
			b.append(" FROM (");
		}
		int numBlockingSets = blocker.getBlockingSets().size();
		for (int i = 0; i < numBlockingSets; ++i) {
			if (i == 0) {
				if (numBlockingSets > 1) {
					b.append(" SELECT ");
				} else {
					b.append(" SELECT DISTINCT ");
				}
			} else {
				b.append(" UNION SELECT ");
			}
			// AJW 2/26/04: to make stuff work for Phoenix.
			// This doesn't fix the problem, it just gets rid of a horrible
			// severe.  If blocking fields are on different tables, and each table
			// has an ID column, then things don't work...
			b.append("v0." + id);
			//b.append(id);
			b.append(" FROM ");
			BlockingSet bs = (BlockingSet) blocker.getBlockingSets().get(i);
			int numViews = bs.getNumTables();
			for (int j = 0; j < numViews; ++j) {
				if (j > 0) {
					b.append(",");
				}
				BlockingSet.GroupTable gt = bs.getTable(j);
				b.append(gt.table.name).append(" v").append(gt.number);
			}
			b.append(" WHERE ");
			int numValues = bs.numFields();
			for (int j = 0; j < numValues; ++j) {
				if (j > 0) {
					b.append(" AND ");
				}
				BlockingValue bv = bs.getBlockingValue(j);
				BlockingField bf = bv.blockingField;
				com.choicemaker.cm.io.blocking.automated.base.DbField dbf = bf.dbField;
				b.append("v").append(bs.getGroupTable(bf).number).append(".").append(dbf.name).append("=");
				if (mustQuote(bf.dbField.type)) {
					b.append("'" + escape(bv.value) + "'");
				} else {
					b.append(escape(bv.value));
				}
			}
			if (numViews > 1) {
				BlockingSet.GroupTable gt0 = bs.getTable(0);
				String g0 = " AND v" + gt0.number + "." + id + "=";
				for (int j = 1; j < numViews; ++j) {
					BlockingSet.GroupTable gt = bs.getTable(j);
					b.append(g0);
					b.append("v" + gt.number + "." + id);
				}
			}
		}
		if (StringUtils.nonEmptyString(condition)) {
			b.append(") b,");
			b.append(condition);
		}
		b.append(Constants.LINE_SEPARATOR);
		//b.append((String) blocker.accessProvider.properties.get(dbr.getName() + ":SQLServer"));
		b.append(getMultiQuery(blocker, dbr));
		
		logger.fine(b.toString());
		
		return b.toString();
	}

	private String getMultiQuery(AutomatedBlocker blocker, DbReaderSequential dbr) {
		String key = dbr.getName() + ":SQLServer";
		if (!blocker.getModel().properties().containsKey(key)) {
			try {
				// NOTE: this loads the multi string into accessProvider.properties
				SqlDbObjectMaker.getAllModels();
			} catch (IOException ex) {
				logger.severe(ex.toString());
			}
		}
		
		return (String) blocker.getModel().properties().get(key);
	}

	private boolean mustQuote(String type) {
		return !(
			type == "byte"
				|| type == "short"
				|| type == "int"
				|| type == "long"
				|| type == "float"
				|| type == "double");
	}

	private String escape(String s) {
		int len = s.length();
		int pos = 0;
		char ch;
		while (pos < len && (ch = s.charAt(pos)) != '\'' && ch >= 32) {
			++pos;
		}
		if (pos == len) {
			return s;
		} else {
			char[] res = new char[len * 2];
			for (int i = 0; i < pos; ++i) {
				res[i] = s.charAt(i);
			}
			int out = pos;
			while (pos < len) {
				ch = s.charAt(pos);
				if (ch == '\'') {
					res[out++] = '\'';
					res[out++] = '\'';
				} else if (ch >= 32) {
					res[out++] = ch;
				}
				++pos;
			}
			return new String(res, 0, out);
		}
	}
}
