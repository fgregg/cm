package com.choicemaker.cm.transitivity.core;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Comparator;

public interface TransitivityResultSerializer extends Serializable {

	/**
	 * This private inner class holds the record id, merge group id, and hold
	 * group id.
	 *
	 * @author pcheung
	 *
	 *         ChoiceMaker Technologies, Inc.
	 */
	@SuppressWarnings("rawtypes")
	public static class Record {
		public static final int MINIMUM_MERGE_GROUP_ID = 0;
		public static final int MINIMUM_MERGE_HOLD_ID = 1;
		
		public final Comparable id;
		public final int mergeGroupId;
		public final int holdGroupId;
		public Record(Comparable id, int mgid, int hgid) {
			if (id == null) {
				throw new IllegalArgumentException("null id");
			}
			if (mgid < MINIMUM_MERGE_GROUP_ID) {
				throw new IllegalArgumentException("invalid merge group id: " + mgid);
			}
			if (hgid < MINIMUM_MERGE_HOLD_ID) {
				throw new IllegalArgumentException("invalid hold group id: " + hgid);
			}
			this.id = id;
			this.mergeGroupId = mgid;
			this.holdGroupId = hgid;
		}
	}

	/**
	 * This private inner class sorts the records by id.
	 *
	 * @author pcheung
	 *
	 *         ChoiceMaker Technologies, Inc.
	 */
	public static class SortByID implements Comparator<Record> {
		@SuppressWarnings("unchecked")
		public int compare(Record r1, Record r2) {
			return r1.id.compareTo(r2.id);
		}
	}

	/**
	 * This private inner class sorts the records by hold group, merge group,
	 * and id.
	 *
	 * @author pcheung
	 *
	 *         ChoiceMaker Technologies, Inc.
	 */
	public static class SortByHoldMergeID implements Comparator<Record> {
		@SuppressWarnings("unchecked")
		public int compare(Record r1, Record r2) {
			if (r1.holdGroupId < r2.holdGroupId)
				return -1;
			else if (r1.holdGroupId > r2.holdGroupId)
				return 1;
			else {
				if (r1.mergeGroupId < r2.mergeGroupId)
					return -1;
				else if (r1.mergeGroupId > r2.mergeGroupId)
					return 1;
				else {
					return r1.id.compareTo(r2.id);
				}
			}
		}
	}

	/**
	 * Serializes a transitivity result to a writer
	 * 
	 * @param result
	 *            a non-null transitivity result
	 * @param fileBase
	 *            The name stem of an output file, excluding any index,
	 *            extension or qualifying path
	 * @param maxFileSize
	 *            the approximate maximum number of records in an output file
	 * @throws IOException
	 */
	void serialize(TransitivityResult result, Writer writer) throws IOException;

	String getCurrentFileName();

}