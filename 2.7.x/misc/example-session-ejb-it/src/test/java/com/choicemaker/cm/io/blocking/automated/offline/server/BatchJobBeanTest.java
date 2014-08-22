package com.choicemaker.cm.io.blocking.automated.offline.server;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
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
import org.junit.Test;
import org.junit.runner.RunWith;

//import com.choicemaker.cm.io.blocking.automated.offline.server.BatchJobBean.NamedQuery;

/*
 import javax.ejb.EJB;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.junit.Assert;
 */

@RunWith(Arquillian.class)
public class BatchJobBeanTest {

	private static final String MAVEN_COORDINATE_SEPARATOR = ":";

	public static final String PROJECT_POM = "pom.xml";

	public static final String DEPENDENCIES_POM =
		"src/test/dependencies/dependency-pom.xml";

	public static final String PERSISTENCE_CONFIGURATION =
		"src/test/resources/jboss/sqlserver/persistence.xml";

	public static final String EJB_MAVEN_GROUPID = "com.choicemaker.cm";

	public static final String EJB_MAVEN_ARTIFACTID =
		"com.choicemaker.cm.io.blocking.automated.offline.server";

	public static final String EJB_MAVEN_VERSION = "2.7.1-SNAPSHOT";

	public static final String EJB_MAVEN_COORDINATES = new StringBuilder()
			.append(EJB_MAVEN_GROUPID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_ARTIFACTID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_VERSION).toString();

	@EJB
	BatchJobController batchJobController;

	@Test
	public void testEntityManager() {
		assertTrue(batchJobController != null);
	}

	@Test
	public void testCreate() {
		@SuppressWarnings("unused")
		BatchJobBean batchJob = new BatchJobBean();
	}

	@Test
	public void testPersistFindRemove() {
		BatchJobBean batchJob = new BatchJobBean();
		assertTrue(batchJob.getId() == 0);
		batchJobController.save(batchJob);
		assertTrue(batchJob.getId() != 0);
		BatchJobBean batchJob2 = batchJobController.find(batchJob.getId());
		assertTrue(batchJob.getId() == batchJob2.getId());
		assertTrue(batchJob.equals(batchJob2));
		batchJobController.delete(batchJob2);
	}

	@Test
	public void testMerge() {
		BatchJobBean batchJob = new BatchJobBean();
		batchJobController.save(batchJob);
		assertTrue(batchJob.getId() != 0);
		final long id = batchJob.getId();
		assertTrue(batchJob.getExternalId() == null);
		final String externalId = "external test id";
		batchJob.setExternalId(externalId);
		batchJobController.save(batchJob);

		batchJob = null;
		BatchJobBean batchJob2 = batchJobController.find(id);
		assertTrue(id == batchJob2.getId());
		assertTrue(externalId.equals(batchJob2.getExternalId()));
		batchJobController.delete(batchJob2);
	}

	@Test
	public void testFindAll() {
		BatchJobBean batchJob = new BatchJobBean();
		assertTrue(batchJob.getId() == 0);
		batchJobController.save(batchJob);
		List<BatchJobBean> results = batchJobController.findAll();
		assertTrue(results != null);
		assertTrue(results.size() > 0);
		int size = results.size();
		batchJobController.delete(batchJob);
		results = batchJobController.findAll();
		assertTrue(results != null);
		assertTrue(results.size() == size - 1);
	}

	public static JavaArchive createEjbJar() {
		// Create a copy of the EJB jar
		// PomEquippedResolveStage pom =
		// Maven.resolver().loadPomFromFile(PROJECT_POM);
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

		// Add persistence configuration
		File f = new File(PERSISTENCE_CONFIGURATION);
		assertTrue(f.exists());
		FileAsset fileAsset = new FileAsset(f);
		retVal.addAsManifestResource(fileAsset, "persistence.xml");

		// Add test classes to JAR
		retVal.addClass(BatchJobBeanTest.class);
		retVal.addClass(BatchJobController.class);

		// Print the JAR contents
		System.out.println();
		System.out.println("EJB JAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	public static File[] createTestDependencies() {
//		Set<File> files = new HashSet<>();
		/*
		 * MavenDependencyResolver resolver = DependencyResolvers
		 * .use(MavenDependencyResolver.class)
		 * .loadMetadataFromPom(DEPENDENCIES_POM);
		 * 
		 * WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
		 * .addAsLibraries
		 * (resolver.artifact("com.google.guava:guava:11.0.2").resolveAsFiles())
		 * .addAsWebResource(EmptyAsset.INSTANCE, "beans.xml"); // verify that
		 * the JAR files ended up in the WAR
		 * System.out.println(war.toString(true));
		 */

		File f0 = new File(DEPENDENCIES_POM);
		assertTrue(f0.exists());
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(DEPENDENCIES_POM);
		pom = pom.importDependencies(ScopeType.COMPILE);
		MavenStrategyStage mss = pom.resolve();
		MavenFormatStage mfs = mss.withTransitivity();

//		File[] directDependencies = mfs.asFile();
//		System.out.println();
//		System.out.println("Direct dependencies:");
//		for (File f : directDependencies) {
//			if (!f.getName().contains(EJB_MAVEN_ARTIFACTID)) {
//				System.out.println(f.getAbsolutePath());
//				files.add(f);
//			} else {
//				System.out.println("Skipping: " + f.getName());
//			}
//		}
		File[] retVal = mfs.asFile();

		System.out.println();
		System.out.println("Test dependencies:");
		for (File f : retVal) {
			System.out.println(f.getAbsolutePath());
		}
		System.out.println();

		return retVal;
	}

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		JavaArchive ejb1 = createEjbJar();
		// create the EAR
		EnterpriseArchive retVal = ShrinkWrap.create(EnterpriseArchive.class);
		retVal.addAsModule(ejb1);
//		retVal.addAsLibrary(ejb1);
		try {
			File[] deps = createTestDependencies();
			retVal.addAsLibraries(deps);
		} catch (Exception x) {
			String msg =
				"WARNING: failed to add test dependencies: " + x.toString();
			System.out.println(msg);
		}
		System.out.println();
		System.out.println("Deployment EAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

}
