package com.choicemaker.cmit.utils;

import static com.choicemaker.cm.io.blocking.automated.AbaSettings.DEFAULT_LIMIT_PER_BLOCKING_SET;
import static com.choicemaker.cm.io.blocking.automated.AbaSettings.DEFAULT_LIMIT_SINGLE_BLOCKING_SET;
import static com.choicemaker.cm.io.blocking.automated.AbaSettings.DEFAULT_SINGLE_TABLE_GRACE_LIMIT;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_INTERVAL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MAX_BLOCKSIZE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MAX_CHUNKSIZE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MAX_OVERSIZED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MIN_FIELDS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.impl.TransitivityJobEntity;
import com.choicemaker.cm.transitivity.server.impl.TransitivityJobJPA;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cm.transitivity.server.impl.TransitivitySettingsEntity;

public class EntityManagerUtils {

	public static final int MAX_MAX_SINGLE = 1000;

	private EntityManagerUtils() {
	}

	private static final Logger logger = Logger
			.getLogger(EntityManagerUtils.class.getName());

	private static final String DEFAULT_EXTERNALID_TAG = "Random external id";
	private static final String DEFAULT_MODEL_NAME = "FakeModelConfig";
	static final String DEFAULT_RECORDSOURCE_TAG = "Random record source";
	private static final String COLON = ":";
	private static final String SPACE = " ";
	private static final String UNDERSCORE = "_";
	static final String TAG_DELIMITER = ": ";
	static final String PREFIX_FAKE_RECORDSOURCE = "FAKE_RECORDSOURCE_";
	static final String PREFIX_FAKE_RECORDSOURCE_FILE = "FAKE_RECORDSOURCE_FILE_";

	private static final Random random = new Random();

	/** Synthesizes an externalId using the specified tag which may be null */
	public static String createExternalId(String tag) {
		if (tag == null) {
			tag = DEFAULT_EXTERNALID_TAG;
		}
		tag = tag.trim();
		if (tag.isEmpty()) {
			tag = DEFAULT_EXTERNALID_TAG;
		}
		StringBuilder sb = new StringBuilder(tag);
		if (tag.endsWith(COLON)) {
			sb.append(SPACE);
		} else {
			sb.append(TAG_DELIMITER);
		}
		sb.append(UUID.randomUUID().toString());
		String retVal = sb.toString();
		return retVal;
	}

	/**
	 * Synthesizes the name of a fake model configuration using the specified
	 * tag which may be null
	 */
	public static String createRandomModelConfigurationName(String tag) {
		if (tag == null) {
			tag = DEFAULT_MODEL_NAME;
		}
		tag = tag.trim();
		if (tag.isEmpty()) {
			tag = DEFAULT_MODEL_NAME;
		}
		StringBuilder sb = new StringBuilder(tag);
		if (!tag.endsWith(UNDERSCORE)) {
			sb.append(UNDERSCORE);
		}
		sb.append(UUID.randomUUID().toString());
		String retVal = sb.toString();
		return retVal;
	}

	public static Thresholds createRandomThresholds() {
		float low = random.nextFloat();
		float highRange = 1.0f - low;
		float f = random.nextFloat();
		float high = low + f * highRange;
		Thresholds retVal = new Thresholds(low, high);
		return retVal;
	}

	public static SerializableRecordSource createFakeSerialRecordSource(String tag) {
		return new FakeSerialRecordSource(tag);
	}

