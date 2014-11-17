package com.choicemaker.cm.io.blocking.automated;

public interface IBlockingSet {

	int numFields();

	boolean containsBlockingValue(IBlockingValue bv);

	boolean containsField(IField f);

	long getExpectedCount();

	IGroupTable getGroupTable(IBlockingField bf);

	IBlockingValue[] getBlockingValues();

	IBlockingValue getBlockingValue(int i);

	IBlockingValue[] getBlockingValues(IGroupTable gt);

	long getMainTableSize();

	IGroupTable getTable(int i);

	IGroupTable[] getTables();

	int getNumTables();

	boolean returnsSupersetOf(IBlockingSet bs);

	void sortValues(boolean ascending);

	// last element decides
	// sorts like values
	void sortTables(boolean likeValues, boolean firstValueDecidesOrder);

}