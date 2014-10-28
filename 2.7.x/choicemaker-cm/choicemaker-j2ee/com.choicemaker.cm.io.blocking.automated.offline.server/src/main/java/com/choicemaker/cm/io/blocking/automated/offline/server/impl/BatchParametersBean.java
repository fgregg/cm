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

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersJPA.*;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;

/**
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = BatchParametersJPA.QN_BATCHPARAMETERS_FIND_ALL,
		query = BatchParametersJPA.JPQL_BATCHJOB_FIND_ALL)
@Entity
@Table(/* schema = "CHOICEMAKER", */name = BatchParametersJPA.TABLE_NAME)
public class BatchParametersBean implements Serializable, BatchParameters {

	private static final long serialVersionUID = 271L;

	/** Default value when no jobId is assigned */
	public static final long INVALID_JOBID = 0;

	@Id
	@Column(name = BatchParametersJPA.CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	private long id;

	@Column(name = BatchParametersJPA.CN_STAGE_MODEL)
	private String stageModel;

	@Column(name = BatchParametersJPA.CN_MASTER_MODEL)
	private String masterModel;

	@Column(name = BatchParametersJPA.CN_MAX_SINGLE)
	private int maxSingle;

	@Column(name = BatchParametersJPA.CN_LOW_THRESHOLD)
	private float lowThreshold;

	@Column(name = BatchParametersJPA.CN_HIGH_THRESHOLD)
	private float highThreshold;

	@Transient
	private SerialRecordSource stageRs;

	@Transient
	private SerialRecordSource masterRs;

	public BatchParametersBean() {
	}

	public BatchParametersBean(BatchParameters bp) {
		this.stageModel = bp.getStageModel();
		this.masterModel = bp.getMasterModel();
		this.maxSingle = bp.getMaxSingle();
		this.lowThreshold = bp.getLowThreshold();
		this.highThreshold = bp.getHighThreshold();
		this.stageRs = bp.getStageRs();
		this.masterRs = bp.getMasterRs();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getStageModel() {
		return stageModel;
	}

	@Override
	public void setStageModel(String stageModel) {
		this.stageModel = stageModel;
	}

	@Override
	public String getMasterModel() {
		return masterModel;
	}

	@Override
	public void setMasterModel(String masterModel) {
		this.masterModel = masterModel;
	}

	@Override
	public int getMaxSingle() {
		return maxSingle;
	}

	@Override
	public void setMaxSingle(int maxSingle) {
		this.maxSingle = maxSingle;
	}

	@Override
	public float getLowThreshold() {
		return lowThreshold;
	}

	@Override
	public void setLowThreshold(float lowThreshold) {
		this.lowThreshold = lowThreshold;
	}

	@Override
	public float getHighThreshold() {
		return highThreshold;
	}

	@Override
	public void setHighThreshold(float highThreshold) {
		this.highThreshold = highThreshold;
	}

	@Override
	public SerialRecordSource getStageRs() {
		return stageRs;
	}

	@Override
	public void setStageRs(SerialRecordSource stageRs) {
		this.stageRs = stageRs;
	}

	@Override
	public SerialRecordSource getMasterRs() {
		return masterRs;
	}

	@Override
	public void setMasterRs(SerialRecordSource masterRs) {
		this.masterRs = masterRs;
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
			prime * result
					+ ((masterModel == null) ? 0 : masterModel.hashCode());
		result =
			prime * result + ((masterRs == null) ? 0 : masterRs.hashCode());
		result = prime * result + maxSingle;
		result =
			prime * result + ((stageModel == null) ? 0 : stageModel.hashCode());
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
		BatchParametersBean other = (BatchParametersBean) obj;
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
		BatchParametersBean other = (BatchParametersBean) obj;
		if (Float.floatToIntBits(highThreshold) != Float
				.floatToIntBits(other.highThreshold)) {
			return false;
		}
		if (Float.floatToIntBits(lowThreshold) != Float
				.floatToIntBits(other.lowThreshold)) {
			return false;
		}
		if (masterModel == null) {
			if (other.masterModel != null) {
				return false;
			}
		} else if (!masterModel.equals(other.masterModel)) {
			return false;
		}
		if (masterRs == null) {
			if (other.masterRs != null) {
				return false;
			}
		} else if (!masterRs.equals(other.masterRs)) {
			return false;
		}
		if (maxSingle != other.maxSingle) {
			return false;
		}
		if (stageModel == null) {
			if (other.stageModel != null) {
				return false;
			}
		} else if (!stageModel.equals(other.stageModel)) {
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
		return "BatchParametersBean [id=" + id + ", model=" + stageModel
				+ ", lowThreshold=" + lowThreshold + ", highThreshold="
				+ highThreshold + "]";
	}

}
