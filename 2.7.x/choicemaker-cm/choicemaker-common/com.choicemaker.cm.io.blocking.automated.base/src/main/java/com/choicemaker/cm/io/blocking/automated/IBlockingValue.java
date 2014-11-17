package com.choicemaker.cm.io.blocking.automated;

public interface IBlockingValue extends Cloneable {

	public Object clone();

	boolean containsBase(IBlockingSet bs);

	IBlockingField getBlockingField();

	IBlockingValue[][] getBase();

	String getValue();

	String getGroup();

	int getCount();

	int getTableSize();

	IGroupTable getGroupTable();

	void setTableSize(int tableSize);

	void setCount(int count);

	int compareTo(IBlockingValue obv);

	boolean equals(Object o);

	int hashCode();

}