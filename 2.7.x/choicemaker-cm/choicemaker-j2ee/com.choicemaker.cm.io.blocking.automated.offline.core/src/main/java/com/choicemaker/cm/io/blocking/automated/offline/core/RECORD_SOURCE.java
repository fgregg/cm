package com.choicemaker.cm.io.blocking.automated.offline.core;

public enum RECORD_SOURCE {
	STAGING('S'), MASTER('D');
	public final char symbol;

	RECORD_SOURCE(char c) {
		this.symbol = c;
	}

	public static RECORD_SOURCE getRecordSource(char c) {
		RECORD_SOURCE retVal;
		if (c == 'S' || c == 's') {
			retVal = STAGING;
		} else if (c == 'D' || c == 'd') {
			retVal = MASTER;
		} else {
			throw new IllegalArgumentException("invalid symbol: " + c);
		}
		return retVal;
	}
}