package com.choicemaker.cm.io.db.sqlserver;

public class SqlServerXmlUtils {

	public static final String EN_SQLSERVERCOMPOSITERECORDSOURCE =
		"sqlServerCompositeRecordSource";
	
	public static final String EN_MARKEDRECORDPAIRSOURCE =
			"MarkedRecordPairSource";
		
	public static final String EN_RECORDSOURCE =
			"RecordSource";
		
	public static final String EN_MRPSQUERY =
			"mrpsQuery";

	// XML Attributes

	public static final String AN_CLASS = "class";
	public static final String AN_DATASOURCENAME = "dataSourceName";
	public static final String AN_MODEL = "model";
	public static final String AN_DBCONFIGURATION = "dbConfiguration";
	public static final String AN_IDSQUERY = "idsQuery";
	public static final String AN_MAXCOMPOSITESIZE = "maxCompositeSize";

	// Deprecated model properties

	/** @deprecated */
	public static final String PN_BLOCKING_CONFIGURATION = "blockingConfiguration";

	/** @deprecated */
	public static final String PN_DB_CONFIGURATION = AN_DBCONFIGURATION;

	/** @deprecated */
	public static final String PN_LIMITPERBLOCKINGSET = "limitPerBlockingSet";

	/** @deprecated */
	public static final String PN_SINGLETABLEBLOCKINGSETGRACELIMIT = "limitSingleBlockingSet";

	/** @deprecated */
	public static final String PN_LIMITSINGLEBLOCKINGSET = "limitPerBlockingSet";

	private SqlServerXmlUtils() {
	}

	public static String xmlElementStart(String entityName) {
		String retVal =
			new StringBuilder().append("<").append(entityName).append(" ")
					.toString();
		return retVal;
	}
	
	public static String xmlElementEnd(String entityName) {
		String retVal =
				new StringBuilder().append("</").append(entityName).append(">")
						.toString();
			return retVal;
	}

	public static String xmlElementEndInline() {
		return "/>";
	}

	public static String xmlAttribute(String name, String value) {
		String retVal =
			new StringBuilder().append(name).append("=\"").append(value)
					.append("\" ").toString();
		return retVal;
	}

}
