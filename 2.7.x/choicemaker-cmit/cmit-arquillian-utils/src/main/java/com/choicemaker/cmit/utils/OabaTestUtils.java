package com.choicemaker.cmit.utils;

import static com.choicemaker.cm.batch.impl.AbstractPersistentObject.NONPERSISTENT_ID;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;

/**
 * Standardized procedures for testing intermediate stages of OABA processing
 * (which are implemented as message-driven beans).
 * 
 * @author rphall
 */
public class OabaTestUtils {

	private static final Logger logger = Logger.getLogger(OabaTestUtils.class
			.getName());

	private static final String LOG_SOURCE = OabaTestUtils.class
			.getSimpleName();

	public static BatchJob startOabaJob(final OabaLinkageType linkage,
			final String tag, final OabaTestParameters test,
			final String externalId) {

		// Preconditions
		if (linkage == null || tag == null || test == null
				|| externalId == null) {
			throw new IllegalArgumentException("null argument");
		}

		final String LOG_SOURCE = test.getSourceName();
		logger.entering(LOG_SOURCE, tag);

		final TestEntityCounts te = test.getTestEntityCounts();
		final WellKnownTestConfiguration c = test.getTestConfiguration();

		final PersistableRecordSource staging =
			test.getRecordSourceController().save(c.getQueryRecordSource());
		assertTrue(staging.isPersistent());
		te.add(staging);

		final PersistableRecordSource master;
		if (OabaLinkageType.STAGING_DEDUPLICATION == linkage) {
			master = null;
		} else {
			master =
				test.getRecordSourceController()
						.save(c.getReferenceRecordSource());
			assertTrue(master.isPersistent());
			te.add(master);
		}

		final String modelId = c.getModelConfigurationName();
		final ImmutableProbabilityModel model =
			PMManager.getImmutableModelInstance(modelId);
		
		// Create default or generic settings 
		OabaSettings _os0 =
			test.getSettingsController().findDefaultOabaSettings(model);
		if (_os0 == null) {
			// Creates generic settings and saves them
			_os0 = new OabaSettingsEntity();
			_os0 = test.getSettingsController().save(_os0);
			te.add(_os0);
		}
		assertTrue(_os0 != null);
		
		// Update the default or generic settings using the test parameters
		OabaSettings _os1 = updateSettings(_os0, test);
		final OabaSettings updatedSettings;
		if (!_os0.equals(_os1)) {
			updatedSettings = test.getSettingsController().save(_os1);
			te.add(updatedSettings);
		} else {
			updatedSettings = _os0;
		}
		assertTrue(updatedSettings != null);
	
		final String hostName =
			ServerConfigurationControllerBean.computeHostName();
		logger.info("Computed host name: " + hostName);
		final DefaultServerConfiguration dsc =
			test.getServerController().findDefaultServerConfiguration(hostName);
		ServerConfiguration serverConfiguration = null;
		if (dsc != null) {
			long id = dsc.getServerConfigurationId();
			logger.info("Default server configuration id: " + id);
			serverConfiguration =
				test.getServerController().findServerConfiguration(id);
		}
		if (serverConfiguration == null) {
			logger.info("No default server configuration for: " + hostName);
			serverConfiguration =
				test.getServerController().computeGenericConfiguration();
			try {
				serverConfiguration =
					test.getServerController().save(serverConfiguration);
			} catch (ServerConfigurationException e) {
				fail("Unable to save server configuration: " + e.toString());
			}
			te.add(serverConfiguration);
		}
		logger.info(ServerConfigurationEntity.dump(serverConfiguration));
		assertTrue(serverConfiguration != null);

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging,
					c.getQueryDatabaseConfiguration(),
					c.getQueryBlockingConfiguration(), master,
					c.getReferenceDatabaseConfiguration(),
					c.getReferenceBlockingConfiguration(), c.getOabaTask());
		te.add(bp);

		final OabaService batchQuery = test.getOabaService();
		long jobId = NONPERSISTENT_ID;
		try {
			switch (linkage) {
			case STAGING_DEDUPLICATION:
				logger.info(tag + ": invoking OabaService.startDeduplication");
				jobId =
					batchQuery.startDeduplication(externalId, bp, updatedSettings,
							serverConfiguration, null);
				logger.info(tag + ": returned jobId '" + jobId
						+ "' from OabaService.startDeduplication");
				break;
			case STAGING_TO_MASTER_LINKAGE:
			case MASTER_TO_MASTER_LINKAGE:
				logger.info(tag + ": invoking OabaService.startLinkage");
				jobId =
					batchQuery.startLinkage(externalId, bp, updatedSettings,
							serverConfiguration, null);
				logger.info(tag + ": returned jobId '" + jobId
						+ "' from OabaService.startLinkage");
				break;
			default:
				fail("Unexpected linkage type: " + linkage);
			}
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}

		final OabaJobController jobController = test.getOabaJobController();
		assertTrue(jobId != NONPERSISTENT_ID);
		BatchJob retVal = jobController.findOabaJob(jobId);
		assertTrue(retVal != null);

		// Validate that the job parameters are correct
		final OabaParametersController paramsController =
			test.getOabaParamsController();
		OabaParameters params =
			paramsController.findOabaParametersByBatchJobId(jobId);
		te.add(params);
		validateJobParameters(retVal, bp, params);

		return retVal;
	}

	/**
	 * Updates settings based on test parameters
	 * @param te 
	 */
	public static OabaSettings updateSettings(final OabaSettings s0,
			OabaTestParameters p) {
		if (s0 == null || p == null) {
			throw new IllegalArgumentException(LOG_SOURCE + ": null constructor arg");
		}
		
		final WellKnownTestConfiguration c = p.getTestConfiguration();
		final int maxSingle = c.getSingleRecordMatchingThreshold();
		boolean isDifferent = s0.getMaxSingle() != maxSingle;
		
		OabaSettings retVal;
		if (isDifferent) {
			retVal = new OabaSettingsEntity(s0, maxSingle);
		} else {
			retVal = s0;
		}
		return retVal;
	}

	public static void validateJobParameters(final BatchJob batchJob,
			final OabaParameters expected, final OabaParameters params) {

		// Validate that the job parameters are correct
		assertTrue(params != null);
		assertTrue(params.getLowThreshold() == expected.getLowThreshold());
		assertTrue(params.getHighThreshold() == expected.getHighThreshold());
		assertTrue(params.getOabaLinkageType() == expected.getOabaLinkageType());

		final OabaLinkageType linkage = params.getOabaLinkageType();
		if (OabaLinkageType.STAGING_DEDUPLICATION == linkage) {
			assertTrue(params.getReferenceRsId() == null);
			assertTrue(params.getReferenceRsType() == null);
		} else {
			assertTrue(params.getReferenceRsId() != null
					&& params.getReferenceRsId().equals(expected.getReferenceRsId()));
			assertTrue(params.getReferenceRsType() != null
					&& params.getReferenceRsType().equals(
							expected.getReferenceRsType()));
		}
		assertTrue(params.getQueryRsId() == expected.getQueryRsId());
		assertTrue(params.getQueryRsType() != null
				&& params.getQueryRsType().equals(expected.getQueryRsType()));
		assertTrue(params.getModelConfigurationName() != null
				&& params.getModelConfigurationName().equals(
						expected.getModelConfigurationName()));

	}

	private OabaTestUtils() {
	}

}
