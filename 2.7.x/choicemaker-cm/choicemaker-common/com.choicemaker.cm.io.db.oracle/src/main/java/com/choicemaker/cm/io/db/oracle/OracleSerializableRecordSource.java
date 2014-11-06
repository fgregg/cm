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
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.IncompleteSpecificationException;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.AbstractRecordSourceSerializer;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.db.base.DbRecordSource;
import com.choicemaker.cm.io.db.base.ISerializableDbRecordSource;
import com.choicemaker.util.Precondition;
import com.choicemaker.util.StringUtils;

/**
 * This is a wrapper object around Oracle DbRecordSource and it can be serialized, because it
 * stores string values with which to create the DbRecordSource.
 *
 * @author pcheung (initial version implemented as SerialRecordSource)
 * @author rphall (rewrote to ISerializableRecordSource)
 *
 */
public class OracleSerializableRecordSource implements ISerializableDbRecordSource {

	private static final Logger log = Logger
			.getLogger(OracleSerializableRecordSource.class.getName());

	// 2010-03-12 rphall
	// Serial version UID unchanged.
	//
	// There were various changes that affect the value of
	// serialVersionUID that would be calculated by the JDK
	// serialver tool:
	// (*) Same fields, but now some are declared final
	// (*) The interface implemented by this class changed
	// (*) New methods were added
	// However, because the field types, number and order did not
	// change, previously serialized data from the old class version
	// should readable by the new class version.
	//
	// Before 2010-03-10, serialver would otherwise return
	// static final long serialVersionUID = 1659993220770055037L;
	// As of 2010-03-12, serialver would otherwise return
	// static final long serialVersionUID = 7186538778968134791L;
	//
	static final long serialVersionUID = 1659993220770055037L;

	private String dsJNDIName;
	private String modelName;
	private String dbConfig;
	private String sqlQuery;

	private transient DbRecordSource sqlRS;
	private transient DataSource ds;
	private transient ImmutableProbabilityModel model;

	public OracleSerializableRecordSource(
		String dsJNDIName,
		String modelName,
		String dbConfig,
		String sqlQuery) {

		this.dsJNDIName = dsJNDIName;
		setModelName(modelName);
		this.dbConfig = dbConfig;
		this.sqlQuery = sqlQuery;
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

	private DbRecordSource getRS () {
		if (sqlRS == null) {
			sqlRS = new DbRecordSource ();
			sqlRS.setDs(getDataSource ());
			sqlRS.setModel(getModel ());
			sqlRS.setConf(getDbConfig());
			sqlRS.setSelection(getSqlQuery());

			log.fine("dbConfig: " + getDbConfig() + " sql: " + getSqlQuery());
		}
		return sqlRS;
	}

	public ImmutableProbabilityModel getModel () {
		if (model == null) {
			model = PMManager.getModelInstance(getModelName());
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
		Precondition.assertNonEmptyString(name);
		getRS().setName(name);
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Source#setModel(com.choicemaker.cm.core.ProbabilityModel)
	 */
	public void setModel(ImmutableProbabilityModel m) {
		Precondition.assertNonNullArgument("null model", m);
		getRS().setModel(m);
		this.setModelName(m.getModelName());
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

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getDbConfig() == null) ? 0 : getDbConfig().hashCode());
		result = prime * result
				+ ((getDsJNDIName() == null) ? 0 : getDsJNDIName().hashCode());
		result = prime * result
				+ ((getModelName() == null) ? 0 : getModelName().hashCode());
		result = prime * result
				+ ((getSqlQuery() == null) ? 0 : getSqlQuery().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OracleSerializableRecordSource other = (OracleSerializableRecordSource) obj;
		if (getDbConfig() == null) {
			if (other.getDbConfig() != null)
				return false;
		} else if (!getDbConfig().equals(other.getDbConfig()))
			return false;
		if (getDsJNDIName() == null) {
			if (other.getDsJNDIName() != null)
				return false;
		} else if (!getDsJNDIName().equals(other.getDsJNDIName()))
			return false;
		if (getModelName() == null) {
			if (other.getModelName() != null)
				return false;
		} else if (!getModelName().equals(other.getModelName()))
			return false;
		if (getSqlQuery() == null) {
			if (other.getSqlQuery() != null)
				return false;
		} else if (!getSqlQuery().equals(other.getSqlQuery()))
			return false;
		return true;
	}

	/**
	 * Obsolete method for {@link #equals(Object)}. Used for testing only.
	 * @deprecated
	 */
	public boolean equals_00 (Object o) {
		if (o instanceof OracleSerializableRecordSource) {
			OracleSerializableRecordSource rs = (OracleSerializableRecordSource) o;
			return rs.getDbConfig().equals(this.getDbConfig())
				&& rs.getDsJNDIName().equals(this.getDsJNDIName())
				&& rs.getModelName().equals(this.getModelName())
				&& rs.getSqlQuery().equals(this.getSqlQuery());
		} else {
			return false;
		}
	}

	public Properties getProperties() {
		Properties retVal = new Properties();
		retVal.setProperty(PN_DATASOURCE_JNDI_NAME, this.getDsJNDIName());
		retVal.setProperty(PN_MODEL_NAME, this.getModelName());
		retVal.setProperty(PN_DATABASE_CONFIG, this.getDbConfig());
		retVal.setProperty(PN_SQL_QUERY, this.getSqlQuery());
		return retVal;
	}

	public void  setProperties(Properties properties) throws IncompleteSpecificationException {

		Precondition.assertNonNullArgument("null properties",properties);

		String s = properties.getProperty(PN_DATABASE_CONFIG);
		if (!StringUtils.nonEmptyString(s)) {
			String msg = "Missing property '" + PN_DATABASE_CONFIG + "'";
			log.severe(msg);
			throw new IncompleteSpecificationException(msg);
		}
		this.dbConfig = s;

		s = properties.getProperty(PN_DATASOURCE_JNDI_NAME);
		if (!StringUtils.nonEmptyString(s)) {
			String msg = "Missing property '" + PN_DATASOURCE_JNDI_NAME + "'";
			log.severe(msg);
			throw new IncompleteSpecificationException(msg);
		}
		this.dsJNDIName = s;

		s = properties.getProperty(PN_MODEL_NAME);
		if (!StringUtils.nonEmptyString(s)) {
			String msg = "Missing property '" + PN_MODEL_NAME + "'";
			log.severe(msg);
			throw new IncompleteSpecificationException(msg);
		}
		this.modelName = s;

		s = properties.getProperty(PN_SQL_QUERY);
		if (!StringUtils.nonEmptyString(s)) {
			String msg = "Missing property '" + PN_SQL_QUERY + "'";
			log.severe(msg);
			throw new IncompleteSpecificationException(msg);
		}
		this.sqlQuery = s;

		// Reset the underlaying record source (it will be lazily recreated)
		try {
			if (sqlRS != null) {
				sqlRS.close();
			}
		} catch (Exception x) {
			String msg = "Unable to close " + (sqlRS == null ? "record source" : sqlRS.getName());
			log.warning(msg);
		} finally {
			sqlRS = null;
		}
	}

	public String getDsJNDIName() {
		return dsJNDIName;
	}

	private void setModelName(String modelName) {
		// Blank names are currently allowed
		Precondition.assertNonNullArgument("null model name",modelName);
		this.modelName = modelName;
	}

	public String getModelName() {
		return modelName;
	}

	public String getDbConfig() {
		return dbConfig;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public String toXML() {
		String retVal = AbstractRecordSourceSerializer.toXML(this);
		return retVal;
	}

}
