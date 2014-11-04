package com.choicemaker.cmit.oaba;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersJPA;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

/**
 * An EJB used to test BatchParameter beans within container-defined
 * transactions; see {@link BatchJobController} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
@Stateless
public class BatchParametersController {

	private static final String DEFAULT_MODEL_NAME = "FakeModelConfig";
	private static final String UNDERSCORE = "_";
	private transient Random random;

	@PostConstruct
	protected void init() {
		this.random = new Random();
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

	public Thresholds createRandomThresholds() {
		float low = random.nextFloat();
		float highRange = 1.0f - low;
		float f = random.nextFloat();
		float high = low + f * highRange;
		Thresholds retVal = new Thresholds(low, high);
		return retVal;
	}

	public BatchParametersBean createBatchParameters(String tag,
			TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		final boolean runTransitivity = false;
		BatchParametersBean retVal =
			new BatchParametersBean(createRandomModelConfigurationName(tag),
					random.nextInt(EntityManagerUtils.MAX_MAX_SINGLE),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), null, null, runTransitivity);
		em.persist(retVal);
		te.add(retVal);
		return retVal;
	}

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public BatchParametersBean save(BatchParametersBean batchParameters) {
		if (batchParameters.getId() == 0) {
			em.persist(batchParameters);
		} else {
			batchParameters = em.merge(batchParameters);
		}
		return batchParameters;
	}

	public BatchParametersBean find(long id) {
		BatchParametersBean batchParameters =
			em.find(BatchParametersBean.class, id);
		return batchParameters;
	}

//	public List<BatchParametersBean> findAll() {
//		Query query =
//			em.createNamedQuery(BatchParametersJPA.QN_BATCHPARAMETERS_FIND_ALL);
//		@SuppressWarnings("unchecked")
//		List<BatchParametersBean> entries = query.getResultList();
//		if (entries == null) {
//			entries = new ArrayList<BatchParametersBean>();
//		}
//		return entries;
//	}

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

//	public List<TransitivityJobBean> findAllTransitivityJobs() {
//		Query query =
//			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
//		@SuppressWarnings("unchecked")
//		List<TransitivityJobBean> entries = query.getResultList();
//		if (entries == null) {
//			entries = new ArrayList<TransitivityJobBean>();
//		}
//		return entries;
//	}

	public void removeTestEntities(TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		te.removePersistentObjects(em);
	}

	public void delete(BatchParametersBean batchParameters) {
		batchParameters = em.merge(batchParameters);
		em.remove(batchParameters);
		em.flush();
	}

	public void detach(BatchParameters batchParameters) {
		em.detach(batchParameters);
	}

}
