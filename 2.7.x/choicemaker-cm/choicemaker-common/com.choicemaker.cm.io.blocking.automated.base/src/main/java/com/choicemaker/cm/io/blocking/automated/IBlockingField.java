package com.choicemaker.cm.io.blocking.automated;

public interface IBlockingField extends IField {

	int getNumber();

	IQueryField getQueryField();

	IDbField getDbField();

	String getGroup();

}