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
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
			.getLogger(BatchQueryServiceBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	protected EntityManager em;

	@Resource
	protected SessionContext sc;

	@Resource(name = "jms/startQueue",
			lookup = "java:/choicemaker/urm/jms/startQueue")
	protected Queue queue;

	@Inject
	protected JMSContext context;

	public long startOABAStage(String externalID, SerialRecordSource staging,
			float lowThreshold, float highThreshold, String stageModelName,
			int maxSingle) throws JMSException {

		return startOABA(externalID, null, staging, null, lowThreshold,
				highThreshold, stageModelName, null, maxSingle, false);
	}

	public long startOABA(String externalID, SerialRecordSource staging,
			SerialRecordSource master, float lowThreshold, float highThreshold,
			String stageModelName, String masterModelName, int maxSingle)
			throws JMSException {

		return this.startOABA(externalID, null, staging, master, lowThreshold,
				highThreshold, stageModelName, masterModelName, maxSingle,
				false);
	}

	public long startOABA(String externalID, SerialRecordSource staging,
			SerialRecordSource master, float lowThreshold, float highThreshold,
			String stageModelName, String masterModelName, int maxSingle,
			boolean runTransitivity) throws JMSException {

		return this.startOABA(externalID, null, staging, master, lowThreshold,
				highThreshold, stageModelName, masterModelName, maxSingle,
				runTransitivity);
	}

	public long startOABA(String externalID, Long transactionId,
			SerialRecordSource staging, SerialRecordSource master,
			float lowThreshold, float highThreshold, String stageModelName,
			String masterModelName, int maxSingle, boolean runTransitivity)
			throws JMSException {

		log.fine("starting startBatch...");

		// BatchJob batchJob =
		// EJBConfiguration.getInstance().createBatchJob(externalID);
		BatchJobBean batchJob = new BatchJobBean(externalID);
		em.persist(batchJob);
		batchJob.setDescription(stageModelName + ":" + masterModelName);
		batchJob.markAsQueued();
		final long id = batchJob.getId();
		if (transactionId != null) {
			batchJob.setTransactionId(transactionId);
		}

		// create a new current status EJB
		// EJBConfiguration.getInstance().createNewStatusLog(id);
		StatusLogBean statusLog = new StatusLogBean(batchJob.getId());
		em.persist(statusLog);

		// set the parameters
		// BatchParameters batchParams =
		// EJBConfiguration.getInstance().createBatchParameters(id);
		BatchParametersBean batchParams =
			new BatchParametersBean(batchJob.getId());
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

		throw new RemoteException("abortBatch: not yet implemented");
		// log.info("aborting job " + jobID + " " + cleanStatus);
		// BatchJob batchJob =
		// EJBConfiguration.getInstance().findBatchJobById(jobID);
		// batchJob.markAsAbortRequested();
		//
		// // set status as done, so it won't continue during the next run
		// if (cleanStatus) {
		// batchJob.setDescription(BatchJob.CLEAR);
		// }
		//
		// return 0;
	}

	public BatchJobStatus getStatus(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {

		throw new RemoteException("getStatus: not yet implemented");
		// BatchJob batchJob =
		// EJBConfiguration.getInstance().findBatchJobById(jobID);
		//
		// BatchJobStatus status =
		//
		// new BatchJobStatus(batchJob.getId().longValue(), batchJob
		// .getTransactionId().longValue(), batchJob.getDescription(),
		// batchJob.getStatus(), batchJob.getStarted(),
		// batchJob.getCompleted());
		//
		// return status;
	}

	public String checkStatus(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {
		BatchJob batchJob =
			EJBConfiguration.getInstance().findBatchJobById(jobID);
		return batchJob.getStatus().name();
	}

	public boolean removeDir(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {

		throw new RemoteException("checkStatus: not yet implemented");
		// BatchParameters batchParams =
		// EJBConfiguration.getInstance().findBatchParamsById(jobID);
		//
		// String stageModelName = batchParams.getStageModel();
		// OABAConfiguration oConfig =
		// new OABAConfiguration(stageModelName, jobID);
		// return oConfig.removeTempDir();
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

		throw new RemoteException("resumeJob: not yet implemented");
		// int ret = 1;
		// BatchJob job =
		// EJBConfiguration.getInstance().findBatchJobById(jobID);
		//
		// if (!job.getStarted().equals(BatchJob.STATUS_COMPLETED)
		// && !job.getDescription().equals(BatchJob.CLEAR)) {
		//
		// // change aborted to started
		// job.markAsReStarted();
		//
		// BatchParameters batchParams =
		// EJBConfiguration.getInstance().findBatchParamsById(jobID);
		//
		// OABAConfiguration oConfig =
		// new OABAConfiguration(batchParams.getStageModel(), jobID);
		//
		// try {
		// StartData data = oConfig.getStartData();
		//
		// sendToStartOABA(jobID, data.staging, data.master,
		// data.stageModelName, data.masterModelName, data.low,
		// data.high, data.maxCountSingle, false);
		//
		// } catch (IOException e) {
		// ret = -1;
		// log.severe(e.toString());
		// } catch (ClassNotFoundException e) {
		// ret = -1;
		// log.severe(e.toString());
		// }
		// } else {
		// log.warning("Could not resume job " + jobID);
		// ret = -1;
		// }
		//
		// return ret;
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

		throw new RemoteException("getMatchList: not yet implemented");
		// MatchListSource mls = null;
		//
		// // check to make sure the job is completed
		// BatchJob batchJob =
		// EJBConfiguration.getInstance().findBatchJobById(jobID);
		// if (!batchJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
		// throw new IllegalStateException("The job has not completed.");
		// } else {
		// String fileName = batchJob.getDescription();
		// MatchRecordSource mrs =
		// new MatchRecordSource(fileName, Constants.STRING);
		// mls = new MatchListSource(mrs);
		// }
		//
		// return mls;
	}

	@SuppressWarnings("rawtypes")
	public IMatchRecord2Source getMatchRecordSource(long jobID)
			throws RemoteException, CreateException, NamingException,
			JMSException, FinderException {

		// MatchRecord2Source mrs = null;
		//
		// // check to make sure the job is completed
		// BatchJob batchJob =
		// EJBConfiguration.getInstance().findBatchJobById(jobID);
		// if (!batchJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
		// throw new IllegalStateException("The job has not completed.");
		// } else {
		// String fileName = batchJob.getDescription();
		// mrs = new MatchRecord2Source(fileName, Constants.STRING);
		// }
		//
		// return mrs;
		throw new RemoteException("getMatchRecordSource: not yet implemented");
	}

	public static StartData createStartData(long jobID,
			SerialRecordSource staging, SerialRecordSource master,
			String stageModelName, String masterModelName, float low,
			float high, int maxSingle, boolean runTransitivity) {

		StartData retVal = new StartData();
		retVal.jobID = jobID;
		retVal.master = master;
		retVal.staging = staging;
		retVal.stageModelName = stageModelName;
		retVal.masterModelName = masterModelName;
		retVal.low = low;
		retVal.high = high;
		retVal.maxCountSingle = maxSingle;
		retVal.runTransitivity = runTransitivity;

		return retVal;
	}

	/**
	 * This method sends a message to the StartOABA message bean.
	 * 
	 * @throws JMSException
	 */
	protected void sendToStartOABA(long jobID, SerialRecordSource staging,
			SerialRecordSource master, String stageModelName,
			String masterModelName, float low, float high, int maxSingle,
			boolean runTransitivity) throws JMSException {

		log.fine("Sending on queue '" + queue.getQueueName() + "'");

		StartData data =
			createStartData(jobID, staging, master, stageModelName,
					masterModelName, low, high, maxSingle, runTransitivity);
		ObjectMessage message = null;
		JMSProducer sender = null;
		try {
			message = context.createObjectMessage(data);
			sender = context.createProducer();

			log.fine("Sending on queue '" + queue.getQueueName() + "' data '"
					+ data + "' by sender '" + sender + "'");
			sender.send(queue, message);
			log.fine("Sent on queue '" + queue.getQueueName() + "' data '"
					+ data + "' by sender '" + sender + "'");
		} catch (JMSException t) {
			log.severe("queue: '" + queue.getQueueName() + "', data: '" + data
					+ "', sender: '" + sender + "'");
			log.severe(t.toString());
			sc.setRollbackOnly();
		}
	}

//	public void ejbCreate() throws CreateException {
//		log.severe("ejbCreate: not yet implemented");
//		// try {
//		// // 2014-04-24 rphall: Commented out unused local variable.
//		// // InitialContext ic = new InitialContext();
//		//
//		// this.EJBConfiguration.getInstance() = EJBConfiguration.getInstance();
//		//
//		// if (!initialized) {
//		// // ICompiler compiler = DoNothingCompiler.instance;
//		// // XmlConfigurator.embeddedInit(compiler);
//		// EmbeddedXmlConfigurator.getInstance().embeddedInit(null);
//		// initialized = true;
//		// }
//		// } catch (Exception ex) {
//		// log.severe(ex.toString());
//		// throw new CreateException(ex.getMessage());
//		// }
//	} // ejbCreate()

}
