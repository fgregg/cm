package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.util.logging.Logger;

/**
 * The OABA is currently implemented only for record identifiers of type
 * Integer, Long and String.
 */
public enum RECORD_ID_TYPE {
	TYPE_INTEGER('1', Integer.class, int.class), TYPE_LONG('2', Long.class,
			long.class), TYPE_STRING('3', String.class);

	public static final String LOG_SOURCE = "RECORD_ID_TYPE";

	private final char charSymbol;
	private final int intSymbol;
	private final String strSymbol;
	private final Class<?> recordIdClass;
	private final Class<?> primitiveIdClass;

	private final static Logger logger = Logger.getLogger(RECORD_ID_TYPE.class
			.getName());

	RECORD_ID_TYPE(int i, Class<?> c) {
		this(i, c, null);
	}

	RECORD_ID_TYPE(int i, Class<?> c, Class<?> p) {
		this.charSymbol = (char) i;
		this.strSymbol = String.valueOf(this.charSymbol);
		this.intSymbol = Integer.parseInt(this.strSymbol);
		this.recordIdClass = c;
		if (p == null) {
			primitiveIdClass = c;
		} else {
			primitiveIdClass = p;
		}
	}

	/**
	 * Note the return value is numeric: 0x01, 0x02 or 0x03. It is not equal to
	 * getCharSymbol()
	 */
	public int getIntValue() {
		return intSymbol;
	}

	/**
	 * Note the return value is an ASCII symbol: 0x31, 0x32 or 0x33. It is not
	 * equal to getIntSymbol()
	 */
	public char getCharSymbol() {
		return charSymbol;
	}

	/** Consistent with getCharSymbol() */
	public String getStringSymbol() {
		return strSymbol;
	}

	public Class<?> getRecordIdClass() {
		return recordIdClass;
	}

	private Class<?> getPrimitiveIdClass() {
		return primitiveIdClass;
	}

	@SuppressWarnings("unchecked")
	public <T extends Comparable<T>> T idFromString(String s) {
		T retVal;
		if (getRecordIdClass().equals(Integer.class)) {
			retVal = (T) Integer.valueOf(s);
		} else if (getRecordIdClass().equals(Long.class)) {
			retVal = (T) Long.valueOf(s);
		} else if (getRecordIdClass().equals(String.class)) {
			retVal = (T) s;
		} else {
			throw new Error("Unreachable");
		}
		return retVal;
	}

	public static <T extends Comparable<T>> RECORD_ID_TYPE fromInstance(T o) {
		if (o == null) {
			String msg = LOG_SOURCE + ": null instance";
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		RECORD_ID_TYPE retVal = fromClass(o.getClass());
		return retVal;
	}

	public static RECORD_ID_TYPE fromSymbol(char c) {
		RECORD_ID_TYPE retVal = null;
		switch (c) {
		case '1':
			retVal = TYPE_INTEGER;
			break;
		case '2':
			retVal = TYPE_LONG;
			break;
		case '3':
			retVal = TYPE_STRING;
			break;
		default:
			String hex = Integer.toHexString(c);
			String msg = LOG_SOURCE + ": invalid symbol: '" + c + "' (0x" + hex + ")";
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		assert retVal != null;
		return retVal;
	}

	public static RECORD_ID_TYPE fromValue(int i) {
		String s = Integer.toString(i);
		return fromSymbol(s.charAt(0));
	}

	public static RECORD_ID_TYPE fromSymbol(String s) {
		if (s == null || !s.equals(s.trim()) || s.length() != 1) {
			String msg = LOG_SOURCE + ": invalid String: " + s;
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		return fromSymbol(s.charAt(0));
	}

	public static RECORD_ID_TYPE fromClass(Class<?> c) {
		if (c == null) {
			String msg = LOG_SOURCE + ": null class";
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		RECORD_ID_TYPE retVal = null;
		for (RECORD_ID_TYPE rit : RECORD_ID_TYPE.values()) {
			if (c.equals(rit.getRecordIdClass())
					|| c.equals(rit.getPrimitiveIdClass())) {
				retVal = rit;
			}
		}
		if (retVal == null) {
			String msg = LOG_SOURCE + ": invalid class: " + c.getName();
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		return retVal;
	}

}
