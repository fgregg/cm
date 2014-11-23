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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersJPA.ID_GENERATOR_VALUE_COLUMN_NAME;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.choicemaker.cm.batch.impl.BatchJobJPA;
import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;

/**
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQueries({
@NamedQuery(name = OabaParametersJPA.QN_BATCHPARAMETERS_FIND_ALL,
		query = OabaParametersJPA.JPQL_BATCHJOB_FIND_ALL),
//		@NamedQuery(name = OabaParametersJPA.QN_BATCHPARAMETERS_FIND_BY_JOB_ID,
//		query = OabaParametersJPA.JPQL_BATCHPARAMETERS_FIND_BY_JOB_ID),
})
@Entity
@Table(/* schema = "CHOICEMAKER", */name = OabaParametersJPA.TABLE_NAME)
public class OabaParametersEntity implements Serializable, OabaParameters {

	private static final long serialVersionUID = 271L;

	private static final Logger logger = Logger
			.getLogger(OabaParametersEntity.class.getName());

	protected static final String INVALID_NAME = null;

	protected static final int INVALID_MAX_SINGLE = -1;

	protected static final float INVALID_THRESHOLD = -1f;

	protected static final SerializableRecordSource INVALID_RECORD_SOURCE = null;
	
	protected static boolean isInvalidBatchParamsId(long id) {
		return id == BatchJobJPA.INVALID_ID;
	}

	public static boolean isPersistent(OabaParameters params) {
		boolean retVal = false;
		if (params != null) {
			retVal = !isInvalidBatchParamsId(params.getId());
		}
		return retVal;
	}

	@Id
	@Column(name = OabaParametersJPA.CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	private long id;

	@Column(name = OabaParametersJPA.CN_STAGE_MODEL)
	private final String modelConfigName;

	@Column(name = OabaParametersJPA.CN_LOW_THRESHOLD)
	private final float lowThreshold;

	@Column(name = OabaParametersJPA.CN_HIGH_THRESHOLD)
	private final float highThreshold;

	@Column(name = OabaParametersJPA.CN_STAGE_RS)
	private final SerializableRecordSource stageRs;

	@Column(name = OabaParametersJPA.CN_MASTER_RS)
	private final SerializableRecordSource masterRs;

	/** Required by JPA; do not invoke directly */
	protected OabaParametersEntity() {
		this.modelConfigName =INVALID_NAME;
		this.lowThreshold = INVALID_THRESHOLD;
		this.highThreshold = INVALID_THRESHOLD;
		this.stageRs = INVALID_RECORD_SOURCE;
		this.masterRs = INVALID_RECORD_SOURCE;
	}

	public OabaParametersEntity(String modelConfigurationName,
			float lowThreshold, float highThreshold,
			SerializableRecordSource stageRs) {
		this(modelConfigurationName, lowThreshold, highThreshold, stageRs, null);
	}

	public OabaParametersEntity(String modelConfigurationName,
			float lowThreshold, float highThreshold,
			SerializableRecordSource stageRs, SerializableRecordSource masterRs) {
		
		if (modelConfigurationName == null || modelConfigurationName.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank model");
		}
		Thresholds.validate(lowThreshold, highThreshold);
		if (stageRs == null) {
			throw new IllegalArgumentException("null staging source");
		}
		if (masterRs == null) {
			logger.info("null master source");
		}

		this.modelConfigName = modelConfigurationName.trim();
		this.lowThreshold = lowThreshold;
		this.highThreshold = highThreshold;
		this.stageRs = stageRs;
		this.masterRs = masterRs;
	}

	public OabaParametersEntity(OabaParameters bp) {
		this(bp.getModelConfigurationName(), bp.getLowThreshold(), bp
				.getHighThreshold(), bp.getStageRs(), bp.getMasterRs());
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getModelConfigurationName() {
		return modelConfigName;
	}

	@Override
	public String getStageModel() {
		return getModelConfigurationName();
	}

	@Override
	public String getMasterModel() {
		return getModelConfigurationName();
	}

	@Override
	public float getLowThreshold() {
		return lowThreshold;
	}

	@Override
	public float getHighThreshold() {
		return highThreshold;
	}

	@Override
	public SerializableRecordSource getStageRs() {
		return stageRs;
	}

	@Override
	public SerializableRecordSource getMasterRs() {
		return masterRs;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (id == 0) {
			result = hashCode0();
		} else {
			result = prime * result + (int) (id ^ (id >>> 32));
		}
		return result;
	}

	/**
	 * Hashcode for instances with id == 0
	 */
	protected int hashCode0() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(highThreshold);
		result = prime * result + Float.floatToIntBits(lowThreshold);
		result =
			prime * result + ((masterRs == null) ? 0 : masterRs.hashCode());
		result =
			prime * result + ((modelConfigName == null) ? 0 : modelConfigName.hashCode());
		result = prime * result + ((stageRs == null) ? 0 : stageRs.hashCode());
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
		OabaParametersEntity other = (OabaParametersEntity) obj;
		if (id != other.id) {
			return false;
		}
		if (id == 0) {
			return equals0(other);
		}
		return true;
	}

	/**
	 * Equality test for instances with id == 0
	 */
	protected boolean equals0(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OabaParametersEntity other = (OabaParametersEntity) obj;
		if (Float.floatToIntBits(highThreshold) != Float
				.floatToIntBits(other.highThreshold)) {
			return false;
		}
		if (Float.floatToIntBits(lowThreshold) != Float
				.floatToIntBits(other.lowThreshold)) {
			return false;
		}
		if (masterRs == null) {
			if (other.masterRs != null) {
				return false;
			}
		} else if (!masterRs.equals(other.masterRs)) {
			return false;
		}
		if (modelConfigName == null) {
			if (other.modelConfigName != null) {
				return false;
			}
		} else if (!modelConfigName.equals(other.modelConfigName)) {
			return false;
		}
		if (stageRs == null) {
			if (other.stageRs != null) {
				return false;
			}
		} else if (!stageRs.equals(other.stageRs)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OabaParametersEntity [id=" + id + ", model=" + modelConfigName
				+ ", lowThreshold=" + lowThreshold + ", highThreshold="
				+ highThreshold + "]";
	}

}
