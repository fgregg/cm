package com.choicemaker.cm.batch.impl;

import static com.choicemaker.cm.batch.impl.BatchJobJPA.AUDIT_TABLE_NAME;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_AUDIT_JOIN;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_BPARENT_ID;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_DESCRIPTION;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_EXTERNAL_ID;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_ID;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_PARAMS_ID;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_RIGOR;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_SERVER_ID;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_SETTINGS_ID;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_STATUS;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_TIMESTAMP;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_TRANSACTION_ID;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_TYPE;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.CN_WORKING_DIRECTORY;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.TABLE_NAME;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyTemporal;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.TemporalType;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobRigor;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;

@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public abstract class BatchJobEntity implements BatchJob {

	private static final long serialVersionUID = 271L;

	private static Logger log = Logger.getLogger(OabaJobEntity.class.getName());

	private static Map<BatchJobStatus, Set<BatchJobStatus>> allowedTransitions =
		new HashMap<>();
	static {
		// FIXME use EnumSet
		Set<BatchJobStatus> allowed = new HashSet<>();
		allowed.add(BatchJobStatus.QUEUED);
		allowed.add(BatchJobStatus.ABORT_REQUESTED);
		allowed.add(BatchJobStatus.ABORTED);
		allowedTransitions.put(BatchJobStatus.NEW, allowed);
		allowed = new HashSet<>();
		allowed.add(BatchJobStatus.QUEUED);
		allowed.add(BatchJobStatus.ABORT_REQUESTED);
		allowed.add(BatchJobStatus.ABORTED);
		allowedTransitions.put(BatchJobStatus.NEW, allowed);
		allowed = new HashSet<>();
		allowed.add(BatchJobStatus.STARTED);
		allowed.add(BatchJobStatus.ABORT_REQUESTED);
		allowed.add(BatchJobStatus.ABORTED);
		allowedTransitions.put(BatchJobStatus.QUEUED, allowed);
		allowed = new HashSet<>();
		allowed.add(BatchJobStatus.STARTED);
		allowed.add(BatchJobStatus.COMPLETED);
		allowed.add(BatchJobStatus.FAILED);
		allowed.add(BatchJobStatus.ABORT_REQUESTED);
		allowed.add(BatchJobStatus.ABORTED);
		allowedTransitions.put(BatchJobStatus.STARTED, allowed);
		allowed = new HashSet<>();
		allowed.add(BatchJobStatus.ABORTED);
		allowedTransitions.put(BatchJobStatus.ABORT_REQUESTED, allowed);
		// Terminal transitions (unless re-queued/re-started)
		allowed = new HashSet<>();
		allowedTransitions.put(BatchJobStatus.COMPLETED, allowed);
		allowed = new HashSet<>();
		allowedTransitions.put(BatchJobStatus.FAILED, allowed);
		allowed = new HashSet<>();
		allowedTransitions.put(BatchJobStatus.ABORTED, allowed);
	}

	public static boolean isInvalidBatchJobId(long id) {
		return id == INVALID_ID;
	}

	public static boolean isPersistent(BatchJob oabaJob) {
		boolean retVal = false;
		if (oabaJob != null) {
			retVal = !isInvalidBatchJobId(oabaJob.getId());
		}
		return retVal;
	}

	public static boolean isTopLevelJob(BatchJob oabaJob) {
		boolean retVal = true;
		if (oabaJob != null) {
			retVal = isInvalidBatchJobId(oabaJob.getBatchParentId());
		}
		return retVal;
	}

	public static long randomTransactionId() {
		String uuid = UUID.randomUUID().toString();
		return uuid.hashCode();
	}

	@Id
	@Column(name = CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	protected long id;

	@Column(name = CN_TYPE)
	protected final String type;

	/**
	 * {@link OabaJob#INVALID_BATCHJOB_ID} or references the id of some other
	 * OabaJobEntity
	 */
	@Column(name = CN_BPARENT_ID)
	protected final long bparentId;

	/**
	 * References the persistent id of some parameters bean instance
	 */
	@Column(name = CN_PARAMS_ID)
	protected final long paramsId;

	/**
	 * References the persistent id of some settings bean instance
	 */
	@Column(name = CN_SETTINGS_ID)
	protected final long settingsId;

	/**
	 * References the persistent id of some server configuration
	 */
	@Column(name = CN_SERVER_ID)
	protected final long serverId;

	/**
	 * {@link OabaJob#INVALID_BATCHJOB_ID} or references the id of some
	 * UrmJobBean
	 */
	@Column(name = BatchJobJPA.CN_URM_ID)
	protected final long urmId;

	@Column(name = CN_TRANSACTION_ID)
	protected final long transactionId;

	@Column(name = CN_EXTERNAL_ID)
	protected final String externalId;

	@Column(name = CN_WORKING_DIRECTORY)
	protected String workingDirectory;

	@Column(name = CN_RIGOR)
	protected char rigor;

	@Column(name = CN_DESCRIPTION)
	protected String description;

	@Column(name = CN_STATUS)
	protected String status;

	@ElementCollection
	@MapKeyColumn(name = CN_TIMESTAMP)
	@MapKeyTemporal(TemporalType.TIMESTAMP)
	@Column(name = CN_STATUS)
	@CollectionTable(name = AUDIT_TABLE_NAME, joinColumns = @JoinColumn(
			name = CN_AUDIT_JOIN))
	protected Map<Date, String> audit = new HashMap<>();

	private static final String[] _nonterminal = new String[] {
			BatchJobStatus.NEW.name(), BatchJobStatus.QUEUED.name(),
			BatchJobStatus.STARTED.name(),
			BatchJobStatus.ABORT_REQUESTED.name() };

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getBatchParentId() {
		return this.bparentId;
	}

	// Override is not declared here
	public long getParametersId() {
		return paramsId;
	}

	// Override is not declared here
	public long getSettingsId() {
		return settingsId;
	}

	// Override is not declared here
	public long getServerId() {
		return serverId;
	}

	@Override
	public long getUrmId() {
		return this.urmId;
	}

	@Override
	public long getTransactionId() {
		return transactionId;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public BatchJobRigor getBatchJobRigor() {
		return BatchJobRigor.valueOf(this.rigor);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public File getWorkingDirectory() {
		File retVal = null;
		if (workingDirectory != null) {
			retVal = new File(workingDirectory);
		}
		return retVal;
	}

	@Override
	public BatchJobStatus getStatus() {
		return BatchJobStatus.valueOf(status);
	}

	@Override
	public Date getTimeStamp(BatchJobStatus status) {
		return this.mostRecentTimestamp(status);
	}

	/** Backwards compatibility */
	protected Date mostRecentTimestamp(BatchJobStatus status) {
		// This could be replaced with a named, parameterized query
		Date retVal = null;
		if (status != null) {
			for (Map.Entry<Date, String> e : audit.entrySet()) {
				if (status.name().equals(e.getValue())) {
					if (retVal == null || retVal.compareTo(e.getKey()) < 0) {
						retVal = e.getKey();
					}
				}
			}
		}
		return retVal;
	}

	@Override
	public Date getRequested() {
		return mostRecentTimestamp(BatchJobStatus.NEW);
	}

	@Override
	public Date getQueued() {
		return mostRecentTimestamp(BatchJobStatus.QUEUED);
	}

	@Override
	public Date getStarted() {
		return mostRecentTimestamp(BatchJobStatus.STARTED);
	}

	@Override
	public Date getCompleted() {
		return mostRecentTimestamp(BatchJobStatus.COMPLETED);
	}

	@Override
	public Date getFailed() {
		return mostRecentTimestamp(BatchJobStatus.FAILED);
	}

	@Override
	public Date getAbortRequested() {
		return mostRecentTimestamp(BatchJobStatus.ABORT_REQUESTED);
	}

	@Override
	public Date getAborted() {
		return mostRecentTimestamp(BatchJobStatus.ABORTED);
	}

	public boolean shouldStop() {
		if (getStatus().equals(BatchJobStatus.ABORT_REQUESTED)
				|| getStatus().equals(BatchJobStatus.ABORTED))
			return true;
		else
			return false;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	protected void logTransition(BatchJobStatus newStatus) {
		String msg =
			getId() + ", '" + getExternalId() + "': transitioning from "
					+ getStatus() + " to " + newStatus;
		log.warning(msg);
	}

	protected void logIgnoredTransition(String transition) {
		String msg =
			getId() + ", '" + getExternalId() + "': " + transition
					+ " ignored (status == '" + getStatus() + "'";
		log.warning(msg);
	}

	/** For testing only; use markAsXxx() methods instead */
	public void setStatus(BatchJobStatus newStatus) {
		if (newStatus == null) {
			throw new IllegalArgumentException("null status");
		}
		this.status = newStatus.name();
		setTimeStamp(this.status, new Date());
	}

	protected void setTimeStamp(String status, Date date) {
		this.audit.put(date, status);
	}

	public static boolean isAllowedTransition(BatchJobStatus current, BatchJobStatus next) {
		if (current == null || next == null) {
			throw new IllegalArgumentException("null status");
		}
		Set<BatchJobStatus> allowed = allowedTransitions.get(current);
		assert allowed != null;
		boolean retVal = allowed.contains(next);
		return retVal;
	}

	protected BatchJobEntity() {
		this(DISCRIMINATOR_VALUE, INVALID_ID, INVALID_ID, INVALID_ID, null,
				randomTransactionId(), INVALID_ID, INVALID_ID, DEFAULT_RIGOR);
	}

	/**
	 * Constructs an invalid BatchJobEntity with a null working directory.
	 * Subclasses must implement a method to set the working directory to a
	 * valid value after construction.
	 */
	protected BatchJobEntity(String type, long paramsid, long settingsId,
			long serverId, String externalId, long tid, long bpid, long urmid,
			BatchJobRigor bjr) {
		if (type == null) {
			throw new IllegalArgumentException("null type");
		}
		type = type.trim();
		if (type.isEmpty()) {
			throw new IllegalArgumentException("blank type");
		}
		if (bjr == null) {
			throw new IllegalArgumentException("null batch-job rigor");
		}
		this.type = type;
		this.paramsId = paramsid;
		this.settingsId = settingsId;
		this.serverId = serverId;
		this.transactionId = tid;
		this.externalId = externalId;
		this.bparentId = bpid;
		this.urmId = urmid;
		this.rigor = bjr.symbol;
		setStatus(BatchJobStatus.NEW);
	}

	@Override
	public void markAsQueued() {
		if (isAllowedTransition(getStatus(), BatchJobStatus.QUEUED)) {
			logTransition(BatchJobStatus.QUEUED);
			setStatus(BatchJobStatus.QUEUED);
		} else {
			logIgnoredTransition("markAsQueued");
		}
	}

	@Override
	public void markAsStarted() {
		if (isAllowedTransition(getStatus(), BatchJobStatus.STARTED)) {
			logTransition(BatchJobStatus.QUEUED);
			setStatus(BatchJobStatus.STARTED);
		} else {
			logIgnoredTransition("markAsStarted");
		}
	}

	@Override
	public void markAsReStarted() {
		setStatus(BatchJobStatus.QUEUED);
	}

	@Override
	public void markAsCompleted() {
		if (isAllowedTransition(getStatus(), BatchJobStatus.COMPLETED)) {
			logTransition(BatchJobStatus.COMPLETED);
			setStatus(BatchJobStatus.COMPLETED);
		} else {
			logIgnoredTransition("markAsCompleted");
		}
	}

	@Override
	public void markAsFailed() {
		if (isAllowedTransition(getStatus(), BatchJobStatus.FAILED)) {
			logTransition(BatchJobStatus.FAILED);
			setStatus(BatchJobStatus.FAILED);
		} else {
			logIgnoredTransition("markAsFailed");
		}
	}

	@Override
	public void markAsAbortRequested() {
		if (isAllowedTransition(getStatus(), BatchJobStatus.ABORT_REQUESTED)) {
			logTransition(BatchJobStatus.ABORT_REQUESTED);
			setStatus(BatchJobStatus.ABORT_REQUESTED);
		} else {
			logIgnoredTransition("markAsAbortRequested");
		}
	}

	@Override
	public void markAsAborted() {
		if (isAllowedTransition(getStatus(), BatchJobStatus.ABORTED)) {
			if (!BatchJobStatus.ABORT_REQUESTED.equals(getStatus())) {
				markAsAbortRequested();
			}
			logTransition(BatchJobStatus.ABORTED);
			setStatus(BatchJobStatus.ABORTED);
		} else {
			logIgnoredTransition("markAsAborted");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (isInvalidBatchJobId(id)) {
			result = hashCode0();
		} else {
			result = prime * result + (int) (id ^ (id >>> 32));
		}
		return result;
	}

	/**
	 * Hashcode for instances with id == 0 (non-persistent/invalid id)
	 */
	protected int hashCode0() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result + ((externalId == null) ? 0 : externalId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + (int) (bparentId ^ (bparentId >>> 32));
		result =
			prime * result + (int) (transactionId ^ (transactionId >>> 32));
		result = prime * result + DISCRIMINATOR_VALUE.hashCode();
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
		// Implicitly checks Discriminator type
		if (getClass() != obj.getClass()) {
			return false;
		}
		BatchJobEntity other = (BatchJobEntity) obj;
		if (id != other.id) {
			return false;
		}
		if (isInvalidBatchJobId(id)) {
			return equals0(other);
		}
		return true;
	}

	/**
	 * Equality test for instances with id == 0 (non-persistent/invalid id)
	 */
	protected boolean equals0(BatchJobEntity other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (externalId == null) {
			if (other.externalId != null) {
				return false;
			}
		} else if (!externalId.equals(other.externalId)) {
			return false;
		}
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		} else if (!status.equals(other.status)) {
			return false;
		}
		if (bparentId != other.bparentId) {
			return false;
		}
		if (transactionId != other.transactionId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BatchJobEntity [" + id + "/" + externalId + "/" + status + "]";
	}

	public static String[] getStatusValues() {
		List<String> ls = new LinkedList<>();
		for (BatchJobStatus sts : BatchJobStatus.values()) {
			ls.add(sts.name());
		}
		String[] retVal = ls.toArray(new String[ls.size()]);
		return retVal;
	}

	public static String[] getNonTerminalStatusValues() {
		String[] retVal = new String[_nonterminal.length];
		System.arraycopy(_nonterminal, 0, retVal, 0, retVal.length);
		return retVal;
	}

}
