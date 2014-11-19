package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA.CN_INTERVAL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA.CN_MAX_BLOCKSIZE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA.CN_MAX_CHUNKSIZE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA.CN_MAX_OVERSIZE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA.CN_MIN_FIELDS;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA.JPQL_OABA_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA.QN_OABA_FIND_ALL;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.io.blocking.automated.AbaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;

@NamedQuery(name = QN_OABA_FIND_ALL, query = JPQL_OABA_FIND_ALL)
@Entity
@DiscriminatorValue(value = DISCRIMINATOR_VALUE)
public class OabaSettingsBean extends AbaSettingsBean implements OabaSettings {

	private static final long serialVersionUID = 271L;

	// -- Instance data

	@Column(name = CN_MAX_BLOCKSIZE)
	private final int maxBlockSize;

	@Column(name = CN_MAX_CHUNKSIZE)
	private final int maxChunkSize;

	@Column(name = CN_MAX_OVERSIZE)
	private final int maxOversized;

	@Column(name = CN_MIN_FIELDS)
	private final int minFields;

	@Column(name = CN_INTERVAL)
	private final int interval;

	// -- Construction

	/**
	 * Constructs an instance with default limits:
	 * <ul>
	 * <li>{@link #DEFAULT_LIMIT_PER_BLOCKING_SET}</li>
	 * <li>{@link #DEFAULT_LIMIT_SINGLE_BLOCKING_SET}</li>
	 * <li>{@link #DEFAULT_SINGLE_TABLE_GRACE_LIMIT}</li>
	 * <li>{@link # DEFAULT_MAX_BLOCKSIZE = 100}</li>
	 * <li>{@link # DEFAULT_MAX_CHUNKSIZE = 100000}</li>
	 * <li>{@link # DEFAULT_MAX_OVERSIZED = 1000}</li>
	 * <li>{@link # DEFAULT_MIN_FIELDS = 3}</li>
	 * <li>{@link # DEFAULT_INTERVAL = 100}</li>
	 * </ul>
	 */
	public OabaSettingsBean() {
		this(DEFAULT_LIMIT_PER_BLOCKING_SET, DEFAULT_LIMIT_SINGLE_BLOCKING_SET,
				DEFAULT_SINGLE_TABLE_GRACE_LIMIT, DEFAULT_MAX_BLOCKSIZE,
				DEFAULT_MAX_CHUNKSIZE, DEFAULT_MAX_OVERSIZED,
				DEFAULT_MIN_FIELDS, DEFAULT_INTERVAL);
	}

	public OabaSettingsBean(AbaSettings aba) {
		this(aba.getLimitPerBlockingSet(), aba.getLimitSingleBlockingSet(), aba
				.getSingleTableBlockingSetGraceLimit(), DEFAULT_MAX_BLOCKSIZE,
				DEFAULT_MAX_CHUNKSIZE, DEFAULT_MAX_OVERSIZED,
				DEFAULT_MIN_FIELDS, DEFAULT_INTERVAL);
	}

	public OabaSettingsBean(AbaSettings aba, int maxBlockSize,
			int maxChunkSize, int maxOversized, int minFields, int interval) {
		this(aba.getLimitPerBlockingSet(), aba.getLimitSingleBlockingSet(), aba
				.getSingleTableBlockingSetGraceLimit(), maxBlockSize,
				maxChunkSize, maxOversized, minFields, interval);
	}

	public OabaSettingsBean(OabaSettings oaba) {
		this(oaba.getLimitPerBlockingSet(), oaba.getLimitSingleBlockingSet(),
				oaba.getSingleTableBlockingSetGraceLimit(), oaba
						.getMaxBlockSize(), oaba.getMaxChunkSize(), oaba
						.getMaxOversized(), oaba.getMinFields(), oaba
						.getInterval());
	}

	public OabaSettingsBean(int limPerBlockingSet, int limSingleBlockingSet,
			int singleTableGraceLimit, int maxBlockSize, int maxChunkSize,
			int maxOversized, int minFields, int interval) {
		this(limPerBlockingSet, limSingleBlockingSet, singleTableGraceLimit,
				maxBlockSize, maxChunkSize, maxOversized, minFields, interval,
				DISCRIMINATOR_VALUE);
	}

	protected OabaSettingsBean(int limPerBlockingSet, int limSingleBlockingSet,
			int singleTableGraceLimit, int maxBlockSize, int maxChunkSize,
			int maxOversized, int minFields, int interval, String type) {
		super(limPerBlockingSet, limSingleBlockingSet, singleTableGraceLimit,
				type);
		if (maxBlockSize < 0) {
			throw new IllegalArgumentException("invalid maxBlockSize"
					+ maxBlockSize);
		}
		if (maxChunkSize < 0) {
			throw new IllegalArgumentException("invalid maxChunkSize"
					+ maxChunkSize);
		}
		if (maxOversized < 0) {
			throw new IllegalArgumentException("invalid maxOversized: "
					+ maxOversized);
		}
		if (minFields < 0) {
			throw new IllegalArgumentException("invalid minFields: "
					+ minFields);
		}
		if (interval < 0) {
			throw new IllegalArgumentException("invalid interval: " + interval);
		}
		this.maxBlockSize = maxBlockSize;
		this.maxChunkSize = maxChunkSize;
		this.maxOversized = maxOversized;
		this.minFields = minFields;
		this.interval = interval;
	}

	// -- Accessors

	@Override
	public int getMaxBlockSize() {
		return maxBlockSize;
	}

	@Override
	public int getMaxChunkSize() {
		return maxChunkSize;
	}

	@Override
	public int getMaxOversized() {
		return maxOversized;
	}

	@Override
	public int getMinFields() {
		return minFields;
	}

	@Override
	public int getInterval() {
		return interval;
	}

	// -- Identity

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + interval;
		result = prime * result + maxBlockSize;
		result = prime * result + maxChunkSize;
		result = prime * result + maxOversized;
		result = prime * result + minFields;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OabaSettingsBean other = (OabaSettingsBean) obj;
		if (interval != other.interval) {
			return false;
		}
		if (maxBlockSize != other.maxBlockSize) {
			return false;
		}
		if (maxChunkSize != other.maxChunkSize) {
			return false;
		}
		if (maxOversized != other.maxOversized) {
			return false;
		}
		if (minFields != other.minFields) {
			return false;
		}
		return true;
	}

}
