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
package com.choicemaker.demo.oaba0;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.choicemaker.cm.core.ISerializableRecordSource;

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
//	@GeneratedValue(strategy = GenerationType.TABLE, generator = "OABA_BATCHPARAMS")
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
	private ISerializableRecordSource stageRs;

	@Transient
	private ISerializableRecordSource masterRs;

	protected BatchParametersBean() {
		this(INVALID_JOBID);
	}

	protected BatchParametersBean(long jobId) {
		setId(jobId);
	}
	
	public BatchParametersBean(BatchJob batchJob) {
		this(batchJob.getId());
		if (BatchJobBean.isNonPersistent(batchJob)) {
			throw new IllegalArgumentException("non-persistent batch job");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#getId()
	 */
	@Override
	public long getId() {
		return id;
	}

	protected void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#getStageModel()
	 */
	@Override
	public String getStageModel() {
		return stageModel;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#setStageModel(java.lang.String)
	 */
	@Override
	public void setStageModel(String stageModel) {
		this.stageModel = stageModel;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#getMasterModel()
	 */
	@Override
	public String getMasterModel() {
		return masterModel;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#setMasterModel(java.lang.String)
	 */
	@Override
	public void setMasterModel(String masterModel) {
		this.masterModel = masterModel;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#getMaxSingle()
	 */
	@Override
	public int getMaxSingle() {
		return maxSingle;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#setMaxSingle(int)
	 */
	@Override
	public void setMaxSingle(int maxSingle) {
		this.maxSingle = maxSingle;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#getLowThreshold()
	 */
	@Override
	public float getLowThreshold() {
		return lowThreshold;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#setLowThreshold(float)
	 */
	@Override
	public void setLowThreshold(float lowThreshold) {
		this.lowThreshold = lowThreshold;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#getHighThreshold()
	 */
	@Override
	public float getHighThreshold() {
		return highThreshold;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#setHighThreshold(float)
	 */
	@Override
	public void setHighThreshold(float highThreshold) {
		this.highThreshold = highThreshold;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#getStageRs()
	 */
	@Override
	public ISerializableRecordSource getStageRs() {
		return stageRs;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#setStageRs(com.choicemaker.cm.core.ISerializableRecordSource)
	 */
	@Override
	public void setStageRs(ISerializableRecordSource stageRs) {
		this.stageRs = stageRs;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#getMasterRs()
	 */
	@Override
	public ISerializableRecordSource getMasterRs() {
		return masterRs;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.BatchParameters#setMasterRs(com.choicemaker.cm.core.ISerializableRecordSource)
	 */
	@Override
	public void setMasterRs(ISerializableRecordSource masterRs) {
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
