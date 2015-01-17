package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.CN_CLASSNAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.CN_DATASOURCE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.CN_DBCONFIG;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.CN_MODEL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.CN_SQL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.JPQL_SQLRS_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.QN_SQLRS_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.SqlRecordSourceJPA.TABLE_NAME;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.choicemaker.cm.args.PersistableSqlRecordSource;

@NamedQuery(name = QN_SQLRS_FIND_ALL, query = JPQL_SQLRS_FIND_ALL)
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public class SqlRecordSourceEntity extends BaseRecordSourceEntity implements
		PersistableSqlRecordSource {

	private static final long serialVersionUID = 271L;

	// -- Instance data

	@Column(name = CN_CLASSNAME)
	protected final String className;

	@Column(name = CN_DATASOURCE)
	protected final String dataSource;

	@Column(name = CN_MODEL)
	protected final String modelId;

	@Column(name = CN_SQL)
	protected final String sql;

	@Column(name = CN_DBCONFIG)
	protected final String dbConfig;

	// -- Constructors

	/** Required by JPA */
	protected SqlRecordSourceEntity() {
		super(TYPE);
		this.className = null;
		this.dataSource = null;
		this.modelId = null;
		this.sql = null;
		this.dbConfig = null;
	}

	public SqlRecordSourceEntity(PersistableSqlRecordSource psrs) {
		this(psrs.getClassName(), psrs.getDataSource(), psrs.getModelId(), psrs
				.getSqlSelectStatement(), psrs.getDatabaseConfiguration());
	}

	public SqlRecordSourceEntity(String className, String dataSource,
			String model, String sql, String dbConfig) {
		super(TYPE);

		if (className == null || !className.equals(className.trim())
				|| className.isEmpty()) {
			String msg = "invalid class name '" + className + "'";
			throw new IllegalArgumentException(msg);
		}
		if (dataSource == null || !dataSource.equals(dataSource.trim())
				|| dataSource.isEmpty()) {
			String msg = "invalid data source name '" + dataSource + "'";
			throw new IllegalArgumentException(msg);
		}
		if (model == null || !model.equals(model.trim()) || model.isEmpty()) {
			String msg = "invalid model id '" + model + "'";
			throw new IllegalArgumentException(msg);
		}
		if (sql == null || !sql.equals(sql.trim()) || sql.isEmpty()) {
			String msg = "invalid sql select statement '" + sql + "'";
			throw new IllegalArgumentException(msg);
		}
		if (dbConfig == null || !dbConfig.equals(dbConfig.trim())
				|| dbConfig.isEmpty()) {
			String msg = "invalid data configuration name '" + dbConfig + "'";
			throw new IllegalArgumentException(msg);
		}

		this.className = className;
		this.dataSource = dataSource;
		this.modelId = model;
		this.sql = sql;
		this.dbConfig = dbConfig;
	}

	// -- Accessors

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getDataSource() {
		return dataSource;
	}

	@Override
	public String getModelId() {
		return modelId;
	}

	@Override
	public String getSqlSelectStatement() {
		return sql;
	}

	@Override
	public String getDatabaseConfiguration() {
		return dbConfig;
	}

	// -- Identity

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (getId() ^ (getId() >>> 32));
		if (getId() == NONPERSISTENT_ID) {
			result = prime * result + hashCode0();
		}
		return result;
	}

	protected int hashCode0() {
		final int prime = 31;
		int result = 1;
		result =
			prime
					* result
					+ ((getClassName() == null) ? 0 : getClassName().hashCode());
		result =
			prime
					* result
					+ ((getDataSource() == null) ? 0 : getDataSource()
							.hashCode());
		result =
			prime
					* result
					+ ((getDatabaseConfiguration() == null) ? 0
							: getDatabaseConfiguration().hashCode());
		result =
			prime * result
					+ ((getModelId() == null) ? 0 : getModelId().hashCode());
		result =
			prime
					* result
					+ ((getSqlSelectStatement() == null) ? 0
							: getSqlSelectStatement().hashCode());
		result =
			prime * result + ((getType() == null) ? 0 : getType().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SqlRecordSourceEntity other = (SqlRecordSourceEntity) obj;
		if (getId() != other.getId()) {
			return false;
		}
		if (getId() == NONPERSISTENT_ID) {
			return equals0(other);
		}
		return true;
	}

	protected boolean equals0(SqlRecordSourceEntity other) {
		assert other != null;
		if (getClassName() == null) {
			if (other.getClassName() != null) {
				return false;
			}
		} else if (!getClassName().equals(other.getClassName())) {
			return false;
		}
		if (getDataSource() == null) {
			if (other.getDataSource() != null) {
				return false;
			}
		} else if (!getDataSource().equals(other.getDataSource())) {
			return false;
		}
		if (getDatabaseConfiguration() == null) {
			if (other.getDatabaseConfiguration() != null) {
				return false;
			}
		} else if (!getDatabaseConfiguration().equals(
				other.getDatabaseConfiguration())) {
			return false;
		}
		if (getModelId() == null) {
			if (other.getModelId() != null) {
				return false;
			}
		} else if (!getModelId().equals(other.getModelId())) {
			return false;
		}
		if (getSqlSelectStatement() == null) {
			if (other.getSqlSelectStatement() != null) {
				return false;
			}
		} else if (!getSqlSelectStatement().equals(
				other.getSqlSelectStatement())) {
			return false;
		}
		if (getType() == null) {
			if (other.getType() != null) {
				return false;
			}
		} else if (!getType().equals(other.getType())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SqlRecordSource [getId()=" + getId() + ", getDataSourceName()="
				+ getDataSource() + ", getSqlSelectStatement()="
				+ getSqlSelectStatement() + "]";
	}

}
