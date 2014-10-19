package com.choicemaker.cmit.oaba;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class AbstractJeeTestClass {

	public static final String CURRENT_ARTEFACT_ID =
		"com.choicemaker.cmit:cmit-server-oaba-it:2.7.1-SNAPSHOT";
	public static final String TEST_CLASSES_PATH = "./target/test-classes";
	public static final String MODULE_NAME = "ejb-module.jar";

	@Deployment
	public static Archive<?> getEarArchive() {
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile("pom.xml");
		// get project dependencies
		File[] libs =
			pom.importDependencies(ScopeType.COMPILE).resolve()
					.withTransitivity().asFile();
		// get current project
		File module =
			pom.resolve(CURRENT_ARTEFACT_ID).withoutTransitivity()
					.asSingleFile();
//		EnterpriseArchive ear =
//				ShrinkWrap.create(EnterpriseArchive.class).addAsLibraries(libs)
//						.addAsModule(module, MODULE_NAME);
		EnterpriseArchive ear =
			ShrinkWrap.create(EnterpriseArchive.class).addAsLibraries(libs)
					.addAsLibrary(module, MODULE_NAME);
		// add test classes to archive
		ear.getAsType(JavaArchive.class, "lib/" + MODULE_NAME)
				.addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
				.as(ExplodedImporter.class)
				.importDirectory((new File(TEST_CLASSES_PATH)));
		return ear;
	}
}
