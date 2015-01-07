package com.choicemaker.cm.io.blocking.automated.offline.core;

public enum RECORD_SOURCE {
	STAGING('S'), MASTER('D');
	public final char symbol;
	private final String strSymbol;

	RECORD_SOURCE(char c) {
		this.symbol = c;
		this.strSymbol = String.valueOf(c);
	}

	public static RECORD_SOURCE fromSymbol(char c) {
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

	public static RECORD_SOURCE fromSymbol(String s) {
		if (s == null || !s.equals(s.trim()) || s.length() != 1) {
			throw new IllegalArgumentException("invalid String: " + s);
		}
		return fromSymbol(s.charAt(0));
	}

	public char getCharSymbol() {
		return symbol;
	}

	public String getStringSymbol() {
		return strSymbol;
	}

}