package com.choicemaker.cm.transitivity.server.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobProcessingEvent;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.impl.BatchJobFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
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
	private OabaSettingsController settingsController;

	@EJB
	private ServerConfigurationController serverManager;

	@EJB
	private ProcessingController processingController;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/transStatusTopic")
	private Topic transStatusTopic;

	protected TransitivityJobEntity getBean(BatchJob oabaJob) {
		TransitivityJobEntity retVal = null;
		if (oabaJob != null) {
			final long jobId = oabaJob.getId();
			if (oabaJob instanceof TransitivityJobEntity) {
				retVal = (TransitivityJobEntity) oabaJob;
			} else {
				if (oabaJob.isPersistent()) {
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
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public BatchJob createPersistentTransitivityJob(String externalID,
			TransitivityParameters params, BatchJob batchJob,
			OabaSettings settings, ServerConfiguration sc)
			throws ServerConfigurationException {

		if (params == null || sc == null || batchJob == null) {
			throw new IllegalArgumentException("null argument");
		}

		// Check the OABA job persistence and status (should be completed)
		if (!batchJob.isPersistent()) {
			throw new IllegalArgumentException("non-persistent OABA job");
		}
		BatchJobStatus oabaStatus = batchJob.getStatus();
		String msg0 = "Precedessor OABA (job " + batchJob.getId() + ") status: "
				+ oabaStatus;
		logger.info(msg0);
		if (BatchJobStatus.COMPLETED != oabaStatus) {
			String msg = "INVALID: " + msg0;
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}

		// Save the parameters
		paramsController.save(params);
		settingsController.save(settings);
		serverManager.save(sc);

		TransitivityJobEntity retVal =
			new TransitivityJobEntity(params, settings, sc, batchJob, externalID);
		em.persist(retVal);
		assert retVal.isPersistent();

		// Create a new processing entry
//		ProcessingEventLog processing =
//			processingController.getProcessingLog(retVal);
		// Create a new entry in the processing log and check it
		TransitivityProcessingControllerBean.updateStatusWithNotification(em,
				jmsContext, transStatusTopic, retVal, BatchProcessingEvent.INIT,
				new Date(), null);
		BatchJobProcessingEvent ope =
				TransitivityProcessingControllerBean.getCurrentBatchProcessingEvent(em,
					retVal);
		ProcessingEvent currentProcessingEvent = ope.getProcessingEvent();
		assert currentProcessingEvent.getEventId() == BatchProcessingEvent.INIT
				.getEventId();
		assert currentProcessingEvent.getPercentComplete() == BatchProcessingEvent.INIT
				.getPercentComplete();

		// Create the working directory
		File workingDir = BatchJobFileUtils.createWorkingDirectory(sc, retVal);
		retVal.setWorkingDirectory(workingDir);

		// Log the job info
		logger.info("Transitivity job: " + retVal.toString());
		logger.info("Transitivity OABA job: " + batchJob.toString());
		logger.info("Transitivity parameters: " + params.toString());
		logger.info("Server configuration: " + sc.toString());
		logger.info("Current processing event: " + currentProcessingEvent);

		return retVal;
	}

	@Override
	public BatchJob save(BatchJob batchJob) {
		return save(getBean(batchJob));
	}

	public TransitivityJobEntity save(TransitivityJobEntity job) {
		if (job == null) {
			throw new IllegalArgumentException("null job");
		}
		if (!job.isPersistent()) {
			em.persist(job);
		} else {
			job = em.merge(job);
		}
		return job;
	}

	@Override
	public BatchJob findTransitivityJob(long id) {
		TransitivityJobEntity job = em.find(TransitivityJobEntity.class, id);
		return job;
	}

	@Override
	public List<BatchJob> findAllTransitivityJobs() {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<BatchJob> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<BatchJob>();
		}
		return retVal;
	}

	@Override
	public List<BatchJob> findAllByOabaJobId(long batchJobId) {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID);
		query.setParameter(
				TransitivityJobJPA.PN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID_BPARENTID,
				batchJobId);
		@SuppressWarnings("unchecked")
		List<BatchJob> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<BatchJob>();
		}
		return retVal;
	}

	@Override
	public void delete(BatchJob transitivityJob) {
		if (transitivityJob.isPersistent()) {
			TransitivityJobEntity bean =
				em.find(TransitivityJobEntity.class, transitivityJob.getId());
			delete(bean);
		}
	}

	void delete(TransitivityJobEntity bean) {
		if (bean != null) {
			bean = em.merge(bean);
			em.remove(bean);
			em.flush();
		}
	}

	@Override
	public void detach(BatchJob job) {
		em.detach(job);
	}

}
