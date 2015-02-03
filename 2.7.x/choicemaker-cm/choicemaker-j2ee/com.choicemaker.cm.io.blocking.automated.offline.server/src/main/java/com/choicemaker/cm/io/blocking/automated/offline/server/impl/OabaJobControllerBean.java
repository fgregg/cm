package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

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

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.BatchJobFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingEvent;
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
	private OabaProcessingController processingController;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/statusTopic")
	private Topic oabaStatusTopic;

	protected OabaJobEntity getBean(OabaJob oabaJob) {
		OabaJobEntity retVal = null;
		if (oabaJob != null) {
			final long jobId = oabaJob.getId();
			if (oabaJob instanceof OabaJobEntity) {
				retVal = (OabaJobEntity) oabaJob;
			} else {
				if (oabaJob.isPersistent()) {
					retVal = em.find(OabaJobEntity.class, jobId);
					if (retVal == null) {
						String msg =
							"Unable to find persistent OABA job: " + jobId;
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

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public OabaJob createPersistentOabaJob(String externalID,
			OabaParameters params, OabaSettings settings, ServerConfiguration sc)
			throws ServerConfigurationException {

		if (params == null || settings == null || sc == null) {
			throw new IllegalArgumentException("null argument");
		}

		// Save the parameters
		paramsController.save(params);
		oabaSettingsController.save(settings);
		serverManager.save(sc);

		OabaJobEntity retVal =
			new OabaJobEntity(params, settings, sc, externalID);
		em.persist(retVal);
		assert retVal.isPersistent();

		// Create a new entry in the processing log and check it
		OabaProcessingControllerBean.updateStatusWithNotification(em,
				jmsContext, oabaStatusTopic, retVal, OabaEvent.INIT,
				new Date(), null);
		OabaProcessingEvent ope =
			OabaProcessingControllerBean.getCurrentOabaProcessingEvent(em,
					retVal);
		OabaEvent currentProcessingEvent = ope.getOabaEvent();
		assert currentProcessingEvent == OabaEvent.INIT;

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
	public OabaJob save(OabaJob batchJob) {
		return save(getBean(batchJob));
	}

	@Override
	public OabaJobEntity save(OabaJobEntity job) {
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
	public OabaJob findOabaJob(long id) {
		OabaJobEntity batchJob = em.find(OabaJobEntity.class, id);
		return batchJob;
	}

	@Override
	public List<OabaJob> findAll() {
		Query query = em.createNamedQuery(OabaJobJPA.QN_OABAJOB_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaJob> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<OabaJob>();
		}
		return retVal;
	}

	@Override
	public void delete(OabaJob oabaJob) {
		if (oabaJob.isPersistent()) {
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

	@Override
	public void detach(BatchJob oabaJob) {
		em.detach(oabaJob);
	}

}
