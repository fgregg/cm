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
package com.choicemaker.cm.io.db.base;

import java.io.IOException;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.util.logging.Logger;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.base.PMManager;


/**
 * This object creates a MRPS from data in an Oracle database.  It is also serializable and can run in a
 * J2EE server.
 * <p>
 * FIXME: rename and move this Oracle-specific implementation
 * to the com.choicemaker.cm.io.db.oracle package
 * </p>
 *
 * @author pcheung
 *
 */
public class DbSerialMRPSource implements MarkedRecordPairSource, Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 2592253692957626142L;

	private static final int CURSOR = -10;
	private static Logger logger = Logger.getLogger(DbSerialMRPSource.class.getName());

	// Properties
	private String dataSourceName;
	private String modelName;
	private String selection;
	private String conf = "";

	// Cache
	private transient DataSource ds;
	private transient Connection conn;
	private transient CallableStatement stmt;
	private transient ImmutableProbabilityModel model;
	private transient ResultSet[] rs;
	private transient ResultSet outer;
	private transient DbReaderParallel dbr;
	private transient ResultSet rsDecision;
	private transient MutableMarkedRecordPair pair;
	private HashMap recordMap;


	/**
	 * Constructor.
	 */
	public DbSerialMRPSource (String dataSourceName, String modelName, String conf, String selection) {
		this.dataSourceName = dataSourceName;
		this.modelName = modelName;
		this.selection = selection;
		this.conf = conf;
	}


	public void open() throws IOException {
		try {
			dbr = ((DbAccessor) getModel().getAccessor()).getDbReaderParallel(conf);
			int noCursors = dbr.getNoCursors();
			conn = getDataSource().getConnection();
			conn.setAutoCommit(false);
			//((OracleConnection) conn).setDefaultRowPrefetch(100);
			String sql = "call CMTTRAINING.ACCESS_SNAPSHOT (?,?,?,?)";
			stmt = conn.prepareCall(sql);
			stmt.setString(1, selection);
			stmt.setString(2, dbr.getName());
			stmt.registerOutParameter(3, CURSOR);
			stmt.registerOutParameter(4, CURSOR);
			stmt.execute();
			rsDecision = (ResultSet) stmt.getObject(3);
			outer = (ResultSet) stmt.getObject(4);
			outer.next();
			rs = new ResultSet[noCursors];
			if (noCursors == 1) {
				rs[0] = outer;
			} else {
				for (int i = 0; i < noCursors; ++i) {
					logger.fine("Get cursor: " + i);
					rs[i] = (ResultSet) outer.getObject(i + 1);
				}
			}
			dbr.open(rs);

			loadMap ();

			getNextMain();
		} catch (java.sql.SQLException e) {
			throw new IOException("", e);
		}
	}


	/**
	 *  This method loads the records into a map.
	 *
	 */
	private void loadMap () throws SQLException {
		recordMap = new HashMap ();
		while (dbr.hasNext()) {
			Record q = dbr.getNext();
			recordMap.put(q.getId().toString(), q);
		}
	}


	private void getNextMain() throws IOException {
		try {
			if (rsDecision.next()) {
				Record q = (Record) recordMap.get(rsDecision.getString(1));
				Record m = (Record) recordMap.get(rsDecision.getString(2));
				String d = rsDecision.getString(3);
				Decision decision = null;
				if (d != null && d.length() > 0) {
					decision = Decision.valueOf(d.charAt(0));
				}
				Date date = rsDecision.getDate(4);
				String user = rsDecision.getString(5);
				String src = rsDecision.getString(6);
				String comment = rsDecision.getString(7);
				pair = new MutableMarkedRecordPair(q, m, decision, date, user, src, comment);

			} else {
				pair = null;
			}
		} catch (java.sql.SQLException e) {
			throw new IOException("", e);
		}
	}



	public ImmutableRecordPair getNext() throws IOException {
		return getNextMarkedRecordPair();
	}


	public MutableMarkedRecordPair getNextMarkedRecordPair() throws IOException {
		MutableMarkedRecordPair res = pair;
		getNextMain();
		return res;
	}


	public void close() throws IOException {
		Exception ex = null;
		try {
			if (rsDecision != null) rsDecision.close();
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			if (dbr != null) {
				int noCursors = dbr.getNoCursors();
				for (int i = 0; i < noCursors; ++i) {
					if(rs[i] != null) rs[i].close();
				}
			}
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			if (outer != null) outer.close();
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			if (stmt != null) stmt.close();
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			if (conn != null) conn.close();
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		if (ex != null) {
			throw new IOException("", ex);
		}
	}

	public boolean hasNext() throws IOException {
		return pair != null;
	}

	public ImmutableProbabilityModel getModel() {
		if (model == null) model = PMManager.getModelInstance(modelName);
		return model;
	}


	private DataSource getDataSource () {
		try {
			if (ds == null) {
				Context ctx = new InitialContext();
				ds = (DataSource) ctx.lookup (dataSourceName);
			}
		} catch (NamingException ex) {
			logger.severe(ex.toString());
		}
		return ds;
	}


	/** These method below are not used.
	 *
	 */

	public void setModel(ImmutableProbabilityModel m) {
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		return null;
	}

	public String getFileName() {
		return null;
	}

	public String getName() {
		return null;
	}

	public void setName(String name) {
	}


}
