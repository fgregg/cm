package com.choicemaker.cm.transitivity.server.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;

/**
 * A stateless EJB used to manager the persistence of TransitivityJobEntity instances.
 * 
 * @author rphall
 */
@Stateless
public class TransitivityJobControllerBean {

	private static final Logger logger = Logger.getLogger(TransitivityJobControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private ServerConfigurationController serverManager;

	@EJB
	private OabaProcessingControllerBean processingController;
	
//	@Resource
//	UserTransaction utx;

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
						String msg = "Unable to find persistent OABA job: " + jobId;
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

	public TransitivityJob createPersistentBatchJob(String externalID,
			OabaParameters params, OabaSettings settings,
			ServerConfiguration sc) throws ServerConfigurationException {

		if (params == null || settings == null || sc == null) {
			throw new IllegalArgumentException("null argument");
		}

		// Save the parameters
		paramsController.save(params);
		oabaSettingsController.save(settings);
		serverManager.save(sc);
		
		TransitivityJobEntity retVal = new TransitivityJobEntity(params, settings, sc, externalID);		
		em.persist(retVal);
		assert TransitivityJobEntity.isPersistent(retVal);

		// Create a new processing entry
		OabaProcessing processing =
			processingController.createPersistentProcessingLogForBatchJob(retVal);

		// Create the working directory
		File workingDir = OabaFileUtils.createWorkingDirectory(sc, retVal);
		retVal.setWorkingDirectory(workingDir);
		
		// Log the job info
		logger.info("Oaba job: " + retVal.toString());
		logger.info("Oaba parameters: " + params.toString());
		logger.info("Oaba settings: " + settings.toString());
		logger.info("Server configuration: " + sc.toString());
		logger.info("Processing entry: " + processing.toString());

		return retVal;
	}

	public TransitivityJobEntity save(TransitivityJobEntity job) {
		if (job == null) {
			throw new IllegalArgumentException("null job");
		}
		if (job.getId() == 0) {
			em.persist(job);
		} else {
			em.merge(job);
		}
		return job;
	}

	public TransitivityJobEntity findTransitivityJob(long id) {
		TransitivityJobEntity job = em.find(TransitivityJobEntity.class, id);
		return job;
	}

	public List<TransitivityJobEntity> findAllTransitivityJobs() {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityJobEntity> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<TransitivityJobEntity>();
		}
		return retVal;
	}

	public List<TransitivityJobEntity> findAllByParentId(long batchJobId) {
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

	public void detach(TransitivityJob job) {
		em.detach(job);
	}

}
