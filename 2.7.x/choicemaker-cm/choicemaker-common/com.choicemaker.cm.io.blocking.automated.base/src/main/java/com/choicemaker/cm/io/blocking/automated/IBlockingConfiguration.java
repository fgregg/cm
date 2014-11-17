package com.choicemaker.cm.io.blocking.automated;

import com.choicemaker.cm.core.Record;

public interface IBlockingConfiguration {

	String getName();

	IDbTable[] getDbTables();

	IDbField[] getDbFields();

	IBlockingField[] getBlockingFields();

	IBlockingValue[] createBlockingValues(Record q);

}