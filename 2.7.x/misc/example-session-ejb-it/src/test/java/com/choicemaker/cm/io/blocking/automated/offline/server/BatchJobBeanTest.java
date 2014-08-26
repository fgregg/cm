package com.choicemaker.cm.io.blocking.automated.offline.server;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;
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

import com.choicemaker.cm.io.blocking.automated.offline.server.BatchJobBean.STATUS;

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
	BatchJobController controller;

	@Test
	public void testBatchJobController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		Date now = new Date();
		BatchJobBean job = new BatchJobBean();
		Date now2 = new Date();
		
		assertTrue(0 == job.getId());

		assertTrue(STATUS.NEW.equals(job.getStatus()));

		Date d = job.getRequested();
		assertTrue(d != null);
		assertTrue(now.compareTo(d) <= 0);
		assertTrue(d.compareTo(now2) <= 0);

		Date d2 = job.getTimeStamp(STATUS.NEW);
		assertTrue(d.equals(d2));
		
		Date d3 = job.getUpdated();
		assertTrue(d.equals(d3));
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job
		BatchJobBean job = new BatchJobBean();
		assertTrue(job.getId() == 0);

		// Save the job
		controller.save(job);
		assertTrue(job.getId() != 0);
		
		// Find the job
		BatchJobBean batchJob2 = controller.find(job.getId());
		assertTrue(job.getId() == batchJob2.getId());
		assertTrue(job.equals(batchJob2));
		
		// Delete the job
		controller.delete(batchJob2);
		BatchJobBean batchJob3 = controller.find(job.getId());
		assertTrue(batchJob3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		BatchJobBean job = new BatchJobBean();
		controller.save(job);
		assertTrue(job.getId() != 0);
		final long id = job.getId();
		assertTrue(job.getExternalId() == null);
		final String externalId = "external test id";
		job.setExternalId(externalId);
		controller.save(job);

		job = null;
		BatchJobBean batchJob2 = controller.find(id);
		assertTrue(id == batchJob2.getId());
		assertTrue(externalId.equals(batchJob2.getExternalId()));
		controller.delete(batchJob2);
		
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testFindAll() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create and save a job
		BatchJobBean job = new BatchJobBean();
		assertTrue(job.getId() == 0);
		controller.save(job);
		final long id = job.getId();
		assertTrue(id != 0);
		
		// Verify the number of jobs has increased
		List<BatchJobBean> results = controller.findAll();
		assertTrue(results != null);
		assertTrue(initialCount + 1 == results.size());

		// Find the job
		boolean isFound = false;
		for (BatchJobBean j : results) {
			if (id == j.getId()) {
				isFound = true;
				break;
			}
		}
		assertTrue(isFound);
		
		// Remove the job
		controller.delete(job);
		results = controller.findAll();
		assertTrue(results != null);
		assertTrue(initialCount == results.size());

		// Verify the job is removed
		isFound = false;
		for (BatchJobBean j : results) {
			if (id == j.getId()) {
				isFound = true;
				break;
			}
		}
		assertTrue(!isFound);
	}

	@Test
	public void testNoNullStatusTimestamp() {
		int countNullStatus = 0;
		int countNullTimestamp = 0;
		int count = 0;
		for (BatchJobBean job : controller.findAll()) {
			++count;
			STATUS status = job.getStatus();
			if (status == null) {
				++countNullStatus;
			}
			Date ts = job.getTimeStamp(status);
			if ( ts == null) {
				++countNullTimestamp;
			}
		}
		if ((countNullStatus) != 0) {
			fail("Null status: " + countNullStatus + " out of " + count);
		}
		if ((countNullTimestamp) != 0) {
			fail("Null timestamp: " + countNullTimestamp + " out of " + count);
		}
	}
	
//	@Test
//	public void testMarkAsQueued() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdatePercentageCompletedInt() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testShouldStop() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testRequested() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testQueued() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testStarted() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdated() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCompleted() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testFailed() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAbortRequested() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAborted() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testExternalId() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testTransactionId() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testType() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testDescription() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job and set a value
		BatchJobBean job = new BatchJobBean();
		final String v1 = new Date().toString();
		job.setDescription(v1);
		
		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Get the job
		job = controller.find(id1);
		
		// Check the value
		final String v2 = job.getDescription();
		assertTrue(v1.equals(v2));
		
		// Remove the job and the number of remaining jobs
		controller.delete(job);
		assertTrue(initialCount == controller.findAll().size());
	}

//	@Test
//	public void testStatusAsString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPercentageComplete() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testHashCode() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testEqualsObject() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testStateMachine() {
//		// Count existing jobs
//		final int initialCount = controller.findAll().size();
//
//		fail("not yet implemented");
//	}
	
	public void testTimestamp(STATUS sts) {
		final Date now = new Date();
		BatchJobBean job = new BatchJobBean();
		job.setStatus(sts);
		final Date now2 = new Date();

		final STATUS status = job.getStatus();
		assert (sts.equals(status));
		
		final Date ts = job.getTimeStamp(sts);
		assertTrue(ts != null);
		assertTrue(now.compareTo(ts) <= 0);
		assertTrue(ts.compareTo(now2) <= 0);

		final long id = controller.save(job).getId();
		
		job = null;
		job = controller.find(id);
		assertTrue(status.equals(job.getStatus()));
		assertTrue(ts.equals(job.getTimeStamp(sts)));
		
		controller.delete(job);
	}
	
	@Test
	public void testTimestamps() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();
		
		for (STATUS sts : STATUS.values()) {
			testTimestamp(sts);
		}

		assertTrue(initialCount == controller.findAll().size());
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
