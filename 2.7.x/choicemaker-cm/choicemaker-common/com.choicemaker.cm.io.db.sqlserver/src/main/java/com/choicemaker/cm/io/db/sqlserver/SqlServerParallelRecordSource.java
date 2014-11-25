/*
 * Created on Feb 5, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.io.db.base.DataSources;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderParallel;
import com.choicemaker.cm.io.db.base.DbView;

/**
 * @author pcheung
 */
public class SqlServerParallelRecordSource implements RecordSource {

	private static Logger logger = Logger
			.getLogger(SqlServerParallelRecordSource.class.getName());

	private String fileName;

	private ImmutableProbabilityModel model;
	private String dbConfiguration;
	private String idsQuery;

	private String dsName;
	private DataSource ds;
	private Connection connection;
	
	private DbReaderParallel dbr;
	private Statement [] selects;
	private ResultSet [] results;
	
	private static final String DATA_VIEW = "DATAVIEW_1001";
	
	public SqlServerParallelRecordSource() {
	}
	
	public SqlServerParallelRecordSource(String fileName,
			ImmutableProbabilityModel model, String dsName,
			String dbConfiguration, String idsQuery) {
		
		logger.fine("Constructor: " + fileName + " " + model + " " + dsName
				+ " " + dbConfiguration + " " + idsQuery);
		
		this.model = model;
		setDataSourceName(dsName);
		this.dbConfiguration = dbConfiguration;
		this.idsQuery = idsQuery;
	}

	public void open() throws IOException {
		
		if (!isValidQuery())
			throw new IOException("idsQuery must contain ' AS ID '.");
		
		DbAccessor accessor = (DbAccessor) model.getAccessor();
		dbr = accessor.getDbReaderParallel(dbConfiguration);
		
		try {
			if (connection == null) {
				connection = ds.getConnection();
//				connection.setAutoCommit(true);
			}
			//connection.setReadOnly(true);
			
			//1.	Create view
			createView (connection);
			
			//2.	Get the ResultSets
			getResultSets ();
			
			//3.	Open parallel reader
			logger.fine("before dbr.open");
			dbr.open(results);
		} catch (SQLException ex) {
			logger.severe(ex.toString());
			
			throw new IOException(ex.toString(), ex);
		}
	}

	private void createView (Connection conn) throws SQLException {
		Statement view = conn.createStatement();
		String s =
			"IF EXISTS (SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = '"
					+ DATA_VIEW + "') DROP VIEW " + DATA_VIEW;
		logger.fine(s);
		view.execute(s);
		
		s = "create view " + DATA_VIEW + " as " + idsQuery;
		logger.fine(s);
		view.execute(s);
		view.close();
	}
	
	private void dropView (Connection conn) throws SQLException {
		Statement view = conn.createStatement();
		String s =
			"IF EXISTS (SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = '"
					+ DATA_VIEW + "') DROP VIEW " + DATA_VIEW;
		logger.fine(s);
		view.execute(s);
		view.close();
	}

	/**
	 * This method checks to make sure the idsQuery is valid. It must contain
	 * "as id". For example,
	 * 
	 * <pre>
	 * select distinct TAP_CORPORATE_ID as ID
	 *   from CORPORATE where primary_name like 'A%'
	 * </pre>
	 * 
	 * @return
	 */
	private boolean isValidQuery () {
		if (idsQuery.toUpperCase().indexOf(" AS ID ") == -1) return false;
		else return true;
	}

	private void getResultSets () throws SQLException {
		Accessor accessor = model.getAccessor();
		String viewBase =
			"vw_cmt_" + accessor.getSchemaName() + "_r_" + dbConfiguration;
		DbView[] views = dbr.getViews();
		String masterId = dbr.getMasterId();
		
		int numViews = views.length;
		selects = new Statement [numViews];
		results = new ResultSet [numViews];

		for (int i = 0; i < numViews; ++i) {
			String viewName = viewBase + i;
			logger.finest("view: " + viewName);
			DbView v = views[i];

			StringBuffer sb = new StringBuffer ("select * from "); 
			sb.append(viewBase);
			sb.append(i);
			sb.append(" where ");
			sb.append(masterId);
			sb.append(" in (select id from ");
			sb.append(DATA_VIEW);
			sb.append(")");
			
			if (v.orderBy.length > 0) {
				sb.append(" ORDER BY ");
				sb.append(getOrderBy(v));
			}
			
			String queryString = sb.toString();
			
			logger.fine ("Query: " + queryString);
			
			selects[i] = connection.prepareStatement(queryString);
			logger.fine("Prepared statement");
			selects[i].setFetchSize(100);
			logger.fine("Changed Fetch Size to 100");
			results[i] = ((PreparedStatement) selects[i]).executeQuery();
			logger.fine("Executed query " + i);		
		}		
	}

	private static String getOrderBy(DbView v) {
		StringBuffer ob = new StringBuffer();
		for (int j = 0; j < v.orderBy.length; ++j) {
			if (j != 0)
				ob.append(",");
			ob.append(v.orderBy[j].name);
		}
		return ob.toString();
	}

	public boolean hasNext() throws IOException {
		return dbr.hasNext();
	}

	public Record getNext() throws IOException {
		Record r = null;
		try {
			r = dbr.getNext();
		} catch (SQLException e) {
			throw new IOException (e.toString());
		}
		return r;
	}

	public void close() throws IOException {
		try {
			int s = selects.length;
			for (int i=0; i<s; i++) {
				selects[i].close();
				results[i].close();
			}

			dropView (connection);
			
			if (ds != null) {
				connection.close();
				connection = null;
			}
		} catch (SQLException ex) {
			String msg =
				"Problem closing the statement or connection." + ex.toString();
			logger.severe(msg);
			throw new IOException(msg, ex);
		} finally {
			dbr = null;
		}
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void setModel(ImmutableProbabilityModel m) {
		this.model = m;
	}
	
	public String getDataSourceName() {
		return dsName;
	}

	public void setDataSourceName(String dsName) {
		DataSource ds = DataSources.getDataSource(dsName);
		setDataSource(dsName, ds);
	}
		
	public DataSource getDataSource() {
		return ds;
	}
	
	public void setDataSource(String name, DataSource ds) {
		this.dsName = name;
		this.ds = ds;
	}
	
	public void setConnection(Connection connection) {
		this.dsName = null;
		this.ds = null;
		
		this.connection = connection;
	}
	
	public String getDbConfiguration() {
		return dbConfiguration;
	}
	
	public void setDbConfiguration(String dbConfiguration) {
		this.dbConfiguration = dbConfiguration;
	}
	
	public String getIdsQuery() {
		return idsQuery;
	}
	
	public void setIdsQuery(String idsQuery) {
		this.idsQuery = idsQuery;
	}
	
	public String getName() {
		return "SQL Server Parallel Record Source";
	}

	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		throw new UnsupportedOperationException();
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String toString() {
		return "SqlServerParallelRecordSource [fileName=" + fileName
				+ ", model=" + model + ", dbConfiguration=" + dbConfiguration
				+ ", idsQuery=" + idsQuery + ", dsName=" + dsName + "]";
	}

}
