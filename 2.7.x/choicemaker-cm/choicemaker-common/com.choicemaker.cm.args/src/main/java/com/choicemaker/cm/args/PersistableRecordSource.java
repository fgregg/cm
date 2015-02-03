package com.choicemaker.cm.args;

import java.io.Serializable;

public interface PersistableRecordSource extends PersistentObject, Serializable {
	
	long getId();
	
	String getType();
	
}
