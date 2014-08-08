package com.choicemaker.cm.io.blocking.automated.offline.core;

/**
 * The types of record identifiers that the OABA knows how to handle (or not, in
 * the case of {@link RecordIdentifierType#TYPE_UNKNOWN}).
 */
public enum RecordIdentifierType {
	TYPE_UNKNOWN(0, null), TYPE_INTEGER(1, Integer.class), TYPE_LONG(2,
			Long.class), TYPE_STRING(3, String.class);
	public final int typeId;
	public final Class<?> type;

	RecordIdentifierType(int id, Class<?> c) {
		this.typeId = id;
		this.type = c;
	}

	public static RecordIdentifierType checkType(Object o) {
		RecordIdentifierType ret;
		if (o.getClass() == java.lang.Integer.class) {
			ret = TYPE_INTEGER;
		} else if (o.getClass() == java.lang.Long.class) {
			ret = TYPE_LONG;
		} else if (o.getClass() == java.lang.String.class) {
			ret = TYPE_STRING;
		} else {
			ret = TYPE_UNKNOWN;
		}
		return ret;
	}
}
