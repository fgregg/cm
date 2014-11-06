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

	private ImmutableProbabilityModel model;
	private DataSource ds;
	private Connection connection;
	private DbReaderSequential dbr;
	private Statement stmt;
	private String condition;
	private String name;

	public RecordReader(ImmutableProbabilityModel model, DataSource ds, String condition) {
		this.model = model;
		this.ds = ds;
		this.condition = condition;
	}

	public RecordReader(ImmutableProbabilityModel model, Connection connection, String condition) {
		this.model = model;
		this.connection = connection;
		this.condition = condition;
	}

	public void open() throws IOException {
		Accessor acc = model.getAccessor();
		String dbrName = (String) model.properties().get("dbConfiguration");
		dbr = ((DbAccessor) acc).getDbReaderSequential(dbrName);
		try {
			String query = getQuery();
			logger.fine(query);
			if (connection == null) {
				connection = ds.getConnection();
			}
			connection.setAutoCommit(false);
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
		if (ds != null) {
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

	private String getQuery() {
		StringBuffer b = new StringBuffer(16000);
		b.append(
			"DECLARE @ids TABLE (id " + dbr.getMasterIdType() + ")" + Constants.LINE_SEPARATOR + "INSERT INTO @ids ");
		b.append(condition);
		b.append(Constants.LINE_SEPARATOR);
		b.append((String) model.properties().get(dbr.getName() + ":SQLServer"));
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
}
