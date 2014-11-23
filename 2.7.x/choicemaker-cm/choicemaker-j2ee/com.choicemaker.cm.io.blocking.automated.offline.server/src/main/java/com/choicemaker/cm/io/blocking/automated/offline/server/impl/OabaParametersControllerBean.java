package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;

/**
 * An EJB used to test BatchParameter beans within container-defined
 * transactions; see {@link OabaJobControllerBean} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
@Stateless
public class OabaParametersControllerBean {

	private static final Logger logger = Logger
			.getLogger(OabaParametersControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;
	
	@EJB
	private OabaJobControllerBean jobController;

	protected OabaParametersEntity getBean(OabaParameters oabaParameters) {
		OabaParametersEntity retVal = null;
		if (oabaParameters != null) {
			final long jobId = oabaParameters.getId();
			if (oabaParameters instanceof OabaParametersEntity) {
				retVal = (OabaParametersEntity) oabaParameters;
			} else {
				if (OabaParametersEntity.isPersistent(oabaParameters)) {
					retVal = em.find(OabaParametersEntity.class, jobId);
					if (retVal == null) {
						String msg =
							"Unable to find persistent OABA job: " + jobId;
						logger.warning(msg);
					}
				}
			}
			if (retVal == null) {
				retVal = new OabaParametersEntity(oabaParameters);
			}
		}
		return retVal;
	}

	public OabaParameters save(OabaParameters oabaParameters) {
		return save(getBean(oabaParameters));
	}

	OabaParametersEntity save(OabaParametersEntity oabaParameters) {
		if (oabaParameters.getId() == 0) {
			em.persist(oabaParameters);
		} else {
			oabaParameters = em.merge(oabaParameters);
			em.flush();
		}
		return oabaParameters;
	}

	public OabaParameters find(long id) {
		OabaParametersEntity oabaParameters =
			em.find(OabaParametersEntity.class, id);
		return oabaParameters;
	}

	public OabaParameters findBatchParamsByJobId(long jobId) {
		OabaParameters retVal = null;
		OabaJob oabaJob = jobController.find(jobId);
		if (oabaJob != null) {
			long paramsId = oabaJob.getParametersId();
			retVal = find(paramsId);
		}
		return retVal;
	}

	public List<OabaParameters> findAllBatchParameters() {
		Query query =
			em.createNamedQuery(OabaParametersJPA.QN_BATCHPARAMETERS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaParameters> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<OabaParameters>();
		}
		return entries;
	}

	public void delete(OabaParameters oabaParameters) {
		if (OabaParametersEntity.isPersistent(oabaParameters)) {
			OabaParametersEntity bean = getBean(oabaParameters);
			bean = em.merge(bean);
			em.remove(bean);
			em.flush();
		}
	}

	public void detach(OabaParameters oabaParameters) {
		if (OabaParametersEntity.isPersistent(oabaParameters)) {
			OabaParametersEntity bean = getBean(oabaParameters);
			bean = em.merge(bean);
			em.detach(oabaParameters);
		}
	}

}
