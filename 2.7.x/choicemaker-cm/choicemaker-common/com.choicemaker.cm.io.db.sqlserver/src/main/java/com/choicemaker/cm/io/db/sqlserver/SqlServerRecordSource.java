/*
 * Created on Feb 5, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.io.db.base.DataSources;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderSequential;
import com.choicemaker.cm.io.db.sqlserver.dbom.SqlDbObjectMaker;

/**
 * @author ajwinkel
 *
 */
public class SqlServerRecordSource implements RecordSource {
	
	private static final Logger logger = Logger.getLogger(SqlServerRecordSource.class.getName());

	private String fileName;

	private ImmutableProbabilityModel model;
	private String dbConfiguration;
	private String idsQuery;

	private String dsName;
	private DataSource ds;
	private Connection connection;
	private Statement stmt;
	
	private DbReaderSequential dbr;
	
	public SqlServerRecordSource() {
		// do nothing...
	}
	
	public SqlServerRecordSource(String fileName, ImmutableProbabilityModel model, String dsName, String dbConfiguration, String idsQuery) {
		this.model = model;
		setDataSourceName(dsName);
		this.dbConfiguration = dbConfiguration;
		this.idsQuery = idsQuery;
	}

	public void open() throws IOException {
		DbAccessor accessor = (DbAccessor) model.getAccessor();
		dbr = accessor.getDbReaderSequential(dbConfiguration);
		String query = createQuery();
		
		logger.fine(query);
		
		try {
			if (connection == null) {
				connection = ds.getConnection();
				connection.setAutoCommit(true);
			}
			//connection.setAutoCommit(false);
			connection.setReadOnly(true);
			stmt = connection.createStatement();
			stmt.setFetchSize(100);

			ResultSet rs = stmt.executeQuery(query);
			
			dbr.open(rs, stmt);
		} catch (SQLException ex) {
			logger.severe(ex.toString());
			
			throw new IOException(ex.toString());
		}
	}

	public boolean hasNext() throws IOException {
		return dbr.hasNext();
	}

	public Record getNext() throws IOException {
		return dbr.getNext();
	}

	public void close() throws IOException {
		try {
			stmt.close();
			stmt = null;
			
			if (ds != null) {
				connection.close();
				connection = null;
			}
		} catch (SQLException ex) {
			throw new IOException("Problem closing the statement or connection.", ex);
		} finally {
			//free the memory from dbr
			dbr = null;
		}
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void setModel(ImmutableProbabilityModel model) {
		this.model = model;
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
		return "SQL Server Record Source";
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
	
	private String createQuery() {
		StringBuffer b = new StringBuffer(16000);
		b.append("DECLARE @ids TABLE (id " + dbr.getMasterIdType() + ")" + Constants.LINE_SEPARATOR);
		b.append("INSERT INTO @ids " + idsQuery + Constants.LINE_SEPARATOR);
		b.append(SqlDbObjectMaker.getMultiQuery(model, dbConfiguration));
		return b.toString();
	}

}
