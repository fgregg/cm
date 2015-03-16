package com.choicemaker.cmit.utils;

import static com.choicemaker.cm.args.AbaSettings.DEFAULT_LIMIT_PER_BLOCKING_SET;
import static com.choicemaker.cm.args.AbaSettings.DEFAULT_LIMIT_SINGLE_BLOCKING_SET;
import static com.choicemaker.cm.args.AbaSettings.DEFAULT_SINGLE_TABLE_GRACE_LIMIT;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_INTERVAL;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_BLOCKSIZE;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_CHUNKSIZE;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_MATCHES;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_OVERSIZED;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MIN_FIELDS;
import static com.choicemaker.cm.args.TransitivityParameters.DEFAULT_GRAPH_PROPERTY_NAME;
import static com.choicemaker.cm.args.TransitivityParameters.DEFAULT_RESULT_FORMAT;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.transitivity.server.impl.TransitivityJobEntity;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;

/** Utility methods used in tests of OabaJobEntity and TransitivityJobEntity */
public class BatchJobUtils {

	private BatchJobUtils() {
	}

	/**
	 * Creates an ephemeral instance of OabaParametersEntity. If
	 * <code>isTag</code> is <code>true</code>, an externalId for the returned
	 * BatchJob is synthesized using the specified String; otherwise, the String
	 * is used as an externalId without alteration.
	 */
	public static OabaJobEntity createEphemeralOabaJobEntity(
			int maxSingleLimit, UserTransaction utx, ServerConfiguration sc,
			EntityManager em, TestEntityCounts te, String s, boolean isTag) {
		final String METHOD = "createEphemeralOabaJob";
		if (utx == null) {
			throw new IllegalArgumentException("null transaction");
		}
		if (sc == null) {
			throw new IllegalArgumentException("null server configuration");
		}
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		if (te == null) {
			throw new IllegalArgumentException("null test-entity counts");
		}
		OabaJobEntity retVal = null;
		try {
			utx.begin();
			OabaParametersEntity params =
				createPersistentOabaParameters(em, METHOD, te);
			te.add(params);
			OabaSettingsEntity settings =
				createPersistentOabaSettings(maxSingleLimit, em, te);
			te.add(settings);
			String extId;
			if (isTag) {
				extId = EntityManagerUtils.createExternalId(s);
			} else {
				extId = s;
			}
			retVal = new OabaJobEntity(params, settings, sc, extId);
			te.add(retVal);
			utx.commit();
		} catch (Exception x) {
			fail(x.toString());
		}
		assertTrue(retVal != null);
		return retVal;
	}

	public static ServerConfiguration getDefaultServerConfiguration(
			ServerConfigurationController serverController) {
		String hostName = ServerConfigurationControllerBean.computeHostName();
		final boolean computeFallback = true;
		ServerConfiguration retVal =
			serverController.getDefaultConfiguration(hostName, computeFallback);
		assert retVal != null;
		assert retVal.getId() != ServerConfigurationControllerBean.INVALID_ID;
		return retVal;
	}

