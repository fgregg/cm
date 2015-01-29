package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.OperationalProperty;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.StartOabaMDB;
import com.choicemaker.cmit.OabaTestController;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.BatchJobUtils;
import com.choicemaker.cmit.utils.TestEntityCounts;

@RunWith(Arquillian.class)
public class OperationalPropertyControllerBeanIT {

	private static final Logger logger = Logger
			.getLogger(OperationalPropertyControllerBeanIT.class.getName());

	private static final boolean TESTS_AS_EJB_MODULE = true;

	private final static String LOG_SOURCE =
		OperationalPropertyControllerBeanIT.class.getSimpleName();

	public static final int MAX_SINGLE_LIMIT = 1000;

	public static final int MAX_TEST_ITERATIONS = 10;

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * StartOabaMDB message bean. This allows another class to attach to the
	 * startQueue for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { StartOabaMDB.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	private OabaJobControllerBean oabaController;

	@EJB
	protected OabaTestController oabaTestController;

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaParametersControllerBean paramsController;

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

	final protected Random random = new Random(new Date().getTime());

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

	@Before
	public void setUp() throws Exception {
		te =
			new TestEntityCounts(logger, oabaController, paramsController,
					oabaSettingsController, serverController,
					processingController, opPropController, rsController,
					ridController);
	}

	protected OabaJobEntity createEphemeralOabaJobEntity(TestEntityCounts te,
			String tag, boolean isTag) {
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

	protected static String createPropertyName(int index) {
		return "TEST_PROPERTY_" + index;
	}

	@Test
	public void testSaveFindUpdateRemove() {
		final String METHOD = "testSaveFindUpdateRemove";
		logger.entering(LOG_SOURCE, METHOD);

		OabaJobEntity _job = createEphemeralOabaJobEntity(te, METHOD, true);
		assertTrue(_job != null);
		assertTrue(_job.getId() == OabaJobEntity.INVALID_ID);
		OabaJobEntity job = oabaController.save(_job);
		final long jobId = job.getId();
		assertTrue(jobId != OabaJobEntity.INVALID_ID);

		Set<String> _expectedNames = new LinkedHashSet<>();
		Set<String> _expectedValues = new LinkedHashSet<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			String pn = createPropertyName(i);
			_expectedNames.add(pn);
			String pv = String.valueOf(i);
			_expectedValues.add(pv);
			opPropController.setJobProperty(job, pn, pv);
		}
		final Set<String> expectedNames =
			Collections.unmodifiableSet(_expectedNames);
		assertTrue(expectedNames.size() == MAX_TEST_ITERATIONS);
		final Set<String> expectedValues =
			Collections.unmodifiableSet(_expectedValues);
		assertTrue(expectedValues.size() == MAX_TEST_ITERATIONS);
		
		List<OperationalProperty> ops = opPropController.findAllByJob(job);
		assertTrue(ops.size() == MAX_TEST_ITERATIONS);
		int count = 0;
		for (OperationalProperty op : ops) {
			++count;
			assertTrue(jobId == op.getJobId());
			final long pid = op.getId();
			assertTrue(pid != OperationalProperty.INVALID_ID);
			final String pn = op.getName();
			assertTrue(expectedNames.contains(pn));
			final String pv1 = op.getValue();
			assertTrue(expectedValues.contains(pv1));
			
			final String pv2 = opPropController.getJobProperty(job, pn);
			assert(pv2 != null);
			assertTrue(pv2.equals(pv1));

			final OperationalProperty op2 = opPropController.find(pid);
			assertTrue(op2 != null);
			assertTrue(op2.equals(op));
			
			// Implicit test of update
			final String pv3 = String.valueOf(MAX_TEST_ITERATIONS + count);
			assertTrue(!pv3.equals(pv2));
			opPropController.setJobProperty(job, pn, pv3);
			final String pv4 = opPropController.getJobProperty(job, pn);
			assert(pv4 != null);
			assertTrue(pv3.equals(pv4));

			opPropController.remove(op);
			int newSize = opPropController.findAllByJob(job).size();
			assertTrue(newSize == MAX_TEST_ITERATIONS - count);

			final OperationalProperty op5 = opPropController.find(pid);
			assertTrue(op5 == null);
			final String pv5 = opPropController.getJobProperty(job, pn);
			assertTrue(pv5 == null);
		}
		assertTrue(opPropController.findAllByJob(job).isEmpty());
		checkCounts();
	}

}
