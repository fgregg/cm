/*
 * Created on Aug 18, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.IncompleteSpecificationException;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.PMManager;

/**
 * This is a wrapper object around SqlServerRecordSource and it can be serialized, because it
 * stores string values with which to create the SqlServerRecordSource.
 * 
 * @author pcheung
 *
 */
public class SQLServerSerializableCompositeRecordSource implements
		ISerializableRecordSource {
	
	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger
			.getLogger(SQLServerSerializableCompositeRecordSource.class
					.getName());
	
	/** Default maximum composite size (currently 100,000 records) */
	public static final int DEFAULT_MAX_COMPOSITE_SIZE = 100000;
	
	public static final String PN_DATA_SOURCE = "dataSource";
	public static final String PN_MODEL_NAME = "modelName";
	public static final String PN_DATABASE_CONFIGURATION = "databaseConfiguration";
	public static final String PN_SQL_QUERY = "sqlQuery";
	public static final String PN_MAX_COMPOSITE_SIZE = "maxCompositeSize";

	private String dsJNDIName;
	private String modelName;
	private String dbConfig;
	private String sqlQuery;
	private int maxCompositeSize;
	
	private transient SqlServerCompositeRecordSource sqlRS;
	private transient DataSource ds;
	private transient ImmutableProbabilityModel model;
	
	/**
	 * Constructs a record source with a {@link #DEFAULT_MAX_COMPOSITE_SIZE default}
	 * maximum composite size
	 * @param ds
	 *            JNDI name of a configured data source
	 * @param model
	 *            Name of a configured model
	 * @param dbConf
	 *            Name of a database configuration defined by the model
	 * @param sql
	 *            A SQL query that selects record IDs from the data source; e.g.
	 * <pre>
	 * SELECT record_id AS ID FROM records
	 * </pre>
	 */
	public SQLServerSerializableCompositeRecordSource(String ds, String model,
			String dbConf, String sql) {
		this(ds, model, dbConf, sql, DEFAULT_MAX_COMPOSITE_SIZE);
	}

	/**
	 * Constructs a record source with the specified maximum composite size
	 * @param ds
	 *            JNDI name of a configured data source
	 * @param model
	 *            Name of a configured model
	 * @param dbConf
	 *            Name of a database configuration defined by the model
	 * @param sql
	 *            A SQL query that selects record IDs from the data source; e.g.
	 * <pre>
	 * SELECT record_id AS ID FROM records
	 * </pre>
	 * @param maxCompositeSize
	 *            used to construct {@link SqlServerCompositeRecordSource}
	 */
	public SQLServerSerializableCompositeRecordSource(String ds,
			String model, String dbConf, String sql,
			int maxCompositeSize) {
		if (!isTrimmedNonBlankString(ds)) {
			String msg = "invalid data source: '" + ds + "'";
			throw new IllegalArgumentException(msg);
		}
		if (!isTrimmedNonBlankString(model)) {
			String msg = "invalid model name: '" + model + "'";
			throw new IllegalArgumentException(msg);
		}
		if (!isTrimmedNonBlankString(dbConf)) {
			String msg = "invalid database configuration: '" + dbConf + "'";
			throw new IllegalArgumentException(msg);
		}
		if (!isTrimmedNonBlankString(sql)) {
			String msg = "invalid sql query: '" + sql + "'";
			throw new IllegalArgumentException(msg);
		}
		if (maxCompositeSize < 1) {
			String msg = "invalid maximum composite size: " + maxCompositeSize;
			throw new IllegalArgumentException(msg);
		}
		this.dsJNDIName = ds;
		this.modelName = model;
		this.dbConfig = dbConf;
		this.sqlQuery = sql;
		this.maxCompositeSize = maxCompositeSize;
	}

	protected static boolean isTrimmedNonBlankString(String s) {
		boolean retVal = s != null && s.equals(s.trim()) && !s.isEmpty();
		return retVal;
	}
	
	/**
	 * An obsolete constructor with an unused parameter, <code>unused</code>
	 * @deprecated
	 */
	public SQLServerSerializableCompositeRecordSource (String ds, String unused, String modelName,
		String dbConfig, String sql, int maxCompositeSize) {
		this(ds, modelName, dbConfig, sql, maxCompositeSize);
	}
	
	
	private DataSource getDataSource () {
		try {
			if (ds == null) {
				Context ctx = new InitialContext();
				ds = (DataSource) ctx.lookup (dsJNDIName);
			}
		} catch (NamingException ex) {
			log.severe(ex.toString());
		}
		return ds;
	}
	
	private SqlServerCompositeRecordSource getRS () {
		if (sqlRS == null) {
			sqlRS = new SqlServerCompositeRecordSource (getDataSource(), getModel(), sqlQuery, 
			dbConfig, maxCompositeSize);
		}
		return sqlRS;
	}
	
	public ImmutableProbabilityModel getModel () {
		if (model == null) {
			model = PMManager.getModelInstance(modelName);
		}
		return model;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.RecordSource#getNext()
	 */
	public Record getNext() throws IOException {
		return getRS().getNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#open()
	 */
	public void open() throws IOException {
		getRS().open ();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#close()
	 */
	public void close() throws IOException {
		getRS().close();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#hasNext()
	 */
	public boolean hasNext() throws IOException {
		return getRS().hasNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#getName()
	 */
	public String getName() {
		return getRS().getName();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#setName(java.lang.String)
	 */
	public void setName(String name) {
		getRS().setName(name);
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#setModel(com.choicemaker.cm.core.ProbabilityModel)
	 */
	public void setModel(ImmutableProbabilityModel m) {
		getRS().setModel(m);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#hasSink()
	 */
	public boolean hasSink() {
		return getRS().hasSink();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#getSink()
	 */
	public Sink getSink() {
		return getRS().getSink();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#getFileName()
	 */
	public String getFileName() {
		return getRS().getFileName();
	}


	public boolean equals (Object o) {
		if (o instanceof SQLServerSerializableCompositeRecordSource) {
			SQLServerSerializableCompositeRecordSource rs = (SQLServerSerializableCompositeRecordSource) o;
			return rs.dbConfig.equals(this.dbConfig) && 
				rs.dsJNDIName.equals(this.dsJNDIName) &&
				rs.modelName.equals(this.modelName) &&
				rs.sqlQuery.equals(this.sqlQuery) &&
				rs.maxCompositeSize == this.maxCompositeSize;
		} else {
			return false;
		}
	}

	public String toString() {
		return "SQLServerSerializableCompositeRecordSource [dsJNDIName="
				+ dsJNDIName + ", modelName=" + modelName + ", dbConfig="
				+ dbConfig + ", sqlQuery=" + sqlQuery + ", maxCompositeSize="
				+ maxCompositeSize + "]";
	}

	public Properties getProperties() {
		Properties retVal = new Properties();
		retVal.setProperty(PN_DATA_SOURCE, dsJNDIName);
		retVal.setProperty(PN_MODEL_NAME, modelName);
		retVal.setProperty(PN_DATABASE_CONFIGURATION, dbConfig);
		retVal.setProperty(PN_SQL_QUERY, sqlQuery);
		retVal.setProperty(PN_MAX_COMPOSITE_SIZE, Integer.toString(maxCompositeSize));
		return null;
	}

	public void setProperties(Properties p)
			throws IncompleteSpecificationException {
		if (p != null) {
			String s = p.getProperty(PN_DATA_SOURCE, dsJNDIName);
			if (isTrimmedNonBlankString(s) && !s.equals(dsJNDIName)) {
				String msg = "Setting dsJNDIName to '" + s + "'";
				log.info(msg);
				dsJNDIName = s;
			}
			s = p.getProperty(PN_MODEL_NAME, modelName);
			if (isTrimmedNonBlankString(s) && !s.equals(modelName)) {
				String msg = "Setting modelName to '" + s + "'";
				log.info(msg);
				modelName = s;
			}
			s = p.getProperty(PN_DATABASE_CONFIGURATION, dbConfig);
			if (isTrimmedNonBlankString(s) && !s.equals(dbConfig)) {
				String msg = "Setting dbConfig to '" + s + "'";
				log.info(msg);
				dbConfig = s;
			}
			s = p.getProperty(PN_SQL_QUERY, sqlQuery);
			if (isTrimmedNonBlankString(s) && !s.equals(sqlQuery)) {
				String msg = "Setting sqlQuery to '" + s + "'";
				log.info(msg);
				sqlQuery = s;
			}
			final String s0 = Integer.toString(maxCompositeSize);
			s = p.getProperty(PN_MAX_COMPOSITE_SIZE, s0);
			if (isTrimmedNonBlankString(s) && !s.equals(s0)) {
				try {
					int i = Integer.parseInt(s);
					if (i > 0) {
						String msg = "Setting maxCompositeSize to " + i;
						log.info(msg);
						maxCompositeSize = i;
						
					} else {
						String msg = "Ignoring invalid value for maxCompositeSize: " + i;
						log.warning(msg);
					}
				} catch (NumberFormatException x) {
					String msg = "Invalid value for maxCompositeSize: '" + s + "'";
					log.warning(msg);
					msg = "maxCompositeSize remains unchanged: " + maxCompositeSize;
					log.info(msg);
				}
			}
		} else {
			String msg = "Ignoring null properties";
			log.fine(msg);
		}
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<qlServerCompositeRecordSource ");
		sb.append("dataSourceName=").append("\"").append(dsJNDIName).append("\" ");
		sb.append("model=").append("\"").append(modelName).append("\" ");
		sb.append("dbConfiguration=").append("\"").append(dbConfig).append("\" ");
		sb.append("idsQuery=").append("\"").append(sqlQuery).append("\" ");
		sb.append("maxCompositeSize=").append("\"").append(maxCompositeSize).append("\" ");
		sb.append("/>");
		return sb.toString();
	}


}
