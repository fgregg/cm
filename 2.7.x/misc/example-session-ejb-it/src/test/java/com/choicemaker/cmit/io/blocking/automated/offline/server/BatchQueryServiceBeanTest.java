package com.choicemaker.cmit.io.blocking.automated.offline.server;

import static com.choicemaker.cmit.io.blocking.automated.offline.server.BatchDeploymentUtils.DEPENDENCIES_POM;
import static com.choicemaker.cmit.io.blocking.automated.offline.server.BatchDeploymentUtils.EJB_MAVEN_COORDINATES;
import static com.choicemaker.cmit.utils.DeploymentUtils.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.PROJECT_POM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.BatchQueryServiceBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.StatusLog;
import com.choicemaker.cmit.utils.DeploymentUtils;

@RunWith(Arquillian.class)
public class BatchQueryServiceBeanTest {

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(BatchQueryServiceBeanTest.class);
		testClasses.add(BatchJobController.class);
		testClasses.add(BatchParametersController.class);
		testClasses.add(StatusLogController.class);
		JavaArchive ejb1 =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, PERSISTENCE_CONFIGURATION);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb1, deps);
		return retVal;
	}

	private static class StartOabaParameters {
		final String externalID;
		final long transactionId;
		final SerialRecordSource staging;
		final SerialRecordSource master;
		final float lowThreshold;
		final float highThreshold;
		final String stageModelName;
		final String masterModelName;
		final int maxSingle;
		final boolean runTransitivity;

		StartOabaParameters(Object o) {
			this.externalID = "EXT_ID: " + o.toString();
			this.transactionId = random.nextLong();
			this.staging = null;
			this.master = null;
			this.lowThreshold = getRandomThreshold();
			this.highThreshold = getRandomThreshold(lowThreshold);
			this.stageModelName = "STAGE: " + o.toString();
			this.masterModelName = "MASTER: " + o.toString();
			this.maxSingle = random.nextInt(Integer.MAX_VALUE);
			this.runTransitivity = random.nextBoolean();
		}

	};

	private static Random random = new Random(new Date().getTime());

	private static float getRandomThreshold() {
		return random.nextFloat();
	}

	private static float getRandomThreshold(float lowerBound) {
		if (lowerBound < 0 || lowerBound >= 1) {
			throw new IllegalArgumentException("Illegal lower bound: "
					+ lowerBound);
		}
		float range = 1f - lowerBound;
		float f = random.nextFloat();
		float offset = range * f;
		float retVal = lowerBound + offset;
		return retVal;
	}

	@EJB
	protected BatchQueryServiceBean batchQueryService;
	@EJB
	protected BatchJobController jobController;
	@EJB
	protected BatchParametersController prmController;
	@EJB
	protected StatusLogController stsController;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartOABAStage() {
		final StartOabaParameters sop = new StartOabaParameters(new Date());
		BatchJob batchJob = null;
		try {
			final long batchJobId =
				batchQueryService.startOABAStage(sop.externalID, sop.staging,
						sop.lowThreshold, sop.highThreshold,
						sop.stageModelName, sop.maxSingle);
			assertTrue(batchJobId != 0);
			batchJob = jobController.find(batchJobId);
		} catch (RemoteException | CreateException | NamingException
				| JMSException | SQLException e) {
			fail(e.toString());
		}

		assertTrue(batchJob != null);
		assertEquals(sop.externalID, batchJob.getExternalId());
		assertEquals(0L, batchJob.getTransactionId());

		BatchParameters params = prmController.find(batchJob.getId());
		assertTrue(params != null);
		assertEquals(sop.staging, params.getStageRs());
		assertEquals(null, params.getMasterRs());
		assertEquals(sop.lowThreshold, params.getLowThreshold(),
				5 * Math.ulp(sop.lowThreshold));
		assertEquals(sop.highThreshold, params.getHighThreshold(),
				5 * Math.ulp(sop.highThreshold));
		assertEquals(sop.stageModelName, params.getStageModel());
		assertEquals(null, params.getMasterModel());
		assertEquals(sop.maxSingle, params.getMaxSingle());

		StatusLog statusLog = stsController.find(batchJob.getId());
		assertTrue(statusLog != null);

		// FIXME test runTransitivity field of StartData
	}

	@Test
	public void testStartOABA_8() {
		final StartOabaParameters sop = new StartOabaParameters(new Date());
		BatchJob batchJob = null;
		try {
			final long batchJobId =
				batchQueryService.startOABA(sop.externalID, sop.staging,
						sop.master, sop.lowThreshold, sop.highThreshold,
						sop.stageModelName, null, sop.maxSingle);
			assertTrue(batchJobId != 0);
			batchJob = jobController.find(batchJobId);
		} catch (RemoteException | CreateException | NamingException
				| JMSException | SQLException e) {
			fail(e.toString());
		}

		assertTrue(batchJob != null);
		assertEquals(sop.externalID, batchJob.getExternalId());
		assertEquals(0L, batchJob.getTransactionId());

		BatchParameters params = prmController.find(batchJob.getId());
		assertTrue(params != null);
		assertEquals(sop.staging, params.getStageRs());
		assertEquals(sop.master, params.getMasterRs());
		assertEquals(sop.lowThreshold, params.getLowThreshold(),
				5 * Math.ulp(sop.lowThreshold));
		assertEquals(sop.highThreshold, params.getHighThreshold(),
				5 * Math.ulp(sop.highThreshold));
		assertEquals(sop.stageModelName, params.getStageModel());
		assertEquals(null, params.getMasterModel());
		assertEquals(sop.maxSingle, params.getMaxSingle());

		StatusLog statusLog = stsController.find(batchJob.getId());
		assertTrue(statusLog != null);

		// FIXME test runTransitivity field of StartData
	}

	@Test
	public void testStartOABA_9() {
		final StartOabaParameters sop = new StartOabaParameters(new Date());
		BatchJob batchJob = null;
		try {
			final long batchJobId =
				batchQueryService.startOABA(sop.externalID, sop.staging,
						sop.master, sop.lowThreshold, sop.highThreshold,
						sop.stageModelName, sop.masterModelName, sop.maxSingle,
						sop.runTransitivity);
			assertTrue(batchJobId != 0);
			batchJob = jobController.find(batchJobId);
		} catch (RemoteException | CreateException | NamingException
				| JMSException | SQLException e) {
			fail(e.toString());
		}

		assertTrue(batchJob != null);
		assertEquals(sop.externalID, batchJob.getExternalId());
		assertEquals(0L, batchJob.getTransactionId());

		BatchParameters params = prmController.find(batchJob.getId());
		assertTrue(params != null);
		assertEquals(sop.staging, params.getStageRs());
		assertEquals(sop.master, params.getMasterRs());
		assertEquals(sop.lowThreshold, params.getLowThreshold(),
				5 * Math.ulp(sop.lowThreshold));
		assertEquals(sop.highThreshold, params.getHighThreshold(),
				5 * Math.ulp(sop.highThreshold));
		assertEquals(sop.stageModelName, params.getStageModel());
		assertEquals(sop.masterModelName, params.getMasterModel());
		assertEquals(sop.maxSingle, params.getMaxSingle());

		StatusLog statusLog = stsController.find(batchJob.getId());
		assertTrue(statusLog != null);
		
		// FIXME test runTransitivity field of StartData
	}

	@Test
	public void testStartOABA_10() {
		final StartOabaParameters sop = new StartOabaParameters(new Date());
		BatchJob batchJob = null;
		try {
			final long batchJobId =
				batchQueryService
						.startOABA(sop.externalID, sop.transactionId,
								sop.staging, sop.master, sop.lowThreshold,
								sop.highThreshold, sop.stageModelName,
								sop.masterModelName, sop.maxSingle,
								sop.runTransitivity);
			assertTrue(batchJobId != 0);
			batchJob = jobController.find(batchJobId);
		} catch (RemoteException | CreateException | NamingException
				| JMSException | SQLException e) {
			fail(e.toString());
		}

		assertTrue(batchJob != null);
		assertEquals(sop.externalID, batchJob.getExternalId());
		assertEquals(sop.transactionId, batchJob.getTransactionId());

		BatchParameters params = prmController.find(batchJob.getId());
		assertTrue(params != null);
		assertEquals(sop.staging, params.getStageRs());
		assertEquals(sop.master, params.getMasterRs());
		assertEquals(sop.lowThreshold, params.getLowThreshold(),
				5 * Math.ulp(sop.lowThreshold));
		assertEquals(sop.highThreshold, params.getHighThreshold(),
				5 * Math.ulp(sop.highThreshold));
		assertEquals(sop.stageModelName, params.getStageModel());
		assertEquals(sop.masterModelName, params.getMasterModel());
		assertEquals(sop.maxSingle, params.getMaxSingle());

		StatusLog statusLog = stsController.find(batchJob.getId());
		assertTrue(statusLog != null);

		// FIXME test runTransitivity field of StartData
	}

//	@Test
//	public void testAbortJob() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSuspendJob() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCheckStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testRemoveDir() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testResumeJob() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetMatchList() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetMatchRecordSource() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testEjbCreate() {
//		fail("Not yet implemented");
//	}

}
