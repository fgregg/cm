package com.choicemaker.cm.io.blocking.automated.offline.core;

public enum EXTERNAL_DATA_FORMAT {
	STRING(1), BINARY(2);
	public final int symbol;

	EXTERNAL_DATA_FORMAT(int i) {
		symbol = i;
	}

	public static EXTERNAL_DATA_FORMAT fromSymbol(char i) {
		return fromSymbol((int) i);
	}

	public static EXTERNAL_DATA_FORMAT fromSymbol(int i) {
		EXTERNAL_DATA_FORMAT retVal = null;
		switch (i) {
		case 1:
			retVal = STRING;
			break;
		case 2:
			retVal = BINARY;
			break;
		default:
			throw new IllegalArgumentException("invalid symbol: " + i);
		}
		assert retVal != null;
		return retVal;
	}

	public static EXTERNAL_DATA_FORMAT fromSymbol(String s) {
		if (s == null || !s.equals(s.trim()) || s.length() != 1) {
			throw new IllegalArgumentException("invalid String: " + s);
		}
		return fromSymbol(s.charAt(0));
	}

}