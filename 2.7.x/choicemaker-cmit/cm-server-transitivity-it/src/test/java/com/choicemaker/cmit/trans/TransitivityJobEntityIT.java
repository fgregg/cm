package com.choicemaker.cmit.trans;

import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.utils.BatchJobUtils;
import com.choicemaker.cmit.utils.TestEntityCounts;

@RunWith(Arquillian.class)
public class TransitivityJobEntityIT {

	private static final Logger logger = Logger
			.getLogger(TransitivityJobEntityIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = true;

	// private final static String LOG_SOURCE = TransitivityJobEntityIT.class
	// .getSimpleName();

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public static final int MAX_SINGLE_LIMIT = 1000;

	public static final int MAX_TEST_ITERATIONS = 10;

	@Resource
	private UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobController oabaController;

	@EJB
	private OabaJobController oabaJobController;

	@EJB
	protected TransitivityJobController transJobController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaProcessingController processingController;

	@EJB
	private OabaService oabaService;

	@EJB
	private OperationalPropertyController opPropController;

	@EJB
	private RecordIdController ridController;

	@EJB
	private RecordSourceController rsController;

	@EJB
	private ServerConfigurationController serverController;

	private TestEntityCounts te;

//	private final Random random = new Random(new Date().getTime());

	@Before
	public void setUp() throws Exception {
		te = new TestEntityCounts(logger, oabaController, paramsController,
				oabaSettingsController, serverController, processingController,
				opPropController, rsController, ridController);
	}

	public void checkCounts() {
		if (te != null) {
			te.checkCounts(logger, em, utx, oabaController, paramsController,
					oabaSettingsController, serverController,
					processingController, opPropController, rsController,
					ridController);
		} else {
			throw new Error("Counts not initialized");
		}
	}

	@Test
	public void testPrerequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(oabaController != null);
		assertTrue(transJobController != null);
		assertTrue(serverController != null);
	}

	@Test
	public void testConstruction() {
		// FIXME STUBBED
//		final String METHOD = "testConstruction";
//
//		OabaJob oabaJob = createEphemeralOabaJob(te, METHOD, true);
//		oabaController.save(oabaJob);
//		assertTrue(oabaJob.getId() != 0);
//		final Date now = new Date();
//		TransitivityJob job = createEphemeralTransitivityJob(te, METHOD, true);
//		final Date now2 = new Date();
//
//		assertTrue(0 == job.getId());
//		assertTrue(job.getBatchParentId() == oabaJob.getId());
//
//		assertTrue(BatchJobStatus.NEW.equals(job.getStatus()));
//
//		Date d = job.getRequested();
//		assertTrue(d != null);
//		assertTrue(now.compareTo(d) <= 0);
//		assertTrue(d.compareTo(now2) <= 0);
//
//		Date d2 = job.getTimeStamp(BatchJobStatus.NEW);
//		assertTrue(d.equals(d2));
//
//		checkCounts();
	}

	@Test
	public void testPersistFindRemove() {
		// FIXME STUBBED
		// final String METHOD = "testPersistFindRemove";
		//
		// // Create a job
		// TransitivityJob job = createEphemeralTransitivityJob(te, METHOD,
		// true);
		// assertTrue(job.getId() == 0);
		//
		// // Save the job
		// transJobController.save(job);
		// assertTrue(job.getId() != 0);
		//
		// // Find the job
		// TransitivityJob transJob2 =
		// transJobController.findTransitivityJob(job.getId());
		// assertTrue(job.getId() == transJob2.getId());
		// assertTrue(job.equals(transJob2));
		//
		// // Delete the job
		// transJobController.delete(transJob2);
		// TransitivityJob batchJob3 =
		// transJobController.findTransitivityJob(job.getId());
		// assertTrue(batchJob3 == null);
		//
		// checkCounts();
	}

	@Test
	public void testFindAll() {
		// FIXME STUBBED
		// final String METHOD = "testFindAll";
		//
		// List<Long> jobIds = new LinkedList<>();
		// for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
		// // Create and save a job
		// TransitivityJob job =
		// createEphemeralTransitivityJob(te, METHOD, true);
		// transJobController.save(job);
		// long id = job.getId();
		// assertTrue(!jobIds.contains(id));
		// jobIds.add(id);
		// }
		//
		// // Verify the number of jobs has increased
		// List<TransitivityJob> jobs =
		// transJobController.findAllTransitivityJobs();
		// assertTrue(jobs != null);
		//
		// // Find the jobs
		// boolean isFound = false;
		// for (long jobId : jobIds) {
		// for (TransitivityJob job : jobs) {
		// if (jobId == job.getId()) {
		// isFound = true;
		// break;
		// }
		// }
		// assertTrue(isFound);
		// }
		//
		// checkCounts();
	}

	protected TransitivityJob createEphemeralTransitivityJob(
			TestEntityCounts te, String tag, boolean isTag) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		OabaJob oabaJob = createEphemeralOabaJob(te, tag, isTag);
		return BatchJobUtils.createEphemeralTransitivityJob(MAX_SINGLE_LIMIT,
				utx, sc, em, te, oabaJob, tag, isTag);
	}

	protected OabaJob createEphemeralOabaJob(TestEntityCounts te, String tag,
			boolean isTag) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		if (sc == null) {
			sc = serverController.computeGenericConfiguration();
		}
		return BatchJobUtils.createEphemeralOabaJobEntity(MAX_SINGLE_LIMIT,
				utx, sc, em, te, tag, isTag);
	}

	protected ServerConfiguration getDefaultServerConfiguration() {
		return BatchJobUtils.getDefaultServerConfiguration(serverController);
	}

}
