package com.choicemaker.cm.io.blocking.automated;

public interface IDbField extends IField {

	int getNumber();

	String getName();

	String getType();

	IDbTable getTable();

	int getDefaultCount();

}