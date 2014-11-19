package com.choicemaker.cm.io.blocking.automated;

import java.io.Serializable;

public interface IBlockingValue extends Cloneable, Serializable {

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