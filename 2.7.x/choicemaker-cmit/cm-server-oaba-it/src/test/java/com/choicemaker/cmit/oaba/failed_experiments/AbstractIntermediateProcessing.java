package com.choicemaker.cmit.oaba.failed_experiments;

import static org.junit.Assert.assertTrue;

import javax.jms.Queue;

import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaUtils;
//import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatch;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.OabaTestUtils;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;

public abstract class AbstractIntermediateProcessing extends AbstractOabaProcessing {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	// -- Customizable methods
	
	protected abstract Queue getIntermediateResultQueue();
	
	protected abstract int getIntermediateResultEventId();

	protected abstract int getIntermediateResultPercentComplete();

	// -- Customized methods

	protected WellKnownTestConfiguration createTestConfiguration(
			CMPluginRegistry registry) {
		SimplePersonSqlServerTestConfiguration retVal =
			new SimplePersonSqlServerTestConfiguration();
		retVal.initialize(registry);
		return retVal;
	}

	@Test
	@InSequence(5)
	public void testStartLinkage() throws ServerConfigurationException {
		assertTrue(isSetupOK());
		String TEST = "testStartOABALinkage";
		getLogger().entering(getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final WellKnownTestConfiguration c =
			createTestConfiguration(getE2service().getPluginRegistry());

		PersistableRecordSource prs = c.getStagingRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource staging =
			getRecordSourceController().save(prs);
		assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);
		prs = c.getMasterRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource master =
			getRecordSourceController().save(prs);
		assertTrue(master.getId() != PersistableRecordSource.NONPERSISTENT_ID);
		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging, master, c.getOabaTask());
		final OabaSettings oabaSettings =
			OabaUtils.getDefaultOabaSettings(getSettingsController(),
					bp.getStageModel());
		final ServerConfiguration serverConfiguration =
			OabaUtils.getDefaultServerConfiguration(getServerController());

		final Queue resultQueue = this.getIntermediateResultQueue();
		final int expectedEventId = getIntermediateResultEventId();
		final int expectedCompletion = getIntermediateResultPercentComplete();
		OabaTestUtils.testIntermediateOabaProcessing(getSourceName(), TEST,
				externalID, bp, oabaSettings, serverConfiguration,
				getBatchQuery(), getJobController(), getParamsController(),
				getProcessingController(), getJmsContext(), resultQueue,
				getUpdateQueue(), getEm(), getUtx(),
				expectedEventId, expectedCompletion);

		getLogger().exiting(getSourceName(), TEST);
	}

	@Test
	@InSequence(6)
	public void testStartDeduplication() throws ServerConfigurationException {
		assertTrue(isSetupOK());
		String TEST = "testStartOABAStage";
		getLogger().entering(getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.getE2service().getPluginRegistry());

		PersistableRecordSource prs = c.getStagingRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource staging =
			getRecordSourceController().save(prs);
		assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging);
		final OabaSettings oabaSettings =
			OabaUtils.getDefaultOabaSettings(getSettingsController(),
					bp.getStageModel());
		final ServerConfiguration serverConfiguration =
			OabaUtils.getDefaultServerConfiguration(getServerController());
		
		final Queue resultQueue = this.getIntermediateResultQueue();
		final int expectedEventId = getIntermediateResultEventId();
		final int expectedCompletion = getIntermediateResultPercentComplete();
		OabaTestUtils.testIntermediateOabaProcessing(getSourceName(), TEST,
				externalID, bp, oabaSettings, serverConfiguration,
				getBatchQuery(), getJobController(), getParamsController(),
				getProcessingController(), getJmsContext(), resultQueue,
				getUpdateQueue(), getEm(), getUtx(),
				expectedEventId, expectedCompletion);

		getLogger().exiting(getSourceName(), TEST);
	}

}
