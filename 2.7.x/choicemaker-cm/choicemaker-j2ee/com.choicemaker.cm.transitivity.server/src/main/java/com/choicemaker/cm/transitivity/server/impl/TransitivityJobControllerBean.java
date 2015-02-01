package com.choicemaker.cm.transitivity.server.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.batch.impl.BatchJobFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;

/**
 * A stateless EJB used to manage the persistence of TransitivityJobEntity
 * instances.
 * 
 * @author rphall
 */
@Stateless
public class TransitivityJobControllerBean implements TransitivityJobController {

	private static final Logger logger = Logger
			.getLogger(TransitivityJobControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private TransitivityParametersController paramsController;

	@EJB
	private ServerConfigurationController serverManager;

	@EJB
	private OabaProcessingController processingController;

	protected TransitivityJobEntity getBean(TransitivityJob oabaJob) {
		TransitivityJobEntity retVal = null;
		if (oabaJob != null) {
			final long jobId = oabaJob.getId();
			if (oabaJob instanceof TransitivityJobEntity) {
				retVal = (TransitivityJobEntity) oabaJob;
			} else {
				if (TransitivityJobEntity.isPersistent(oabaJob)) {
					retVal = em.find(TransitivityJobEntity.class, jobId);
					if (retVal == null) {
						String msg =
							"Unable to find persistent OABA job: " + jobId;
						logger.warning(msg);
					}
				}
			}
			if (retVal == null) {
				retVal = new TransitivityJobEntity(oabaJob);
			}
		}
		return retVal;
	}

	@Override
	public TransitivityJob createPersistentTransitivityJob(String externalID,
			TransitivityParameters params,
			OabaJob oabaJob, ServerConfiguration sc)
			throws ServerConfigurationException {

		if (params == null || sc == null || oabaJob == null) {
			throw new IllegalArgumentException("null argument");
		}

		// Check the OABA job persistence and status (should be completed)
		if (!BatchJobEntity.isPersistent(oabaJob)) {
			throw new IllegalArgumentException("non-persistent OABA job");
		}
		BatchJobStatus oabaStatus = oabaJob.getStatus();
		if (BatchJobStatus.COMPLETED != oabaStatus) {
			String msg = "invalid OABA job status: " + oabaStatus;
			throw new IllegalArgumentException(msg);
		}

		// Save the parameters
		paramsController.save(params);
		serverManager.save(sc);

		TransitivityJobEntity retVal =
			new TransitivityJobEntity(params, sc, oabaJob, externalID);
		em.persist(retVal);
		assert TransitivityJobEntity.isPersistent(retVal);

		// Create a new processing entry
		// FIXME null transJob, wrong log type
		OabaEventLog processing = processingController.getProcessingLog(null);

		// Create the working directory
		File workingDir = BatchJobFileUtils.createWorkingDirectory(sc, retVal);
		retVal.setWorkingDirectory(workingDir);

		// Log the job info
		logger.info("Transitivity job: " + retVal.toString());
		logger.info("Transitivity OABA job: " + oabaJob.toString());
		logger.info("Transitivity parameters: " + params.toString());
		logger.info("Server configuration: " + sc.toString());
		logger.info("Processing entry: " + processing.toString());

		return retVal;
	}

	@Override
	public TransitivityJob save(TransitivityJob batchJob) {
		return save(getBean(batchJob));
	}

	public TransitivityJobEntity save(TransitivityJobEntity job) {
		if (job == null) {
			throw new IllegalArgumentException("null job");
		}
		if (job.getId() == 0) {
			em.persist(job);
		} else {
			job = em.merge(job);
		}
		return job;
	}

	@Override
	public TransitivityJob findTransitivityJob(long id) {
		TransitivityJobEntity job = em.find(TransitivityJobEntity.class, id);
		return job;
	}

	@Override
	public List<TransitivityJob> findAllTransitivityJobs() {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityJob> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<TransitivityJob>();
		}
		return retVal;
	}

	@Override
	public List<TransitivityJobEntity> findAllByOabaJobId(long batchJobId) {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID);
		query.setParameter(
				TransitivityJobJPA.PN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID_BPARENTID,
				batchJobId);
		@SuppressWarnings("unchecked")
		List<TransitivityJobEntity> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<TransitivityJobEntity>();
		}
		return retVal;
	}

	@Override
	public void delete(TransitivityJob transitivityJob) {
		if (BatchJobEntity.isPersistent(transitivityJob)) {
			TransitivityJobEntity bean = em.find(TransitivityJobEntity.class, transitivityJob.getId());
			delete(bean);
		}
	}

	void delete(TransitivityJobEntity bean) {
		bean = em.merge(bean);
		// for (CMP_AuditEvent e : batchJob.getTimeStamps()) {
		// em.remove(e);
		// }
		em.remove(bean);
		em.flush();
	}

	@Override
	public void detach(TransitivityJob job) {
		em.detach(job);
	}

}
