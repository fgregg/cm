package com.choicemaker.cm.io.blocking.automated.offline.core;

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

	RECORD_ID_TYPE(int i, Class<?> c) {
		this(i, c, null);
	}

	RECORD_ID_TYPE(int i, Class<?> c, Class<?> p) {
		this.charSymbol = (char) i;
		this.strSymbol = String.valueOf(i);
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

	/**
	 * This method checks if the object is an Integer, Long, or String.
	 * 
	 * @param o
	 * @return int - TYPE_INTEGER, or TYPE_LONG, or TYPE_STRING
	 */
	public static <T extends Comparable<T>> int checkType(T o) {
		return fromInstance(o).getIntSymbol();
	}

	public static <T extends Comparable<T>> RECORD_ID_TYPE fromInstance(T o) {
		if (o == null) {
			throw new IllegalArgumentException("null instance");
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
			throw new IllegalArgumentException("invalid String: " + s);
		}
		return fromSymbol(s.charAt(0));
	}

	public static RECORD_ID_TYPE fromClass(Class<?> c) {
		if (c == null) {
			throw new IllegalArgumentException("null class");
		}
		RECORD_ID_TYPE retVal = null;
		for (RECORD_ID_TYPE rit : RECORD_ID_TYPE.values()) {
			if (c.equals(rit.getRecordIdClass())
					|| c.equals(rit.getPrimitiveIdClass())) {
				retVal = rit;
			}
		}
		if (retVal == null) {
			throw new IllegalArgumentException("invalid class: " + c.getName());
		}
		return retVal;
	}

}