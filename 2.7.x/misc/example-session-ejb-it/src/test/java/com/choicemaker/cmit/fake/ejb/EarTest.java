package com.choicemaker.cmit.fake.ejb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cmit.utils.DeploymentUtils;
import com.choicemaker.fake.ejb3.HelloWorld;

@RunWith(Arquillian.class)
public class EarTest {
	
	private static final String MAVEN_COORDINATE_SEPARATOR = ":";
	
	static final String PROJECT_POM = "pom.xml";
	
	static final String DEPENDENCIES_POM = PROJECT_POM;
	
	static final String EJB_MAVEN_GROUPID =
		"com.choicemaker.fake";

	static final String EJB_MAVEN_ARTIFACTID =
			"com.choicemaker.fake.stateless-01";

	static final String EJB_MAVEN_VERSION =
			"0.0.1-SNAPSHOT";

	static final String EJB_MAVEN_COORDINATES = new StringBuilder()
			.append(EJB_MAVEN_GROUPID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_ARTIFACTID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_VERSION).toString();

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(EarTest.class);

		JavaArchive ejb =
				DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
						testClasses, null);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb, deps);
		return retVal;
	}

	@EJB
	HelloWorld helloWorld;

//	public static JavaArchive createEjbJar() {
//		// Create a copy of the EJB jar
//		PomEquippedResolveStage pom =
//			Maven.resolver().loadPomFromFile(PROJECT_POM);
//		File jarFile =
//			pom.resolve(EJB_MAVEN_COORDINATES).withoutTransitivity()
//					.asSingleFile();
//		JavaArchive retVal =
//			ShrinkWrap.create(ZipImporter.class, "ejb.jar").importFrom(jarFile)
//					.as(JavaArchive.class);
//
//		// Flag this JAR to CDI as containing injectable beans
//		retVal.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
//
//		// Add test class to JAR
//		retVal.addClass(EarTest.class);
//
//		// Print the JAR contents
//		/*
//		System.out.println();
//		System.out.println("EJB JAR:");
//		System.out.println(retVal.toString(true));
//		System.out.println();
//		*/
//		return retVal;
//	}
//
//	public static File[] createTestDependencies() {
//		Set<File> files = new HashSet<>();
//
//		PomEquippedResolveStage pom =
//			Maven.resolver().loadPomFromFile(PROJECT_POM);
//		File[] directDependencies =
//			pom.importDependencies(ScopeType.COMPILE).resolve()
//					.withTransitivity().asFile();
//		/*
//		System.out.println();
//		System.out.println("Direct dependencies:");
//    */
//		for (File f : directDependencies) {
//			if (!f.getName().contains(EJB_MAVEN_ARTIFACTID)) {
//				// System.out.println(f.getAbsolutePath());
//				files.add(f);
//			} else {
//				System.out.println("Skipping: " + f.getName());
//			}
//		}
//
//		File[] retVal = files.toArray(new File[files.size()]);
//		/*
//		System.out.println();
//		System.out.println("Test dependencies:");
//		for (File f : directDependencies) {
//			System.out.println(f.getAbsolutePath());
//		}
//		System.out.println();
//		*/
//
//		return retVal;
//	}
//
//	@Deployment
//	public static EnterpriseArchive createEarArchive() {
//		JavaArchive ejb1 = createEjbJar();
//		// create the EAR
//		EnterpriseArchive retVal = ShrinkWrap.create(EnterpriseArchive.class);
//		retVal.addAsModule(ejb1);
//		try {
//			File[] deps = createTestDependencies();
//			retVal.addAsLibraries(deps);
//		} catch (Exception x) {
//			String msg =
//				"WARNING: failed to add test dependencies: " + x.toString();
//			System.out.println(msg);
//		}
//		/*
//		System.out.println();
//		System.out.println("Deployment EAR:");
//		System.out.println(retVal.toString(true));
//		System.out.println();
//		*/
//		return retVal;
//	}
//
	@Test
	public void testEJB() {
		// Exercise the EJB
		Assert.assertTrue (helloWorld != null);
		String hello = helloWorld.sayHello();
		Assert.assertTrue(hello != null);
		// System.out.println(this.getClass().getSimpleName() + ": " +hello);
	}

}
