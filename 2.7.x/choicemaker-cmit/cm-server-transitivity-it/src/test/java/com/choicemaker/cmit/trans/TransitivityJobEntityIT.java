package com.choicemaker.cmit.trans;

import static com.choicemaker.cm.batch.BatchJob.STATUS_NEW;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.impl.TransitivityJobControllerBean;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.trans.util.TransitivityTestController;
import com.choicemaker.cmit.utils.BatchJobUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class TransitivityJobEntityIT {

	private static final Logger logger = Logger
			.getLogger(TransitivityJobEntityIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = true;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public static final int MAX_SINGLE_LIMIT = 1000;

	public static final int MAX_TEST_ITERATIONS = 10;

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	private OabaJobControllerBean oabaController;

	@EJB
	protected TransitivityJobControllerBean transController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	protected TransitivityTestController testController;

	private int initialOabaJobCount;
	private int initialOabaParamsCount;
	private int initialTransitivityJobCount;
	private int initialTransitivityParamsCount;

	@Before
	public void setUp() {
		initialOabaJobCount = testController.findAllOabaJobs().size();
		initialOabaParamsCount = testController.findAllOabaParameters().size();
		initialTransitivityJobCount =
			testController.findAllTransitivityJobs().size();
		initialTransitivityParamsCount =
			testController.findAllTransitivityParameters().size();
	}

	@After
	public void tearDown() {
		int finalOabaJobCount = testController.findAllOabaJobs().size();
		assertTrue(initialOabaJobCount == finalOabaJobCount);

		int finalOabaParamsCount =
			testController.findAllOabaParameters().size();
		assertTrue(initialOabaParamsCount == finalOabaParamsCount);

		int finalTransitivityParamsCount =
			testController.findAllTransitivityParameters().size();
		assertTrue(initialTransitivityParamsCount == finalTransitivityParamsCount);

		int finalTransitivityJobCount =
			testController.findAllTransitivityJobs().size();
		assertTrue(initialTransitivityJobCount == finalTransitivityJobCount);
	}

	@Test
	public void testPrerequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(oabaController != null);
		assertTrue(transController != null);
		assertTrue(serverController != null);
		assertTrue(testController != null);
	}

	@Test
	public void testConstruction() {
//		final String METHOD = "testConstruction";
//		final TestEntities te = new TestEntities();
//
//		OabaJob oabaJob = createEphemeralOabaJob(te, METHOD, true);
//		final Date now = new Date();
//		TransitivityJob job = createEphemeralTransitivityJob(te, METHOD, true);
//		final Date now2 = new Date();
//
//		assertTrue(0 == job.getId());
//		assertTrue(job.getBatchParentId() == oabaJob.getId());
//
//		assertTrue(STATUS_NEW.equals(job.getStatus()));
//
//		Date d = job.getRequested();
//		assertTrue(d != null);
//		assertTrue(now.compareTo(d) <= 0);
//		assertTrue(d.compareTo(now2) <= 0);
//
//		Date d2 = job.getTimeStamp(STATUS_NEW);
//		assertTrue(d.equals(d2));
//
//		try {
//			te.removePersistentObjects(em, utx);
//		} catch (Exception x) {
//			logger.severe(x.toString());
//			fail(x.toString());
//		}
	}

	@Test
	public void testPersistFindRemove() {
//		final String METHOD = "testPersistFindRemove";
//		final TestEntities te = new TestEntities();
//
//		// Create a job
//		TransitivityJob job = createEphemeralTransitivityJob(te, METHOD, true);
//		assertTrue(job.getId() == 0);
//
//		// Save the job
//		transController.save(job);
//		assertTrue(job.getId() != 0);
//
//		// Find the job
//		TransitivityJob transJob2 =
//			transController.findTransitivityJob(job.getId());
//		assertTrue(job.getId() == transJob2.getId());
//		assertTrue(job.equals(transJob2));
//
//		// Delete the job
//		transController.delete(transJob2);
//		TransitivityJob batchJob3 =
//			transController.findTransitivityJob(job.getId());
//		assertTrue(batchJob3 == null);
//
//		try {
//			te.removePersistentObjects(em, utx);
//		} catch (Exception x) {
//			logger.severe(x.toString());
//			fail(x.toString());
//		}
	}

	@Test
	public void testFindAll() {
//		final String METHOD = "testFindAll";
//		final TestEntities te = new TestEntities();
//
//		List<Long> jobIds = new LinkedList<>();
//		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
//			// Create and save a job
//			TransitivityJob job =
//				createEphemeralTransitivityJob(te, METHOD, true);
//			transController.save(job);
//			long id = job.getId();
//			assertTrue(!jobIds.contains(id));
//			jobIds.add(id);
//		}
//
//		// Verify the number of jobs has increased
//		List<TransitivityJob> jobs = transController.findAllTransitivityJobs();
//		assertTrue(jobs != null);
//
//		// Find the jobs
//		boolean isFound = false;
//		for (long jobId : jobIds) {
//			for (TransitivityJob job : jobs) {
//				if (jobId == job.getId()) {
//					isFound = true;
//					break;
//				}
//			}
//			assertTrue(isFound);
//		}
//
//		try {
//			te.removePersistentObjects(em, utx);
//		} catch (Exception x) {
//			logger.severe(x.toString());
//			fail(x.toString());
//		}
	}

	protected TransitivityJob createEphemeralTransitivityJob(TestEntities te,
			String tag, boolean isTag) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		OabaJob oabaJob = createEphemeralOabaJob(te, tag, isTag);
		return BatchJobUtils.createEphemeralTransitivityJob(MAX_SINGLE_LIMIT,
				utx, sc, em, te, oabaJob, tag, isTag);
	}

	protected OabaJob createEphemeralOabaJob(TestEntities te, String tag,
			boolean isTag) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		return BatchJobUtils.createEphemeralOabaJob(MAX_SINGLE_LIMIT, utx, sc,
				em, te, tag, isTag);
	}

	protected ServerConfiguration getDefaultServerConfiguration() {
		return BatchJobUtils.getDefaultServerConfiguration(serverController);
	}

}
