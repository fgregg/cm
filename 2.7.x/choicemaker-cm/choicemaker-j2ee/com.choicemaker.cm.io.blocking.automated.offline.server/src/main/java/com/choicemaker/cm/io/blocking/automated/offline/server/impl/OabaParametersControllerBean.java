package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.batch.impl.BatchJobJPA.PN_BATCHJOB_FIND_BY_JOBID_P1;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.QN_BATCHJOB_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.PN_PARAMETERS_FIND_BY_ID_P1;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.QN_OABAPARAMETERS_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.QN_PARAMETERS_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.QN_PARAMETERS_FIND_BY_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.batch.BatchJob;
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
		logger.fine("Saving " + p);
		if (p.getId() == 0) {
			em.persist(p);
			logger.fine("Saved " + p);
		} else {
			p = em.merge(p);
			em.flush();
			logger.fine("Merged " + p);
		}
		return p;
	}

	@Override
	public OabaParameters findOabaParameters(long id) {
		OabaParametersEntity p = em.find(OabaParametersEntity.class, id);
		return p;
	}

	/** Finds any instance of AbstractParametersEntity */
	@Override
	public AbstractParametersEntity findParameters(long id) {
		AbstractParametersEntity retVal = null;
		Query query = em.createNamedQuery(QN_PARAMETERS_FIND_BY_ID);
		query.setParameter(PN_PARAMETERS_FIND_BY_ID_P1, id);
		@SuppressWarnings("unchecked")
		List<AbstractParametersEntity> entries = query.getResultList();
		if (entries != null && entries.size() > 1) {
			String msg = "Violates primary key constraint: " + entries.size();
			logger.severe(msg);
			throw new IllegalStateException(msg);
		}
		if (entries != null && !entries.isEmpty()) {
			assert entries.size() == 1;
			retVal = entries.get(0);
		}
		return retVal;
	}

	@Override
	public OabaParameters findOabaParametersByBatchJobId(long jobId) {
		OabaParameters retVal = null;
		Query query = em.createNamedQuery(QN_BATCHJOB_FIND_BY_JOBID);
		query.setParameter(PN_BATCHJOB_FIND_BY_JOBID_P1, jobId);
		@SuppressWarnings("unchecked")
		List<BatchJob> entries = query.getResultList();
		if (entries != null && entries.size() > 1) {
			String msg = "Violates 1:{0,1} relationship: " + entries.size();
			logger.severe(msg);
			throw new IllegalStateException(msg);
		}
		if (entries != null && !entries.isEmpty()) {
			assert entries.size() == 1;
			BatchJob batchJob = entries.get(0);
			long paramsId = batchJob.getParametersId();
			AbstractParametersEntity ape = findParameters(paramsId);
			if (ape != null && !(ape instanceof OabaParameters)) {
				String msg =
					"Invalid instance: " + paramsId + ", "
							+ ape.getClass().getName();
				logger.severe(msg);
				throw new IllegalStateException(msg);
			} else {
				retVal = (OabaParameters) ape;
			}
		}
		return retVal;
	}

	@Override
	public List<OabaParameters> findAllOabaParameters() {
		Query query = em.createNamedQuery(QN_OABAPARAMETERS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaParameters> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<OabaParameters>();
		}
		return entries;
	}

	/** Finds all subclasses of AbstractParametersEntity */
	@Override
	public List<AbstractParametersEntity> findAllParameters() {
		Query query = em.createNamedQuery(QN_PARAMETERS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<AbstractParametersEntity> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<AbstractParametersEntity>();
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
