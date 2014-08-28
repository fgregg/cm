package com.choicemaker.cmit.utils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

public class DeploymentUtils {

	public static final String PROJECT_POM = "pom.xml";

	public static final String PERSISTENCE_CONFIGURATION =
			"src/test/resources/jboss/sqlserver/persistence.xml";

	public static JavaArchive createEjbJar(String projectPOM, String MavenCoordinates, List<Class<?>> testClasses, String persistenceConfiguration) {
		if (projectPOM == null) {
			throw new IllegalArgumentException("null POM");
		}
		if (!new File(projectPOM).exists()) {
			throw new IllegalArgumentException("POM doesn't exist: '" + projectPOM + "'");
		}
		if (testClasses == null || testClasses.isEmpty()) {
			throw new IllegalArgumentException("null or empty list of test classes");
		}
		
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(projectPOM);
		File jarFile =
			pom.resolve(MavenCoordinates).withoutTransitivity()
					.asSingleFile();
		JavaArchive retVal =
			ShrinkWrap.create(ZipImporter.class, "ejb.jar").importFrom(jarFile)
					.as(JavaArchive.class);

		// Flag this JAR to CDI as containing injectable beans
		retVal.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		// Add persistence configuration
		if (persistenceConfiguration != null) {
			File f = new File(persistenceConfiguration);
			assertTrue(f.exists());
			FileAsset fileAsset = new FileAsset(f);
			retVal.addAsManifestResource(fileAsset, "persistence.xml");
		}

		// Add test classes to JAR
		for (Class<?> testClass : testClasses) {
			retVal.addClass(testClass);
		}

		// Print the JAR contents
		/*
		System.out.println();
		System.out.println("EJB JAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		*/
		return retVal;
	}

	public static File[] createTestDependencies(String dependenciesPOM) {
		if (dependenciesPOM == null) {
			throw new IllegalArgumentException("null POM");
		}
		if (!new File(dependenciesPOM).exists()) {
			throw new IllegalArgumentException("POM doesn't exist: '" + dependenciesPOM + "'");
		}

		// Break out steps for easier debugging
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(dependenciesPOM);
		pom = pom.importDependencies(ScopeType.COMPILE);
		MavenStrategyStage mss = pom.resolve();
		assertTrue(mss != null);
		MavenFormatStage mfs = mss.withTransitivity();
		assertTrue(mfs != null);

		File[] retVal = mfs.asFile();

		// Print the dependencies
		/*
		System.out.println();
		System.out.println("Test dependencies:");
		for (File f : retVal) {
			System.out.println(f.getAbsolutePath());
		}
		System.out.println();
		*/
		return retVal;
	}

	public static EnterpriseArchive createEarArchive(JavaArchive ejb, File[] dependencies) {
		// Create the EAR
		EnterpriseArchive retVal = ShrinkWrap.create(EnterpriseArchive.class);

		// Create and add the EJB
		if (ejb != null) {
			retVal.addAsModule(ejb);
			// retVal.addAsLibrary(ejb1);
		}

		// Add the EJB dependencies
		if (dependencies != null) {
			try {
				retVal.addAsLibraries(dependencies);
			} catch (Exception x) {
				String msg =
					"WARNING: failed to add test dependencies: " + x.toString();
				System.out.println(msg);
			}
		}

		// Print the EAR contents
		/*
		System.out.println();
		System.out.println("Deployment EAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		*/
		return retVal;
	}

	private DeploymentUtils() {
	}

}
