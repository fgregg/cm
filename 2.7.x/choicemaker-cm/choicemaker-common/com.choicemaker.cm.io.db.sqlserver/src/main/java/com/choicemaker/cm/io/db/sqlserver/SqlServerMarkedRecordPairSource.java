/*
 * Created on Mar 18, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import javax.sql.DataSource;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.io.db.base.DataSources;

/**
 * @author ajwinkel
 *
 */
public class SqlServerMarkedRecordPairSource implements MarkedRecordPairSource {

	private String fileName;
	
	private ImmutableProbabilityModel model;
	private DataSource ds;
	private String dsName;
	private String dbConfiguration;
	private String mrpsQuery;

	private Iterator pairIterator;

	public SqlServerMarkedRecordPairSource() { }

	/**
	 * mrpsQuery should be something like: 
	 * 
	 *  select qid as id, mid as id_matched, decision from table where ...
	 */
	public SqlServerMarkedRecordPairSource(String fileName, ImmutableProbabilityModel model, String dsName, String dbConfiguration, String mrpsQuery) {
		this.fileName = fileName;
		this.model = model;
		this.dsName = dsName;
		this.dbConfiguration = dbConfiguration;
		this.mrpsQuery = mrpsQuery;
	}
	
	public void open() throws IOException {
		if (model == null) {
			throw new IllegalStateException("accessProvider is null");
		} else if (ds == null && dsName == null) {
			throw new IllegalStateException("no data source specified");
		} else if (dbConfiguration == null) {
			throw new IllegalStateException("dbConfiguration is null");
		} else if (mrpsQuery == null) {
			throw new IllegalStateException("mrpsQuery is null");
		}
		
		if (dsName != null && ds == null) {
			ds = DataSources.getDataSource(dsName);
			if (ds == null) {
				throw new IOException("Unable to get data source named: " + dsName);
			}
		}
		
		Connection conn = null;
		try {
			conn = ds.getConnection();
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
		rs.setModel(model);
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
		File f = new File(fileName);
		String name = f.getName();
		
		return name;
	}

	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	public void setModel(ImmutableProbabilityModel m) {
		this.model = m;
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}
	
	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}
	
	public void setDataSourceName(String dsName) {
		this.dsName = dsName;
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
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		throw new UnsupportedOperationException();
	}
	
}
