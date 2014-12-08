package com.choicemaker.cm.args;

public interface PersistableXmlRecordSource extends
		PersistableRecordSource {

	String TYPE = "XML";

	String DEFAULT_CLASSNAME =
		"com.choicemaker.cm.io.xml.base.XmlRecordSource";

	String getClassName();

	String getDescriptorFileName();
	
	String getDataFileName();

	// FIXME add other XML attributes

}
