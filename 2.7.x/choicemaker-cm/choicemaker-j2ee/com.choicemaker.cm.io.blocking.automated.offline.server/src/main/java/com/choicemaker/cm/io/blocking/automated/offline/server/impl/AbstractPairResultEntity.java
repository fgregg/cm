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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.core.Constants.*;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.CN_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.JPQL_PAIR_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.JPQL_PAIR_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.QN_PAIR_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.QN_PAIR_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractPairResultJPA.TABLE_NAME;

import java.util.SortedSet;
import java.util.TreeSet;

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
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaPairResult;
import com.choicemaker.util.HashUtils;

/**
 * This is the EJB implementation of the OABA OabaProcessing interface.
 * 
 * @author pcheung
 *
 */
@NamedQueries({
		@NamedQuery(name = QN_PAIR_FIND_ALL, query = JPQL_PAIR_FIND_ALL),
		@NamedQuery(name = QN_PAIR_FIND_BY_JOBID,
				query = JPQL_PAIR_FIND_BY_JOBID) })
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public abstract class AbstractPairResultEntity<T extends Comparable<T>>
		implements OabaPairResult<T> {

	private static final long serialVersionUID = 271L;

	public static final String INVALID_SHA1 = null;

	public static String notesToString(String[] notes) {
		String retVal = null;
		if (notes != null) {
			SortedSet<String> sorted = notesToSortedSet(notes);
			retVal = notesToString(sorted);
		}
		return retVal;
	}

	protected static SortedSet<String> notesToSortedSet(String[] notes) {
		SortedSet<String> retVal = new TreeSet<>();
		if (notes != null) {
			for (String note : notes) {
				if (note == null) {
					continue;
				}
				note = note.trim();
				if (note.isEmpty()) {
					continue;
				}
				retVal.add(note);
			}
		}
		return retVal;
	}

	protected static String notesToString(SortedSet<String> sorted) {
		String retVal = null;
		if (sorted != null && sorted.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String note : sorted) {
				assert note != null;
				assert !note.isEmpty();
				sb.append(note);
				sb.append(EXPORT_NOTE_SEPARATOR);
			}
			retVal = sb.toString();
			final int lastIndex = retVal.length() - 1;
			assert retVal.charAt(lastIndex) == EXPORT_NOTE_SEPARATOR;
			retVal = retVal.substring(0, lastIndex);
			if (retVal.length() == 0) {
				retVal = null;
			} else {
				assert retVal.charAt(retVal.length() - 1) != EXPORT_NOTE_SEPARATOR;
			}
		} else {
			assert retVal == null;
		}
		return retVal;
	}

	public static String[] notesFromString(String s) {
		String[] retVal = null;
		if (s != null) {
			s = s.trim();
			if (!s.isEmpty()) {
				String regex = "\\\\" + EXPORT_NOTE_SEPARATOR;
				String[] raw = s.split(regex);
				SortedSet<String> sorted = notesToSortedSet(raw);
				if (sorted.size() > 0) {
					retVal = sorted.toArray(new String[sorted.size()]);
				} else {
					assert retVal == null;
				}
			} else {
				assert retVal == null;
			}
		}
		return retVal;
	}

	/**
	 * Computes a base-64 SHA1 signature for a pair based on all fields
	 * excluding the persistence id, jobId, and pair and equivalence class SHA1
	 * signatures. The signature depends on the field ordering; in this method,
	 * the ordering is:
	 * <ol>
	 * <li>the fully qualified class name of the record id
	 * {@link #getRecordIdType() type}</li>
	 * <li>the {@link #getRecord1Id() first record id}
	 * {@link #idToString(Comparable) as a String}</li>
	 * <li>the {@link #getRecord2Id() second record id}
	 * {@link #idToString(Comparable) as a String}</li>
	 * <li>the {@link #getRecord2Source() source} (as a String) of the second
	 * record</li>
	 * <li>the pair probability</li>
	 * <li>the pair decision</li>
	 * <li>an ordered set formed from the pair {@link #getNotes() notes}</li>
	 * <li>
	 * </ol>
	 * 
	 * @param pair
	 * @return
	 */
	protected static <T extends Comparable<T>> String computePairSHA1(
			String recordType, String record1Id, String record2Id,
			char record2Source, float probability, char decision,
			SortedSet<String> notes) {

		StringBuilder sb = new StringBuilder();
		sb.append(recordType);
		sb.append(record1Id);
		sb.append(record2Id);
		sb.append(record2Source);
		sb.append(probability);
		sb.append(decision);

		for (String note : notes) {
			sb.append(note);
		}

		String retVal = HashUtils.toBase64SHA1Hash(sb.toString(), false);
		return retVal;
	}

	/**
	 * Computes a base-64 SHA1 signature for an array of pairs. This method
	 * converts the array to a sorted set and then concatenates the SHA1
	 * signature of each pair into a single String, from which a base-64 SHA1
	 * signature return value is computed and then returned.
	 */
	public static <T extends Comparable<T>> String computeEquivalenceClassSHA1(
			OabaPairResult<T>[] pairs) {
		if (pairs == null) {
			throw new IllegalArgumentException("null pairs");
		}
		SortedSet<OabaPairResult<T>> sortedPairs =
			new TreeSet<>(new OabaPairResultComparator<T>());
		for (OabaPairResult<T> pair : pairs) {
			if (pair == null) {
				throw new IllegalArgumentException("null pair");
			}
			sortedPairs.add(pair);
		}
		StringBuilder sb = new StringBuilder();
		for (OabaPairResult<T> pair : sortedPairs) {
			if (pair == null) {
				throw new IllegalArgumentException("null pair");
			}
			sb.append(pair.getPairSHA1());
		}
		String retVal = HashUtils.toBase64SHA1Hash(sb.toString(), false);
		return retVal;
	}

	public static Class<?> recordTypeFromString(
			String s) {
		RECORD_ID_TYPE rit = RECORD_ID_TYPE.fromSymbol(s);
		return rit.getClass();
	}

	public static <T extends Comparable<T>> String recordTypeToString(Class<T> c) {
		RECORD_ID_TYPE rit = RECORD_ID_TYPE.fromClass(c);
		return rit.getStringSymbol();
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

	private final long jobId;
	private final String recordType;
	private final String record1Id;
	private final String record2Id;
	private final char record2Source;
	private final float probability;
	private final char decision;
	private final String notes;
	private final String pairSHA1;
	private final String ecSHA1;

	// -- Construction

	/** Required by JPA; do not invoke directly */
	protected AbstractPairResultEntity() {
		this.jobId = BatchJob.INVALID_ID;
		this.recordType = null;
		this.record1Id = null;
		this.record2Id = null;
		this.record2Source = 0;
		this.probability = Float.NaN;
		this.decision = 0;
		this.notes = null;
		this.pairSHA1 = INVALID_SHA1;
		this.ecSHA1 = INVALID_SHA1;
	}

	protected AbstractPairResultEntity(long jobId, String recordType,
			String record1Id, String record2Id, char record2Source,
			float probability, char decision, String[] notes, String ecSHA1) {
		this.jobId = jobId;
		this.recordType = recordType;
		this.record1Id = record1Id;
		this.record2Id = record2Id;
		this.record2Source = record2Source;
		this.probability = probability;
		this.decision = decision;
		SortedSet<String> sortedNotes = notesToSortedSet(notes);
		this.notes = notesToString(sortedNotes);
		this.pairSHA1 =
			computePairSHA1(recordType, record1Id, record2Id, record2Source,
					probability, decision, sortedNotes);
		this.ecSHA1 = ecSHA1;
	}

	// -- Abstract call-back methods

	protected abstract T idFromString(String s);

	protected abstract String idToString(T id);

	// -- Template methods

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getJobId() {
		return jobId;
	}

	@Override
	public Class<T> getRecordIdType() {
		@SuppressWarnings("unused")
		Class<?> retVal = recordTypeFromString(recordType);
		throw new Error("not yet implemented");
	}

	@Override
	public T getRecord1Id() {
		return idFromString(record1Id);
	}

	@Override
	public T getRecord2Id() {
		return idFromString(record2Id);
	}

	@Override
	public RECORD_SOURCE_ROLE getRecord2Source() {
		return RECORD_SOURCE_ROLE.fromSymbol(record2Source);
	}

	@Override
	public float getProbability() {
		return probability;
	}

	@Override
	public Decision getDecision() {
		return Decision.valueOf(decision);
	}

	@Override
	public String[] getNotes() {
		return notesFromString(notes);
	}

	@Override
	public String getPairSHA1() {
		return pairSHA1;
	}

	// Override is not declared here -- see TransitivityPairResultEntity
	public String getEquivalenceClassSHA1() {
		return ecSHA1;
	}

	@Override
	public String exportToString() {
		throw new Error("not yet implemented");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + decision;
		result = prime * result + ((ecSHA1 == null) ? 0 : ecSHA1.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (jobId ^ (jobId >>> 32));
		result = prime * result + ((notes == null) ? 0 : notes.hashCode());
		result =
			prime * result + ((pairSHA1 == null) ? 0 : pairSHA1.hashCode());
		result = prime * result + Float.floatToIntBits(probability);
		result =
			prime * result + ((record1Id == null) ? 0 : record1Id.hashCode());
		result =
			prime * result + ((record2Id == null) ? 0 : record2Id.hashCode());
		result = prime * result + record2Source;
		result =
			prime * result + ((recordType == null) ? 0 : recordType.hashCode());
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
		AbstractPairResultEntity other = (AbstractPairResultEntity) obj;
		if (decision != other.decision) {
			return false;
		}
		if (ecSHA1 == null) {
			if (other.ecSHA1 != null) {
				return false;
			}
		} else if (!ecSHA1.equals(other.ecSHA1)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (jobId != other.jobId) {
			return false;
		}
		if (notes == null) {
			if (other.notes != null) {
				return false;
			}
		} else if (!notes.equals(other.notes)) {
			return false;
		}
		if (pairSHA1 == null) {
			if (other.pairSHA1 != null) {
				return false;
			}
		} else if (!pairSHA1.equals(other.pairSHA1)) {
			return false;
		}
		if (Float.floatToIntBits(probability) != Float
				.floatToIntBits(other.probability)) {
			return false;
		}
		if (record1Id == null) {
			if (other.record1Id != null) {
				return false;
			}
		} else if (!record1Id.equals(other.record1Id)) {
			return false;
		}
		if (record2Id == null) {
			if (other.record2Id != null) {
				return false;
			}
		} else if (!record2Id.equals(other.record2Id)) {
			return false;
		}
		if (record2Source != other.record2Source) {
			return false;
		}
		if (recordType == null) {
			if (other.recordType != null) {
				return false;
			}
		} else if (!recordType.equals(other.recordType)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AbstractPairResultEntity [record1Id=" + record1Id
				+ ", record2Id=" + record2Id + ", probability=" + probability
				+ ", decision=" + decision + "]";
	}

}
