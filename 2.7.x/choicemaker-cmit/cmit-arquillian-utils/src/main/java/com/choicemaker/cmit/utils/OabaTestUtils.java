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
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
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

	private static final Logger logger = Logger
			.getLogger(OabaTestUtils.class.getName());

	public static <T extends WellKnownTestConfiguration> OabaJob startOabaJob(
			final OabaLinkageType linkage, final String tag,
			final OabaTestParameters test, final String externalId) {

		// Preconditions
		if (linkage == null || tag == null || test == null
				|| externalId == null) {
			throw new IllegalArgumentException("null argument");
		}

		final String LOG_SOURCE = test.getSourceName();
		logger.entering(LOG_SOURCE, tag);

		final TestEntityCounts te = test.getTestEntityCounts();
		final WellKnownTestConfiguration c = test.getTestConfiguration(linkage);

		final PersistableRecordSource staging =
			test.getRecordSourceController().save(c.getStagingRecordSource());
		assertTrue(staging.isPersistent());
		te.add(staging);

		final PersistableRecordSource master;
		if (OabaLinkageType.STAGING_DEDUPLICATION == linkage) {
			master = null;
		} else {
			master =
				test.getRecordSourceController()
						.save(c.getMasterRecordSource());
			assertTrue(master.isPersistent());
			te.add(master);
		}

		final String modelId = c.getModelConfigurationName();
		final ImmutableProbabilityModel model =
			PMManager.getImmutableModelInstance(modelId);
		OabaSettings oabaSettings =
			test.getSettingsController().findDefaultOabaSettings(model);
		if (oabaSettings == null) {
			// Creates generic settings and saves them
			oabaSettings = new OabaSettingsEntity();
			oabaSettings = test.getSettingsController().save(oabaSettings);
			te.add(oabaSettings);
		}
		assertTrue(oabaSettings != null);

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
					.getMatchThreshold(), staging, master, c.getOabaTask());
		te.add(bp);

		final OabaService batchQuery = test.getOabaService();
		long jobId = NONPERSISTENT_ID;
		try {
			switch (linkage) {
			case STAGING_DEDUPLICATION:
				logger.info(tag
						+ ": invoking BatchQueryService.startDeduplication");
				jobId =
					batchQuery.startDeduplication(externalId, bp, oabaSettings,
							serverConfiguration);
				logger.info(tag + ": returned jobId '" + jobId
						+ "' from BatchQueryService.startDeduplication");
				break;
			case STAGING_TO_MASTER_LINKAGE:
			case MASTER_TO_MASTER_LINKAGE:
				logger.info(tag + ": invoking BatchQueryService.startLinkage");
				jobId =
					batchQuery.startLinkage(externalId, bp, oabaSettings,
							serverConfiguration);
				logger.info(tag + ": returned jobId '" + jobId
						+ "' from BatchQueryService.startLinkage");
				break;
			default:
				fail("Unexpected linkage type: " + linkage);
			}
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}

		final OabaJobController jobController = test.getJobController();
		assertTrue(jobId != NONPERSISTENT_ID);
		OabaJob retVal = jobController.findOabaJob(jobId);
		assertTrue(retVal != null);

		// Validate that the job parameters are correct
		final OabaParametersController paramsController =
			test.getParamsController();
		OabaParameters params =
			paramsController.findOabaParametersByJobId(jobId);
		te.add(params);
		validateJobParameters(retVal, bp, params);

		return retVal;
	}

	public static <T extends WellKnownTestConfiguration> void validateJobParameters(
			final OabaJob oabaJob,
			final OabaParameters expected, final OabaParameters params) {

		// Validate that the job parameters are correct
		assertTrue(params != null);
		assertTrue(params.getLowThreshold() == expected.getLowThreshold());
		assertTrue(params.getHighThreshold() == expected.getHighThreshold());
		assertTrue(params.getOabaLinkageType() == expected.getOabaLinkageType());

		final OabaLinkageType linkage = params.getOabaLinkageType();
		if (OabaLinkageType.STAGING_DEDUPLICATION == linkage) {
			assertTrue(params.getMasterRsId() == null);
			assertTrue(params.getMasterRsType() == null);
		} else {
			assertTrue(params.getMasterRsId() != null
					&& params.getMasterRsId().equals(expected.getMasterRsId()));
			assertTrue(params.getMasterRsType() != null
					&& params.getMasterRsType().equals(
							expected.getMasterRsType()));
		}
		assertTrue(params.getStageRsId() == expected.getStageRsId());
		assertTrue(params.getStageRsType() != null
				&& params.getStageRsType().equals(expected.getStageRsType()));
		assertTrue(params.getModelConfigurationName() != null
				&& params.getModelConfigurationName().equals(
						expected.getModelConfigurationName()));

	}

	private OabaTestUtils() {
	}

}
