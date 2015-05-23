package com.choicemaker.cm.io.blocking.automated.offline.core;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.Test;

public class ComparisonArrayOSTest {

	public static class SyntheticRecordIds implements Comparable<SyntheticRecordIds> {
		public static final RECORD_ID_TYPE ID_TYPE = RECORD_ID_TYPE.TYPE_STRING;
		private final List<String> queryIds;
		private final List<String> referenceIds;

		public SyntheticRecordIds(int countQueryIds, int countReferenceIds) {
			assert countQueryIds >= 0;
			assert countReferenceIds >= 0;

			List<String> ids;
			if (countQueryIds <= 0) {
				ids = Collections.emptyList();
			} else {
				ids = new ArrayList<>(countQueryIds);
				for (int i = 0; i < countQueryIds; i++) {
					String id = "Q_" + UUID.randomUUID().toString();
					ids.add(id);
				}
			}
			queryIds = Collections.unmodifiableList(ids);

			if (countReferenceIds <= 0) {
				ids = Collections.emptyList();
			} else {
				ids = new ArrayList<>(countReferenceIds);
				for (int i = 0; i < countReferenceIds; i++) {
					String id = "R_" + UUID.randomUUID().toString();
					ids.add(id);
				}
			}
			referenceIds = Collections.unmodifiableList(ids);
		}

		public List<String> getQueryIds() {
			return queryIds;
		}

		public List<String> getReferenceIds() {
			return referenceIds;
		}

		/** Only the sizes of the synthetic id lists matter in this test */
		@Override
		public boolean equals(Object o) {
			boolean retVal = false;
			if (this == o) {
				retVal = true;
			} else if (o == null) {
				assert retVal == false;
			} else if (getClass() != o.getClass()) {
				assert retVal == false;
			} else if (o instanceof SyntheticRecordIds) {
				SyntheticRecordIds that = (SyntheticRecordIds) o;
				int thisQuerySize = this.getQueryIds().size();
				int thatQuerySize = that.getQueryIds().size();
				if (thisQuerySize == thatQuerySize) {
					int thisReferenceSize = this.getReferenceIds().size();
					int thatReferenceSize = that.getReferenceIds().size();
					retVal = thisReferenceSize == thatReferenceSize;
				}
			}
			return retVal;
		}

		/** Only the sizes of the synthetic id lists matter in this test */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result =
				prime * result + ((queryIds == null) ? 0 : queryIds.size());
			result =
				prime * result
						+ ((referenceIds == null) ? 0 : referenceIds.size());
			return result;
		}

		/** Only the sizes of the synthetic id lists matter in this test */
		@Override
		public int compareTo(SyntheticRecordIds that) {
			int retVal = -1;
			if (that != null) {
				Integer thisQuerySize = this.getQueryIds().size();
				Integer thatQuerySize = that.getQueryIds().size();
				retVal = thisQuerySize.compareTo(thatQuerySize);
				if (retVal == 0) {
					Integer thisReferenceSize = this.getReferenceIds().size();
					Integer thatReferenceSize = that.getReferenceIds().size();
					retVal = thisReferenceSize.compareTo(thatReferenceSize);
				}
			}
			return retVal;
		}

		@Override
		public String toString() {
			return "SyntheticRecordIds [queryIdCount=" + queryIds.size()
					+ ", referenceIdCount=" + referenceIds.size() + "]";
		}

	}

	public static Set<Integer> boundaryValuesAt(final int n0) {
		Set<Integer> retVal = new LinkedHashSet<>();
		for (int i=-1; i<=1; i++) {
			int n = n0 + i;
			if (n >= 0) {
				retVal.add(n);
			}
		}
		return retVal;
	}

	public static Set<SyntheticRecordIds> generate(final int bQ, final int bR) {
		Set<SyntheticRecordIds> retVal = new LinkedHashSet<>();
		Set<Integer> queryCounts = boundaryValuesAt(bQ);
		Set<Integer> referenceCounts = boundaryValuesAt(bR);
		for (int q : queryCounts) {
			for (int r : referenceCounts) {
				SyntheticRecordIds srids = new SyntheticRecordIds(q,r);
				retVal.add(srids);
			}
		}
		return retVal;
	}

	public static Set<SyntheticRecordIds> generate(int maxBlockSize) {
		SortedSet<SyntheticRecordIds> retVal = new TreeSet<>();

		Set<SyntheticRecordIds> setSynRids = generate(1,1);
		retVal.addAll(setSynRids);

		setSynRids = generate(maxBlockSize/2,1);
		retVal.addAll(setSynRids);
		setSynRids = generate(maxBlockSize/2,maxBlockSize/2);
		retVal.addAll(setSynRids);
		setSynRids = generate(maxBlockSize/2,maxBlockSize);
		retVal.addAll(setSynRids);
		setSynRids = generate(maxBlockSize/2,2*maxBlockSize);
		retVal.addAll(setSynRids);

		setSynRids = generate(maxBlockSize,1);
		retVal.addAll(setSynRids);
		setSynRids = generate(maxBlockSize,maxBlockSize/2);
		retVal.addAll(setSynRids);
		setSynRids = generate(maxBlockSize,maxBlockSize);
		retVal.addAll(setSynRids);
		setSynRids = generate(maxBlockSize,2*maxBlockSize);
		retVal.addAll(setSynRids);

		setSynRids = generate(2*maxBlockSize,1);
		retVal.addAll(setSynRids);
		setSynRids = generate(2*maxBlockSize,maxBlockSize/2);
		retVal.addAll(setSynRids);
		setSynRids = generate(2*maxBlockSize,maxBlockSize);
		retVal.addAll(setSynRids);
		setSynRids = generate(2*maxBlockSize,2*maxBlockSize);
		retVal.addAll(setSynRids);

		return retVal;
	}

	public void test(int maxBlockSize) {
		Set<SyntheticRecordIds> testSet = generate(maxBlockSize);
		for (SyntheticRecordIds srids: testSet) {
			List<String> queryIds = srids.getQueryIds();
			List<String> referenceIds = srids.getReferenceIds();
			ComparisonArrayOS<String> caos = new ComparisonArrayOS<>(
					queryIds, referenceIds, SyntheticRecordIds.ID_TYPE, SyntheticRecordIds.ID_TYPE, maxBlockSize
					);
//			int count = 0;
			while (caos.hasNextPair()) {
//				++count;
				ComparisonPair<String> cp = caos.getNextPair();
				String id1 = cp.getId1();
				assertTrue(id1 != null);
				assertTrue(queryIds.contains(id1));
				String id2 = cp.getId2();
				assertTrue(id2 != null);
				assertTrue(queryIds.contains(id2) || referenceIds.contains(id2));
			}
		}
	}

	@Test
	public void testMaxBlockSize1() {
		test(1);
	}

	@Test
	public void testMaxBlockSize3() {
		test(3);
	}

	@Test
	public void testMaxBlockSize5() {
		test(5);
	}

	@Test
	public void testMaxBlockSize7() {
		test(7);
	}

	@Test
	public void testMaxBlockSize11() {
		test(11);
	}

	@Test
	public void testMaxBlockSize13() {
		test(13);
	}

	@Test
	public void testMaxBlockSize23() {
		test(23);
	}

	@Test
	public void testMaxBlockSize47() {
		test(47);
	}

}
