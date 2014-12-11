package com.choicemaker.cm.transitivity.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;

/**
 * An EJB used to test TransitivityParameter beans within container-defined
 * transactions; see {@link TransitivityJobControllerBean} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
// @Stateless
public class TransitivityParametersControllerBean extends
		OabaParametersControllerBean {

	private static final Logger logger = Logger
			.getLogger(TransitivityParametersControllerBean.class.getName());

	// @PersistenceContext(unitName = "oaba")
	private EntityManager em;
	
	// @EJB
	private TransitivityJobControllerBean jobController;

	protected TransitivityParametersEntity getBean(TransitivityParameters transitivityParameters) {
		TransitivityParametersEntity retVal = null;
		if (transitivityParameters != null) {
			final long jobId = transitivityParameters.getId();
			if (transitivityParameters instanceof TransitivityParametersEntity) {
				retVal = (TransitivityParametersEntity) transitivityParameters;
			} else {
				if (TransitivityParametersEntity.isPersistent(transitivityParameters)) {
					retVal = em.find(TransitivityParametersEntity.class, jobId);
					if (retVal == null) {
						String msg =
							"Unable to find persistent OABA job: " + jobId;
						logger.warning(msg);
					}
				}
			}
			if (retVal == null) {
				retVal = new TransitivityParametersEntity(transitivityParameters);
			}
		}
		return retVal;
	}

	TransitivityParameters save(TransitivityParameters transitivityParameters) {
		return save(getBean(transitivityParameters));
	}

	TransitivityParametersEntity save(TransitivityParametersEntity transitivityParameters) {
		if (transitivityParameters.getId() == 0) {
			em.persist(transitivityParameters);
		} else {
			transitivityParameters = em.merge(transitivityParameters);
			em.flush();
		}
		return transitivityParameters;
	}

	public TransitivityParameters findTransitivityParameters(long id) {
		TransitivityParametersEntity transitivityParameters =
			em.find(TransitivityParametersEntity.class, id);
		return transitivityParameters;
	}

	public TransitivityParameters findTransitivityParamsByJobId(long jobId) {
		TransitivityParameters retVal = null;
		TransitivityJob oabaJob = jobController.findTransitivityJob(jobId);
		if (oabaJob != null) {
			long paramsId = oabaJob.getParametersId();
			retVal = findTransitivityParameters(paramsId);
		}
		return retVal;
	}

	public List<TransitivityParameters> findAllTransitivityParameters() {
		Query query =
			em.createNamedQuery(TransitivityParametersJPA.QN_TRANSPARAMS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityParameters> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<TransitivityParameters>();
		}
		return entries;
	}

	public void delete(TransitivityParameters transitivityParameters) {
		transitivityParameters = em.merge(transitivityParameters);
		em.remove(transitivityParameters);
		em.flush();
	}

	public void detach(TransitivityParameters transitivityParameters) {
		em.detach(transitivityParameters);
	}

}
