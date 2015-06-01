/*
 * @(#)$RCSfile: RecordReader.java,v $        $Revision: 1.4.124.1 $ $Date: 2009/11/18 01:00:11 $
 *
 * Copyright (c) 2002 ChoiceMaker Technologies, Inc.
 * 41 East 11th Street, New York, NY 10003
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */

package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderSequential;

/**
 *
 * @author    
 * @version   $Revision: 1.4.124.1 $ $Date: 2009/11/18 01:00:11 $
 */
public class RecordReader implements RecordSource {
	private static Logger logger = Logger.getLogger(RecordReader.class.getName());

	// These two objects have the same life span -- see isConsistent()
	private final DataSource ds;
	private final Properties p;

	private ImmutableProbabilityModel model;
	private final String databaseConfiguration;
	private Connection connection;
	private DbReaderSequential dbr;
	private Statement stmt;
	private String condition;
	private String name;

	protected static boolean isConsistent(DataSource ds, Properties p) {
		boolean isConsistent =
			(ds == null && p == null) || (ds != null && p != null);
		return isConsistent;
	}

	/**
	 * An invariant for this class
	 */
	protected boolean isConsistent() {
		return isConsistent(getDataSource(), getProperties());
	}

	public RecordReader(ImmutableProbabilityModel model, String dbConf,
			DataSource ds, Properties p, String condition) {
		if (!isConsistent(ds,p)) {
			throw new IllegalArgumentException("inconsistent data source and  properties");
		}
		this.model = model;
		this.databaseConfiguration = dbConf;
		this.ds = ds;
		this.p = p;
		this.condition = condition;
		assert isConsistent();
	}

	private DataSource getDataSource() {
		assert isConsistent();
		return ds;
	}

	private Properties getProperties() {
		assert isConsistent();
		return p;
	}

	public void open() throws IOException {
		Accessor acc = model.getAccessor();
		dbr = ((DbAccessor) acc).getDbReaderSequential(databaseConfiguration);
		try {
			String query = getQuery(getProperties());
			logger.fine(query);
			if (connection == null) {
				connection = getDataSource().getConnection();
			}
//			connection.setAutoCommit(false); // 2015-04-01a EJB3 CHANGE rphall
			stmt = connection.createStatement();
			stmt.setFetchSize(100);
			ResultSet rs = stmt.executeQuery(query);
			rs.setFetchSize(100);
			dbr.open(rs, stmt);
		} catch (SQLException ex) {
			logger.severe(ex.toString());
			throw new IOException(ex.toString(), ex);
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
		if (getDataSource() != null) {
			if (connection != null) {
				// EJB3 CHANGE 2015-04-01 rphall
				// Record readers are used sometimes used with EJB3 managed
				// connections. In these cases, they should not be explicitly
				// closed, but rather rely on the EJB3 container to do so.
				//
				// The rub is that a record reader may be used with non-managed
				// connections. Because auto-commit is off, the connection
				// must be committed by some other mechanism. The close method
				// probably needs a boolean flag to indicate whether the
				// connection needs to be committed, or a record reader needs
				// to be constructed with such a flag.
//				try {
//					connection.commit();
//				} catch (java.sql.SQLException e) {
//					ex = e;
//					logger.severe("Commiting: " + e.toString());
//				}
				// END EJB3 CHANGE
				try {
					connection.close();
					connection = null;
				} catch (java.sql.SQLException e) {
					ex = e;
					logger.severe("Closing connection: " + e.toString());
				}
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

	private String getQuery(final Properties p) {
		assert p != null;
		StringBuffer b = new StringBuffer(16000);
		b.append(
			"DECLARE @ids TABLE (id " + dbr.getMasterIdType() + ")" + Constants.LINE_SEPARATOR + "INSERT INTO @ids ");
		b.append(condition);
		b.append(Constants.LINE_SEPARATOR);
		b.append((String) p.getProperty(dbr.getName() + ":SQLServer"));
		return b.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void setModel(ImmutableProbabilityModel model) {
		this.model = model;
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		return null;
	}

	public String getFileName() {
		throw new UnsupportedOperationException();
	}

	public String toString() {
		return "RecordReader [model=" + model + ", condition=" + condition
				+ ", name=" + name + "]";
	}

}
