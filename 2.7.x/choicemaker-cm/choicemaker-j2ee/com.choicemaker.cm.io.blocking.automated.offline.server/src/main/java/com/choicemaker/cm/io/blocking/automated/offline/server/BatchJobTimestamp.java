package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.choicemaker.cm.io.blocking.automated.offline.server.BatchJobBean.STATUS;

@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMT_OABA_BATCHJOB_TIMESTAMPS")
public class BatchJobTimestamp implements Serializable {

	private static final long serialVersionUID = 271L;

	@Id
	@Column(name = "ID")
	@TableGenerator(name = "CMT_BATCHJOB_TIMESTAMP_SEQUENCE",
			table = "CMT_SEQUENCE", pkColumnName = "SEQ_NAME",
			valueColumnName = "SEQ_COUNT",
			pkColumnValue = "OABA_BATCHJOB_TIMESTAMP")
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = "CMT_BATCHJOB_TIMESTAMP_SEQUENCE")
	private long id;

	@ManyToOne
	@JoinColumn(name = "BATCHJOB_ID")
	private BatchJobBean batchJob;

	@Column(name = "STATUS", insertable = false, updatable = false)
	private String status;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TIMESTAMP")
	private Date timestamp;

	protected BatchJobTimestamp() {
		this((STATUS) null, null);
	}

	public BatchJobTimestamp(String sts, Date date) {
		this(STATUS.valueOf(sts), date);
	}

	public BatchJobTimestamp(STATUS sts, Date date) {
		setStatus(sts);
		setTimeStamp(date);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		// result =
		// prime * result
		// + ((timestamp == null) ? 0 : timestamp.hashCode());
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
		BatchJobTimestamp other = (BatchJobTimestamp) obj;
		if (id != other.id) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		// if (timestamp == null) {
		// if (other.timestamp != null) {
		// return false;
		// }
		// } else if (!timestamp.equals(other.timestamp)) {
		// return false;
		// }
		return true;
	}

	@Override
	public String toString() {
		return "BatchJobTimestamp [id=" + id + ", status="
				+ status + ", timestamp=" + timestamp + "]";
	}

	public long getBatchjobId() {
		return id;
	}

	public void setBatchjobId(long batchjobId) {
		this.id = batchjobId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String s) {
		// Throws IllegalArgumentException if s isn't a valid STATUS name
		setStatus(STATUS.valueOf(s));
	}

	public void setStatus(STATUS sts) {
		this.status = sts == null ? null : sts.name();
	}

	public Date getTimeStamp() {
		return timestamp;
	}

	public void setTimeStamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}