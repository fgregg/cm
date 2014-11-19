package com.choicemaker.cm.io.blocking.automated;

import java.io.Serializable;

public interface IDbTable extends Serializable {

	String getName();

	int getNum();

	String getUniqueId();

}