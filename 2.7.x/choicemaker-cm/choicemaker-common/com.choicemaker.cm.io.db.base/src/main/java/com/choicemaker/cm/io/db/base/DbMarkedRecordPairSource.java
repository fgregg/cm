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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;

import javax.sql.DataSource;

import java.util.logging.Logger;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.NameUtils;

/**
 * Db 8i marked record pair source implementing
 * <code>MarkedRecordPairSource</code>. Used for reading training
 * data from a training database.
 * <p>
 * FIXME: rename and move this Oracle-specific implementation
 * to the com.choicemaker.cm.io.db.oracle package
 * </p>
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 22:35:01 $
 * @deprecated use DbMarkedRecordPairSource2 instead
 */
class DbMarkedRecordPairSource implements MarkedRecordPairSource {
	private static Logger logger = Logger.getLogger(DbMarkedRecordPairSource.class.getName());
	private static final int CURSOR = -10;

	// Properties
	private String fileName;
	private String name;
	private DataSource ds;
	private String dataSourceName;
	private ImmutableProbabilityModel model;
	private String selection;
	private String conf = "";

	// Cache
	private Connection conn;
	private CallableStatement stmt;
	private ResultSet[] rs;
	private ResultSet outer;
	private DbReaderParallel dbr;
	// BUGFIX rphall 2008-07-14
	// Need a separate set of cursors for id_matched
	private ResultSet[] rs2;
	private ResultSet outer2;
	private DbReaderParallel dbr2;
	// END BUGFIX
	private ResultSet rsDecision;
	private MutableMarkedRecordPair pair;

	/**
	 * Creates an uninitialized instance.
	 */
	public DbMarkedRecordPairSource() {
		name = "";
		selection = "";
	}

	/**
	 * Constructor.
	 */
	public DbMarkedRecordPairSource(String fileName, String dataSourceName, ImmutableProbabilityModel model, String conf, String selection) {
		setFileName(fileName);
		setDataSourceName(dataSourceName);
		setModel(model);
		this.selection = selection;
		this.conf = conf;
	}

	public void open() throws IOException {
		try {
			dbr = ((DbAccessor) model.getAccessor()).getDbReaderParallel(conf);
			final int numCursors = dbr.getNoCursors();
			// BUGFIX rphall 2008-07-14
			// Need a separate set of cursors for id_matched
			dbr2 = ((DbAccessor) model.getAccessor()).getDbReaderParallel(conf);
			if (dbr2.getNoCursors() != numCursors) {
				throw new Error("Unexpected difference in number of cursors");
			}
			if (!dbr.getName().equals(dbr2.getName())) {
				throw new Error("Unexpected difference in dbr names");
			}
			// END BUGFIX

			conn = ds.getConnection();
			conn.setAutoCommit(false);
			//((OracleConnection) conn).setDefaultRowPrefetch(100);
			//String sql = "call CMTTRAINING.ACCESS_SNAPSHOT (?,?,?,?)"; // 4 params
			String sql = "call CMTTRAINING.MRPS_SNAPSHOT2 (?,?,?,?,?)"; // 5 params, part of BUGFIX
			stmt = conn.prepareCall(sql);
			stmt.setString(1, selection);
			stmt.setString(2, dbr.getName());
			stmt.registerOutParameter(3, CURSOR);
			stmt.registerOutParameter(4, CURSOR);
			stmt.registerOutParameter(5, CURSOR); // part of BUGFIX
			stmt.execute();
			rsDecision = (ResultSet) stmt.getObject(3);
			outer = (ResultSet) stmt.getObject(4);
			outer.next();
			rs = new ResultSet[numCursors];
			if (numCursors == 1) {
				rs[0] = outer;
			} else {
				for (int i = 0; i < numCursors; ++i) {
					logger.fine("Get cursor: " + i);
					rs[i] = (ResultSet) outer.getObject(i + 1);
				}
			}
			dbr.open(rs);

			// BUGFIX rphall 2008-07-14
			// Need a separate set of cursors for id_matched
			outer2 = (ResultSet) stmt.getObject(5);
			outer2.next();
			rs2 = new ResultSet[numCursors];
			if (numCursors == 1) {
				rs2[0] = outer2;
			} else {
				for (int i = 0; i < numCursors; ++i) {
					logger.fine("Get cursor: " + i);
					rs2[i] = (ResultSet) outer2.getObject(i + 1);
				}
			}
			dbr2.open(rs2);
			// END BUGFIX

			getNextMain();
		} catch (java.sql.SQLException e) {
			throw new IOException("", e);
		}
	}

	public boolean hasNext() {
		return pair != null;
	}

	public ImmutableRecordPair getNext() throws IOException {
		return getNextMarkedRecordPair();
	}

	public MutableMarkedRecordPair getNextMarkedRecordPair() throws IOException {
		MutableMarkedRecordPair res = pair;
		getNextMain();
		return res;
	}

	private void getNextMain() throws IOException {
		try {
			boolean hasAnotherPair =
				rsDecision.next() && dbr.hasNext() && dbr2.hasNext();
			if (hasAnotherPair) {
				Record q = dbr.getNext();
				Record m = dbr2.getNext();
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

	public void close() throws IOException {
		Exception ex = null;
		try {
			rsDecision.close();
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			int noCursors = dbr.getNoCursors();
			for (int i = 0; i < noCursors; ++i) {
				rs[i].close();
				rs2[i].close();
			}
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			outer.close();
			outer2.close();
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			stmt.close();
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			conn.close();
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		if (ex != null) {
			throw new IOException("", ex);
		}
	}

	/**
	 * Get the value of name.
	 * @return value of name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the value of name.
	 * @param v  Value to assign to name.
	 */
	public void setName(String v) {
		this.name = v;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		setName(NameUtils.getNameFromFilePath(fileName));
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * Get the value of ds.
	 * @return value of ds.
	 */
	public DataSource getDs() {
		return ds;
	}

	/**
	 * Set the value of ds.
	 * @param v  Value to assign to ds.
	 */
	public void setDs(DataSource v) {
		this.ds = v;
	}

	/**
	 * Get the value of dataSourceName.
	 * @return value of dataSourceName.
	 */
	public String getDataSourceName() {
		return dataSourceName;
	}

	/**
	 * Set the value of dataSourceName.
	 * @param v  Value to assign to dataSourceName.
	 */
	public void setDataSourceName(String v) {
		this.dataSourceName = v;
		this.ds = DataSources.getDataSource(dataSourceName);
	}

	/**
	 * Get the value of model.
	 * @return value of model.
	 */
	public ImmutableProbabilityModel getModel() {
		return model;
	}

	/**
	 * Set the value of model.
	 * @param v  Value to assign to model.
	 */
	public void setModel(ImmutableProbabilityModel v) {
		this.model = v;
	}

	/**
	 * Get the value of selection.
	 * @return value of selection.
	 */
	public String getSelection() {
		return selection;
	}

	/**
	 * Set the value of selection.
	 * @param v  Value to assign to selection.
	 */
	public void setSelection(String v) {
		this.selection = v;
	}

	public String getConf() {
		return conf;
	}

	public void setConf(String v) {
		conf = v;
	}

	public String toString() {
		return name;
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		return null;
	}
}
