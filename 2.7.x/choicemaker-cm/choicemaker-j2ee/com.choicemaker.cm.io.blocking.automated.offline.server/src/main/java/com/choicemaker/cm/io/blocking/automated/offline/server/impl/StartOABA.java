/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.services.RecValService3;

/**
 * This message bean is the first step of the OABA. It creates rec_id, val_id
 * files using internal id translation.
 *
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/startQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class StartOABA implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(StartOABA.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ StartOABA.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;

	@PostConstruct
	public void init() {
		this.configuration = EJBConfiguration.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		StartData data = null;
		BatchJob batchJob = null;

		log.info("StartOABA In onMessage");

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();

				batchJob =
					configuration.findBatchJobById(em, BatchJobBean.class,
							data.jobID);
				// update status to mark as start
				batchJob.markAsStarted();

				BatchParameters params = configuration.findBatchParamsByJobId(em, batchJob.getId());
				IProbabilityModel stageModel =
					PMManager.getModelInstance(params.getModelConfigurationName());
				OABAConfiguration oabaConfig =
					new OABAConfiguration(data.modelConfigurationName,
							data.jobID);

				// get the status
				OabaProcessing status =
					configuration.getProcessingLog(em, data);

				log.info(data.jobID + " " + data.modelConfigurationName + " "
						+ data.low + " "
						+ data.high + " " + data.runTransitivity);
				log.info(data.staging + " " + data.master);

				// check to see if there are a lot of records in stage.
				// if not use single record matching instead of batch.
				if (!isMoreThanThreshold(data.staging, stageModel,
						data.maxCountSingle)) {
					log.info("Using single record matching");
					sendToSingleRecordMatching(data);

				} else {
					log.info("Using batch record matching");

					RecordIDTranslator2 translator =
						new RecordIDTranslator2(oabaConfig.getTransIDFactory());

					// create rec_id, val_id files
					RecValService3 rvService =
						new RecValService3(data.staging, data.master,
								stageModel,
								oabaConfig.getRecValFactory(), translator,
								status, batchJob);
					rvService.runService();

					data.stageType = rvService.getStageType();
					data.masterType = rvService.getMasterType();

					data.numBlockFields = rvService.getNumBlockingFields();

					log.info("Done creating rec_id, val_id files: "
							+ rvService.getTimeElapsed());

					// create the validator after rvService
					// Validator validator = new Validator (true, translator);
					ValidatorBase validator =
						new ValidatorBase(true, translator);
					data.validator = validator;

					sendToUpdateStatus(data.jobID, 10);
					sendToBlocking(data);
				}

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (Exception e) {
			log.severe(e.toString());
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/**
	 * This method checks to see if the number of records in the RecordSource is
	 * greater than the threshold.
	 *
	 * @param rs
	 *            - RecordSource
	 * @param accessProvider
	 *            - Probability Model of this RecordSource
	 * @param threshold
	 *            - The number of records threshold
	 * @return boolean - true if the RecordSource contains more than the
	 *         threshold
	 * @throws OABABlockingException
	 */
	private boolean isMoreThanThreshold(RecordSource rs,
			IProbabilityModel model, int threshold) throws BlockingException {

		if (threshold == 0)
			return true;

		boolean ret = false;

		log.info("Checking if moreThanThreshold " + threshold);

		try {
			rs.setModel(model);
			rs.open();
			int count = 1;

			while (count <= threshold && rs.hasNext()) {
				// 2014-04-24 rphall: Commented out unused local variable.
				/* Record r = */
				rs.getNext();
				count++;
			}

			if (rs.hasNext())
				ret = true;

			rs.close();

		} catch (IOException ex) {
			throw new BlockingException(ex.toString(), ex);
		}

		return ret;
	}

	/**
	 * This method sends a message to the UpdateStatus message bean.
	 *
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	private void sendToUpdateStatus(long jobID, int percentComplete)
			throws NamingException, JMSException {
		Queue queue = configuration.getUpdateMessageQueue();

		UpdateData data = new UpdateData();
		data.jobID = jobID;
		data.percentComplete = percentComplete;

		configuration.sendMessage(queue, data);
	}

	/**
	 * This method sends a message to the BlockingOABA message bean.
	 *
	 * @param data
	 * @throws NamingException
	 */
	private void sendToBlocking(StartData data) throws NamingException,
			JMSException {
		Queue queue = configuration.getBlockingMessageQueue();
		configuration.sendMessage(queue, data);
	}

	/**
	 * This method sends a message to the BlockingOABA message bean.
	 *
	 * @param data
	 * @throws NamingException
	 */
	private void sendToSingleRecordMatching(StartData data)
			throws NamingException, JMSException {
		Queue queue = configuration.getSingleMatchMessageQueue();
		configuration.sendMessage(queue, data);
	}

}
