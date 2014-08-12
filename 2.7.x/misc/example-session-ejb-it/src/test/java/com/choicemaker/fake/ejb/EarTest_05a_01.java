package com.choicemaker.fake.ejb;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EarTest_05a_01 {
	
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

	public static JavaArchive createEjbNoCdiJar() {
		// Create a copy of the EJB jar
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(PROJECT_POM);
		File jarFile =
			pom.resolve(EJB_MAVEN_COORDINATES).withoutTransitivity()
					.asSingleFile();
		JavaArchive retVal =
			ShrinkWrap.create(ZipImporter.class, "ejb_no-cdi.jar").importFrom(jarFile)
					.as(JavaArchive.class);

		// Flag this JAR to CDI as containing injectable beans
		retVal.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		// Add test class(es) to JAR
		retVal.addClass(EarTest_05a_01.class);
		retVal.addClass(EarTest_05b_00.class);

		// Print the JAR contents
		System.out.println();
		System.out.println("EJB JAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	public static File[] createTestDependencies() {
		Set<File> files = new HashSet<>();

		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(PROJECT_POM);
		File[] directDependencies =
			pom.importDependencies(ScopeType.COMPILE).resolve()
					.withTransitivity().asFile();
		System.out.println();
		System.out.println("Direct dependencies:");
		for (File f : directDependencies) {
			if (!f.getName().contains(EJB_MAVEN_ARTIFACTID)) {
				System.out.println(f.getAbsolutePath());
				files.add(f);
			} else {
				System.out.println("Skipping: " + f.getName());
			}
		}

//		PomEquippedResolveStage dependenciesPOM =
//			Maven.resolver().loadPomFromFile(DEPENDENCIES_POM);
//		File[] ejbDependencies =
//			dependenciesPOM.importDependencies(ScopeType.COMPILE).resolve()
//					.withTransitivity().asFile();
//		System.out.println();
//		System.out.println("EJB dependencies:");
//		for (File f : ejbDependencies) {
//			if (!f.getName().contains(EJB_MAVEN_ARTIFACTID)) {
//				System.out.println(f.getAbsolutePath());
//				files.add(f);
//			} else {
//				System.out.println("Skipping: " + f.getName());
//			}
//		}

		File[] retVal = files.toArray(new File[files.size()]);
		System.out.println();
		System.out.println("Test dependencies:");
		for (File f : directDependencies) {
			System.out.println(f.getAbsolutePath());
		}
		System.out.println();

		return retVal;
	}

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		JavaArchive ejb1 = createEjbNoCdiJar();
		// create the EAR
		EnterpriseArchive retVal =
			ShrinkWrap.create(EnterpriseArchive.class);
		retVal.addAsModule(ejb1);
		try {
		File[] deps = createTestDependencies();
		retVal.addAsLibraries(deps);
		} catch( Exception x) {
			String msg = "WARNING: failed to add test dependencies: " + x.toString();
			System.out.println(msg);
		}
		System.out.println();
		System.out.println("Deployment EAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	@Test
	public void testEJB() {
		// Exercise the EJB
//		Assert.assertTrue (helloWorld != null);
//		String hello = helloWorld.sayHello();
//		Assert.assertTrue(hello != null);
		System.out.println("HERE 5a_01");
	}

	@Test
	public void testEJB2() {
		System.out.println("Start testEBJ2");
		EarTest_05b_00 test = new EarTest_05b_00();
		test.testEJB();
		System.out.println("Finish testEBJ2");
	}

}
