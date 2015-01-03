package com.choicemaker.cmit.utils;

public enum OabaProcessingPhase {
	INITIAL(true), INTERMEDIATE(true), FINAL(false);
	public final boolean isIntermediateExpected;
	public final boolean isUpdateExpected;

	OabaProcessingPhase(boolean intermediate) {
		this.isIntermediateExpected = intermediate;
		this.isUpdateExpected = true;
	}
}