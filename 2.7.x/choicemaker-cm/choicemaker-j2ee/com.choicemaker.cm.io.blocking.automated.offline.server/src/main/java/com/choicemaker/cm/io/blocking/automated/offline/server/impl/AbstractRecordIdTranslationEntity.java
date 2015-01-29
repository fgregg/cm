package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_INTEGER;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_LONG;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_STRING;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.CN_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.CN_JOB_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.CN_RECORD_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.CN_RECORD_SOURCE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.CN_RECORD_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.CN_TRANSLATED_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.DV_ABSTRACT;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.DV_INTEGER;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.DV_LONG;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.DV_STRING;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.JPQL_TRANSLATEDID_DELETE_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.JPQL_TRANSLATEDID_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.JPQL_TRANSLATEDID_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.QN_TRANSLATEDID_DELETE_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.QN_TRANSLATEDID_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.QN_TRANSLATEDID_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.TABLE_NAME;

import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdTranslation;

@NamedQueries({
		@NamedQuery(name = QN_TRANSLATEDID_FIND_ALL,
				query = JPQL_TRANSLATEDID_FIND_ALL),
		@NamedQuery(name = QN_TRANSLATEDID_FIND_BY_JOBID,
				query = JPQL_TRANSLATEDID_FIND_BY_JOBID),
		@NamedQuery(name = QN_TRANSLATEDID_DELETE_BY_JOBID,
				query = JPQL_TRANSLATEDID_DELETE_BY_JOBID) })
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DV_ABSTRACT)
public abstract class AbstractRecordIdTranslationEntity<T extends Comparable<T>>
		implements RecordIdTranslation<T> {

	private static final long serialVersionUID = 271;

	private static final Logger logger = Logger
			.getLogger(AbstractRecordIdTranslationEntity.class.getName());

	/** Magic values to indicate an non-initialized record id */
	public static final String INVALID_RECORD_ID = null;

	/** Magic value to indicate an non-initialized record source */
	public static final char INVALID_RECORD_SOURCE = '\0';

	/**
	 * Checks for consistency between the JPA interface and the RECORD_ID_TYPE
	 * interface
	 */
	public static boolean isClassValid() {
		// Counts the number of discriminator values
		int dvCount = 0;

		boolean retVal = DV_ABSTRACT.length() == 1;
		++dvCount;

		retVal = retVal && DV_INTEGER.equals(TYPE_INTEGER.getStringSymbol());
		retVal = retVal && !DV_INTEGER.equals(DV_ABSTRACT);
		++dvCount;

		retVal = retVal && DV_LONG.equals(TYPE_LONG.getStringSymbol());
		retVal = retVal && !DV_LONG.equals(DV_ABSTRACT);
		++dvCount;

		retVal = retVal && DV_STRING.equals(TYPE_STRING.getStringSymbol());
		retVal = retVal && !DV_STRING.equals(DV_ABSTRACT);
		++dvCount;

		// One more discriminator (ABSTRACT) value than enum type
		int ritCount = RECORD_ID_TYPE.values().length;
		retVal = retVal && dvCount == ritCount + 1;

		return retVal;
	}

	static {
		if (!isClassValid()) {
			String msg =
				"AbstractRecordIdTranslationEntity class is inconsistent with "
						+ " RECORD_ID_TYPE enumeration";
			logger.severe(msg);
			// An exception is not thrown here because it would create a class
			// initialization exception, which can be hard to debug
		}
	}

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

	@Column(name = CN_TRANSLATED_ID)
	private final int translatedId;

	@Column(name = CN_RECORD_ID)
	protected final String recordId;

	@Column(name = CN_RECORD_TYPE)
	private final char recordType;

	@Column(name = CN_RECORD_SOURCE)
	private final char recordSource;

	// -- Construction

	protected AbstractRecordIdTranslationEntity() {
		this.jobId = BatchJob.INVALID_ID;
		this.translatedId = INVALID_TRANSLATED_ID;
		this.recordId = INVALID_RECORD_ID;
		this.recordType = DV_ABSTRACT.charAt(0);
		this.recordSource = INVALID_RECORD_SOURCE;
	}

	protected AbstractRecordIdTranslationEntity(long jobId, String recordId,
			char recordType, char recordSource, int translatedId) {
		assert jobId != BatchJob.INVALID_ID;
		assert translatedId > INVALID_TRANSLATED_ID;
		assert recordId != INVALID_RECORD_ID;
		assert recordType != DV_ABSTRACT.charAt(0);
		assert recordSource != INVALID_RECORD_SOURCE;

		this.jobId = jobId;
		this.translatedId = translatedId;
		this.recordId = recordId;
		this.recordType = recordType;
		this.recordSource = recordSource;
	}

	// -- Abstract methods

	@Override
	public abstract T getRecordId();

	// -- Template methods

	@Override
	public final long getId() {
		return id;
	}

	@Override
	public final long getJobId() {
		return jobId;
	}

	@Override
	public final int getTranslatedId() {
		return translatedId;
	}

	@Override
	public final RECORD_ID_TYPE getRecordIdType() {
		return RECORD_ID_TYPE.fromSymbol(recordType);
	}

	@Override
	public final RECORD_SOURCE_ROLE getRecordSourceRole() {
		return RECORD_SOURCE_ROLE.fromSymbol(recordSource);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (jobId ^ (jobId >>> 32));
		result =
			prime * result + ((recordId == null) ? 0 : recordId.hashCode());
		result = prime * result + recordSource;
		result = prime * result + recordType;
		result = prime * result + (translatedId ^ (translatedId >>> 32));
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
		@SuppressWarnings("rawtypes")
		AbstractRecordIdTranslationEntity other =
			(AbstractRecordIdTranslationEntity) obj;
		if (id != other.id) {
			return false;
		}
		if (jobId != other.jobId) {
			return false;
		}
		if (translatedId != other.translatedId) {
			return false;
		}
		if (recordId == null) {
			if (other.recordId != null) {
				return false;
			} else {
				inconsistentRecordIds(this, other);
			}
		} else if (!recordId.equals(other.recordId)) {
			return false;
		} else {
			inconsistentRecordIds(this, other);
		}
		if (recordSource != other.recordSource) {
			return false;
		} else {
			inconsistentRecordSources(this, other);
		}
		if (recordType != other.recordType) {
			return false;
		} else {
			inconsistentRecordTypes(this, other);
		}
		return true;
	}

	@Override
	public int compareTo(RecordIdTranslation<T> trid) {
		final int GREATER_THAN = 1;
		final int LESS_THAN = -1;
		final int EQUALS = 0;

		if (trid == null) {
			return GREATER_THAN;
		} else {

			// Job ids
			if (this.getJobId() < trid.getJobId())
				return LESS_THAN;
			else if (this.getJobId() > trid.getJobId())
				return GREATER_THAN;
			assert this.getJobId() == trid.getJobId();

			// Translated ids
			if (this.getTranslatedId() < trid.getTranslatedId())
				return LESS_THAN;
			else if (this.getTranslatedId() > trid.getTranslatedId())
				return GREATER_THAN;
			assert this.getTranslatedId() == trid.getTranslatedId();

			if (this.getRecordId() == null && trid.getRecordId() != null)
				return LESS_THAN;
			else if (this.getRecordId() != null && trid.getRecordId() == null)
				return LESS_THAN;
			else if (this.getRecordId() != null && trid.getRecordId() != null) {
				int retVal = this.getRecordId().compareTo(trid.getRecordId());
				if (retVal != 0) {
					inconsistentRecordIds(this, trid);
					return retVal;
				}
			}
			assert (this.getRecordId() == null && trid.getRecordId() == null)
					|| this.getRecordId().equals(trid.getRecordId());

			if (this.getRecordIdType() == null && trid.getRecordIdType() != null)
				return LESS_THAN;
			else if (this.getRecordIdType() != null
					&& trid.getRecordIdType() == null)
				return LESS_THAN;
			else if (this.getRecordIdType() != null
					&& trid.getRecordIdType() != null) {
				int retVal =
					this.getRecordIdType().compareTo(trid.getRecordIdType());
				if (retVal != 0) {
					inconsistentRecordTypes(this, trid);
					return retVal;
				}
			}
			assert (this.getRecordIdType() == null && trid.getRecordIdType() == null)
					|| this.getRecordIdType().equals(trid.getRecordIdType());

			if (this.getRecordSourceRole() == null
					&& trid.getRecordSourceRole() != null)
				return LESS_THAN;
			else if (this.getRecordSourceRole() != null
					&& trid.getRecordSourceRole() == null)
				return LESS_THAN;
			else if (this.getRecordSourceRole() != null
					&& trid.getRecordSourceRole() != null) {
				int retVal =
					this.getRecordSourceRole().compareTo(trid.getRecordSourceRole());
				if (retVal != 0) {
					inconsistentRecordSources(this, trid);
					return retVal;
				}
			}
			assert (this.getRecordSourceRole() == null && trid.getRecordSourceRole() == null)
					|| this.getRecordSourceRole().equals(trid.getRecordSourceRole());

		}

		assert (this.equals(trid));
		return EQUALS;
	}

	protected static void inconsistentRecordSources(
			AbstractRecordIdTranslationEntity<?> o1, RecordIdTranslation<?> o2) {
		assert o1 != null && o2 != null;
		String msg =
			"Inconsistent record sources: same job id (" + o1.getJobId()
					+ "), same translated id (" + o1.getTranslatedId()
					+ ") but different record sources (" + o1.getRecordId()
					+ ", " + o2.getRecordId() + ")";
		logger.warning(msg);
	}

	protected static void inconsistentRecordTypes(
			AbstractRecordIdTranslationEntity<?> o1, RecordIdTranslation<?> o2) {
		assert o1 != null && o2 != null;
		String msg =
			"Inconsistent record-id translations: same job id ("
					+ o1.getJobId() + "), same translated id ("
					+ o1.getTranslatedId() + ") but different record types ("
					+ o1.getRecordId() + ", " + o2.getRecordId() + ")";
		logger.warning(msg);
	}

	protected static void inconsistentRecordIds(RecordIdTranslation<?> o1,
			RecordIdTranslation<?> o2) {
		assert o1 != null && o2 != null;
		String msg =
			"Inconsistent record-id translations: same job id ("
					+ o1.getJobId() + "), same translated id ("
					+ o1.getTranslatedId() + ") but different record ids ("
					+ o1.getRecordId() + ", " + o2.getRecordId() + ")";
		logger.warning(msg);
	}

	@Override
	public String toString() {
		return "AbstractRecordIdTranslationEntity [jobId=" + jobId
				+ ", translatedId=" + translatedId + ", recordId=" + recordId
				+ "]";
	}

}
