package com.choicemaker.cm.args;

public interface PersistableSqlRecordSource extends
		PersistableRecordSource {

	String TYPE = "SQL";

	String getClassName();

	String getDataSource();

	String getSqlSelectStatement();
	
	String getModelId();
	
	String getDatabaseConfiguration();

}
