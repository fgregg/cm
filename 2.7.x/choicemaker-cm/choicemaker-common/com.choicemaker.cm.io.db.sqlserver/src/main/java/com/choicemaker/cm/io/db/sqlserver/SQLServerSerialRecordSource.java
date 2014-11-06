/*
 * Created on Aug 18, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.PMManager;

/**
 * This is a wrapper object around SqlServerRecordSource and it can be serialized, because it
 * stores string values with which to create the SqlServerRecordSource.
 * 
 * @author pcheung
 *
 */
public class SQLServerSerialRecordSource implements SerialRecordSource {
	
	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger.getLogger(SQLServerSerialRecordSource.class.getName());

	private String dsJNDIName;
	private String dsMapName;
	private String modelName;
	private String dbConfig;
	private String sqlQuery;
	private int maxCompositeSize;
	
	private transient SqlServerCompositeRecordSource sqlRS;
	private transient DataSource ds;
	private transient ImmutableProbabilityModel model;
	
	
	public SQLServerSerialRecordSource (String dsJNDIName, String dsMapName, String modelName,
		String dbConfig, String sqlQuery, int maxCompositeSize) {
		
		this.dsJNDIName = dsJNDIName;
		this.dsMapName = dsMapName;
		this.modelName = modelName;
		this.dbConfig = dbConfig;
		this.sqlQuery = sqlQuery;
		this.maxCompositeSize = maxCompositeSize;
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

/*			
			sqlRS.setDataSource(dsMapName, getDataSource ());
			sqlRS.setModel(getModel ());
			sqlRS.setDbConfiguration(dbConfig);
			sqlRS.setIdsQuery(sqlQuery);
*/

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
		if (o instanceof SQLServerSerialRecordSource) {
			SQLServerSerialRecordSource rs = (SQLServerSerialRecordSource) o;
			return rs.dbConfig.equals(this.dbConfig) && 
				rs.dsJNDIName.equals(this.dsJNDIName) &&
				rs.modelName.equals(this.modelName) &&
				rs.sqlQuery.equals(this.sqlQuery) &&
				rs.dsMapName.equals(this.dsMapName) &&
				rs.maxCompositeSize == this.maxCompositeSize;
		} else {
			return false;
		}
	}


}
