package com.choicemaker.cmit.transitivity.server;

public class TransitivityDeploymentUtils {

	private static final String MAVEN_COORDINATE_SEPARATOR = ":";
	
	static final String DEPENDENCIES_POM =
	"src/test/dependencies/transitivity-dependency-pom.xml";
	
	static final String EJB_MAVEN_GROUPID = "com.choicemaker.cm";
	
	static final String EJB_MAVEN_ARTIFACTID =
	"com.choicemaker.cm.transitivity.server";
	
	static final String EJB_MAVEN_VERSION = "2.7.1-SNAPSHOT";
	
	static final String EJB_MAVEN_COORDINATES = new StringBuilder()
	.append(EJB_MAVEN_GROUPID).append(MAVEN_COORDINATE_SEPARATOR)
	.append(EJB_MAVEN_ARTIFACTID).append(MAVEN_COORDINATE_SEPARATOR)
	.append(EJB_MAVEN_VERSION).toString();

	static final String PROJECT_POM = "pom.xml";

	private TransitivityDeploymentUtils() {
	}

}
