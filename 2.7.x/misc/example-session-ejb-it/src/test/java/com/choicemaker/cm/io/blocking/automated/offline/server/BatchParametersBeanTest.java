package com.choicemaker.cm.io.blocking.automated.offline.server;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.Random;

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

@RunWith(Arquillian.class)
public class BatchParametersBeanTest {
	
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

	public final int MAX_TEST_ITERATIONS = 10;

	final protected Random random = new Random(new Date().getTime());
	
	protected float getRandomThreshold() {
		return random.nextFloat();
	}

	@EJB
	protected BatchParametersController controller;

	@Test
	public void testBatchParametersController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		BatchParametersBean params = new BatchParametersBean();
		assertTrue(0 == params.getId());
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a params
		BatchParametersBean params = new BatchParametersBean();
		assertTrue(params.getId() == 0);

		// Save the params
		controller.save(params);
		assertTrue(params.getId() != 0);

		// Find the params
		BatchParametersBean batchParameters2 = controller.find(params.getId());
		assertTrue(params.getId() == batchParameters2.getId());
		assertTrue(params.equals(batchParameters2));

		// Delete the params
		controller.delete(batchParameters2);
		BatchParametersBean batchParameters3 = controller.find(params.getId());
		assertTrue(batchParameters3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		BatchParametersBean params = new BatchParametersBean();
		controller.save(params);
		assertTrue(params.getId() != 0);
		final long id = params.getId();
		controller.detach(params);

		assertTrue(params.getHighThreshold() == 0f);
		final float highThreshold = getRandomThreshold();
		params.setHighThreshold(highThreshold);
		controller.save(params);

		params = null;
		BatchParametersBean batchParameters2 = controller.find(id);
		assertTrue(id == batchParameters2.getId());
		assertTrue(highThreshold == batchParameters2.getHighThreshold());
		controller.delete(batchParameters2);

		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testEqualsHashCode() {
		// Create two generic parameter sets and verify equality
		BatchParametersBean params1 = new BatchParametersBean();
		BatchParametersBean params2 = new BatchParametersBean();
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());
		
		// Change something on one of the parameter sets and verify inequality
		params1.setLowThreshold(getRandomThreshold());
		assertTrue(params1.getLowThreshold() != params2.getLowThreshold());
		assertTrue(!params1.equals(params2));
		assertTrue(params1.hashCode() != params2.hashCode());
		
		// Restore equality
		params2.setLowThreshold(params1.getLowThreshold());
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());
		
		// Verify non-persistent parameters is not equal to persistent parameters
		params1 = controller.save(params1);
		assertTrue(!params1.equals(params2));
		assertTrue(params1.hashCode() != params2.hashCode());
		
		// Verify that equality of persisted parameter sets is set only by persistence id
		controller.detach(params1);
		params2 = controller.find(params1.getId());
		controller.detach(params2);
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());
		
		params1.setLowThreshold(getRandomThreshold());
		assertTrue(params1.getLowThreshold() != params2.getLowThreshold());
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());
	}

//	@Test
//	public void testGetId() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetStageModel() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetMasterModel() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetMaxSingle() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLowThreshold() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetHighThreshold() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetStageRs() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetMasterRs() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testEqualsObject() {
//		fail("Not yet implemented");
//	}

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
		retVal.addClass(BatchParametersBeanTest.class);
		retVal.addClass(BatchParametersController.class);

		// Print the JAR contents
		System.out.println();
		System.out.println("EJB JAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	public static File[] createTestDependencies() {

		// Break out steps for easier debugging
		File f0 = new File(DEPENDENCIES_POM);
		assertTrue(f0.exists());
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(DEPENDENCIES_POM);
		pom = pom.importDependencies(ScopeType.COMPILE);
		MavenStrategyStage mss = pom.resolve();
		assertTrue(mss != null);
		MavenFormatStage mfs = mss.withTransitivity();
		assertTrue(mfs != null);

		File[] retVal = mfs.asFile();

		// Print the dependencies
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

		// Create the EAR
		EnterpriseArchive retVal = ShrinkWrap.create(EnterpriseArchive.class);

		// Create and add the EJB
		JavaArchive ejb1 = createEjbJar();
		retVal.addAsModule(ejb1);
		// retVal.addAsLibrary(ejb1);

		// Add the EJB dependencies
		try {
			File[] deps = createTestDependencies();
			retVal.addAsLibraries(deps);
		} catch (Exception x) {
			String msg =
				"WARNING: failed to add test dependencies: " + x.toString();
			System.out.println(msg);
		}

		// Print the EAR contents
		System.out.println();
		System.out.println("Deployment EAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

}
