package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

public class DeploymentUtils {

	private static final Logger logger = Logger.getLogger(DeploymentUtils.class
			.getName());

	public static final String DEFAULT_POM_FILE = "pom.xml";

	public static final String DEFAULT_TEST_CLASSES_PATH =
		"./target/test-classes";

	public static final String DEFAULT_MODULE_NAME = "tests.jar";

	public static final String DEFAULT_PATH_TO_PERSISTENCE_CONFIG = null;

	public static final boolean DEFAULT_HAS_BEANS = true;

	/**
	 * Creates a POM object that is used to build JAR files and libraries.
	 * 
	 * @param pathToPomFile
	 * @return a non-null pom object
	 */
	public static PomEquippedResolveStage resolvePom(String pathToPomFile) {
		PomEquippedResolveStage retVal =
			Maven.resolver().loadPomFromFile(pathToPomFile);
		return retVal;
	}

	/**
	 * Resolves compile-time dependencies declared with a POM object.
	 * 
	 * @param pom
	 *            a non-null pom object
	 * @return
	 */
	public static File[] resolveDependencies(PomEquippedResolveStage pom) {
		File[] retVal =
			pom.importDependencies(ScopeType.COMPILE).resolve()
					.withTransitivity().asFile();
		return retVal;
	}

	/**
	 * Creates a JAR file of tests
	 * 
	 * @param poma
	 *            non-null pom object
	 * @param mavenCoordinates
	 *            the Maven coordinates of the POM
	 * @param jarFileName
	 *            the name of the JAR that will be created
	 * @param pathToClasses
	 *            the path to directory root for the test classes
	 * @param hasBeans
	 *            if true, the JAR will be marked with a
	 *            <code>META-INF/bean.xml</code> file for CDI code injection.
	 * @return a non-null JAR object
	 */
	public static JavaArchive createJAR(PomEquippedResolveStage pom,
			String mavenCoordinates, String jarFileName, String pathToClasses,
			String pathToPersistenceConfiguration, boolean hasBeans) {
		File jarFile =
			pom.resolve(mavenCoordinates).withoutTransitivity().asSingleFile();
		JavaArchive jar =
			ShrinkWrap.create(ZipImporter.class, DEFAULT_MODULE_NAME)
					.importFrom(jarFile).as(JavaArchive.class);
		if (hasBeans) {
			jar.addAsManifestResource(EmptyAsset.INSTANCE,
					ArchivePaths.create("beans.xml"));
		}
		// Add persistence configuration
		if (pathToPersistenceConfiguration != null) {
			File f = new File(pathToPersistenceConfiguration);
			assertTrue(f.exists());
			FileAsset fileAsset = new FileAsset(f);
			jar.addAsManifestResource(fileAsset, "persistence.xml");
		}

		jar.as(ExplodedImporter.class).importDirectory(
				(new File(DEFAULT_TEST_CLASSES_PATH)));
		return jar;
	}

	/**
	 * Assembles a JAR of tests and library of the tests' dependencies into an
	 * EAR. The JAR of tests will be added as an EJB module or as a plain
	 * library JAR, depending on the value of <code>testsAsEjbModule</code>. <br/>
	 * <strong>NOTE</strong>: the JAR of tests should be added as an EJB module
	 * only if the JAR contains classes that are annotated as EJB's. If the
	 * tests JAR does not contain annotated EJB classes, and the JAR is
	 * inadvertently added as an EJB module, the tests will fail with
	 * ClassNotFound exceptions at runtime.
	 * 
	 * @param tests
	 * @param libs
	 * @param testsAsEjbModule
	 *            if true, the JAR of tests will be added as an EJB module;
	 *            otherwise, the JAR will be added a library JAR.
	 * @return
	 */
	public static EnterpriseArchive createEAR(JavaArchive tests, File[] libs,
			boolean testsAsEjbModule) {
		EnterpriseArchive retVal = ShrinkWrap.create(EnterpriseArchive.class);
		retVal.addAsLibraries(libs);
		if (testsAsEjbModule) {
			retVal.addAsModule(tests);
		} else {
			retVal.addAsLibrary(tests);
		}
		return retVal;
	}

	/**
	 * A convenience method equivalent to:
	 * 
	 * <pre>
	 * PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
	 * File[] libs = resolveDependencies(pom);
	 * JavaArchive tests = createJAR(pom, mavenCoordinates, DEFAULT_MODULE_NAME,
	 * 		DEFAULT_TEST_CLASSES_PATH, DEFAULT_PATH_TO_PERSISTENCE_CONFIG,
	 * 		DEFAULT_HAS_BEANS);
	 * Archive&lt;?&gt; retVal = createEAR(tests, libs, testsAsEjbModule);
	 * </pre>
	 * 
	 * @param mavenCoordinates
	 * @param testsAsEjbModule
	 * @return
	 */
	public static EnterpriseArchive createEAR(String mavenCoordinates,
			boolean testsAsEjbModule) {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);
		JavaArchive tests =
			createJAR(pom, mavenCoordinates, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH,
					DEFAULT_PATH_TO_PERSISTENCE_CONFIG, DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal = createEAR(tests, libs, testsAsEjbModule);
		return retVal;
	}

	/**
	 * @deprecated instead use #createEAR(String, boolean)
	 * @param projectPOM
	 * @param MavenCoordinates
	 * @param testClasses
	 * @param persistenceConfiguration
	 * @return
	 */
	@Deprecated
	public static JavaArchive createTestJar(String projectPOM,
			String MavenCoordinates, List<Class<?>> testClasses,
			String persistenceConfiguration) {

		if (testClasses == null || testClasses.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty list of test classes");
		}

		JavaArchive retVal;
		if (MavenCoordinates != null) {
			if (projectPOM == null) {
				throw new IllegalArgumentException("null POM");
			}
			if (!new File(projectPOM).exists()) {
				throw new IllegalArgumentException("POM doesn't exist: '"
						+ projectPOM + "'");
			}
			PomEquippedResolveStage pom =
				Maven.resolver().loadPomFromFile(projectPOM);
			File jarFile =
				pom.resolve(MavenCoordinates).withoutTransitivity()
						.asSingleFile();
			retVal =
				ShrinkWrap.create(ZipImporter.class, "ejb.jar")
						.importFrom(jarFile).as(JavaArchive.class);
		} else {
			retVal = ShrinkWrap.create(JavaArchive.class);
		}

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
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("");
			logger.fine("EJB JAR:");
			logger.fine(retVal.toString(true));
			logger.fine("");
		}
		return retVal;
	}

	@Deprecated
	public static File[] createTestDependencies(String dependenciesPOM) {
		if (dependenciesPOM == null) {
			throw new IllegalArgumentException("null POM");
		}
		if (!new File(dependenciesPOM).exists()) {
			throw new IllegalArgumentException("POM doesn't exist: '"
					+ dependenciesPOM + "'");
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
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("");
			logger.fine("Test dependencies:");
			for (File f : retVal) {
				logger.fine(f.getAbsolutePath());
			}
			logger.fine("");
		}
		return retVal;
	}

	@Deprecated
	public static EnterpriseArchive createEarArchive(JavaArchive ejb,
			File[] dependencies) {
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
				System.err.println(msg);
			}
		}

		// Print the EAR contents
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("");
			logger.fine("Deployment EAR:");
			logger.fine(retVal.toString(true));
			logger.fine("");
		}
		return retVal;
	}

	private DeploymentUtils() {
	}

}
