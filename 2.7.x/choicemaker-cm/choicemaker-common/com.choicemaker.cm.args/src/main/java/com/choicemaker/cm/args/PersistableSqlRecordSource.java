package com.choicemaker.cm.args;

public interface PersistableSqlRecordSource extends
		PersistableRecordSource {

	String TYPE = "SQL";

	String getClassName();

	String getDataSourceName();

	String getSqlSelectStatement();

}
