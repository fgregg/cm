package com.choicemaker.cm.args;

import java.io.Serializable;

public interface PersistableRecordSource extends Serializable {
	
	long NONPERSISTENT_ID = 0;
	
	long getId();
	
	String getType();
	
}
