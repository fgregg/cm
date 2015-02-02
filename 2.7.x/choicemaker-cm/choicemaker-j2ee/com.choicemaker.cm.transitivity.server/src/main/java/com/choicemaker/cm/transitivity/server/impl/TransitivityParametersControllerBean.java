package com.choicemaker.cm.transitivity.server.impl;

import static com.choicemaker.cm.args.TransitivityParameters.NONPERSISTENT_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
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
public class TransitivityParametersControllerBean extends
		OabaParametersControllerBean implements
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
				if (TransitivityParametersEntity.isPersistent(p)) {
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
		if (p == null) {
			throw new IllegalArgumentException("null parameters");
		}
		final boolean persist0 = TransitivityParametersEntity.isPersistent(p);
		final int h0 = p.hashCode();

		TransitivityParametersEntity retVal = null;
		if (!persist0) {
			// The hashCode changes after saving; return a new instance
			assert p.getId() != NONPERSISTENT_ID;
			retVal = p;
			em.persist(p);
			assert retVal == p;
			assert h0 != p.hashCode();
			assert p.getId() != NONPERSISTENT_ID;
			retVal = new TransitivityParametersEntity(p.getId(),p);
		} else {
			// The hashCode is unchanged after saving; return the same instance
			p = em.merge(p);
			em.flush();
			retVal = p;
		}
		assert retVal != null;
		assert (!persist0 && retVal != p && retVal.hashCode() != h0)
				|| (retVal == p && retVal.hashCode() == h0);
		return retVal;
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
		TransitivityJob job =
			getTransJobController().findTransitivityJob(jobId);
		if (job != null) {
			long paramsId = job.getTransitivityParametersId();
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
		if (TransitivityParametersEntity.isPersistent(p)) {
			TransitivityParametersEntity bean = getBean(p);
			bean = em.merge(bean);
			em.remove(bean);
			em.flush();
		}
	}

	@Override
	public void detach(TransitivityParameters p) {
		if (TransitivityParametersEntity.isPersistent(p)) {
			TransitivityParametersEntity bean = getBean(p);
			bean = em.merge(bean);
			em.detach(p);
		}
	}

}
