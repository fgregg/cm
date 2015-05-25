package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.CN_BLOCKING_CONFIGURATION;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.CN_DB_ACCESSOR;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.CN_DB_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.CN_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.CN_MODELNAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.CN_QUERY_CONFIGURATION;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.CN_REFERENCE_CONFIGURATION;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordAccessJPA.TABLE_NAME;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.choicemaker.cm.args.RecordAccess;
import com.choicemaker.cm.batch.impl.AbstractPersistentObject;

@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
public class RecordAccessEntity extends AbstractPersistentObject implements
		Serializable, RecordAccess {

	private static final long serialVersionUID = 271L;

	private static Logger log = Logger.getLogger(RecordAccessEntity.class.getName());

	@Id
	@Column(name = CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	protected long id;

	@Column(name = CN_MODELNAME)
	protected final String modelName;

	@Column(name = CN_DB_TYPE)
	protected final String databaseType;

	@Column(name = CN_DB_ACCESSOR)
	protected final String databaseAccessor;

	@Column(name = CN_QUERY_CONFIGURATION)
	protected final String queryConfiguration;

	@Column(name = CN_REFERENCE_CONFIGURATION)
	protected final String referenceConfiguration;

	@Column(name = CN_BLOCKING_CONFIGURATION)
	protected final String blockingConfiguration;

	/**
	 * Constructs an invalid RecordAccessEntity with a null working directory.
	 * Subclasses must implement a method to set the working directory to a
	 * valid value after construction.
	 */
	public RecordAccessEntity(String modelName, String databaseType,
			String databaseAccessor, String queryConfiguration,
			String referenceConfiguration, String blockingConfiguration) {
		if (modelName == null || databaseType == null
				|| databaseAccessor == null || queryConfiguration == null
				|| referenceConfiguration == null
				|| blockingConfiguration == null) {
			throw new IllegalArgumentException("null argument");
		}

		modelName = modelName.trim();
		databaseType = databaseType.trim();
		databaseAccessor = databaseAccessor.trim();
		queryConfiguration = queryConfiguration.trim();
		referenceConfiguration = referenceConfiguration.trim();
		blockingConfiguration = blockingConfiguration.trim();

		if (modelName.isEmpty() || databaseType.isEmpty()
				|| databaseAccessor.isEmpty() || queryConfiguration.isEmpty()
				|| referenceConfiguration.isEmpty()
				|| blockingConfiguration.isEmpty()) {
			throw new IllegalArgumentException("blank argument");
		}

		this.modelName = modelName;
		this.databaseType = databaseType;
		this.databaseAccessor = databaseAccessor;
		this.queryConfiguration = queryConfiguration;
		this.referenceConfiguration = referenceConfiguration;
		this.blockingConfiguration = blockingConfiguration;
		log.fine("Constructed " + this.toString());
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getModelName() {
		return modelName;
	}

	@Override
	public String getDatabaseType() {
		return databaseType;
	}

	@Override
	public String getDatabaseAccessor() {
		return databaseAccessor;
	}

	@Override
	public String getDatabaseQueryConfiguration() {
		return queryConfiguration;
	}

	@Override
	public String getDatabaseReferenceConfiguration() {
		return queryConfiguration;
	}

	@Override
	public String getBlockingConfiguration() {
		return blockingConfiguration;
	}

	@Override
	public String toString() {
		return "RecordAccessEntity [id=" + id + ", modelName=" + modelName
				+ ", databaseType=" + databaseType + ", abaAccessor="
				+ databaseAccessor + ", queryConfiguration="
				+ queryConfiguration + ", referenceConfiguration="
				+ referenceConfiguration + ", blockingConfiguration="
				+ blockingConfiguration + "]";
	}

}
