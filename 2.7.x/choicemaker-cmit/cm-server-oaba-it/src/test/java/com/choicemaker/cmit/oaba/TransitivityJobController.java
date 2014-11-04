package com.choicemaker.cmit.oaba;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaBatchJobProcessingBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobBean;
import com.choicemaker.cmit.utils.EntityManagerUtils;
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

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public BatchParametersBean createPersistentBatchParameters(String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		return EntityManagerUtils.createPersistentBatchParameters(em, tag, te);
	}

	/**
	 * An externalId for the returned BatchJob is synthesized using the
	 * specified tag
	 */
	public BatchJobBean createPersistentBatchJobBean(String tag, TestEntities te) {
		return createPersistentBatchJobBean(te, EntityManagerUtils.createExternalId(tag));
	}

	/**
	 * The specified externalId is assigned without alteration to the returned
	 * BatchJob
	 */
	public BatchJobBean createPersistentBatchJobBean(TestEntities te, String extId) {
		return EntityManagerUtils.createPersistentBatchJobBean(em, te, extId);
	}

	public TransitivityJobBean createEphemeralTransitivityJob(String tag,
			TestEntities te, BatchJob job) {
		return EntityManagerUtils.createEphemeralTransitivityJob(em, tag, te, job);
	}

	public TransitivityJobBean createEphemeralTransitivityJob(String tag, TestEntities te) {
		return EntityManagerUtils.createEphemeralTransitivityJob(em, tag, te);
	}

	public TransitivityJobBean createEphemeralTransitivityJob(TestEntities te,
			BatchJob job, String extId) {
		return EntityManagerUtils.createEphemeralTransitivityJob(em, te, job, extId);
	}

	public void removeTestEntities(TestEntities te) {
		EntityManagerUtils.removeTestEntities(em, te);
	}

	public TransitivityJobBean save(TransitivityJobBean job) {
		return EntityManagerUtils.save(em, job);
	}

	public TransitivityJobBean findTransitivityJob(long id) {
		return EntityManagerUtils.findTransitivityJob(em, id);
	}

	public List<BatchParametersBean> findAllBatchParameters() {
		return EntityManagerUtils.findAllBatchParameters(em);
	}

	public List<BatchJobBean> findAllBatchJobs() {
		return EntityManagerUtils.findAllBatchJobs(em);
	}

	public List<TransitivityJobBean> findAllTransitivityJobs() {
		return EntityManagerUtils.findAllTransitivityJobs(em);
	}

	public List<TransitivityJobBean> findAllByParentId(long batchJobId) {
		return EntityManagerUtils.findAllByParentId(em, batchJobId);
	}

	public List<OabaBatchJobProcessingBean> findAllOabaProcessing() {
		return EntityManagerUtils.findAllOabaProcessing(em);
	}

	public void detach(TransitivityJob job) {
		EntityManagerUtils.detach(em, job);
	}

}
