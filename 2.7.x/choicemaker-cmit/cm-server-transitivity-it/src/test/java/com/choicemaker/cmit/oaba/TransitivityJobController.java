package com.choicemaker.cmit.oaba;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
import com.choicemaker.cmit.utils.TestEntities;

/**
 * An EJB used to test TransitivityJob beans within container-defined
 * transactions; see {@link BatchJobController} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
@Stateless
public class TransitivityJobController {

	private static final Logger logger = Logger
			.getLogger(TransitivityJobController.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	private static final String DEFAULT_TAG = "Random external id";
	private static final String DEFAULT_MODEL_NAME = "FakeModelConfig";
	private static final String COLON = ":";
	private static final String SPACE = " ";
	private static final String UNDERSCORE = "_";
	private static final String TAG_DELIMITER = ": ";
	private static final int MAX_MAX_SINGLE = 1000;

	private transient Random random;

	@PostConstruct
	protected void init() {
		this.random = new Random();
	}

	/** Synthesizes an externalId using the specified tag which may be null */
	public String createExternalId(String tag) {
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
	public String createRandomModelConfigurationName(String tag) {
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

	private Thresholds createRandomThresholds() {
		float low = random.nextFloat();
		float highRange = 1.0f - low;
		float f = random.nextFloat();
		float high = low + f * highRange;
		Thresholds retVal = new Thresholds(low, high);
		return retVal;
	}

	private BatchParametersBean createBatchParameters(String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		BatchParametersBean retVal = new BatchParametersBean(
				createRandomModelConfigurationName(tag),
				random.nextInt(MAX_MAX_SINGLE), thresholds.getDifferThreshold(),
				thresholds.getMatchThreshold(),
				null,
				null
		);
		em.persist(retVal);
		te.add(retVal);
		return retVal;
	}

	/**
	 * An externalId for the returned BatchJob is synthesized using the
	 * specified tag
	 */
	public BatchJobBean createBatchJobBean(String tag, TestEntities te) {
		return createBatchJobBean(te, createExternalId(tag));
	}

	/**
	 * The specified externalId is assigned without alteration to the returned
	 * BatchJob
	 */
	private BatchJobBean createBatchJobBean(TestEntities te, String extId) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		BatchJobBean retVal = new BatchJobBean(extId);
		em.persist(retVal);
		te.add(retVal);
		return retVal;
	}

	// public BatchJobBean createBatchJob(String tag, TestEntities te) {
	// if (te == null) {
	// throw new IllegalArgumentException("null test entities");
	// }
	// throw new Error("not yet implemented");
	// }

	// private BatchJobBean createBatchJob(String tag, TestEntities te,
	// BatchParameters params) {
	// if (te == null) {
	// throw new IllegalArgumentException("null test entities");
	// }
	// throw new Error("not yet implemented");
	// }

	// private BatchJobBean createBatchJob(TestEntities te, BatchParameters
	// params,
	// String extId) {
	// if (te == null) {
	// throw new IllegalArgumentException("null test entities");
	// }
	// throw new Error("not yet implemented");
	// }

	public TransitivityJobBean createTransitivityJob(String tag,
			TestEntities te, BatchJob job) {
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

	// private TransitivityJobBean createTransitivityJob(String tag,
	// TestEntities te, BatchParameters params, BatchJob job) {
	// if (te == null) {
	// throw new IllegalArgumentException("null test entities");
	// }
	// throw new Error("not yet implemented");
	// }

	public TransitivityJobBean createTransitivityJob(String tag, TestEntities te) {
		BatchParametersBean params = createBatchParameters(tag,te);
		BatchJobBean job = createBatchJobBean(tag,te);
		TransitivityJobBean retVal = new TransitivityJobBean(params,job);
		te.add(retVal);
		return retVal;
	}

	// private TransitivityJobBean createTransitivityJob(TestEntities te,
	// String extId) {
	// if (te == null) {
	// throw new IllegalArgumentException("null test entities");
	// }
	// throw new Error("not yet implemented");
	// }

	public TransitivityJobBean createTransitivityJob(TestEntities te,
			BatchJob job, String extId) {
		BatchParametersBean params = createBatchParameters(null,te);
		TransitivityJobBean retVal = new TransitivityJobBean(params,job,extId);
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		te.add(retVal);
		return retVal;
	}

	public void removeTestEntities(TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		te.removePersistentObjects(em);
	}

	public TransitivityJobBean save(TransitivityJobBean job) {
		if (job.getId() == 0) {
			em.persist(job);
		} else {
			em.merge(job);
		}
		return job;
	}

	public TransitivityJobBean find(long id) {
		TransitivityJobBean job = em.find(TransitivityJobBean.class, id);
		return job;
	}

	public List<BatchParametersBean> findAllBatchParameters() {
		Query query =
				em.createNamedQuery(BatchParametersJPA.QN_BATCHPARAMETERS_FIND_ALL);
			@SuppressWarnings("unchecked")
			List<BatchParametersBean> entries = query.getResultList();
			if (entries == null) {
				entries = new ArrayList<BatchParametersBean>();
			}
			return entries;
	}

	public List<BatchJobBean> findAllBatchJobs() {
		Query query =
				em.createNamedQuery(BatchJobJPA.QN_BATCHJOB_FIND_ALL);
			@SuppressWarnings("unchecked")
			List<BatchJobBean> entries = query.getResultList();
			if (entries == null) {
				entries = new ArrayList<BatchJobBean>();
			}
			return entries;
	}

	public List<TransitivityJobBean> findAllTransitivityJobs() {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<TransitivityJobBean>();
		}
		return entries;
	}

	public List<TransitivityJobBean> findAllByParentId(long batchJobId) {
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

	// public void deleteTransitivityJobAndParent(TransitivityJob job) {
	// if (job != null) {
	// final long parentId = job.getBatchParentId();
	// assert parentId != BatchJob.INVALID_BATCHJOB_ID;
	//
	// if (BatchJobBean.isPersistent(job)) {
	// final long jobId = job.getId();
	// TransitivityJob persistentJob = em.find(TransitivityJobBean.class,
	// jobId);
	// if (persistentJob != null) {
	// em.remove(persistentJob);
	// }
	// }
	//
	// BatchJob parent = em.find(BatchJobBean.class, parentId);
	// if (parent != null) {
	// em.remove(parent);
	// }
	// }
	// }

	public void detach(TransitivityJob job) {
		em.detach(job);
	}

}
