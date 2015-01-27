package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.util.logging.Logger;

/**
 * The OABA is currently implemented only for record identifiers of type
 * Integer, Long and String.
 */
public enum RECORD_ID_TYPE {
	TYPE_INTEGER('1', Integer.class, int.class), TYPE_LONG('2', Long.class,
			long.class), TYPE_STRING('3', String.class);
	private final char charSymbol;
	private final String strSymbol;
	private final Class<?> recordIdClass;
	private final Class<?> primitiveIdClass;
	
	private final static Logger logger = Logger.getLogger(RECORD_ID_TYPE.class.getName());

	RECORD_ID_TYPE(int i, Class<?> c) {
		this(i, c, null);
	}

	RECORD_ID_TYPE(int i, Class<?> c, Class<?> p) {
		this.charSymbol = (char) i;
		this.strSymbol = String.valueOf(this.charSymbol);
		this.recordIdClass = c;
		if (p == null) {
			primitiveIdClass = c;
		} else {
			primitiveIdClass = p;
		}
	}

	public int getIntSymbol() {
		return charSymbol;
	}

	public char getCharSymbol() {
		return charSymbol;
	}

	public String getStringSymbol() {
		return strSymbol;
	}

	public Class<?> getRecordIdClass() {
		return recordIdClass;
	}

	private Class<?> getPrimitiveIdClass() {
		return primitiveIdClass;
	}

	public static <T extends Comparable<T>> RECORD_ID_TYPE fromInstance(T o) {
		if (o == null) {
			String msg = "null instance";
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
			String msg =
				"invalid symbol: '" + c + "' (0x" + hex + ")";
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		assert retVal != null;
		return retVal;
	}

	public static RECORD_ID_TYPE fromSymbol(int i) {
		return fromSymbol((char) i);
	}

	public static RECORD_ID_TYPE fromSymbol(String s) {
		if (s == null || !s.equals(s.trim()) || s.length() != 1) {
			String msg = "invalid String: " + s;
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		return fromSymbol(s.charAt(0));
	}

	public static RECORD_ID_TYPE fromClass(Class<?> c) {
		if (c == null) {
			String msg = "null class";
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
			String msg = "invalid class: " + c.getName();
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		return retVal;
	}

}