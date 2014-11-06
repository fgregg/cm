/*
 * Created on Mar 18, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.base.PMManager;

/**
 * @author ajwinkel
 *
 */
public class SqlServerSerialMRPSource implements MarkedRecordPairSource, Serializable {

	private static Logger logger = Logger.getLogger(SqlServerSerialMRPSource.class.getName());

	/* As of 2010-03-10 */
	static final long serialVersionUID = -5758345719984451125L;

//	private String fileName;
	private String dsName;
	private String modelName;
	private String dbConfiguration;
	private String mrpsQuery;
//	private String conf;
	
	private transient ImmutableProbabilityModel model;
	private transient DataSource ds;

	private Iterator pairIterator;

	public SqlServerSerialMRPSource() { }

	/**
	 * mrpsQuery should be something like: 
	 * 
	 *  select qid as id, mid as id_matched, decision from table where ...
	 */
	public SqlServerSerialMRPSource(String dsName, String modelName, String dbConfiguration, String mrpsQuery) {
		this.dsName = dsName;
		this.modelName = modelName;
		this.dsName = dsName;
		this.dbConfiguration = dbConfiguration;
		this.mrpsQuery = mrpsQuery;
	}
	
	public ImmutableProbabilityModel getModel() {
		if (model == null) model = PMManager.getModelInstance(modelName);
		return model;
	}

	public void open() throws IOException {
		if (getModel () == null) {
			throw new IllegalStateException("accessProvider is null");
		} else if (getDataSource () == null && dsName == null) {
			throw new IllegalStateException("no data source specified");
		} else if (dbConfiguration == null) {
			throw new IllegalStateException("dbConfiguration is null");
		} else if (mrpsQuery == null) {
			throw new IllegalStateException("mrpsQuery is null");
		}
		
		Connection conn = null;
		try {
			conn = getDataSource ().getConnection();
			conn.setReadOnly(true);
			
			MarkedRecordPairSourceSpec spec = null;
			try {
				spec = createSpecFromQuery(conn, mrpsQuery);
			} catch (SQLException ex) {
				throw new IOException("Problem opening MRPS: " + ex.getMessage(), ex);
			}
			
			RecordSource rs = createRecordSource(conn, mrpsQuery);
			
			this.pairIterator = spec.createPairs(rs).iterator();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new IOException("Problem opening MRPS: " + ex.getMessage());	
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}		
	}

	private static MarkedRecordPairSourceSpec createSpecFromQuery(Connection conn, String query) throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		MarkedRecordPairSourceSpec spec = new MarkedRecordPairSourceSpec();
		while (rs.next()) {
			String qIdStr = rs.getString(1);
			String mIdStr = rs.getString(2);
			String dStr = rs.getString(3);
			
			Decision d = Decision.valueOf(dStr.charAt(0));
			
			spec.addMarkedPair(qIdStr, mIdStr, d);
		}
		
		rs.close();
		stmt.close();
		
		return spec;
	}
	
	private RecordSource createRecordSource(Connection conn, String mrpsQuery) {
		String rsQuery = createRsQuery(mrpsQuery);
		
		SqlServerRecordSource rs = new SqlServerRecordSource();
		rs.setModel(getModel ());
		rs.setConnection(conn);
		rs.setDbConfiguration(dbConfiguration);
		rs.setIdsQuery(rsQuery);
		
		return rs;
	}
	
	private static String createRsQuery(String mrpsQuery) {
		StringBuffer buff = new StringBuffer(mrpsQuery.length() * 4);
		
		buff.append("select id from (" + mrpsQuery + ") foo");
		buff.append(" union ");
		buff.append("select id_matched from (" + mrpsQuery + ") bar");
				
		return buff.toString();
	}

	public boolean hasNext() throws IOException {
		return pairIterator.hasNext();
	}

	public ImmutableRecordPair getNext() throws IOException {
		return getNextMarkedRecordPair();
	}

	public MutableMarkedRecordPair getNextMarkedRecordPair() throws IOException {
		Object obj = pairIterator.next();
		if (obj instanceof ImmutableMarkedRecordPair) {
			return (MutableMarkedRecordPair)obj;	
		} else if (obj instanceof RecordPairRetrievalException) {
			throw (RecordPairRetrievalException)obj;
		} else {
			throw new IllegalStateException("pairIterator may contain only " +
				"MarkedRecordPair objects and RecordNotFoundException objects.");	
		}
	}

	public void close() throws IOException {
		pairIterator = null;
	}

	public String getName() {
		return dsName;
	}

	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	private DataSource getDataSource () {
		try {
			if (ds == null) {
				Context ctx = new InitialContext();
				ds = (DataSource) ctx.lookup (dsName);
			}
		} catch (NamingException ex) {
			logger.severe(ex.toString());
		}
		return ds;
	}

	//this is not used
	public void setModel(ImmutableProbabilityModel m) {
	}

	public void setDataSource(DataSource ds) {
	}
	
	public void setDataSourceName(String dsName) {
	}

	public String getDataSourceName() {
		return dsName;
	}
	
	public void setDbConfiguration(String dbConfiguration) {
		this.dbConfiguration = dbConfiguration;
	}
	
	public String getDbConfiguration() {
		return dbConfiguration;
	}
	
	public void setMrpsQuery(String mrpsQuery) {
		this.mrpsQuery = mrpsQuery;
	}
	
	public String getMrpsQuery() {
		return mrpsQuery;
	}
	

	public void setFileName(String fileName) {
	}

	public String getFileName() {
		return null;
	}


	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		throw new UnsupportedOperationException();
	}
	
}
