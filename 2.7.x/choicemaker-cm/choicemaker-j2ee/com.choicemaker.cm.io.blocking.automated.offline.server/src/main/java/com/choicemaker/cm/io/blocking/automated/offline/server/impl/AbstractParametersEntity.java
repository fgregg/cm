package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_FORMAT;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_GRAPH;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_HIGH_THRESHOLD;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_LOW_THRESHOLD;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_MASTER_RS;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_MASTER_RS_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_MODEL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_STAGE_RS;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_STAGE_RS_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_TASK;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.DV_ABSTRACT;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.JPQL_PARAMETERS_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.QN_PARAMETERS_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.TABLE_NAME;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.core.base.ImmutableThresholds;

@NamedQuery(name = QN_PARAMETERS_FIND_ALL, query = JPQL_PARAMETERS_FIND_ALL)
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DV_ABSTRACT)
public class AbstractParametersEntity implements Serializable {

	private static final long serialVersionUID = 271L;

	protected static final int INVALID_MAX_SINGLE = -1;

	protected static final float INVALID_THRESHOLD = -1f;

	private static final long NONPERSISTENT_ID = 0;

	protected static boolean isInvalidParametersId(long id) {
		return id == NONPERSISTENT_ID;
	}

	public static boolean isPersistent(OabaParameters params) {
		boolean retVal = false;
		if (params != null) {
			retVal = !isInvalidParametersId(params.getId());
		}
		return retVal;
	}

	public static final String DEFAULT_DUMP_TAG = "BP";
	@Id
	@Column(name = CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	protected long id;

	@Column(name = CN_TYPE)
	protected final String type;

	@Column(name = CN_MODEL)
	protected final String modelConfigName;

	@Column(name = CN_LOW_THRESHOLD)
	protected final float lowThreshold;

	@Column(name = CN_HIGH_THRESHOLD)
	protected final float highThreshold;

	@Column(name = CN_STAGE_RS)
	protected final long stageRsId;

	@Column(name = CN_STAGE_RS_TYPE)
	protected final String stageRsType;

	@Column(name = CN_MASTER_RS)
	protected final Long masterRsId;

	@Column(name = CN_MASTER_RS_TYPE)
	protected final String masterRsType;

	@Column(name = CN_TASK)
	protected final String task;

	@Column(name = CN_FORMAT)
	protected final String format;

	@Column(name = CN_GRAPH)
	protected final String graph;

	/** Required by JPA; do not invoke directly */
	public AbstractParametersEntity() {
		this.type = DV_ABSTRACT;
		this.modelConfigName = null;
		this.lowThreshold = INVALID_THRESHOLD;
		this.highThreshold = INVALID_THRESHOLD;
		this.stageRsId = NONPERSISTENT_ID;
		this.stageRsType = null;
		this.masterRsId = null;
		this.masterRsType = null;
		this.task = null;
		this.format = null;
		this.graph = null;
	}

	/**
	 * 
	 * @param modelConfigurationName
	 *            model configuration name
	 * @param lowThreshold
	 *            differ threshold
	 * @param highThreshold
	 *            match threshold
	 * @param sId
	 *            persistence id of the staging record source
	 * @param sType
	 *            the type of the staging record source (FlatFile, XML, DB)
	 * @param mId
	 *            the persistence id of the master record. Must be null if the
	 *            <code>taskType</code> is STAGING_DEDUPLICATION; otherwise must
	 *            be non-null.
	 * @param mType
	 *            the type of the master record source. Must be null if the
	 *            <code>taskType</code> is STAGING_DEDUPLICATION; otherwise must
	 *            be non-null.
	 * @param taskType
	 *            the record matching task: duplication of a staging source;
	 *            linkage of a staging source to a master source; or linkage of
	 *            two master sources.
	 * @param format
	 *            Used by the constructor for TransitivityParametersEntity;
	 *            otherwise should be null.
	 * @param graph
	 *            Used by the constructor for TransitivityParametersEntity;
	 *            otherwise should be null.
	 */
	protected AbstractParametersEntity(String type,
			String modelConfigurationName, float lowThreshold,
			float highThreshold, long sId, String sType, Long mId,
			String mType, OabaLinkageType taskType, String format, String graph) {

		if (type == null || type.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank type");
		}
		if (modelConfigurationName == null
				|| modelConfigurationName.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank modelId");
		}
		ImmutableThresholds.validate(lowThreshold, highThreshold);
		if (sType == null || !sType.equals(sType.trim()) || sType.isEmpty()) {
			throw new IllegalArgumentException("invalid stage RS type: "
					+ sType);
		}
		if (taskType == null) {
			throw new IllegalArgumentException("null task type");
		}

		// The masterRsId, masterRsType and taskType must be consistent.
		// If the task type is STAGING_DEDUPLICATION, then the masterRsId
		// and the masterRsType must be null.
		// If the type is STAGING_TO_MASTER_LINKAGE or MASTER_TO_MASTER_LINKAGE,
		// then the masterRsId and the masterRsType must be non-null.
		// If the type is TRANSITIVITY_ANALYSIS, the masterRsId and the
		// masterRsType must be consistent with one another, but they are
		// otherwise unconstrained.
		if (taskType == OabaLinkageType.STAGING_DEDUPLICATION) {
			if (mId != null) {
				String msg =
					"non-null master source id '" + mId + "' (taskType is '"
							+ taskType + "')";
				throw new IllegalArgumentException(msg);
			}
			if (mType != null) {
				String msg =
					"non-null master source type '" + mType
							+ "' (taskType is '" + taskType + "')";
				throw new IllegalArgumentException(msg);
			}
		} else if (taskType == OabaLinkageType.STAGING_TO_MASTER_LINKAGE
				|| taskType == OabaLinkageType.MASTER_TO_MASTER_LINKAGE) {
			if (mId == null) {
				String msg =
					"null master source id '" + mId + "' (taskType is '"
							+ taskType + "')";
				throw new IllegalArgumentException(msg);
			}
			if (mType == null || !mType.equals(mType.trim()) || mType.isEmpty()) {
				String msg =
					"invalid master source type '" + mType + "' (taskType is '"
							+ taskType + "')";
				throw new IllegalArgumentException(msg);
			}
		} else {
			assert taskType == OabaLinkageType.TRANSITIVITY_ANALYSIS;
			if ((mType == null && mId != null)
					|| (mType != null && mId == null)) {
				String msg =
					"inconsistent master source id '" + mId
							+ "' and master source type '" + mType
							+ "' (taskType is '" + taskType + "')";
				throw new IllegalArgumentException(msg);
			}
		}

		this.type = type;
		this.modelConfigName = modelConfigurationName.trim();
		this.lowThreshold = lowThreshold;
		this.highThreshold = highThreshold;
		this.stageRsId = sId;
		this.stageRsType = sType;
		this.masterRsId = mId;
		this.masterRsType = mType;
		this.task = taskType.name();
		this.format = format;
		this.graph = graph;
	}

	public final long getId() {
		return id;
	}

	public final float getLowThreshold() {
		return lowThreshold;
	}

	public final float getHighThreshold() {
		return highThreshold;
	}

	public OabaLinkageType getOabaLinkageType() {
		return OabaLinkageType.valueOf(this.task);
	}

	// -- Identity

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (id != NONPERSISTENT_ID) {
			result = prime * result + (int) (id ^ (id >>> 32));
		} else {
			result = hashCode0();
		}
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
		AbstractParametersEntity other = (AbstractParametersEntity) obj;
		if (id != other.id) {
			return false;
		}
		if (id == NONPERSISTENT_ID) {
			return equals0(other);
		}
		return true;
	}

