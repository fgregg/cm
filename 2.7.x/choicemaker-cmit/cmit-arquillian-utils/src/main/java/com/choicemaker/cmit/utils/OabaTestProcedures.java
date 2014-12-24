package com.choicemaker.cmit.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jms.Queue;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaUtils;
//import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatchMDB;
import com.choicemaker.e2.CMPluginRegistry;

public class OabaTestProcedures {

	private OabaTestProcedures() {
	}

	public static boolean isValidConfigurationClass(Class<?> c) {
		boolean retVal = false;
		if (c != null && WellKnownTestConfiguration.class.isAssignableFrom(c)) {
			retVal = true;
		}
		return retVal;
	}

	public static <T extends WellKnownTestConfiguration> T createTestConfiguration(
			Class<T> c, CMPluginRegistry registry) {

		if (!isValidConfigurationClass(c)) {
			String msg = "invalid configuration class: " + c;
			throw new IllegalArgumentException(msg);
		}
		if (registry == null) {
			throw new IllegalArgumentException("null registry");
		}

		T retVal = null;
		try {
			Class<T> cWKTC = (Class<T>) c;
			retVal = cWKTC.newInstance();
			retVal.initialize(registry);
		} catch (Exception x) {
			fail(x.toString());
		}
		assertTrue(retVal != null);
		return retVal;
	}

	public static <T extends WellKnownTestConfiguration> void testLinkageProcessing(
			OabaProcessingTest<T> test) throws ServerConfigurationException {
		if (test == null) {
			throw new IllegalArgumentException("null argument");
		}
		
		assertTrue(test.isSetupOK());
		String TEST = "testStartOABALinkage";
		test.getLogger().entering(test.getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final WellKnownTestConfiguration c = test.getTestConfiguration();

		PersistableRecordSource prs = c.getStagingRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource staging =
			test.getRecordSourceController().save(prs);
		assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);
		prs = c.getMasterRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource master =
			test.getRecordSourceController().save(prs);
		assertTrue(master.getId() != PersistableRecordSource.NONPERSISTENT_ID);
		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging, master, c.getOabaTask());
		final OabaSettings oabaSettings =
			OabaUtils.getDefaultOabaSettings(test.getSettingsController(),
					bp.getStageModel());
		final ServerConfiguration serverConfiguration =
			OabaUtils.getDefaultServerConfiguration(test
					.getServerController());

		final Queue resultQueue = test.getResultQueue();
		final int expectedEventId = test.getResultEventId();
		final int expectedCompletion = test.getResultPercentComplete();
		OabaTestUtils.testOabaProcessing(test.getSourceName(),
				TEST, externalID, bp, oabaSettings, serverConfiguration,
				test.getBatchQuery(), test.getJobController(),
				test.getParamsController(),
				test.getProcessingController(), test.getJmsContext(),
				resultQueue, test.getUpdateQueue(), test.getEm(),
				test.getUtx(), expectedEventId, expectedCompletion,
				test.getOabaProcessingPhase());

		test.getLogger().exiting(test.getSourceName(), TEST);
	}

	public static <T extends WellKnownTestConfiguration> void testDeduplicationProcessing(
			OabaProcessingTest<T> test) throws ServerConfigurationException {
		if (test == null) {
			throw new IllegalArgumentException("null argument");
		}

		assertTrue(test.isSetupOK());
		String TEST = "testStartOABAStage";
		test.getLogger().entering(test.getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final WellKnownTestConfiguration c = test.getTestConfiguration();

		PersistableRecordSource prs = c.getStagingRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource staging =
			test.getRecordSourceController().save(prs);
		assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging);
		final OabaSettings oabaSettings =
			OabaUtils.getDefaultOabaSettings(test.getSettingsController(),
					bp.getStageModel());
		final ServerConfiguration serverConfiguration =
			OabaUtils.getDefaultServerConfiguration(test
					.getServerController());

		final Queue resultQueue = test.getResultQueue();
		final int expectedEventId = test.getResultEventId();
		final int expectedCompletion = test.getResultPercentComplete();
		OabaTestUtils.testOabaProcessing(test.getSourceName(),
				TEST, externalID, bp, oabaSettings, serverConfiguration,
				test.getBatchQuery(), test.getJobController(),
				test.getParamsController(),
				test.getProcessingController(), test.getJmsContext(),
				resultQueue, test.getUpdateQueue(), test.getEm(),
				test.getUtx(), expectedEventId, expectedCompletion,
				test.getOabaProcessingPhase());

		test.getLogger().exiting(test.getSourceName(), TEST);
	}

}
