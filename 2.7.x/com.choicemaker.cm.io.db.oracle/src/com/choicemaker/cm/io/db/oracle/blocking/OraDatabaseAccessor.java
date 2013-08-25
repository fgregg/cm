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
package com.choicemaker.cm.io.db.oracle.blocking;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.io.blocking.automated.base.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.base.BlockingField;
import com.choicemaker.cm.io.blocking.automated.base.BlockingSet;
import com.choicemaker.cm.io.blocking.automated.base.BlockingValue;
import com.choicemaker.cm.io.blocking.automated.base.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.base.BlockingSet.GroupTable;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderParallel;
import com.choicemaker.cm.io.db.base.Index;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2013/02/23 19:47:11 $
 */
public class OraDatabaseAccessor implements DatabaseAccessor {
	private static final char BS_SEP = '^';
	private static final char TB_VAL_SEP = '`';
	private static final int MAX_LEN = 3950;
	private static final String SET_FROM_MODEL = new String();
	private static Logger logger = Logger.getLogger(OraDatabaseAccessor.class);
	private DataSource ds;
	private Connection connection;
	private DbReaderParallel dbr;
	private ResultSet outer;
	private ResultSet[] rs;
	private CallableStatement stmt;
	private String condition1;
	private String condition2;
	private String startSession;
	private String endSession;

	public OraDatabaseAccessor() {
	}

	public OraDatabaseAccessor(DataSource ds, String condition1, String condition2) {
		this.ds = ds;
		this.condition1 = condition1;
		this.condition2 = condition2;
		startSession = SET_FROM_MODEL;
	}

	public OraDatabaseAccessor(
		DataSource ds,
		String condition1,
		String condition2,
		String startSession,
		String endSession) {
		this(ds, condition1, condition2);
		this.startSession = startSession;
		this.endSession = endSession;
	}

	
	public void setDataSource(DataSource dataSource) {
		ds = dataSource;
	}
	
	public void setCondition(Object condition) {
		if (condition instanceof String []) {
			String [] cs = (String [])condition;
			condition1 = cs[0];
			condition2 = cs[1];
		} else if (condition instanceof String) {
			condition2 = (String) condition;
		}
	}

	public DatabaseAccessor cloneWithNewConnection()
		throws CloneNotSupportedException {
		DatabaseAccessor retVal = new OraDatabaseAccessor(
				this.ds,
				this.condition1,
				this.condition2,
				this.startSession,
				this.endSession);
		return retVal;
	}

	public void open(AutomatedBlocker blocker) throws IOException {
		logger.debug("open");
		ImmutableProbabilityModel model = blocker.getModel();
		if (startSession == SET_FROM_MODEL) {
			startSession = (String) model.properties().get("startSession");
			endSession = (String) model.properties().get("endSession");
		}
		Accessor acc = model.getAccessor();
		String dbrName = (String) model.properties().get("dbConfiguration");
		dbr = ((DbAccessor) acc).getDbReaderParallel(dbrName);
		String query = getQuery(blocker,dbr);
		
		logger.debug("query length: " + query.length());
		logger.debug("query: " + query);
		
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);

			if (startSession != null) {
				Statement stmt = connection.createStatement();
				stmt.execute(startSession);
				stmt.close();
			}