	/** Creates an ephemeral instance of OabaParametersEntity */
	public static OabaParametersEntity createEphemeralOabaParameters(
			String tag, TestEntityCounts te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = EntityManagerUtils.createRandomThresholds();
		PersistableRecordSource stage = new FakePersistableRecordSource(tag);
		OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		PersistableRecordSource master =
			EntityManagerUtils.createFakeMasterRecordSource(tag, task);
		OabaParametersEntity retVal =
			new OabaParametersEntity(
					EntityManagerUtils.createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, task);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of OabaParametersEntity An externalId for
	 * the returned BatchJob is synthesized using the specified tag.
	 */
	public static OabaParametersEntity createPersistentOabaParameters(
			EntityManager em, String tag, TestEntityCounts te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		OabaParametersEntity retVal = createEphemeralOabaParameters(tag, te);
		em.persist(retVal);
		return retVal;
	}

	/** Creates an ephemeral instance of OabaSettingsEntity */
	public static OabaSettingsEntity createEphemeralOabaSettings(
			int maxSingleLimit, TestEntityCounts te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		if (maxSingleLimit < 0) {
			throw new IllegalArgumentException("invalid maxSingle limit: "
					+ maxSingleLimit);
		}
		final Random random = new Random(new Date().getTime());
		int limPerBlockingSet = random.nextInt(DEFAULT_LIMIT_PER_BLOCKING_SET);
		int limSingleBlockingSet =
			random.nextInt(DEFAULT_LIMIT_SINGLE_BLOCKING_SET);
		int singleTableGraceLimit =
			random.nextInt(DEFAULT_SINGLE_TABLE_GRACE_LIMIT);
		int maxSingle =
			maxSingleLimit == 0 ? 0 : random.nextInt(maxSingleLimit);
		int maxBlockSize = random.nextInt(DEFAULT_MAX_BLOCKSIZE);
		int maxChunkSize = random.nextInt(DEFAULT_MAX_CHUNKSIZE);
		int maxMatches = random.nextInt(DEFAULT_MAX_MATCHES);
		int maxOversized = random.nextInt(DEFAULT_MAX_OVERSIZED);
		int minFields = random.nextInt(DEFAULT_MIN_FIELDS);
		int interval = random.nextInt(DEFAULT_INTERVAL);
		OabaSettingsEntity retVal =
			new OabaSettingsEntity(limPerBlockingSet, limSingleBlockingSet,
					singleTableGraceLimit, maxSingle, maxBlockSize, maxMatches,
					maxChunkSize, maxOversized, minFields, interval);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of OabaSettingsEntity An externalId for the
	 * returned BatchJob is synthesized using the specified tag.
	 */
	public static OabaSettingsEntity createPersistentOabaSettings(
			int maxSingleLimit, EntityManager em, TestEntityCounts te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		OabaSettingsEntity retVal =
			createEphemeralOabaSettings(maxSingleLimit, te);
		em.persist(retVal);
		return retVal;
	}

	public static String getRandomNonTerminalStatus() {
		final Random random = new Random(new Date().getTime());
		String[] nonTerminal = BatchJobEntity.getNonTerminalStatusValues();
		int i = random.nextInt(nonTerminal.length);
		return nonTerminal[i];
	}

	public static BatchJob createEphemeralTransitivityJob(int maxSingleLimit,
			UserTransaction utx, OabaSettings settings, ServerConfiguration sc,
			EntityManager em, TestEntityCounts te, BatchJob batchJob,
			OabaParametersController oabaParamsController, String s,
			boolean isTag) {
		final String METHOD = "createEphemeralTransitivityJob";
		if (utx == null || sc == null || em == null || te == null
				|| batchJob == null) {
			throw new IllegalArgumentException("null argument");
		}
		TransitivityJobEntity retVal = null;
		try {
			utx.begin();
			long oabaParamsId = batchJob.getParametersId();
			OabaParameters oabaParams =
				oabaParamsController.findOabaParameters(oabaParamsId);
			TransitivityParameters params =
				createPersistentTransitivityParameters(oabaParams, em, te,
						METHOD);
			String extId;
			if (isTag) {
				extId = EntityManagerUtils.createExternalId(s);
			} else {
				extId = s;
			}
			retVal =
				new TransitivityJobEntity(params, settings, sc, batchJob, extId);
			te.add(retVal);
			utx.commit();
		} catch (Exception x) {
			fail(x.toString());
		}
		assertTrue(retVal != null);
		return retVal;
	}

	/** Creates an ephemeral instance of TransitivityParametersEntity */
	public static TransitivityParametersEntity createEphemeralTransitivityParameters(
			OabaParameters oabaParams, String tag, TestEntityCounts te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		// Thresholds thresholds = EntityManagerUtils.createRandomThresholds();
		// PersistableRecordSource stage = new FakePersistableRecordSource(tag);
		// OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		// PersistableRecordSource master =
		// EntityManagerUtils.createFakeMasterRecordSource(tag, task);
		TransitivityParametersEntity retVal = new TransitivityParametersEntity(
		// EntityManagerUtils.createRandomModelConfigurationName(tag),
		// thresholds.getDifferThreshold(),
		// thresholds.getMatchThreshold(), stage, master
				oabaParams, DEFAULT_RESULT_FORMAT, DEFAULT_GRAPH_PROPERTY_NAME);
		te.add((TransitivityParameters) retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of TransitivityParametersEntity An
	 * externalId for the returned TransitivityJob is synthesized using the
	 * specified tag.
	 */
	public static TransitivityParametersEntity createPersistentTransitivityParameters(
			OabaParameters oabaParams, EntityManager em, TestEntityCounts te,
			String tag) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		TransitivityParametersEntity retVal =
			createEphemeralTransitivityParameters(oabaParams, tag, te);
		em.persist(retVal);
		return retVal;
	}

}
