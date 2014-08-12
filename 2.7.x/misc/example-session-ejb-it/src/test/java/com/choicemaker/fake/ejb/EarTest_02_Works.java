package com.choicemaker.fake.ejb;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.fake.ejb3.HelloWorld;

@RunWith(Arquillian.class)
public class EarTest_02_Works {
	
	private static final String MAVEN_COORDINATE_SEPARATOR = ":";
	
	public static final String PROJECT_POM = "pom.xml";
	
	public static final String DEPENDENCIES_POM = "src/test/dependencies/pom.xml";
	
	public static final String EJB_MAVEN_GROUPID =
		"com.choicemaker.fake";
	public static final String EJB_MAVEN_ARTIFACTID =
			"com.choicemaker.fake.stateless-01";
	public static final String EJB_MAVEN_VERSION =
			"0.0.1-SNAPSHOT";
	public static final String EJB_MAVEN_COORDINATES = new StringBuilder()
			.append(EJB_MAVEN_GROUPID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_ARTIFACTID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_VERSION).toString();

//	@Inject
//	HelloWorld helloWorld;

	@Deployment
	public static JavaArchive createEjbJar() {
		// Create a copy of the EJB jar
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(PROJECT_POM);
		File jarFile =
			pom.resolve(EJB_MAVEN_COORDINATES).withoutTransitivity()
					.asSingleFile();
		JavaArchive retVal =
			ShrinkWrap.create(ZipImporter.class, "ejb.jar").importFrom(jarFile)
					.as(JavaArchive.class);

		// Flag this JAR to CDI as containing injectable beans
		retVal.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		// Add test class to JAR
		retVal.addClass(EarTest_02_Works.class);

		// Print the JAR contents
		System.out.println();
		System.out.println("EJB JAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	@Test
	public void testEJB() {
		System.out.println("HERE");
	}

}