			if (query.length() >= MAX_LEN) {
				PreparedStatement prep = null;
				try {
					prep = connection.prepareStatement("INSERT INTO tb_cmt_temp_q VALUES(?)");
					while (query.length() >= MAX_LEN) {
						int pos = query.lastIndexOf(BS_SEP, MAX_LEN);
						String pre = query.substring(0, pos);
						if (logger.isDebugEnabled()) {
							logger.debug("INSERT INTO tb_cmt_temp_q VALUES('" + pre + ")");
						}
						prep.setString(1, pre);
						prep.execute();
						query = query.substring(pos + 1, query.length());
					}
					//					Statement st = connection.createStatement();
					//					st.executeQuery("delete from tb_cmt_temp_q");
					//					st.close();
				} finally {
					if (prep != null) {
						try {
							prep.close();
						} catch (Exception ex) {
						}
					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug(
					"call CMTBlocking.Blocking('"
						+ blocker.getBlockingConfiguration().getName()
						+ "', '"
						+ query
						+ "', '"
						+ condition1
						+ "', '"
						+ condition2
						+ "' ,'"
						+ acc.getSchemaName() + ":r:" + dbrName
						+ "', '?')");
			}

			stmt = connection.prepareCall("call CMTBlocking.Blocking(?, ?, ?, ?, ?, ?)");
			stmt.setFetchSize(100);
			stmt.setString(1, blocker.getBlockingConfiguration().getName());
			stmt.setString(2, query);
			stmt.setString(3, condition1);
			stmt.setString(4, condition2);
			stmt.setString(5, acc.getSchemaName() + ":r:" + dbrName);
			stmt.registerOutParameter(6, OracleTypes.CURSOR);
			logger.debug("execute");
			stmt.execute();
			logger.debug("retrieve outer");
			outer = (ResultSet) stmt.getObject(6);
			outer.setFetchSize(100);
			int len = dbr.getNoCursors();
			if (len == 1) {
				rs = new ResultSet[] { outer };
				outer = null;
			} else {
				outer.next();
				rs = new ResultSet[len];
				for (int i = 0; i < len; ++i) {
					logger.debug("retrieve nested cursor: " + i);
					rs[i] = (ResultSet) outer.getObject(i + 1);
					rs[i].setFetchSize(100);
				}
			}
			logger.debug("open dbr");
			dbr.open(rs);
		} catch (SQLException ex) {
			logger.error(
				"call CMTBlocking.Blocking('"
					+ blocker.getBlockingConfiguration().getName()
					+ "', '"
					+ query
					+ "', '"
					+ condition1
					+ "', '"
					+ condition2
					+ "' ,'"
					+ acc.getSchemaName() + ":r:" + dbrName
					+ "', '?')",
				ex);
			throw new IOException(ex.toString());
		}
	}

