package com.choicemaker.cmit.utils;

import static org.junit.Assert.assertTrue;

public enum OabaProcessingPhase {
	INITIAL(true, false), INTERMEDIATE(true, true), FINAL(false, true);
	public final boolean isIntermediateExpected;
	public final boolean isUpdateExpected;

	OabaProcessingPhase(boolean intermediate, boolean update) {
		this.isIntermediateExpected = intermediate;
		this.isUpdateExpected = update;
		assertTrue(isIntermediateExpected || isUpdateExpected);
	}
}