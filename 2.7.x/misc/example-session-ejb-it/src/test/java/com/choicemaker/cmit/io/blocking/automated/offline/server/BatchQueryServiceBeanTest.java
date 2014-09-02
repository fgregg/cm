package com.choicemaker.cmit.io.blocking.automated.offline.server;

import static com.choicemaker.cmit.io.blocking.automated.offline.server.BatchDeploymentUtils.DEPENDENCIES_POM;
import static com.choicemaker.cmit.io.blocking.automated.offline.server.BatchDeploymentUtils.EJB_MAVEN_COORDINATES;
import static com.choicemaker.cmit.utils.DeploymentUtils.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.PROJECT_POM;
import static com.choicemaker.cmit.io.blocking.automated.offline.server.RandomStartOabaParameters.PARAMETER_OPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.StatusLog;
import com.choicemaker.cmit.utils.DeploymentUtils;

@RunWith(Arquillian.class)
public class BatchQueryServiceBeanTest {

	public static final int TIME_OUT_MILLISECS = 5000;

	private static RandomStartOabaParameters currentParams;

	static RandomStartOabaParameters getCurrentParams() {
		return currentParams;
	}

	static void setCurrentParams(RandomStartOabaParameters currentParams) {
		BatchQueryServiceBeanTest.currentParams = currentParams;
	}

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(BatchQueryServiceBeanTest.class);
		testClasses.add(BatchJobController.class);
		testClasses.add(BatchParametersController.class);
		testClasses.add(StatusLogController.class);
		testClasses.add(BatchQueryServiceTestExtension.class);
		testClasses.add(RandomStartOabaParameters.class);
		testClasses.add(VerifyMessageData.class);
		JavaArchive ejb1 =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, PERSISTENCE_CONFIGURATION);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb1, deps);
		return retVal;
	}

	@EJB
	protected BatchQueryServiceTestExtension batchQueryService;
	@EJB
	protected BatchJobController jobController;
	@EJB
	protected BatchParametersController prmController;
	@EJB
	protected StatusLogController stsController;

	@Resource(lookup = "java:/queue/test")
	protected Queue testQueue;

	@Inject
	protected JMSContext testJmsContext;

	public int emptyQueue() {
		JMSConsumer consumer = testJmsContext.createConsumer(testQueue);
		int count = 0;
		Message m = consumer.receive(1000);
		while (m != null) {
			++count;
			m = consumer.receive(1000);
		}
		return count;
	}

	public void checkQueue() {

		// Cache and then reset the current parameters
		if (getCurrentParams() == null) {
			emptyQueue();
			fail("Current params are not set");
		}
		final long batchJobId = getCurrentParams().getJobID();
		final RandomStartOabaParameters sop = getCurrentParams();
		setCurrentParams(null);

		// Create a verifier
		VerifyMessageData verifier =
			new VerifyMessageData(batchJobId, sop.externalID, sop.staging,
					sop.master, sop.lowThreshold, sop.highThreshold,
					sop.stageModelName, sop.masterModelName, sop.maxSingle,
					sop.runTransitivity);

		// Retrieve and verify a message from the test testQueue
		JMSConsumer consumer = testJmsContext.createConsumer(testQueue);
		Message m = consumer.receive(TIME_OUT_MILLISECS);
		if (m == null) {
			fail("No test message received after " + TIME_OUT_MILLISECS
					+ " milliseconds");
		} else {
			verifier.assertExpectedMessage(m);
		}

		// Check that no more messages remain on the queue
		int count = emptyQueue();
		assertTrue(0 == count);
	}

	public void checkPersistence(final long batchJobId,
			final RandomStartOabaParameters sop) {
		assertTrue(batchJobId != 0);
		assertTrue(sop != null);

		BatchJob batchJob = jobController.find(batchJobId);
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

		setCurrentParams(sop);
		getCurrentParams().setJobID(batchJob.getId());
	}

	@Test
	@InSequence(1)
	public void testStartOABAStage() {
		emptyQueue();
		setCurrentParams(null);
		
		final RandomStartOabaParameters sop =
			new RandomStartOabaParameters(PARAMETER_OPTION.OPTION_6, new Date());
		long batchJobId = 0;
		try {
			batchJobId =
				batchQueryService.startOABAStage(sop.externalID, sop.staging,
						sop.lowThreshold, sop.highThreshold,
						sop.stageModelName, sop.maxSingle);
		} catch (JMSException e) {
			fail(e.toString());
		}

		checkPersistence(batchJobId, sop);
		assertTrue(getCurrentParams() != null);
		assertTrue(getCurrentParams().getJobID() != 0);
	}

	@Test
	@InSequence(2)
	public void checkStartOABAStage() {
		checkQueue();
	}

	@Test
	@InSequence(3)
	public void testStartOABA_8_Params() {
		emptyQueue();
		setCurrentParams(null);
		
		final RandomStartOabaParameters sop =
			new RandomStartOabaParameters(PARAMETER_OPTION.OPTION_8, new Date());
		long batchJobId = 0;
		try {
			batchJobId =
				batchQueryService.startOABA(sop.externalID, sop.staging,
						sop.master, sop.lowThreshold, sop.highThreshold,
						sop.stageModelName, sop.masterModelName, sop.maxSingle);
		} catch (JMSException e) {
			fail(e.toString());
		}

		checkPersistence(batchJobId, sop);
		assertTrue(getCurrentParams() != null);
		assertTrue(getCurrentParams().getJobID() != 0);
	}

	@Test
	@InSequence(4)
	public void checkStartOABA_8_Params() {
		checkQueue();
	}

	@Test
	@InSequence(5)
	public void testStartOABA_9_Params() {
		emptyQueue();
		setCurrentParams(null);
		
		assertTrue(getCurrentParams() == null);
		final RandomStartOabaParameters sop =
			new RandomStartOabaParameters(PARAMETER_OPTION.OPTION_9, new Date());
		long batchJobId = 0;
		try {
			batchJobId =
				batchQueryService.startOABA(sop.externalID, sop.staging,
						sop.master, sop.lowThreshold, sop.highThreshold,
						sop.stageModelName, sop.masterModelName, sop.maxSingle,
						sop.runTransitivity);
			assertTrue(batchJobId != 0);
		} catch (JMSException e) {
			fail(e.toString());
		}

		checkPersistence(batchJobId, sop);
		assertTrue(getCurrentParams() != null);
		assertTrue(getCurrentParams().getJobID() != 0);
	}

	@Test
	@InSequence(6)
	public void checkStartOABA_9_Params() {
		checkQueue();
	}

	@Test
	@InSequence(7)
	public void testStartOABA_10_Params() {
		emptyQueue();
		setCurrentParams(null);
		
		assertTrue(getCurrentParams() == null);
		final RandomStartOabaParameters sop =
			new RandomStartOabaParameters(PARAMETER_OPTION.OPTION_10, new Date());
		long batchJobId = 0;
		try {
			batchJobId =
				batchQueryService
						.startOABA(sop.externalID, sop.transactionId,
								sop.staging, sop.master, sop.lowThreshold,
								sop.highThreshold, sop.stageModelName,
								sop.masterModelName, sop.maxSingle,
								sop.runTransitivity);
		} catch (JMSException e) {
			fail(e.toString());
		}

		checkPersistence(batchJobId, sop);
		assertTrue(getCurrentParams() != null);
		assertTrue(getCurrentParams().getJobID() != 0);
	}

	@Test
	@InSequence(8)
	public void checkStartOABA_10_Paramsa() {
		checkQueue();
	}

	// @Test
	// public void testAbortJob() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSuspendJob() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetStatus() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testCheckStatus() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testRemoveDir() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testResumeJob() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMatchList() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMatchRecordSource() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testEjbCreate() {
	// fail("Not yet implemented");
	// }

}
