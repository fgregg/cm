package com.choicemaker.cm.args;

public interface PersistableFlatFileRecordSource extends
		PersistableRecordSource {

	String TYPE = "FLATFILE";

	String DEFAULT_CLASSNAME =
		"com.choicemaker.cm.io.flatfile.base.FlatFileRecordSource";

	String getClassName();

	String getDescriptorFileName();

	String getDataFileName();
	
	// FIXME add other FlatFile attributes

}
