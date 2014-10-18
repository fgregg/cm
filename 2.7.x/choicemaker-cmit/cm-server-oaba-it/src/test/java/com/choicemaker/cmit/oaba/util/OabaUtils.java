package com.choicemaker.cmit.oaba.util;

public class OabaUtils {

	private static final String MAVEN_COORDINATE_SEPARATOR = ":";

	public static final String PROJECT_POM = "pom.xml";

	public static final String DEPENDENCIES_POM = PROJECT_POM;

	public static final String EJB_MAVEN_GROUPID = "com.choicemaker.cm";

	public static final String EJB_MAVEN_ARTIFACTID =
		"com.choicemaker.cm.io.blocking.automated.offline.server";

	public static final String EJB_MAVEN_VERSION = "2.7.1-SNAPSHOT";

	public static final String EJB_MAVEN_COORDINATES = new StringBuilder()
			.append(EJB_MAVEN_GROUPID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_ARTIFACTID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_VERSION).toString();

	public static final String PERSISTENCE_CONFIGURATION =
			"src/test/resources/jboss/sqlserver/persistence.xml";

	private OabaUtils() {
	}

}
