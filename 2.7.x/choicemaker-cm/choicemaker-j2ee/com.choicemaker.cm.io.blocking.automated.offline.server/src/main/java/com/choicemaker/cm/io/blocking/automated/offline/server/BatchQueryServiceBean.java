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
package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchListSource;

/**
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 */
@Stateless
public class BatchQueryServiceBean implements Serializable {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger
			.getLogger(BatchQueryServiceBean.class);

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

//	private static boolean initialized = false;

	public long startOABAStage(String externalID, SerialRecordSource staging,
			float lowThreshold, float highThreshold, String stageModelName,
			int maxSingle) throws RemoteException, CreateException,
			NamingException, JMSException, SQLException {

		return startOABA(externalID, null, staging, null, lowThreshold,
				highThreshold, stageModelName, null, maxSingle, false);
	}

	public long startOABA(String externalID, SerialRecordSource staging,
			SerialRecordSource master, float lowThreshold, float highThreshold,
			String stageModelName, String masterModelName, int maxSingle)
			throws RemoteException, CreateException, NamingException,
			JMSException, SQLException {

		return this.startOABA(externalID, null, staging, master, lowThreshold,
				highThreshold, stageModelName, masterModelName, maxSingle,
				false);
	}

	public long startOABA(String externalID, SerialRecordSource staging,
			SerialRecordSource master, float lowThreshold, float highThreshold,
			String stageModelName, String masterModelName, int maxSingle,
			boolean runTransitivity) throws RemoteException, CreateException,
			NamingException, JMSException, SQLException {

		return this.startOABA(externalID, null, staging, master, lowThreshold,
				highThreshold, stageModelName, masterModelName, maxSingle,
				runTransitivity);
	}

	public long startOABA(String externalID, Long transactionId,
			SerialRecordSource staging, SerialRecordSource master,
			float lowThreshold, float highThreshold, String stageModelName,
			String masterModelName, int maxSingle, boolean runTransitivity)
			throws RemoteException, CreateException, NamingException,
			JMSException, SQLException {

		log.debug("starting startBatch...");

//		BatchJob batchJob = EJBConfiguration.getInstance().createBatchJob(externalID);
		BatchJobBean batchJob = new BatchJobBean(externalID);
		em.persist(batchJob);
		batchJob.setDescription(stageModelName + ":" + masterModelName);
		batchJob.markAsQueued();
		final long id = batchJob.getId();
		if (transactionId != null) {
			batchJob.setTransactionId(transactionId);
		}

		// create a new current status EJB
//		EJBConfiguration.getInstance().createNewStatusLog(id);
		StatusLogBean statusLog = new StatusLogBean(batchJob.getId());
		em.persist(statusLog);

		// set the parameters
//		BatchParameters batchParams = EJBConfiguration.getInstance().createBatchParameters(id);
		BatchParametersBean batchParams = new BatchParametersBean(batchJob.getId());
		batchParams.setHighThreshold(new Float(highThreshold));
		batchParams.setLowThreshold(new Float(lowThreshold));
		batchParams.setMaxSingle(new Integer(maxSingle));
		batchParams.setStageModel(stageModelName);
		batchParams.setMasterModel(masterModelName);
		batchParams.setMasterRs(master);
		batchParams.setStageRs(staging);
		em.persist(batchParams);

		sendToStartOABA(id, staging, master, stageModelName, masterModelName,
				lowThreshold, highThreshold, maxSingle, runTransitivity);

		return id;
	}

	public int abortJob(long jobID) throws RemoteException, CreateException,
			NamingException, JMSException, FinderException {

		return abortBatch(jobID, true);
	}

	public int suspendJob(long jobID) throws RemoteException, CreateException,
			NamingException, JMSException, FinderException {

		return abortBatch(jobID, false);
	}

	/**
	 * This method aborts a job. If cleanStatus is true, then the aborted job
	 * will not be recoverable.
	 *
	 */
	private int abortBatch(long jobID, boolean cleanStatus)
			throws RemoteException, CreateException, NamingException,
			JMSException, FinderException {

		throw new RemoteException("not yet implemented");
//		log.info("aborting job " + jobID + " " + cleanStatus);
//		BatchJob batchJob = EJBConfiguration.getInstance().findBatchJobById(jobID);
//		batchJob.markAsAbortRequested();
//
//		// set status as done, so it won't continue during the next run
//		if (cleanStatus) {
//			batchJob.setDescription(BatchJob.CLEAR);
//		}
//
//		return 0;
	}

