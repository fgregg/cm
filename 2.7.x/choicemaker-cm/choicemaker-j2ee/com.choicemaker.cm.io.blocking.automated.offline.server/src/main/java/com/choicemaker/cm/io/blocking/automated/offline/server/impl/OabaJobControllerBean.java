package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.batch.impl.BatchJobJPA.PN_BATCHJOB_FIND_BY_JOBID_P1;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.QN_BATCHJOB_FIND_BY_JOBID;

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
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobProcessingEvent;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.impl.BatchJobFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;

/**
 * A stateless EJB used to manage the persistence of OabaJobEntity instances.
 * 
 * @author rphall
 */
@Stateless
public class OabaJobControllerBean implements OabaJobController {

	private static final Logger logger = Logger
			.getLogger(OabaJobControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private ServerConfigurationController serverManager;

	@EJB
	private ProcessingController processingController;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/statusTopic")
	private Topic oabaStatusTopic;

	protected OabaJobEntity getBean(BatchJob batchJob) {
		OabaJobEntity retVal = null;
		if (batchJob != null) {
			final long jobId = batchJob.getId();
			if (batchJob instanceof OabaJobEntity) {
				retVal = (OabaJobEntity) batchJob;
			} else {
				if (batchJob.isPersistent()) {
					retVal = em.find(OabaJobEntity.class, jobId);
					if (retVal == null) {
						String msg =
							"Unable to find persistent OABA job: " + jobId;
						logger.warning(msg);
					}
				}
			}
			if (retVal == null) {
				retVal = new OabaJobEntity(batchJob);
			}
		}
		return retVal;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public BatchJob createPersistentOabaJob(String externalID,
			OabaParameters params, OabaSettings settings, ServerConfiguration sc)
			throws ServerConfigurationException {
		return createPersistentOabaJob(externalID, params, settings, sc, null);
	}

	@Override
	public BatchJob createPersistentOabaJob(String externalID,
			OabaParameters params, OabaSettings settings,
			ServerConfiguration sc, BatchJob urmJob)
			throws ServerConfigurationException {
		if (params == null || settings == null || sc == null) {
			throw new IllegalArgumentException("null argument");
		}

		// Save the parameters
		paramsController.save(params);
		oabaSettingsController.save(settings);
		serverManager.save(sc);

		OabaJobEntity retVal =
			new OabaJobEntity(params, settings, sc, urmJob, externalID);
		em.persist(retVal);
		assert retVal.isPersistent();

		// Create a new entry in the processing log and check it
		OabaProcessingControllerBean.updateStatusWithNotification(em,
				jmsContext, oabaStatusTopic, retVal, BatchProcessingEvent.INIT,
				new Date(), null);
		BatchJobProcessingEvent ope =
			OabaProcessingControllerBean.getCurrentBatchProcessingEvent(em,
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
		logger.info("Oaba job: " + retVal.toString());
		logger.info("Oaba parameters: " + params.toString());
		logger.info("Oaba settings: " + settings.toString());
		logger.info("Server configuration: " + sc.toString());
		logger.info("Current processing event: " + currentProcessingEvent);

		return retVal;
	}

	@Override
	public BatchJob save(BatchJob batchJob) {
		return save(getBean(batchJob));
	}

	public OabaJobEntity save(OabaJobEntity job) {
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
	public BatchJob findOabaJob(long id) {
		OabaJobEntity batchJob = em.find(OabaJobEntity.class, id);
		return batchJob;
	}

	@Override
	public List<BatchJob> findAll() {
		Query query = em.createNamedQuery(OabaJobJPA.QN_OABAJOB_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<BatchJob> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<BatchJob>();
		}
		return retVal;
	}

	@Override
	public void delete(BatchJob batchJob) {
		if (batchJob.isPersistent()) {
			OabaJobEntity bean = em.find(OabaJobEntity.class, batchJob.getId());
			delete(bean);
		}
	}

	void delete(OabaJobEntity bean) {
		if (bean != null) {
			bean = em.merge(bean);
			em.remove(bean);
			em.flush();
		}
	}

	@Override
	public void detach(BatchJob oabaJob) {
		em.detach(oabaJob);
	}

	@Override
	public BatchJob findBatchJob(long id) {
		Query query = em.createNamedQuery(QN_BATCHJOB_FIND_BY_JOBID);
		query.setParameter(PN_BATCHJOB_FIND_BY_JOBID_P1, id);
		@SuppressWarnings("unchecked")
		List<BatchJob> entries = query.getResultList();
		if (entries != null && entries.size() > 1) {
			String msg = "Violates primary key constraint: " + entries.size();
			logger.severe(msg);
			throw new IllegalStateException(msg);
		}
		BatchJob retVal = null;
		if (entries != null && !entries.isEmpty()) {
			assert entries.size() == 1;
			retVal = entries.get(0);
		}
		return retVal;
	}

}