	/** Creates an ephemeral instance of OabaParametersEntity */
	public static OabaParametersEntity createEphemeralOabaParameters(
			String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		SerializableRecordSource stage = createFakeSerialRecordSource(tag);
		SerializableRecordSource master;
		if (random.nextBoolean()) {
			master = createFakeSerialRecordSource(tag);
		} else {
			master = null;
		}
//		File workingDir = ServerConfigurationControllerBean.computeGenericLocation();
		OabaParametersEntity retVal =
			new OabaParametersEntity(createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of OabaParametersEntity An externalId for
	 * the returned OabaJob is synthesized using the specified tag.
	 */
	public static OabaParametersEntity createPersistentOabaParameters(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		OabaParametersEntity retVal =
			createEphemeralOabaParameters(tag, te);
		em.persist(retVal);
		return retVal;
	}

	/** Creates an ephemeral instance of OabaSettingsEntity */
	public static OabaSettingsEntity createEphemeralOabaSettings(
			String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		int limPerBlockingSet = random.nextInt(DEFAULT_LIMIT_PER_BLOCKING_SET);
		int limSingleBlockingSet = random.nextInt(DEFAULT_LIMIT_SINGLE_BLOCKING_SET);
		int singleTableGraceLimit = random.nextInt(DEFAULT_SINGLE_TABLE_GRACE_LIMIT);
		int maxSingle = random.nextInt(MAX_MAX_SINGLE);
		int maxBlockSize = random.nextInt(DEFAULT_MAX_BLOCKSIZE);
		int maxChunkSize = random.nextInt(DEFAULT_MAX_CHUNKSIZE);
		int maxOversized = random.nextInt(DEFAULT_MAX_OVERSIZED);
		int minFields = random.nextInt(DEFAULT_MIN_FIELDS);
		int interval = random.nextInt(DEFAULT_INTERVAL);
		OabaSettingsEntity retVal =
		new OabaSettingsEntity(limPerBlockingSet, limSingleBlockingSet,
				singleTableGraceLimit, maxSingle, maxBlockSize,
				maxChunkSize, maxOversized, minFields, interval);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of OabaSettingsEntity An externalId for
	 * the returned OabaJob is synthesized using the specified tag.
	 */
	public static OabaSettingsEntity createPersistentOabaSettings(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		OabaSettingsEntity retVal =
			createEphemeralOabaSettings(tag, te);
		em.persist(retVal);
		return retVal;
	}

	/** Creates an ephemeral instance of TransitivityParametersEntity */
	public static TransitivityParametersEntity createEphemeralTransitivityParameters(
			String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		SerializableRecordSource stage = createFakeSerialRecordSource(tag);
		SerializableRecordSource master;
		if (random.nextBoolean()) {
			master = createFakeSerialRecordSource(tag);
		} else {
			master = null;
		}
//		File workingDir = ServerConfigurationControllerBean.computeGenericLocation();
		TransitivityParametersEntity retVal =
			new TransitivityParametersEntity(createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of TransitivityParametersEntity An externalId for
	 * the returned TransitivityJob is synthesized using the specified tag.
	 */
	public static TransitivityParametersEntity createPersistentTransitivityParameters(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		TransitivityParametersEntity retVal =
			createEphemeralTransitivityParameters(tag, te);
		em.persist(retVal);
		return retVal;
	}

	/** Creates an ephemeral instance of TransitivitySettingsEntity */
	public static TransitivitySettingsEntity createEphemeralTransitivitySettings(
			String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		SerializableRecordSource stage = createFakeSerialRecordSource(tag);
		SerializableRecordSource master;
		if (random.nextBoolean()) {
			master = createFakeSerialRecordSource(tag);
		} else {
			master = null;
		}
//		File workingDir = ServerConfigurationControllerBean.computeGenericLocation();
		TransitivitySettingsEntity retVal =
			new TransitivitySettingsEntity(createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of TransitivitySettingsEntity An externalId for
	 * the returned TransitivityJob is synthesized using the specified tag.
	 */
	public static TransitivitySettingsEntity createPersistentTransitivitySettings(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		TransitivitySettingsEntity retVal =
			createEphemeralTransitivitySettings(tag, te);
		em.persist(retVal);
		return retVal;
	}

	/**
	 * Creates an ephemeral instance of OabaParametersEntity. An externalId for
	 * the returned OabaJob is synthesized using the specified tag.
	 */
	public static OabaJobEntity createEphemeralOabaJob(ServerConfiguration sc,
			EntityManager em, String tag, TestEntities te) {
		return createEphemeralOabaJob(sc, em, te, createExternalId(tag));
	}

	/**
	 * Creates an ephemeral instance of OabaParametersEntity. The specified
	 * externalId is assigned without alteration to the returned OabaJob.
	 */
	public static OabaJobEntity createEphemeralOabaJob(ServerConfiguration sc,
			EntityManager em, TestEntities te, String extId) {
		final String METHOD = "createEphemeralOabaJob";
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		OabaParametersEntity params =
			createPersistentOabaParameters(em, METHOD, te);
		OabaSettingsEntity settings =
			createPersistentOabaSettings(em, METHOD, te);
		OabaJobEntity retVal = new OabaJobEntity(params, settings, sc, extId);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of OabaParametersEntity. An externalId for
	 * the returned OabaJob is synthesized using the specified tag.
	 */
	public static OabaJobEntity createPersistentOabaJobBean(
			ServerConfiguration sc, EntityManager em, String tag,
			TestEntities te) {
		return createPersistentOabaJobBean(sc, em, te, createExternalId(tag));
	}

	/**
	 * Creates a persistent instance of OabaParametersEntity. The specified
	 * externalId is assigned without alteration to the returned OabaJob.
	 */
	public static OabaJobEntity createPersistentOabaJobBean(
			ServerConfiguration sc, EntityManager em, TestEntities te,
			String extId) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		OabaParametersEntity params =
			createPersistentOabaParameters(em, null, te);
		OabaSettingsEntity settings =
			createPersistentOabaSettings(em, null, te);
		OabaJobEntity retVal = new OabaJobEntity(params, settings, sc, extId);
		em.persist(retVal);
		te.add(retVal);
		return retVal;
	}

	public static TransitivityJobEntity createEphemeralTransitivityJob(
			EntityManager em, String tag, TestEntities te, OabaJob job) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		if (job == null) {
			throw new IllegalArgumentException("null batch job");
		}
		if (!te.contains(job)) {
			logger.warning("Adding batchJob '" + job
					+ "' to test entities that will be removed from database");
			te.add(job);
		}
		String extId = createExternalId(tag);
		OabaParametersEntity params =
			em.find(OabaParametersEntity.class, job.getParametersId());
		if (params == null) {
			throw new IllegalArgumentException("non-persistent parameters");
		}
		if (!te.contains(params)) {
			logger.warning("Adding batchJob '" + job
					+ "' to test entities that will be removed from database");
			te.add(params);
		}
		TransitivityJobEntity retVal =
			new TransitivityJobEntity(params, job, extId);
		te.add(retVal);
		return retVal;
	}

	public static TransitivityJobEntity createEphemeralTransitivityJob(
			ServerConfiguration sc, EntityManager em, String tag,
			TestEntities te) {
		OabaParametersEntity params =
			createPersistentOabaParameters(em, tag, te);
		OabaJobEntity job = createPersistentOabaJobBean(sc, em, tag, te);
		TransitivityJobEntity retVal = new TransitivityJobEntity(params, job);
		te.add(retVal);
		return retVal;
	}

	public static TransitivityJobEntity createEphemeralTransitivityJob(
			EntityManager em, TestEntities te, OabaJob job, String extId) {
		OabaParametersEntity params =
			createPersistentOabaParameters(em, null, te);
		TransitivityJobEntity retVal =
			new TransitivityJobEntity(params, job, extId);
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		te.add(retVal);
		return retVal;
	}

	public static void removeTestEntities(EntityManager em, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		te.removePersistentObjects(em);
	}

	public static TransitivityJob save(EntityManager em,
			TransitivityJob job) {
		if (job.getId() == 0) {
			em.persist(job);
		} else {
			em.merge(job);
		}
		return job;
	}

	public static OabaParametersEntity findOabaParameters(EntityManager em,
			long id) {
		OabaParametersEntity job = em.find(OabaParametersEntity.class, id);
		return job;
	}

	public static OabaJobEntity findOabaJob(EntityManager em, long id) {
		OabaJobEntity job = em.find(OabaJobEntity.class, id);
		return job;
	}

	public static TransitivityJobEntity findTransitivityJob(EntityManager em,
			long id) {
		TransitivityJobEntity job = em.find(TransitivityJobEntity.class, id);
		return job;
	}

	public static List<OabaParameters> findAllOabaParameters(
			EntityManager em) {
		Query query =
			em.createNamedQuery(OabaParametersJPA.QN_BATCHPARAMETERS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaParameters> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<OabaParameters>();
		}
		return entries;
	}

	public static List<OabaJobEntity> findAllOabaJobs(EntityManager em) {
		Query query = em.createNamedQuery(OabaJobJPA.QN_OABAJOB_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaJobEntity> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<OabaJobEntity>();
		}
		return retVal;
	}

	public static List<TransitivityJob> findAllTransitivityJobs(
			EntityManager em) {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityJob> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<TransitivityJob>();
		}
		return retVal;
	}

	public static List<OabaProcessing> findAllOabaProcessing(
			EntityManager em) {
		Query query =
			em.createNamedQuery(OabaProcessingJPA.QN_OABAPROCESSING_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaProcessing> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<OabaProcessing>();
		}
		return retVal;
	}

	public static List<TransitivityJob> findAllByParentId(EntityManager em,
			long batchJobId) {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID);
		query.setParameter(
				TransitivityJobJPA.PN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID_BPARENTID,
				batchJobId);
		@SuppressWarnings("unchecked")
		List<TransitivityJob> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<TransitivityJob>();
		}
		return retVal;
	}

	public static void detach(EntityManager em, TransitivityJob job) {
		em.detach(job);
	}
}
