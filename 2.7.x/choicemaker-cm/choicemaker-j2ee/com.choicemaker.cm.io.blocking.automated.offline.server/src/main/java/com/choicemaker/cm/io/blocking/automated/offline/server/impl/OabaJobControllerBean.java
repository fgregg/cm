package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;

/**
 * A stateless EJB used to manager the persistence of OabaJobEntity instances.
 * 
 * @author rphall
 */
@Stateless
public class OabaJobControllerBean {
	
	private static final Logger logger = Logger.getLogger(OabaJobControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private SettingsController settingsController;

	@EJB
	private ServerConfigurationController serverManager;

	@EJB
	private OabaProcessingControllerBean processingController;
	
//	@Resource
//	UserTransaction utx;

	protected OabaJobEntity getBean(OabaJob oabaJob) {
		OabaJobEntity retVal = null;
		if (oabaJob != null) {
			final long jobId = oabaJob.getId();
			if (oabaJob instanceof OabaJobEntity) {
				retVal = (OabaJobEntity) oabaJob;
			} else {
				if (OabaJobEntity.isPersistent(oabaJob)) {
					retVal = em.find(OabaJobEntity.class, jobId);
					if (retVal == null) {
						String msg = "Unable to find persistent OABA job: " + jobId;
						logger.warning(msg);
					}
				}
			}
			if (retVal == null) {
				retVal = new OabaJobEntity(oabaJob);
			}
		}
		return retVal;
	}

	public OabaJob createPersistentBatchJob(String externalID,
			OabaParameters params, OabaSettings settings,
			ServerConfiguration sc) throws ServerConfigurationException {

		if (params == null || settings == null || sc == null) {
			throw new IllegalArgumentException("null argument");
		}

		// Save the parameters
		paramsController.save(params);
		settingsController.save(settings);
		serverManager.save(sc);
		
		OabaJobEntity retVal = new OabaJobEntity(params, settings, sc, externalID);		
		em.persist(retVal);
		assert OabaJobEntity.isPersistent(retVal);

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

	public OabaJob save(OabaJob batchJob) {
		return save(getBean(batchJob));
	}

	public OabaJobEntity save(OabaJobEntity batchJob) {
		if (batchJob.getId() == 0) {
			em.persist(batchJob);
		} else {
			batchJob = em.merge(batchJob);
		}
		return batchJob;
	}

	public OabaJob find(long id) {
		OabaJobEntity batchJob = em.find(OabaJobEntity.class, id);
		return batchJob;
	}

	public List<OabaJob> findAll() {
		Query query = em.createNamedQuery(OabaJobJPA.QN_OABAJOB_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaJob> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<OabaJob>();
		}
		return entries;
	}
	
	public void delete(BatchJob oabaJob) {
		if (OabaJobEntity.isPersistent(oabaJob)) {
			OabaJobEntity bean = em.find(OabaJobEntity.class, oabaJob.getId());
			delete(bean);
		}
	}

	void delete(OabaJobEntity bean) {
		bean = em.merge(bean);
		// for (CMP_AuditEvent e : batchJob.getTimeStamps()) {
		// em.remove(e);
		// }
		em.remove(bean);
		em.flush();
	}

	public void detach(BatchJob oabaJob) {
		em.detach(oabaJob);
	}

}
