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
package com.choicemaker.cm.io.db.oracle;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.NameUtils;
import com.choicemaker.cm.io.db.base.DataSources;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderParallel;

/**
 * Db 8i marked record pair source implementing
 * <code>MarkedRecordPairSource</code>. Used for reading training
 * data from a training database.
 *
 * This version fixes a problem with the getNextMethod.  dbr and rsDecision are not necessarily in order so
 * we need to put dbr records in a hashmap first.
 * <p>
 * FIXME: rename and move this Oracle-specific implementation
 * to the com.choicemaker.cm.io.db.oracle package
 * </p>
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 22:36:05 $
 */
public class OracleMarkedRecordPairSource2 implements MarkedRecordPairSource {
	private static Logger logger = Logger.getLogger(OracleMarkedRecordPairSource2.class.getName());
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
	private ResultSet rsDecision;
	private MutableMarkedRecordPair pair;
	private HashMap recordMap;

	/**
	 * Creates an uninitialized instance.
	 */
	public OracleMarkedRecordPairSource2() {
		name = "";
		selection = "";
	}

	/**
	 * Constructor.
	 */
	public OracleMarkedRecordPairSource2(String fileName, String dataSourceName, ImmutableProbabilityModel model, String conf, String selection) {
		setFileName(fileName);
		setDataSourceName(dataSourceName);
		setModel(model);
		this.selection = selection;
		this.conf = conf;
	}


	public OracleMarkedRecordPairSource2(String fileName, DataSource ds, ImmutableProbabilityModel model, String conf, String selection) {
		setFileName(fileName);
		this.dataSourceName = ds.toString();
		this.ds = ds;
		setModel(model);
		this.selection = selection;
		this.conf = conf;
	}



	public void open() throws IOException {
		try {
			dbr = ((DbAccessor) model.getAccessor()).getDbReaderParallel(conf);
			int noCursors = dbr.getNoCursors();
			conn = ds.getConnection();
//			conn.setAutoCommit(false); // 2015-04-01a EJB3 CHANGE rphall
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
			}
		} catch (java.sql.SQLException e) {
			ex = e;
		}
		try {
			outer.close();
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
