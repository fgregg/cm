package com.choicemaker.cm.io.blocking.automated.offline.core;

/**
 * Defines the role of a record source in a linkage process.
 * <ul>
 * <li>A <code>STAGING</code> source is assumed to contain duplicate records. It
 * is de-duplicated internally before being linked to another data source.</li>
 * <li>A <code>MASTER</code> source is assumed to contain only unique records.
 * It is linked without any internal de-duplication to another data source.
 * </ul>
 * The most common linkage scenario is a linkage of <code>STAGING</code> source
 * against a <code>MASTER</code> source.
 * 
 * @author rphall
 *
 */
public enum RECORD_SOURCE_ROLE {
	STAGING('S'), MASTER('D');
	public final char symbol;
	private final String strSymbol;

	RECORD_SOURCE_ROLE(char c) {
		this.symbol = c;
		this.strSymbol = String.valueOf(c);
	}

	public static RECORD_SOURCE_ROLE fromSymbol(char c) {
		RECORD_SOURCE_ROLE retVal;
		if (c == 'S' || c == 's') {
			retVal = STAGING;
		} else if (c == 'D' || c == 'd') {
			retVal = MASTER;
		} else {
			throw new IllegalArgumentException("invalid symbol: " + c);
		}
		return retVal;
	}

	public static RECORD_SOURCE_ROLE fromSymbol(String s) {
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