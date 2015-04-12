package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_FORMAT;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_GRAPH;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_HIGH_THRESHOLD;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_LOW_THRESHOLD;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_REFERENCE_RS;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_REFERENCE_RS_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_MODEL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_QUERY_RS;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_QUERY_RS_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_QUERY_RS_DEDUPED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_TASK;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.CN_TYPE;
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
import com.choicemaker.cm.batch.impl.AbstractPersistentObject;
import com.choicemaker.cm.core.base.ImmutableThresholds;

@NamedQuery(name = QN_PARAMETERS_FIND_ALL, query = JPQL_PARAMETERS_FIND_ALL)
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DV_ABSTRACT)
public class AbstractParametersEntity extends AbstractPersistentObject
		implements Serializable {

	private static final long serialVersionUID = 271L;

	protected static final int INVALID_MAX_SINGLE = -1;

	protected static final float INVALID_THRESHOLD = -1f;

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

	@Column(name = CN_QUERY_RS)
	protected final long queryRsId;

	@Column(name = CN_QUERY_RS_TYPE)
	protected final String queryRsType;

	@Column(name = CN_QUERY_RS_DEDUPED)
	protected final boolean queryRsIsDeduplicated;

	@Column(name = CN_REFERENCE_RS)
	protected final Long referenceRsId;

	@Column(name = CN_REFERENCE_RS_TYPE)
	protected final String referenceRsType;

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
		this.queryRsId = NONPERSISTENT_ID;
		this.queryRsType = null;
		this.queryRsIsDeduplicated = false;
		this.referenceRsId = null;
		this.referenceRsType = null;
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
			float highThreshold, long sId, String sType, boolean qIsDeduplicated,
			Long mId,
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

		// The referenceRsId, referenceRsType and taskType must be consistent.
		// If the task type is STAGING_DEDUPLICATION, then the referenceRsId
		// and the referenceRsType must be null.
		// If the type is STAGING_TO_MASTER_LINKAGE or MASTER_TO_MASTER_LINKAGE,
		// then the referenceRsId and the referenceRsType must be non-null.
		// If the type is TRANSITIVITY_ANALYSIS, the referenceRsId and the
		// referenceRsType must be consistent with one another, but they are
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
		this.queryRsId = sId;
		this.queryRsType = sType;
		this.queryRsIsDeduplicated = qIsDeduplicated;
		this.referenceRsId = mId;
		this.referenceRsType = mType;
		this.task = taskType.name();
		this.format = format;
		this.graph = graph;
	}

	// HACK FIXME REMOVEME
	protected AbstractParametersEntity(long persistenceId, String type,
			String modelConfigurationName, float lowThreshold,
			float highThreshold, long sId, String sType, boolean qIsDeduplicated, Long mId,
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

		// The referenceRsId, referenceRsType and taskType must be consistent.
		// If the task type is STAGING_DEDUPLICATION, then the referenceRsId
		// and the referenceRsType must be null.
		// If the type is STAGING_TO_MASTER_LINKAGE or MASTER_TO_MASTER_LINKAGE,
		// then the referenceRsId and the referenceRsType must be non-null.
		// If the type is TRANSITIVITY_ANALYSIS, the referenceRsId and the
		// referenceRsType must be consistent with one another, but they are
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

		this.id = persistenceId;
		this.type = type;
		this.modelConfigName = modelConfigurationName.trim();
		this.lowThreshold = lowThreshold;
		this.highThreshold = highThreshold;
		this.queryRsId = sId;
		this.queryRsType = sType;
		this.queryRsIsDeduplicated = qIsDeduplicated;
		this.referenceRsId = mId;
		this.referenceRsType = mType;
		this.task = taskType.name();
		this.format = format;
		this.graph = graph;
	}
	// END HACK FIXME REMOVEME

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
	public String toString() {
		return "AbstractParametersEntity [id=" + id + ", uuid=" + getUUID()
				+ ", type=" + type + ", task=" + task + "]";
	}

}