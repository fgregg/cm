package com.choicemaker.cmit.oaba;

import java.io.File;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class AbstractJeeTestNoEjbs {

	public static final String DEFAULT_POM_FILE = "pom.xml";
	public static final String DEFAULT_TEST_CLASSES_PATH =
		"./target/test-classes";
	public static final String DEFAULT_MODULE_NAME = "tests.jar";
	public static final boolean DEFAULT_HAS_BEANS = true;

	public static PomEquippedResolveStage resolvePom(String pathToPomFile) {
		PomEquippedResolveStage retVal =
			Maven.resolver().loadPomFromFile(pathToPomFile);
		return retVal;
	}

	public static File[] resolveDependencies(PomEquippedResolveStage pom) {
		File[] retVal =
			pom.importDependencies(ScopeType.COMPILE).resolve()
					.withTransitivity().asFile();
		return retVal;
	}

	public static JavaArchive createJAR(PomEquippedResolveStage pom,
			String CURRENT_ARTEFACT_ID, String jarFileName,
			String pathToClasses, boolean hasBeans) {
		File jarFile =
			pom.resolve(CURRENT_ARTEFACT_ID).withoutTransitivity()
					.asSingleFile();
		JavaArchive jar =
			ShrinkWrap.create(ZipImporter.class, DEFAULT_MODULE_NAME)
					.importFrom(jarFile).as(JavaArchive.class);
		if (hasBeans) {
			jar.addAsManifestResource(EmptyAsset.INSTANCE,
					ArchivePaths.create("beans.xml"));
		}
		jar.as(ExplodedImporter.class).importDirectory(
				(new File(DEFAULT_TEST_CLASSES_PATH)));
		return jar;
	}

	public static Archive<?> createEAR(JavaArchive tests,
			File[] libs, boolean testsAsEjbModule) {
		EnterpriseArchive ear2 = ShrinkWrap.create(EnterpriseArchive.class);
		ear2.addAsLibraries(libs);
		if (testsAsEjbModule) {
			ear2.addAsModule(tests);
		} else {
			ear2.addAsLibrary(tests);
		}
		return ear2;

	}

	public static Archive<?> createEAR(String CURRENT_ARTEFACT_ID, boolean testsAsEjbModule) {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);
		JavaArchive tests = createJAR(pom, CURRENT_ARTEFACT_ID, DEFAULT_MODULE_NAME, DEFAULT_TEST_CLASSES_PATH, DEFAULT_HAS_BEANS);
		Archive<?> retVal = createEAR(tests,libs,testsAsEjbModule);
		return retVal;
//		
//
//		// get project dependencies
//		File[] libs =
//			pom.importDependencies(ScopeType.COMPILE).resolve()
//					.withTransitivity().asFile();
//
//		// get current project
//		File jarFile =
//			pom.resolve(CURRENT_ARTEFACT_ID).withoutTransitivity()
//					.asSingleFile();
//		JavaArchive jar =
//			ShrinkWrap.create(ZipImporter.class, DEFAULT_MODULE_NAME)
//					.importFrom(jarFile).as(JavaArchive.class);
//		jar.addAsManifestResource(EmptyAsset.INSTANCE,
//				ArchivePaths.create("beans.xml"));
//		jar.as(ExplodedImporter.class).importDirectory(
//				(new File(DEFAULT_TEST_CLASSES_PATH)));
//
//		// add test classes to archive
//		EnterpriseArchive ear2 = ShrinkWrap.create(EnterpriseArchive.class);
//		ear2.addAsLibraries(libs);
//		ear2.addAsLibrary(jar);
//		return ear2;
	}

}
