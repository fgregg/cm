package com.choicemaker.cm.transitivity.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;

/**
 * An EJB used to test TransitivityParameter beans within container-defined
 * transactions; see {@link TransitivityJobControllerBean} as an example of a
 * similar controller.
 *
 * @author rphall
 */
@Stateless
public class TransitivityParametersControllerBean implements
		TransitivityParametersController {

	private static final Logger logger = Logger
			.getLogger(TransitivityParametersControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private TransitivityJobController jobController;

	protected TransitivityJobController getTransJobController() {
		return jobController;
	}

	protected TransitivityParametersEntity getBean(TransitivityParameters p) {
		TransitivityParametersEntity retVal = null;
		if (p != null) {
			final long jobId = p.getId();
			if (p instanceof TransitivityParametersEntity) {
				retVal = (TransitivityParametersEntity) p;
			} else {
				if (p.isPersistent()) {
					retVal = em.find(TransitivityParametersEntity.class, jobId);
					if (retVal == null) {
						String msg =
							"Unable to find persistent Transitivity job: "
									+ jobId;
						logger.warning(msg);
					}
				}
			}
			if (retVal == null) {
				retVal = new TransitivityParametersEntity(p);
			}
		}
		return retVal;
	}

	@Override
	public TransitivityParameters save(TransitivityParameters p) {
		return save(getBean(p));
	}

	TransitivityParametersEntity save(TransitivityParametersEntity p) {
		if (p.getId() == 0) {
			em.persist(p);
		} else {
			p = em.merge(p);
			em.flush();
		}
		return p;
	}

	@Override
	public TransitivityParameters findTransitivityParameters(long id) {
		TransitivityParametersEntity p =
			em.find(TransitivityParametersEntity.class, id);
		return p;
	}

	@Override
	public TransitivityParameters findTransitivityParametersByJobId(long jobId) {
		TransitivityParameters retVal = null;
		BatchJob job =
			getTransJobController().findTransitivityJob(jobId);
		if (job != null) {
			long paramsId = job.getParametersId();
			retVal = findTransitivityParameters(paramsId);
		}
		return retVal;
	}

	@Override
	public List<TransitivityParameters> findAllTransitivityParameters() {
		Query query =
			em.createNamedQuery(TransitivityParametersJPA.QN_TRANSPARAMETERS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityParameters> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<TransitivityParameters>();
		}
		return entries;
	}

	@Override
	public void delete(TransitivityParameters p) {
		if (p.isPersistent()) {
			TransitivityParametersEntity bean = getBean(p);
			bean = em.merge(bean);
			em.remove(bean);
			em.flush();
		}
	}

	@Override
	public void detach(TransitivityParameters p) {
		if (p.isPersistent()) {
			TransitivityParametersEntity bean = getBean(p);
			bean = em.merge(bean);
			em.detach(p);
		}
	}

}
