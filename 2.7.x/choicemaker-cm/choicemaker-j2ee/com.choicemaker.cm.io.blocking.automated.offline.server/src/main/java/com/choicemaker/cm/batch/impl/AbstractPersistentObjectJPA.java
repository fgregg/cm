package com.choicemaker.cm.batch.impl;

public interface AbstractPersistentObjectJPA {

	String CN_UUID = "UUID";

	String CN_OPTLOCK = "OPTLOCK";
	
	String CD_OPTLOCK = "integer DEFAULT 0";
	
	boolean NULLABLE_OPTLOCK = false;

}
