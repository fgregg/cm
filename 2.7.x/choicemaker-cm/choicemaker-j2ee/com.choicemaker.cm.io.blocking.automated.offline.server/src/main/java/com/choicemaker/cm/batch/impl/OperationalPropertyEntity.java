package com.choicemaker.cm.batch.impl;

import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.CN_ID;
import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.CN_JOB_ID;
import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.CN_NAME;
import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.CN_VALUE;
import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.ID_GENERATOR_VALUE_COLUMN_NAME;

import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.OperationalProperty;

public class OperationalPropertyEntity implements OperationalProperty {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(OperationalPropertyEntity.class.getName());
	
	public static final String INVALID_NAME = null;
	public static final String INVALID_VALUE = null;
	
	// -- Instance data

	@Id
	@Column(name = CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	private long id;

	@Column(name = CN_JOB_ID)
	private final long jobId;
	
	@Column(name = CN_NAME)
	private final String name;
	
	@Column(name = CN_VALUE)
	private final String value;

	// -- Constructors

	protected OperationalPropertyEntity() {
		this.id = INVALID_ID;
		this.jobId = BatchJob.INVALID_ID;
		this.name = INVALID_NAME;
		this.value = INVALID_VALUE;
	}
	
	public OperationalPropertyEntity(
			BatchJob job, final String name, final String value
			) {
		if (job == null || !BatchJobEntity.isPersistent(job)) {
			throw new IllegalArgumentException("invalid job: " + job);
		}
		if (name == null || !name.equals(name.trim()) || name.isEmpty()) {
			throw new IllegalArgumentException("invalid property name: '" + name + "'");
		}
		if (value == null) {
			throw new IllegalArgumentException("invalid property value: '"
					+ value + "'");
		}
		
		final String stdName = name.toUpperCase();
		if (!name.equals(stdName)) {
			logger.warning("Converting property name '" + name + "' to upper-case '" + stdName + "'");
		}
		if (value.trim().isEmpty()) {
			logger.warning("Blank value for '" + stdName + "'");
		}
		
		this.jobId = job.getId();
		this.name = stdName;
		this.value = value;
	}

	// -- Accessors
	
	public OperationalPropertyEntity(OperationalProperty p) {
		this.id = p.getId();
		this.jobId = p.getJobId();
		this.name = p.getName();
		this.value = p.getValue();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getJobId() {
		return jobId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (getId() ^ (getId() >>> 32));
		if (getId() == INVALID_ID) {
			result = hashCode0();
		}
		return result;
	}

	protected int hashCode0() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
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
		OperationalPropertyEntity other = (OperationalPropertyEntity) obj;
		if (getId() != other.getId()) {
			return false;
		}
		if (this.getId() == INVALID_ID) {
			return equals0(other);
		}
		return true;
	}

	protected boolean equals0(OperationalProperty property) {
		assert property != null;

		if (getJobId() != property.getJobId()) {
			return false;
		}
		if (getName() == null) {
			if (property.getName() != null) {
				return false;
			}
		} else if (!getName().equals(property.getName())) {
			return false;
		}
		if (getValue() == null) {
			if (property.getValue() != null) {
				return false;
			}
		} else if (!getValue().equals(property.getValue())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OperationalPropertyEntity [id=" + getId() + ", jobId=" + getJobId()
				+ ", name=" + getName() + ", value=" + getValue() + "]";
	}

}