	protected int hashCode0() {
		assert id == NONPERSISTENT_ID;

		final int prime = 31;
		int result = 1;
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((graph == null) ? 0 : graph.hashCode());
		result = prime * result + Float.floatToIntBits(highThreshold);
		result = prime * result + Float.floatToIntBits(lowThreshold);
		result =
			prime * result + ((masterRsId == null) ? 0 : masterRsId.hashCode());
		result =
			prime * result
					+ ((masterRsType == null) ? 0 : masterRsType.hashCode());
		result =
			prime
					* result
					+ ((modelConfigName == null) ? 0 : modelConfigName
							.hashCode());
		result = prime * result + (int) (stageRsId ^ (stageRsId >>> 32));
		result =
			prime * result
					+ ((stageRsType == null) ? 0 : stageRsType.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	protected boolean equals0(AbstractParametersEntity other) {
		assert this != other;
		assert other != null;
		assert getClass() == other.getClass();
		assert id == NONPERSISTENT_ID && other.id == NONPERSISTENT_ID;

		if (format == null) {
			if (other.format != null) {
				return false;
			}
		} else if (!format.equals(other.format)) {
			return false;
		}
		if (graph == null) {
			if (other.graph != null) {
				return false;
			}
		} else if (!graph.equals(other.graph)) {
			return false;
		}
		if (Float.floatToIntBits(highThreshold) != Float
				.floatToIntBits(other.highThreshold)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (Float.floatToIntBits(lowThreshold) != Float
				.floatToIntBits(other.lowThreshold)) {
			return false;
		}
		if (masterRsId == null) {
			if (other.masterRsId != null) {
				return false;
			}
		} else if (!masterRsId.equals(other.masterRsId)) {
			return false;
		}
		if (masterRsType == null) {
			if (other.masterRsType != null) {
				return false;
			}
		} else if (!masterRsType.equals(other.masterRsType)) {
			return false;
		}
		if (modelConfigName == null) {
			if (other.modelConfigName != null) {
				return false;
			}
		} else if (!modelConfigName.equals(other.modelConfigName)) {
			return false;
		}
		if (stageRsId != other.stageRsId) {
			return false;
		}
		if (stageRsType == null) {
			if (other.stageRsType != null) {
				return false;
			}
		} else if (!stageRsType.equals(other.stageRsType)) {
			return false;
		}
		if (task == null) {
			if (other.task != null) {
				return false;
			}
		} else if (!task.equals(other.task)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AbstractParametersEntity [id=" + id + ", type=" + type
				+ ", task=" + task + "]";
	}

}