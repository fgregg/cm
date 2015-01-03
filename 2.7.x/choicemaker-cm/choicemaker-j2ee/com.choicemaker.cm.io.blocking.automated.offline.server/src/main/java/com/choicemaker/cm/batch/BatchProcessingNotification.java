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
package com.choicemaker.cm.batch;

import java.io.Serializable;
import java.util.Date;

/**
 * This is the data object that gets passed to the UpdateStatusMDB message bean.
 * 
 * @author pcheung
 *
 */
public abstract class BatchProcessingNotification implements Serializable {

	static final long serialVersionUID = 271;

	private final long jobId;
	private final String jobType;
//	private final String jobStatus;
	private final float jobPercentComplete;
	private final String eventType;
	private final long eventId;
	private final Date eventTimestamp;
	private final String info;

	public long getJobId() {
		return jobId;
	}

	public String getJobType() {
		return jobType;
	}

//	public String getJobStatus() {
//		return jobStatus;
//	}

	public float getJobPercentComplete() {
		return jobPercentComplete;
	}

	public long getEventId() {
		return eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public Date getEventTimestamp() {
		return eventTimestamp;
	}

	public String getInfo() {
		return info;
	}

	protected BatchProcessingNotification(long jobId, String jobType,
			/*String jobStatus,*/ float percentComplete, long eventId,
			String eventType, Date timestamp) {
		this(jobId, jobType, /*jobStatus,*/ percentComplete, eventId, eventType,
				timestamp, null);
	}

	protected BatchProcessingNotification(long jobId, String jobType,
			/*String jobStatus,*/ float percentComplete, long eventId,
			String eventType, Date timestamp, String info) {
		this.jobId = jobId;
		this.jobType = jobType;
//		this.jobStatus = jobStatus;
		this.jobPercentComplete = percentComplete;
		this.eventId = eventId;
		this.eventType = eventType;
		this.eventTimestamp = timestamp;
		this.info = info;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (eventId ^ (eventId >>> 32));
		result =
			prime
					* result
					+ ((eventTimestamp == null) ? 0 : eventTimestamp.hashCode());
		result =
			prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + (int) (jobId ^ (jobId >>> 32));
		result = prime * result + Float.floatToIntBits(jobPercentComplete);
//		result =
//			prime * result + ((jobStatus == null) ? 0 : jobStatus.hashCode());
		result = prime * result + ((jobType == null) ? 0 : jobType.hashCode());
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
		BatchProcessingNotification other = (BatchProcessingNotification) obj;
		if (eventId != other.eventId) {
			return false;
		}
		if (eventTimestamp == null) {
			if (other.eventTimestamp != null) {
				return false;
			}
		} else if (!eventTimestamp.equals(other.eventTimestamp)) {
			return false;
		}
		if (eventType == null) {
			if (other.eventType != null) {
				return false;
			}
		} else if (!eventType.equals(other.eventType)) {
			return false;
		}
		if (info == null) {
			if (other.info != null) {
				return false;
			}
		} else if (!info.equals(other.info)) {
			return false;
		}
		if (jobId != other.jobId) {
			return false;
		}
		if (Float.floatToIntBits(jobPercentComplete) != Float
				.floatToIntBits(other.jobPercentComplete)) {
			return false;
		}
//		if (jobStatus == null) {
//			if (other.jobStatus != null) {
//				return false;
//			}
//		} else if (!jobStatus.equals(other.jobStatus)) {
//			return false;
//		}
		if (jobType == null) {
			if (other.jobType != null) {
				return false;
			}
		} else if (!jobType.equals(other.jobType)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BatchProcessingNotification [jobId=" + jobId + ", jobType="
				+ jobType /*+ ", jobStatus=" + jobStatus*/
				+ ", jobPercentComplete=" + jobPercentComplete + ", eventType="
				+ eventType + ", eventId=" + eventId + ", eventTimestamp="
				+ eventTimestamp + ", info=" + info + "]";
	}

}
