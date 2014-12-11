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

/**
 * This is the EJB implemenation of the OABA OabaProcessing interface.
 * 
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = "statusLogFindAll",
query = "Select status from StatusLogBean status")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMT_OABA_STATUS_LOG")
public class StatusLogBean implements Serializable, StatusLog {
	
	private static final long serialVersionUID = DEFAULT_VERSION;
	
	/** Default value when no jobId is assigned */
	public static final long INVALID_JOBID = 0;

	public static enum NamedQuery {
		FIND_ALL("statusLogFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	@Id
	@Column(name = "JOB_ID")
//	@TableGenerator(name = "OABA_STATUSLOG", table = "CMT_SEQUENCE",
//			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
//			pkColumnValue = "OABA_STATUSLOG")
//	@GeneratedValue(strategy = GenerationType.TABLE, generator = "OABA_STATUSLOG")
	private long jobId;

	@Column(name = "JOB_TYPE")
	private String jobType = BatchJobBean.TABLE_DISCRIMINATOR;

	@Column(name = "STATUS_ID")
	private int statusId;

	@Column(name = "VERSION")
	private int version = DEFAULT_VERSION;

	@Column(name = "INFO")
	private String info;
	
	protected StatusLogBean() {
		this(INVALID_JOBID);
	}

	protected StatusLogBean(long jobId) {
		setJobId(jobId);
	}
	
	public StatusLogBean(BatchJob batchJob) {
		this(batchJob.getId());
		if (BatchJobBean.isNonPersistent(batchJob)) {
			throw new IllegalArgumentException("non-persistent batch job");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#getJobId()
	 */
	@Override
	public long getJobId() {
		return jobId;
	}

	protected void setJobId(long jobId) {
		this.jobId = jobId;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#getJobType()
	 */
	@Override
	public String getJobType() {
		return jobType;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#setJobType(java.lang.String)
	 */
	@Override
	public void setJobType(String jobType) {
		if (jobType == null) {
			throw new IllegalArgumentException("null job type");
		}
		this.jobType = jobType;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#getStatusId()
	 */
	@Override
	public int getStatusId() {
		return statusId;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#setStatusId(int)
	 */
	@Override
	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#getVersion()
	 */
	@Override
	public int getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#setVersion(int)
	 */
	@Override
	public void setVersion(int version) {
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#getInfo()
	 */
	@Override
	public String getInfo() {
		return info;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.demo.oaba0.StatusLog#setInfo(java.lang.String)
	 */
	@Override
	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (jobId == 0) {
			result = hashCode0();
		} else {
			result = prime * result + (int) (jobId ^ (jobId >>> 32));
			result = prime * result + ((jobType == null) ? 0 : jobType.hashCode());
		}
		return result;
	}

	/**
	 * Hashcode for instances with jobId == 0
	 */
	public int hashCode0() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + ((jobType == null) ? 0 : jobType.hashCode());
		result = prime * result + statusId;
		result = prime * result + version;
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
		StatusLogBean other = (StatusLogBean) obj;
		if (jobId != other.jobId) {
			return false;
		}
		if (jobType == null) {
			if (other.jobType != null) {
				return false;
			}
		} else if (!jobType.equals(other.jobType)) {
			return false;
		}
		if (jobId == 0) {
			return equals0(other);
		}
		return true;
	}

	/**
	 * Equality test for instances with jobId == 0
	 */
	public boolean equals0(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StatusLogBean other = (StatusLogBean) obj;
		if (info == null) {
			if (other.info != null) {
				return false;
			}
		} else if (!info.equals(other.info)) {
			return false;
		}
		if (jobType == null) {
			if (other.jobType != null) {
				return false;
			}
		} else if (!jobType.equals(other.jobType)) {
			return false;
		}
		if (statusId != other.statusId) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StatusLogBean [jobId=" + jobId + ", jobType=" + jobType
				+ ", statusId=" + statusId + ", info=" + info + "]";
	}

}
