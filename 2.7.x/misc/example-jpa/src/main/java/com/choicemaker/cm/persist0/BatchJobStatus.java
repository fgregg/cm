package com.choicemaker.cm.persist0;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum BatchJobStatus {
	NEW(false), QUEUED(false), STARTED(false), COMPLETED(true), FAILED(true),
	ABORT_REQUESTED(false), ABORTED(true), CLEAR(true);

	public boolean isTerminal;

	private BatchJobStatus(boolean terminal) {
		this.isTerminal = terminal;
	}

	private static Map<BatchJobStatus, EnumSet<BatchJobStatus>> allowedTransitions =
		new HashMap<>();
	static {
		allowedTransitions.put(BatchJobStatus.NEW, EnumSet.of(
				BatchJobStatus.QUEUED, BatchJobStatus.ABORT_REQUESTED,
				BatchJobStatus.ABORTED));
		allowedTransitions.put(BatchJobStatus.QUEUED, EnumSet.of(
				BatchJobStatus.STARTED, BatchJobStatus.ABORT_REQUESTED,
				BatchJobStatus.ABORTED));
		allowedTransitions.put(BatchJobStatus.STARTED, EnumSet.of(
				BatchJobStatus.STARTED, BatchJobStatus.COMPLETED,
				BatchJobStatus.FAILED, BatchJobStatus.ABORT_REQUESTED,
				BatchJobStatus.ABORTED));
		allowedTransitions.put(BatchJobStatus.ABORT_REQUESTED,
				EnumSet.of(BatchJobStatus.ABORTED));
		// Terminal transitions (unless re-queued/re-started)
		allowedTransitions.put(BatchJobStatus.COMPLETED,
				EnumSet.noneOf(BatchJobStatus.class));
		allowedTransitions.put(BatchJobStatus.FAILED,
				EnumSet.noneOf(BatchJobStatus.class));
		allowedTransitions.put(BatchJobStatus.ABORTED,
				EnumSet.noneOf(BatchJobStatus.class));
		allowedTransitions.put(BatchJobStatus.CLEAR,
				EnumSet.noneOf(BatchJobStatus.class));
	}

	// -- State machine

	public static boolean isAllowedTransition(BatchJobStatus current,
			BatchJobStatus next) {
		if (current == null || next == null) {
			throw new IllegalArgumentException("null batchJobStatus");
		}
		Set<BatchJobStatus> allowed =
			BatchJobStatus.allowedTransitions.get(current);
		assert allowed != null;
		boolean retVal = allowed.contains(next);
		return retVal;
	}

	// -- String representation
	
	public static final String toString(BatchJobStatus status) {
		String retVal = null;
		if (status != null) {
			retVal = status.name();
			assert retVal.equals(retVal.trim().toUpperCase());
		}
		return retVal;
	}

	public static final BatchJobStatus fromString(String s) {
		BatchJobStatus retVal = null;
		if (s != null) {
			retVal = BatchJobStatus.valueOf(s.trim().toUpperCase());
		}
		return retVal;
	}
	
}