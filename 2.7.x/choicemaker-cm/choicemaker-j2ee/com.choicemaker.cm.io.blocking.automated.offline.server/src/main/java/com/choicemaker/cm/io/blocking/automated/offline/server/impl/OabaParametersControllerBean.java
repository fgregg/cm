package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;

/**
 * An EJB used to test BatchParameter beans within container-defined
 * transactions; see {@link OabaJobControllerBean} as an example of a similar
 * controller.
 *
 * @author rphall
 */
@Stateless
public class OabaParametersControllerBean implements OabaParametersController {

	private static final Logger logger = Logger
			.getLogger(OabaParametersControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobController jobController;

	protected OabaJobController getOabaJobController() {
		return jobController;
	}

	protected OabaParametersEntity getBean(OabaParameters p) {
		OabaParametersEntity retVal = null;
		if (p != null) {
			final long jobId = p.getId();
			if (p instanceof OabaParametersEntity) {
				retVal = (OabaParametersEntity) p;
			} else {
				if (p.isPersistent()) {
					retVal = em.find(OabaParametersEntity.class, jobId);
					if (retVal == null) {
						String msg =
							"Unable to find persistent OABA job: " + jobId;
						logger.warning(msg);
					}
				}
			}
			if (retVal == null) {
				retVal = new OabaParametersEntity(p);
			}
		}
		return retVal;
	}

	@Override
	public OabaParameters save(OabaParameters p) {
		return save(getBean(p));
	}

	OabaParametersEntity save(OabaParametersEntity p) {
		if (p.getId() == 0) {
			em.persist(p);
		} else {
			p = em.merge(p);
			em.flush();
		}
		return p;
	}

	@Override
	public OabaParameters findOabaParameters(long id) {
		OabaParametersEntity p = em.find(OabaParametersEntity.class, id);
		return p;
	}

	@Override
	public OabaParameters findOabaParametersByJobId(long jobId) {
		OabaParameters retVal = null;
		OabaJob job = getOabaJobController().findOabaJob(jobId);
		if (job != null) {
			long paramsId = job.getOabaParametersId();
			retVal = findOabaParameters(paramsId);
		}
		return retVal;
	}

	@Override
	public List<OabaParameters> findAllOabaParameters() {
		Query query =
			em.createNamedQuery(AbstractParametersJPA.QN_OABAPARAMETERS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaParameters> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<OabaParameters>();
		}
		return entries;
	}

	@Override
	public void delete(OabaParameters p) {
		if (p.isPersistent()) {
			OabaParametersEntity bean = getBean(p);
			bean = em.merge(bean);
			em.remove(bean);
			em.flush();
		}
	}

	@Override
	public void detach(OabaParameters p) {
		if (p.isPersistent()) {
			OabaParametersEntity bean = getBean(p);
			bean = em.merge(bean);
			em.detach(p);
		}
	}

}
