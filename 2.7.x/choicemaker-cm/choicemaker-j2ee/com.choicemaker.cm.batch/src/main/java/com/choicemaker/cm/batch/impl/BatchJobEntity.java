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

@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public abstract class BatchJobEntity extends AbstractPersistentObject implements
		BatchJob {

	private static final long serialVersionUID = 271L;

	private static Logger log = Logger.getLogger(BatchJobEntity.class.getName());

	private static Map<BatchJobStatus, Set<BatchJobStatus>> allowedTransitions =
		new HashMap<>();
	static {
		// FIXME use EnumSet
		Set<BatchJobStatus> allowed = new HashSet<>();
		allowed = new HashSet<>();
		allowed.add(BatchJobStatus.QUEUED);
		allowed.add(BatchJobStatus.PROCESSING);
		allowed.add(BatchJobStatus.ABORT_REQUESTED);
		allowed.add(BatchJobStatus.ABORTED);
		allowedTransitions.put(BatchJobStatus.NEW, allowed);
		allowed = new HashSet<>();
		allowed.add(BatchJobStatus.PROCESSING);
		allowed.add(BatchJobStatus.ABORT_REQUESTED);
		allowed.add(BatchJobStatus.ABORTED);
		allowedTransitions.put(BatchJobStatus.QUEUED, allowed);
		allowed = new HashSet<>();
		allowed.add(BatchJobStatus.PROCESSING);
		allowed.add(BatchJobStatus.COMPLETED);
		allowed.add(BatchJobStatus.FAILED);
		allowed.add(BatchJobStatus.ABORT_REQUESTED);
		allowed.add(BatchJobStatus.ABORTED);
		allowedTransitions.put(BatchJobStatus.PROCESSING, allowed);
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

	public static boolean isTopLevelJob(BatchJob oabaJob) {
		boolean retVal = true;
		if (oabaJob != null) {
			retVal = !isPersistentId(oabaJob.getBatchParentId());
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
	 * {@link BatchJob#INVALID_BATCHJOB_ID} or references the id of some other
	 * BatchJobEntity
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
	 * {@link BatchJob#INVALID_BATCHJOB_ID} or references the id of some
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
			BatchJobStatus.PROCESSING.name(),
			BatchJobStatus.ABORT_REQUESTED.name() };

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getBatchParentId() {
		return this.bparentId;
	}

	@Override
	public long getParametersId() {
		return paramsId;
	}

	@Override
	public long getSettingsId() {
		return settingsId;
	}

	@Override
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
		return mostRecentTimestamp(BatchJobStatus.PROCESSING);
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

	@Override
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
		if (newStatus != null && newStatus.equals(getStatus())) {
			log.warning("UNNECESSARY TRANSITION: " + msg);
		} else {
			log.info(msg);
		}
	}

	protected void logIgnoredTransition(String transition) {
		String msg =
			getId() + ", '" + getExternalId() + "': " + transition
					+ " ignored (status == '" + getStatus() + "')";
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

	public static boolean isAllowedTransition(BatchJobStatus current,
			BatchJobStatus next) {
		if (current == null || next == null) {
			throw new IllegalArgumentException("null status");
		}
		Set<BatchJobStatus> allowed = allowedTransitions.get(current);
		assert allowed != null;
		boolean retVal = allowed.contains(next);
		return retVal;
	}

	protected BatchJobEntity() {
		this(DISCRIMINATOR_VALUE, NONPERSISTENT_ID, NONPERSISTENT_ID,
				NONPERSISTENT_ID, null, randomTransactionId(),
				NONPERSISTENT_ID, NONPERSISTENT_ID, DEFAULT_RIGOR);
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
		if (isAllowedTransition(getStatus(), BatchJobStatus.PROCESSING)) {
			logTransition(BatchJobStatus.QUEUED);
			setStatus(BatchJobStatus.PROCESSING);
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
	public String toString() {
		return "BatchJob [" + id + "/" + getUUID() + "/" + externalId + "/" + status + "]";
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
