package com.choicemaker.cmit.utils.j2ee;

public enum BatchProcessingPhase {
	INITIAL(true), INTERMEDIATE(true), FINAL(false);
	public final boolean isIntermediateExpected;
	public final boolean isUpdateExpected;

	BatchProcessingPhase(boolean intermediate) {
		this.isIntermediateExpected = intermediate;
		this.isUpdateExpected = true;
	}
}