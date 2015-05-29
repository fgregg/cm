package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.args.PersistableSqlRecordSource.TYPE;
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
import com.choicemaker.cm.args.PersistableSqlRecordSource;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.AbstractPersistentObject;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SqlRecordSourceController;

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

	@EJB
	private SqlRecordSourceController sqlController;

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

	public static boolean isValidSqlRecordSourceType(String type) {
		boolean retVal = true;
		if (type == null) {
			logger.warning("null SQL record source type");
			retVal = false;
		} else if (!TYPE.equals(type)) {
			logger.warning("invalid SQL record source type: '" + type + "'");
			retVal = false;
		} else {
			assert TYPE.equals(type);
			assert retVal == true;
		}
		return retVal;
	}

	public boolean isValidSqlRecordSourceId(Long id) {
		boolean retVal = true;
		if (id == null) {
			logger.warning("null SQL record source id");
			retVal = false;
		} else if (!AbstractPersistentObject.isPersistentId(id)) {
			logger.warning("non-persistent SQL record source id: " + id);
			retVal = false;
		} else {
			assert AbstractPersistentObject.isPersistentId(id);
			assert retVal == true;
		}
		return retVal;
	}

	protected String getDatabaseConfiguration(String type, Long id) {
		String retVal = null;
		if (!isValidSqlRecordSourceType(type) || !isValidSqlRecordSourceId(id)) {
			String msg = "database configuration: null";
			logger.warning(msg);
			assert retVal == null;
		} else {
			PersistableSqlRecordSource psrs = sqlController.find(id, type);
			retVal = psrs.getDatabaseConfiguration();
			String msg = "database configuration: " + retVal;
			logger.fine(msg);
			assert retVal != null;
		}
		return retVal;
	}

	protected String getDatabaseAccessor(String type, Long id) {
		String retVal = null;
		if (!isValidSqlRecordSourceType(type) || !isValidSqlRecordSourceId(id)) {
			String msg = "database accessor: null";
			logger.warning(msg);
			assert retVal == null;
		} else {
			PersistableSqlRecordSource psrs = sqlController.find(id, type);
			retVal = psrs.getDatabaseAccessor();
			String msg = "database accessor: " + retVal;
			logger.fine(msg);
			assert retVal != null;
		}
		return retVal;
	}

	@Override
	public String getQueryDatabaseConfiguration(OabaParameters oabaParams) {
		if (oabaParams == null) {
			throw new IllegalArgumentException("null parameters");
		}
		final String type = oabaParams.getQueryRsType();
		final Long id = oabaParams.getQueryRsId();
		return getDatabaseConfiguration(type, id);
	}

	@Override
	public String getQueryDatabaseAccessor(OabaParameters oabaParams) {
		if (oabaParams == null) {
			throw new IllegalArgumentException("null parameters");
		}
		final String type = oabaParams.getQueryRsType();
		final Long id = oabaParams.getQueryRsId();
		return getDatabaseAccessor(type, id);
	}

	@Override
	public String getReferenceDatabaseConfiguration(OabaParameters oabaParams) {
		if (oabaParams == null) {
			throw new IllegalArgumentException("null parameters");
		}
		final String type = oabaParams.getReferenceRsType();
		final Long id = oabaParams.getReferenceRsId();
		return getDatabaseConfiguration(type, id);
	}

	@Override
	public String getReferenceDatabaseAccessor(OabaParameters oabaParams) {
		if (oabaParams == null) {
			throw new IllegalArgumentException("null parameters");
		}
		final String type = oabaParams.getReferenceRsType();
		final Long id = oabaParams.getReferenceRsId();
		return getDatabaseAccessor(type, id);
	}

}
