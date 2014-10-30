package com.choicemaker.cmit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobJPA;

public class EntityManagerUtils {

	private EntityManagerUtils() {
	}

	private static final Logger logger = Logger
			.getLogger(EntityManagerUtils.class.getName());

	private static final String DEFAULT_TAG = "Random external id";
	private static final String DEFAULT_MODEL_NAME = "FakeModelConfig";
	private static final String COLON = ":";
	private static final String SPACE = " ";
	private static final String UNDERSCORE = "_";
	private static final String TAG_DELIMITER = ": ";
	private static final int MAX_MAX_SINGLE = 1000;

	private static final Random random = new Random();

	/** Synthesizes an externalId using the specified tag which may be null */
	public static String createExternalId(String tag) {
		if (tag == null) {
			tag = DEFAULT_TAG;
		}
		tag = tag.trim();
		if (tag.isEmpty()) {
			tag = DEFAULT_TAG;
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

	/** Creates an ephemeral instance of BatchParametersBean */
	public static BatchParametersBean createEphemeralBatchParameters(
			String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		BatchParametersBean retVal =
			new BatchParametersBean(createRandomModelConfigurationName(tag),
					random.nextInt(MAX_MAX_SINGLE),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), null, null);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of BatchParametersBean An externalId for
	 * the returned BatchJob is synthesized using the specified tag.
	 */
	public static BatchParametersBean createPersistentBatchParameters(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		BatchParametersBean retVal = createEphemeralBatchParameters(tag, te);
		em.persist(retVal);
		return retVal;
	}

	/**
	 * Creates an ephemeral instance of BatchParametersBean. An externalId for
	 * the returned BatchJob is synthesized using the specified tag.
	 */
	public static BatchJobBean createEphemeralBatchJob(
			EntityManager em, String tag, TestEntities te) {
		return createEphemeralBatchJob(em,te,createExternalId(tag));
	}

	/**
	 * Creates an ephemeral instance of BatchParametersBean. The specified
	 * externalId is assigned without alteration to the returned BatchJob.
	 */
	public static BatchJobBean createEphemeralBatchJob(
			EntityManager em, TestEntities te, String extId) {
		final String METHOD = "createEphemeralBatchJob";
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		BatchParametersBean params =
			createPersistentBatchParameters(em, METHOD, te);
		BatchJobBean retVal = new BatchJobBean(params, extId);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of BatchParametersBean. An externalId for
	 * the returned BatchJob is synthesized using the specified tag.
	 */
	public static BatchJobBean createPersistentBatchJobBean(EntityManager em,
			String tag, TestEntities te) {
		return createPersistentBatchJobBean(em, te, createExternalId(tag));
	}

	/**
	 * Creates a persistent instance of BatchParametersBean. The specified
	 * externalId is assigned without alteration to the returned BatchJob.
	 */
	public static BatchJobBean createPersistentBatchJobBean(EntityManager em,
			TestEntities te, String extId) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		BatchParametersBean params =
			createPersistentBatchParameters(em, null, te);
		BatchJobBean retVal = new BatchJobBean(params, extId);
		em.persist(retVal);
		te.add(retVal);
		return retVal;
	}

	public static TransitivityJobBean createEphemeralTransitivityJob(
			EntityManager em, String tag, TestEntities te, BatchJob job) {
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
		BatchParametersBean params =
			em.find(BatchParametersBean.class, job.getBatchParametersId());
		if (params == null) {
			throw new IllegalArgumentException("non-persistent parameters");
		}
		if (!te.contains(params)) {
			logger.warning("Adding batchJob '" + job
					+ "' to test entities that will be removed from database");
			te.add(params);
		}
		TransitivityJobBean retVal =
			new TransitivityJobBean(params, job, extId);
		te.add(retVal);
		return retVal;
	}

	public static TransitivityJobBean createEphemeralTransitivityJob(
			EntityManager em, String tag, TestEntities te) {
		BatchParametersBean params =
			createPersistentBatchParameters(em, tag, te);
		BatchJobBean job = createPersistentBatchJobBean(em, tag, te);
		TransitivityJobBean retVal = new TransitivityJobBean(params, job);
		te.add(retVal);
		return retVal;
	}

	public static TransitivityJobBean createEphemeralTransitivityJob(
			EntityManager em, TestEntities te, BatchJob job, String extId) {
		BatchParametersBean params =
			createPersistentBatchParameters(em, null, te);
		TransitivityJobBean retVal =
			new TransitivityJobBean(params, job, extId);
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

	public static TransitivityJobBean save(EntityManager em,
			TransitivityJobBean job) {
		if (job.getId() == 0) {
			em.persist(job);
		} else {
			em.merge(job);
		}
		return job;
	}

	public static BatchParametersBean findBatchParameters(EntityManager em,
			long id) {
		BatchParametersBean job = em.find(BatchParametersBean.class, id);
		return job;
	}

	public static BatchJobBean findBatchJob(EntityManager em, long id) {
		BatchJobBean job = em.find(BatchJobBean.class, id);
		return job;
	}

	public static TransitivityJobBean findTransitivityJob(EntityManager em,
			long id) {
		TransitivityJobBean job = em.find(TransitivityJobBean.class, id);
		return job;
	}

	public static List<BatchParametersBean> findAllBatchParameters(
			EntityManager em) {
		Query query =
			em.createNamedQuery(BatchParametersJPA.QN_BATCHPARAMETERS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<BatchParametersBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<BatchParametersBean>();
		}
		return entries;
	}

	public static List<BatchJobBean> findAllBatchJobs(EntityManager em) {
		Query query = em.createNamedQuery(BatchJobJPA.QN_BATCHJOB_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<BatchJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<BatchJobBean>();
		}
		return entries;
	}

	public static List<TransitivityJobBean> findAllTransitivityJobs(
			EntityManager em) {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<TransitivityJobBean>();
		}
		return entries;
	}

	public static List<TransitivityJobBean> findAllByParentId(EntityManager em,
			long batchJobId) {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID);
		query.setParameter(
				TransitivityJobJPA.PN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID_BPARENTID,
				batchJobId);
		@SuppressWarnings("unchecked")
		List<TransitivityJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<TransitivityJobBean>();
		}
		return entries;
	}

	public static void detach(EntityManager em, TransitivityJob job) {
		em.detach(job);
	}

}
