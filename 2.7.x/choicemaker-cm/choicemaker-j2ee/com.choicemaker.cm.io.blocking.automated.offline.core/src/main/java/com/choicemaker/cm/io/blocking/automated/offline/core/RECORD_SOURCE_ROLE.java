package com.choicemaker.cm.io.blocking.automated.offline.core;

/**
 * Defines the role of a record source in a linkage process.
 * <ul>
 * <li>A <code>STAGING</code> source is assumed to contain duplicate records. It
 * is de-duplicated internally before being linked to another data source.</li>
 * <li>A <code>MASTER</code> source is assumed to contain only unique records.
 * It is linked without any internal deduplication to another data source.
 * </ul>
 * The most common linkage scenario is a linkage of <code>STAGING</code> source
 * against a <code>MASTER</code> source, or a deduplication of
 * <code>STAGING</code> source against itself.<br/>
 * <br/>
 * However, other linkages are possible, such as a master source against a
 * second master source. There might even cases where it makes sense to link a
 * staging source against a second staging source. In less common cases, the
 * source of a set of records needs to be distinguished from its role.
 * Previously, the OABA design assumed that the source with duplicates is always
 * the first source (1), and the source without duplicates is always the second
 * source (2). To link a master against a second master, or staging against a
 * staging, this version of the OABA defines a 'first source without duplicates'
 * and a 'second source with duplicates':
 * <ul>
 * <li><code>STAGING</code> possibly renamed to <code>SOURCE1_DUPES</code></li>
 * <li><code>SOURCE1_NODUPES</code> (for master1 linked against master2)</li>
 * <li><code>MASTER</code> possibly renamed to <code>SOURCE2_NODUPES</code></li>
 * <li><code>SOURCE2_DUPES</code> (for staging1 linked against staging2)</li>
 * </ul>
 * To maintain some backward compatibility, the symbols for <code>STAGING</code>
 * (a.k.a. <code>SOURCE1_DUPES</code>) and <code>MASTER</code>(a.k.a.
 * <code>SOURCE2_NODUPES</code>) are unchanged. The symbols for the additional
 * members (<code>SOURCE1_NODUPES</code> and <code>SOURCE2_DUPES</code>) are
 * new.
 * 
 * @author rphall
 *
 */
public enum RECORD_SOURCE_ROLE {
	/** Source 1, with duplicates */
	STAGING('S'),
	/** Source 1, without duplicates */
	SOURCE1_NODUPES('A'),
	/** Source 2, without duplicates */
	MASTER('D'),
	/** Source 3, without duplicates */
	SOURCE2_DUPES('Z'),
	/** A magic value internally */
	SPLIT_INDEX('0');
	public final char symbol;
	private final String strSymbol;

	RECORD_SOURCE_ROLE(char c) {
		this.symbol = c;
		this.strSymbol = String.valueOf(c);
	}

	public boolean isDeduped() {
		return symbol == MASTER.symbol || symbol == SOURCE1_NODUPES.symbol;
	}

	public boolean isFirstSource() {
		return symbol == STAGING.symbol || symbol == SOURCE1_NODUPES.symbol;
	}

	public boolean isSplitIndex() {
		return symbol == SPLIT_INDEX.symbol;
	}

	public static RECORD_SOURCE_ROLE fromSymbol(char c) {
		RECORD_SOURCE_ROLE retVal;
		switch (c) {
		case 'S':
		case 's':
			retVal = STAGING;
			break;
		case 'A':
		case 'a':
			retVal = SOURCE1_NODUPES;
			break;
		case 'D':
		case 'd':
			retVal = MASTER;
			break;
		case 'Z':
		case 'z':
			retVal = SOURCE2_DUPES;
			break;
		case '0':
			retVal = SPLIT_INDEX;
			break;
		default:
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