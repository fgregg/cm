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
package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * This is the EJB implemenation of the OABA IStatus interface.
 * 
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = "statusLogFindAll",
query = "Select status from StatusLogBean status")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMT_OABA_STATUS_LOG")
public class StatusLogBean implements Serializable {
	
	public static final int DEFAULT_VERSION = 271;
	
	private static final long serialVersionUID = DEFAULT_VERSION;

	public static enum NamedQuery {
		FIND_ALL("statusLogFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	@Id
	@Column(name = "JOB_ID")
	@TableGenerator(name = "OABA_STATUSLOG", table = "CMT_SEQUENCE",
			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
			pkColumnValue = "OABA_STATUSLOG")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "OABA_STATUSLOG")
	private long jobId;

	@Column(name = "JOB_TYPE")
	private String jobType = BatchJobBean.TABLE_DISCRIMINATOR;

	@Column(name = "STATUS_ID")
	private int statusId;

	@Column(name = "VERSION")
	private int version = DEFAULT_VERSION;

	@Column(name = "INFO")
	private String info;

	public long getJobId() {
		return jobId;
	}

	protected void setJobId(long jobId) {
		this.jobId = jobId;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		if (jobType == null) {
			throw new IllegalArgumentException("null job type");
		}
		this.jobType = jobType;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getInfo() {
		return info;
	}

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