	public void close() throws IOException {
		logger.debug("close");
		Exception ex = null;
		if (rs != null) {
			for (int i = 0; i < rs.length; ++i) {
				if (rs[i] != null) {
					try {
						rs[i].close();
					} catch (java.sql.SQLException e) {
						ex = e;
						logger.error("Closing cursors.", e);
					}
				}
			}
		}
		rs = null;
		if (outer != null) {
			try {
				outer.close();
			} catch (java.sql.SQLException e) {
				ex = e;
				logger.error("Closing cursors.", e);
			}
			outer = null;
		}
		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		} catch (java.sql.SQLException e) {
			ex = e;
			logger.error("Closing statement.", e);
		}
		if (connection != null) {
			try {
				connection.commit();
			} catch (java.sql.SQLException e) {
				ex = e;
				logger.error("Commiting.", e);
			}
			if (endSession != null) {
				try {
					Statement stmt = connection.createStatement();
					stmt.execute(startSession);
					stmt.close();
				} catch (SQLException e) {
					ex = e;
					logger.error("Ending session", e);
				}
			}
			try {
				connection.close();
				connection = null;
			} catch (java.sql.SQLException e) {
				ex = e;
				logger.error("Closing connection.", e);
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
		try {
			return dbr.getNext();
		} catch (SQLException ex) {
			logger.error("getNext", ex);
			throw new IOException(ex.toString());
		}
	}

	// Public for testing
	public static String getQuery(AutomatedBlocker blocker, DbReaderParallel dbr) {
		
		// Preconditions
		if (blocker == null || dbr == null) {
			throw new IllegalArgumentException("null argument");
		}

		StringBuffer b = new StringBuffer(4000);
		boolean firstBlockingSet = true;
		String masterId = null;
		Iterator iBlockingSets = blocker.getBlockingSets().iterator();
		while (iBlockingSets.hasNext()) {
			BlockingSet bs = (BlockingSet) iBlockingSets.next();
			if (firstBlockingSet) {
				masterId = bs.getTable(0).table.uniqueId;
				firstBlockingSet = false;
			} else {
				b.append(BS_SEP);
			}
			//			bs.sortValues(false);
			//			bs.sortTables(true, false);
			b.append(getHints(bs,dbr));
			b.append(" v0.");
			b.append(masterId);
			b.append(" FROM ");
			int numTables = bs.getNumTables();
			for (int j = 0; j < numTables; ++j) {
				if (j != 0) {
					b.append(",");
				}
				BlockingSet.GroupTable gt = bs.getTable(j);
				b.append(gt.table.name).append(" v").append(gt.number);
			}
			b.append(TB_VAL_SEP);
			int numValues = bs.numFields();
			for (int j = 0; j < numValues; ++j) {
				if (j != 0) {
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
		}
		return b.toString();
	}

	// Public for testing
	public static String getHints(BlockingSet bs, DbReaderParallel dbr) {
		
		// Preconditions
		if (bs == null || dbr == null) {
			throw new IllegalArgumentException("null argument");
		}

		int numFields = bs.numFields();
		int numTables = bs.getNumTables();
		if (numFields > 1 && numFields > numTables) {
			StringBuffer joins = null;
			Map indices = dbr.getIndices();
			for (int i = 0; i < numTables; ++i) {
				GroupTable gt = bs.getTable(i);
				BlockingValue[] bvs = bs.getBlockingValues(gt);
				Map tableIndices = (Map)indices.get(gt.table.name);
				if (bvs.length > 1 && tableIndices != null) {
					String[] fields = new String[bvs.length];
					for (int j = 0; j < fields.length; j++) {
						fields[j] = bvs[j].blockingField.dbField.name;
					}
					Arrays.sort(fields);
					StringBuffer rep = new StringBuffer(fields.length * 32);
					for (int j = 0; j < fields.length; j++) {
						rep.append(fields[j]);
						rep.append('|');
					}
					Index[] uis = (Index[])tableIndices.get(rep.toString());
					if(uis != null && uis.length > 1) {
						if(joins == null) joins = new StringBuffer(127);
						joins.append("index_join(v");
						joins.append(gt.number);
						joins.append(' ');
						joins.append(uis[0].getName());
						for(int j = 1; j < uis.length; ++j) {
							joins.append(',');
							joins.append(uis[j].getName());
						}
						joins.append(") ");
					}
				}
			}
			if (joins == null) {
				return "";
			} else {
				return "/*+ " + joins + "*/";
			}
		} else {
			return "";
		}
	}
	
	private boolean covers(Index index, BlockingValue[] bvs) {
		for (int i = 0; i < bvs.length; i++) {
			int idx = index.find(bvs[i].blockingField.dbField.name);
			if (idx == -1 || idx > bvs.length) {
				return false;
			}
		}
		return true;
	}

	public static boolean mustQuote(String type) {
		return !(
			type == "byte"
				|| type == "short"
				|| type == "int"
				|| type == "long"
				|| type == "float"
				|| type == "double");
	}

	private static String escape(String s) {
		int len = s.length();
		int pos = 0;
		char ch;
		while (pos < len && (ch = s.charAt(pos)) != BS_SEP && ch != TB_VAL_SEP && ch != '\'' && ch >= 32) {
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
				} else if (ch >= 32 && ch != BS_SEP && ch != TB_VAL_SEP) {
					res[out++] = ch;
				}
				++pos;
			}
			return new String(res, 0, out);
		}
	}

	/**
	 * @return
	 */
	public String getEndSession() {
		return endSession;
	}

	/**
	 * @return
	 */
	public String getStartSession() {
		return startSession;
	}

	/**
	 * @param string
	 */
	public void setEndSession(String string) {
		endSession = string;
	}

	/**
	 * @param string
	 */
	public void setStartSession(String string) {
		startSession = string;
	}
}
