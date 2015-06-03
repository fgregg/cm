package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.AbaStatistics;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.AbaStatisticsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MatchDedupMDB;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.oaba.util.OabaMdbTestProcedures;
import com.choicemaker.cmit.testconfigs.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.utils.j2ee.TestEntityCounts;
import com.choicemaker.cmit.utils.j2ee.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.ejb.EjbPlatform;

@RunWith(Arquillian.class)
public class AbaStatisticsControllerIT {

	private static final Logger logger = Logger
			.getLogger(AbaStatisticsControllerIT.class.getName());

	private static final boolean TESTS_AS_EJB_MODULE = false;

	private final static String LOG_SOURCE = AbaStatisticsControllerIT.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * MatchDedupMDB message bean. This allows other classes to attach to the
	 * matchDedup and update queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { MatchDedupMDB.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	@EJB
	private EjbPlatform e2service;

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobController oabaController;

	@EJB(beanName = "OabaJobControllerBean")
	private OabaJobController jobController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private ProcessingController processingController;

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

	@EJB
	private AbaStatisticsController statsController;

	TestEntityCounts te;

	WellKnownTestConfiguration getTestConfiguration() {
		final Class<SimplePersonSqlServerTestConfiguration> c =
			SimplePersonSqlServerTestConfiguration.class;
		final OabaLinkageType olt = OabaLinkageType.STAGING_TO_MASTER_LINKAGE;
		CMPluginRegistry r = e2service.getPluginRegistry();
		WellKnownTestConfiguration retVal =
			OabaMdbTestProcedures.createTestConfiguration(c, olt, r);
		return retVal;
	}

	OabaParameters createPersistentOabaParameters(String methodName)
			throws ServerConfigurationException {
		logger.entering(LOG_SOURCE, methodName);

		WellKnownTestConfiguration c = getTestConfiguration();

		final PersistableRecordSource staging =
			rsController.save(c.getQueryRecordSource());
		te.add(staging);

		final PersistableRecordSource master =
			rsController.save(c.getReferenceRecordSource());
		te.add(master);

		final String dbConfig0 = c.getQueryDatabaseConfiguration();
		final String blkConf0 = c.getBlockingConfiguration();
		final String dbConfig1 = c.getReferenceDatabaseConfiguration();

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), blkConf0, staging, dbConfig0, master,
					dbConfig1, c.getOabaTask());
		te.add(bp);
		final OabaParameters retVal = paramsController.save(bp);
		te.add(retVal);

		return retVal;
	}

	@Before
	public void setUp() throws Exception {
		te =
			new TestEntityCounts(logger, oabaController, paramsController,
					oabaSettingsController, serverController,
					processingController, opPropController, rsController,
					ridController);
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
	@InSequence(1)
	public void testUpdateMasterAbaStatistics() throws Exception {
		final String METHOD = "testUpdateMasterAbaStatistics";
		OabaParameters params = createPersistentOabaParameters(METHOD);
		statsController.updateReferenceStatistics(params);
		checkCounts();
	}

	@Test
	@InSequence(2)
	public void testPutGetStatistics() throws Exception {
		final String METHOD = "testUpdateMasterAbaStatistics";
		OabaParameters params = createPersistentOabaParameters(METHOD);
		String modelName = params.getModelConfigurationName();
		ImmutableProbabilityModel model =
			PMManager.getImmutableModelInstance(modelName);
		AbaStatistics stats = statsController.getStatistics(model);
		assertTrue(stats != null);
		checkCounts();
	}

}
