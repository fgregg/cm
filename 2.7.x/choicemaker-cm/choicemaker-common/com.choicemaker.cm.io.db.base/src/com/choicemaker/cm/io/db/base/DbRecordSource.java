/*
 * Copyright (c) 2001, 2013 ChoiceMaker Technologies, Inc. and others.
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
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.Record;
import com.choicemaker.cm.core.base.RecordSource;
import com.choicemaker.cm.core.base.Sink;
import com.choicemaker.cm.core.util.ChainedIOException;
import com.choicemaker.cm.core.util.NameUtils;

/**
 * Oracle record source.
 * <p>
 * FIXME: rename and move this Oracle-specific implementation
 * to the com.choicemaker.cm.io.db.oracle package
 * </p>
 *
 * @author    Adam Winkel
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 22:36:54 $
 */
public class DbRecordSource implements RecordSource {
	private static Logger logger = Logger.getLogger(DbRecordSource.class);
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
	private Record record;

	/**
	 * Creates an uninitialized instance.
	 */
	public DbRecordSource() {
		name = "";
		selection = "";
	}

	/**
	 * Constructor.
	 */
	public DbRecordSource(String fileName, String dataSourceName, ImmutableProbabilityModel model, String conf, String selection) {
		setFileName(fileName);
		setDataSourceName(dataSourceName);
		setModel(model);
		this.selection = selection;
		this.conf = conf;
	}

	public void open() throws IOException {
		try {
			conn = ds.getConnection();
			conn.setAutoCommit(false);

//			System.out.println (" user: " + conn.getMetaData().getUserName());
			
			DbAccessor dba = (DbAccessor) model.getAccessor();
			dbr = (dba).getDbReaderParallel(conf);
			
			logger.debug (conf + " " + dbr);
			
			// Set _debugSql=true for SqlDeveloper debugging
			//
			// See Sue Harper, 2006-07-13, http://links.rph.cx/XyfaZ5,
			// "Remote Debugging with SQL Developer"
			//
			// See Sanat Pattanaik, 2011-01-14, http://links.rph.cx/16ER9Dw,
			// "Debugging PL-SQL calls from Java Session Using Eclipse and SQL Developer"
			//
			boolean _debugSql = false;
			if (_debugSql) {
				try {
					CallableStatement _stmt =
						conn.prepareCall("begin DBMS_DEBUG_JDWP.CONNECT_TCP( '127.0.0.1', 4000 ); end;");
					_stmt.execute();
					_stmt.close();
				} catch (Exception _x) {
					logger.warn(_x.toString());
				}
			}
			// END SqlDeveloper debugging

			String sql = "call CMTTRAINING.RS_SNAPSHOT (?,?,?)";
			stmt = conn.prepareCall(sql);
			
			stmt.setString(1, selection);
			String s = dbr.getName();
			stmt.setString(2, s);
			stmt.registerOutParameter(3, CURSOR);

			logger.debug("select: " + selection);
			logger.debug("dbrName: " + s);

			stmt.execute();

			outer = (ResultSet) stmt.getObject(3);
			outer.next();

			int noCursors = dbr.getNoCursors();
			rs = new ResultSet[noCursors];
			if (noCursors == 1) {
				rs[0] = outer;
			} else {
				for (int i = 0; i < noCursors; ++i) {
					logger.debug("Get cursor: " + i);
					rs[i] = (ResultSet) outer.getObject(i + 1);
				}
			}
			
			dbr.open(rs);

			getNextMain();

		} catch (java.sql.SQLException e) {
			e.printStackTrace();
			throw new ChainedIOException(e.getMessage(), e);
		}
	}

	public boolean hasNext() {
		return record != null;
	}

	public Record getNext() throws IOException {
		Record r = record;
		getNextMain();
		return r;
	}

	private void getNextMain() throws IOException {
		try {
			if (dbr.hasNext()) {
				record = dbr.getNext();
			} else {
				record = null;
			}
		} catch (java.sql.SQLException e) {
			throw new ChainedIOException(e.getMessage(), e);
		}
	}

	public void close() throws IOException {
		Exception ex = null;
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
			throw new ChainedIOException(ex.getMessage(), ex);
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
		setName(NameUtils.getNameFromFileName(fileName));
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
	 * @return value of model. May be null.
	 */
	public ImmutableProbabilityModel getModel() {
		return model;
	}

	/**
	 * Set the value of model.
	 * @param v  Value to assign to model.
	 * May be null (required for ISerializableRecordSource impl).
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