	public BatchJobStatus getStatus(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {

		throw new RemoteException("not yet implemented");
//		BatchJob batchJob = EJBConfiguration.getInstance().findBatchJobById(jobID);
//
//		BatchJobStatus status =
//
//			new BatchJobStatus(batchJob.getId().longValue(), batchJob
//					.getTransactionId().longValue(), batchJob.getDescription(),
//					batchJob.getStatus(), batchJob.getStarted(),
//					batchJob.getCompleted());
//
//		return status;
	}

	public String checkStatus(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {
		BatchJob batchJob = EJBConfiguration.getInstance().findBatchJobById(jobID);
		return batchJob.getStatus().name();
	}

	public boolean removeDir(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {

		throw new RemoteException("not yet implemented");
//		BatchParameters batchParams = EJBConfiguration.getInstance().findBatchParamsById(jobID);
//
//		String stageModelName = batchParams.getStageModel();
//		OABAConfiguration oConfig =
//			new OABAConfiguration(stageModelName, jobID);
//		return oConfig.removeTempDir();
	}

	/**
	 * This method tries to resume a stop job.
	 *
	 * @param modelName
	 *            - staging accessProvider name
	 * @param jobID
	 *            - job id of the job you want to resume
	 * @return int = 1 if OK, or -1 if failed
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public int resumeJob(long jobID) throws RemoteException, CreateException,
			NamingException, JMSException, FinderException {

		throw new RemoteException("not yet implemented");
//		int ret = 1;
//		BatchJob job = EJBConfiguration.getInstance().findBatchJobById(jobID);
//
//		if (!job.getStarted().equals(BatchJob.STATUS_COMPLETED)
//				&& !job.getDescription().equals(BatchJob.CLEAR)) {
//
//			// change aborted to started
//			job.markAsReStarted();
//
//			BatchParameters batchParams =
//				EJBConfiguration.getInstance().findBatchParamsById(jobID);
//
//			OABAConfiguration oConfig =
//				new OABAConfiguration(batchParams.getStageModel(), jobID);
//
//			try {
//				StartData data = oConfig.getStartData();
//
//				sendToStartOABA(jobID, data.staging, data.master,
//						data.stageModelName, data.masterModelName, data.low,
//						data.high, data.maxCountSingle, false);
//
//			} catch (IOException e) {
//				ret = -1;
//				log.error(e.toString(), e);
//			} catch (ClassNotFoundException e) {
//				ret = -1;
//				log.error(e.toString(), e);
//			}
//		} else {
//			log.warn("Could not resume job " + jobID);
//			ret = -1;
//		}
//
//		return ret;
	}

	/**
	 * This method returns the MatchCandidate List Source for the job ID.
	 *
	 * @param jobID
	 * @return MatchListSource - return a source from which to read MatchList
	 *         objects.
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public MatchListSource getMatchList(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {

		throw new RemoteException("not yet implemented");
//		MatchListSource mls = null;
//
//		// check to make sure the job is completed
//		BatchJob batchJob = EJBConfiguration.getInstance().findBatchJobById(jobID);
//		if (!batchJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
//			throw new IllegalStateException("The job has not completed.");
//		} else {
//			String fileName = batchJob.getDescription();
//			MatchRecordSource mrs =
//				new MatchRecordSource(fileName, Constants.STRING);
//			mls = new MatchListSource(mrs);
//		}
//
//		return mls;
	}

	public IMatchRecord2Source getMatchRecordSource(long jobID)
			throws RemoteException, CreateException, NamingException,
			JMSException, FinderException {

//		MatchRecord2Source mrs = null;
//
//		// check to make sure the job is completed
//		BatchJob batchJob = EJBConfiguration.getInstance().findBatchJobById(jobID);
//		if (!batchJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
//			throw new IllegalStateException("The job has not completed.");
//		} else {
//			String fileName = batchJob.getDescription();
//			mrs = new MatchRecord2Source(fileName, Constants.STRING);
//		}
//
//		return mrs;
		throw new RemoteException("not yet implemented");
	}

	/**
	 * This method sends a message to the StartOABA message bean.
	 *
	 * @param request
	 * @param queue
	 * @throws RemoteException
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void sendToStartOABA(long jobID, SerialRecordSource staging,
			SerialRecordSource master, String stageModelName,
			String masterModelName, float low, float high, int maxSingle,
			boolean runTransitivity) throws JMSException, RemoteException,
			CreateException, NamingException {

		log.error("not yet implemented");
//		Queue queue = EJBConfiguration.getInstance().getStartMessageQueue();
//
//		log.debug("Sending on queue '" + queue.getQueueName() + "'");
//
//		StartData data = new StartData();
//		data.jobID = jobID;
//		data.master = master;
//		data.staging = staging;
//		data.stageModelName = stageModelName;
//		data.masterModelName = masterModelName;
//		data.low = low;
//		data.high = high;
//		data.maxCountSingle = maxSingle;
//		data.runTransitivity = runTransitivity;
//
//		try {
//			EJBConfiguration.getInstance().sendMessage(queue, data);
//		} catch (Exception ex) {
//			log.error(ex.toString(), ex);
//		} finally {
//			// if (session != null) session.close ();
//		}
//
//		log.debug("...finished sendToStartOABA");
	}

	public void ejbCreate() throws CreateException {
		log.error("not yet implemented");
//		try {
//			// 2014-04-24 rphall: Commented out unused local variable.
//			// InitialContext ic = new InitialContext();
//
//			this.EJBConfiguration.getInstance() = EJBConfiguration.getInstance();
//
//			if (!initialized) {
//				// ICompiler compiler = DoNothingCompiler.instance;
//				// XmlConfigurator.embeddedInit(compiler);
//				EmbeddedXmlConfigurator.getInstance().embeddedInit(null);
//				initialized = true;
//			}
//		} catch (Exception ex) {
//			log.error(ex.toString(), ex);
//			throw new CreateException(ex.getMessage());
//		}
	} // ejbCreate()

}
