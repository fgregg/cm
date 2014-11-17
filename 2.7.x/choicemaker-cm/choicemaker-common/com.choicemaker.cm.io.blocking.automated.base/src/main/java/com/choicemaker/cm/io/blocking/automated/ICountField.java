package com.choicemaker.cm.io.blocking.automated;

import java.util.Map;

public interface ICountField {

	void putValueCount(String value, Integer count);

	void putAll(Map<String, Integer> m);

	Integer getCountForValue(String value);

	int getDefaultCount();

	int getTableSize();

	String getColumn();

	String getView();

	String getUniqueId();

}