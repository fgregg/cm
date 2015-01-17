package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.Comparator;

import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecordUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaPairResult;

public class OabaPairResultComparator<T extends Comparable<T>> implements
		Comparator<OabaPairResult<T>> {

	/**
	 * Orders pairs by:
	 * <ol>
	 * <li>record 1 id</li>
	 * <li>record 2 id</li>
	 * <li>probability</li>
	 * <li>decision</li>
	 * <li>notes</li>
	 * <li>record 2 source</li>
	 * <li>record type</li>
	 * <li>record pair SHA1</li>
	 * <li>job id</li>
	 * <li>equivalence class SHA1 (in the case of AbstractPairResultEntity)</li>
	 * <li>persistence id</li>
	 * </ol>
	 */
	@Override
	public int compare(OabaPairResult<T> o1, OabaPairResult<T> o2) {
		final int GREATER_THAN = 1;
		final int LESS_THAN = -1;
		final int EQUALS = 0;

		int retVal = EQUALS;
		if (o1 == null && o2 != null) {
			retVal = LESS_THAN;
		}
		if (o1 != null && o2 == null) {
			retVal = GREATER_THAN;
		}
		if (o1 != null && o2 != null) {
			assert retVal == EQUALS;

			assert o1.getRecord1Id() != null && o2.getRecord1Id() != null;
			assert o1.getRecord2Id() != null && o2.getRecord2Id() != null;
			assert o1.getDecision() != null && o2.getDecision() != null;

			retVal = o1.getRecord1Id().compareTo(o2.getRecord1Id());
			if (retVal == EQUALS)
				retVal = o1.getRecord2Id().compareTo(o2.getRecord2Id());
			if (retVal == EQUALS)
				retVal =
					Float.compare(o1.getProbability(), o2.getProbability());
			if (retVal == EQUALS)
				retVal = o1.getDecision().compareTo(o2.getDecision());
			if (retVal == EQUALS) {
				String notes1 = MatchRecordUtils.notesToString(o1.getNotes());
				String notes2 = MatchRecordUtils.notesToString(o2.getNotes());
				if (notes1 == null && notes2 != null) {
					retVal = LESS_THAN;
				}
				if (notes1 != null && notes2 == null) {
					retVal = GREATER_THAN;
				}
				if (notes1 != null && notes2 != null) {
					retVal = notes1.compareTo(notes2);
				}
			}
			if (retVal == EQUALS) {
				RECORD_ID_TYPE rit1 = o1.getRecordIdType();
				RECORD_ID_TYPE rit2 = o2.getRecordIdType();
				if (rit1 == null && rit2 != null) {
					retVal = LESS_THAN;
				}
				if (rit1 != null && rit2 == null) {
					retVal = GREATER_THAN;
				}
				if (rit1 != null && rit2 != null) {
					retVal = rit1.compareTo(rit2);
				}
			}
			if (retVal == EQUALS) {
				assert o1.getPairSHA1() != null && o2.getPairSHA1() != null;
				assert o1.getPairSHA1().compareTo(o2.getPairSHA1()) == EQUALS;
				retVal = Long.compare(o1.getJobId(), o2.getJobId());
			}
			if (retVal == EQUALS && (o1 instanceof AbstractPairResultEntity)
					&& (o1 instanceof AbstractPairResultEntity)) {
				AbstractPairResultEntity<T> a1 =
					(AbstractPairResultEntity<T>) o1;
				AbstractPairResultEntity<T> a2 =
					(AbstractPairResultEntity<T>) o2;
				String ecSig1 = a1.getEquivalenceClassSHA1();
				String ecSig2 = a2.getEquivalenceClassSHA1();
				if (ecSig1 == null && ecSig2 != null) {
					retVal = LESS_THAN;
				}
				if (ecSig1 != null && ecSig2 == null) {
					retVal = GREATER_THAN;
				}
				if (ecSig1 != null && ecSig2 != null) {
					retVal = ecSig1.compareTo(ecSig2);
				}
			}
			if (retVal == EQUALS)
				retVal = Long.compare(o1.getId(), o2.getId());
		}
		return retVal;
	}

}
