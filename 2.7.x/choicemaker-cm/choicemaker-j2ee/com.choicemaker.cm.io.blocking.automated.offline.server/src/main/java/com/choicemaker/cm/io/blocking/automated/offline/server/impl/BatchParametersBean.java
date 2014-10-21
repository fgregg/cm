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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;

/**
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = "batchParametersFindAll",
		query = "Select params from BatchParametersBean params")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMT_OABA_BATCH_PARAMS")
public class BatchParametersBean implements Serializable, BatchParameters {

	private static final long serialVersionUID = 271L;

	public static enum NamedQuery {
		FIND_ALL("batchParametersFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	/** Default value when no jobId is assigned */
	public static final long INVALID_JOBID = 0;

	@Id
	@Column(name = "ID")
//	@TableGenerator(name = "OABA_BATCHPARAMS", table = "CMT_SEQUENCE",
//			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
//			pkColumnValue = "OABA_BATCHPARAMS")
//	@GeneratedValue(strategy = GenerationType.TABLE,
//			generator = "OABA_BATCHPARAMS")
	private long id;

	@Column(name = "STAGE_MODEL")
	private String stageModel;

	@Column(name = "MASTER_MODEL")
	private String masterModel;

	@Column(name = "MAX_SINGLE")
	private int maxSingle;

	@Column(name = "LOW_THRESHOLD")
	private float lowThreshold;

	@Column(name = "HIGH_THRESHOLD")
	private float highThreshold;

	@Transient
	private SerialRecordSource stageRs;

	@Transient
	private SerialRecordSource masterRs;

	protected BatchParametersBean() {
		this(INVALID_JOBID);
	}

	protected BatchParametersBean(long jobId) {
		this.id = jobId;
	}

	public BatchParametersBean(BatchJob batchJob) {
		this(batchJob.getId());
		if (BatchJobBean.isNonPersistent(batchJob)) {
			throw new IllegalArgumentException("non-persistent batch job");
		}
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
