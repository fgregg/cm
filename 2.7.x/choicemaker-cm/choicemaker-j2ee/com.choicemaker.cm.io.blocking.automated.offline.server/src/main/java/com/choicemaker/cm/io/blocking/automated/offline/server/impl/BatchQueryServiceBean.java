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
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchListSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.BatchJobStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
//import javax.naming.InitialContext;

/**
 * @author pcheung
 *
 */
@SuppressWarnings("rawtypes")
public class BatchQueryServiceBean implements SessionBean {

	@PersistenceContext (unitName = "oaba")
	EntityManager em;

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(BatchQueryServiceBean.class.getName());

	private static boolean initialized = false;
//	private static boolean countsInitialized = false;

//	private transient SessionContext sessionContext;
//	private transient DataSource dataSource;
	private transient EJBConfiguration configuration = null;


	public long startOABA(String externalID, SerialRecordSource staging,
			SerialRecordSource master, float lowThreshold, float highThreshold,
			String stageModelName, String masterModelName, int maxSingle)
			throws RemoteException, CreateException, NamingException,
			JMSException, SQLException {

		return startOABA(externalID, staging, master, lowThreshold,
				highThreshold, stageModelName, masterModelName, maxSingle,
				false);

	}

	public long startOABA(String externalID, SerialRecordSource staging,
			SerialRecordSource master, float lowThreshold, float highThreshold,
			String stageModelName, String masterModelName, int maxSingle,
			boolean runTransitivity) throws RemoteException, CreateException,
			NamingException, JMSException, SQLException {

		log.fine("starting startBatch...");

		BatchJob batchJob = configuration.createBatchJob(em,externalID);
		batchJob.setDescription(stageModelName + ":" + masterModelName);
		batchJob.markAsQueued();

		long id = batchJob.getId();

		batchJob.setTransactionId(batchJob.getId());

		//create a new current status EJB
		configuration.createNewStatusLog(id);

		//set the parameters
		BatchParameters batchParams = configuration.createBatchParameters(id);
		batchParams.setHighThreshold(new Float(highThreshold));
		batchParams.setLowThreshold(new Float(lowThreshold));
		batchParams.setMaxSingle(new Integer(maxSingle));
		batchParams.setStageModel(stageModelName);
		batchParams.setMasterModel(masterModelName);

		batchParams.setMasterRs(master);
		batchParams.setStageRs(staging);

		sendToStartOABA (id, staging, master, stageModelName, masterModelName,
			lowThreshold, highThreshold, maxSingle, runTransitivity );

		return id;

	}

	public long startOABA (String externalID,
		int transactionId,
		SerialRecordSource staging,
		SerialRecordSource master,
		float lowThreshold,
		float highThreshold,
		String stageModelName, String masterModelName,
		int maxSingle,
		boolean runTransitivity)
		throws RemoteException, CreateException, NamingException, JMSException, SQLException {

		BatchJob batchJob = configuration.createBatchJob(em,externalID);
		batchJob.setDescription(stageModelName + ":" + masterModelName);
		batchJob.markAsQueued();
		batchJob.setTransactionId(new Long (transactionId));

		long id = batchJob.getId();

		//set the parameters
		BatchParameters batchParams = configuration.createBatchParameters(id);
		batchParams.setHighThreshold(new Float(highThreshold));
		batchParams.setLowThreshold(new Float(lowThreshold));
		batchParams.setMaxSingle(new Integer(maxSingle));
		batchParams.setStageModel(stageModelName);
		batchParams.setMasterModel(masterModelName);
		batchParams.setMasterRs(master);
		batchParams.setStageRs(staging);

		//create a new current status EJB
		configuration.createNewStatusLog(id);

		sendToStartOABA (id, staging, master, stageModelName, masterModelName,
			lowThreshold, highThreshold, maxSingle, runTransitivity );

		return id;
	}



	public long startOABAStage (String externalID,
	SerialRecordSource staging,
	float lowThreshold,
	float highThreshold,
	String stageModelName,
	int maxSingle)
	throws RemoteException, CreateException, NamingException, JMSException, SQLException {
		return startOABA (externalID, staging, null, lowThreshold, highThreshold,
			stageModelName, "", maxSingle);
	}




	public int abortJob(long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException {

		return abortBatch (jobID, true);
	}


	public int suspendJob(long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException{

		return abortBatch (jobID, false);
	}


	/**
	 *  This method aborts a job.  If cleanStatus is true, then the aborted job will not be
	 * recoverable.
	 *
	 */
	private int abortBatch(long jobID, boolean cleanStatus) throws RemoteException, CreateException, NamingException, JMSException, FinderException {
		log.info("aborting job " + jobID + " " + cleanStatus);
		BatchJob batchJob = configuration.findBatchJobById(em, jobID);
		batchJob.markAsAbortRequested();

		//set status as done, so it won't continue during the next run
		if (cleanStatus) {
			batchJob.setDescription(BatchJob.STATUS_CLEAR);
		}

		return 0;
	}



	public BatchJobStatus getStatus (long jobID) throws RemoteException, CreateException, NamingException, JMSException, FinderException {
		BatchJob batchJob = configuration.findBatchJobById(em, jobID);

		BatchJobStatus status = new BatchJobStatus (
			batchJob.getId(),
			batchJob.getTransactionId(),
			batchJob.getDescription(),
			batchJob.getStatus(),
			batchJob.getStarted(),
			batchJob.getCompleted()
		);

		return status;
	}


	public String checkStatus (long jobID) throws RemoteException, CreateException, NamingException, JMSException, FinderException {
		BatchJob batchJob = configuration.findBatchJobById(em, jobID);
		return batchJob.getStatus();
	}

	public boolean removeDir (long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException {

			BatchParameters batchParams = configuration.findBatchParamsById(jobID);

			String stageModelName = batchParams.getStageModel();
			OABAConfiguration oConfig = new OABAConfiguration (stageModelName, jobID);
			return oConfig.removeTempDir();
	}


	/** This method tries to resume a stop job.
	 *
	 * @param modelName - staging accessProvider name
	 * @param jobID - job id of the job you want to resume
	 * @return int = 1 if OK, or -1 if failed
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public int resumeJob ( long jobID)
	throws RemoteException, CreateException, NamingException, JMSException, FinderException {
		int ret = 1;
		BatchJob job = configuration.findBatchJobById(em, jobID);

		if (!job.getStarted().equals(BatchJob.STATUS_COMPLETED) &&
			!job.getDescription().equals(BatchJob.STATUS_CLEAR)) {

			//change aborted to started
			job.markAsReStarted();

			BatchParameters batchParams = configuration.findBatchParamsById(jobID);

			OABAConfiguration oConfig = new OABAConfiguration (batchParams.getStageModel(), jobID);

			try {
				StartData data = oConfig.getStartData();

				sendToStartOABA (jobID, data.staging, data.master, data.stageModelName, data.masterModelName,
					data.low, data.high, data.maxCountSingle, false );

			} catch (IOException e) {
				ret = -1;
				log.severe(e.toString());
			} catch (ClassNotFoundException e) {
				ret = -1;
				log.severe(e.toString());
			}
		} else {
			log.warning("Could not resume job " + jobID);
			ret = -1;
		}

		return ret;
	}


	/** This method returns the MatchCandidate List Source for the job ID.
	 *
	 * @param jobID
	 * @return MatchListSource - return a source from which to read MatchList objects.
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public MatchListSource getMatchList (long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException {

		MatchListSource mls = null;

		//check to make sure the job is completed
		BatchJob batchJob = configuration.findBatchJobById(em, jobID);
		if (!batchJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
			throw new IllegalStateException ("The job has not completed.");
		} else {
			String fileName = batchJob.getDescription();
			MatchRecordSource mrs = new MatchRecordSource (fileName, Constants.STRING);
			mls = new MatchListSource (mrs);
		}

		return mls;
	}


	public IMatchRecord2Source getMatchRecordSource (long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, FinderException {

		MatchRecord2Source mrs = null;

		//check to make sure the job is completed
		BatchJob batchJob = configuration.findBatchJobById(em, jobID);
		if (!batchJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
			throw new IllegalStateException ("The job has not completed.");
		} else {
			String fileName = batchJob.getDescription();
			mrs = new MatchRecord2Source (fileName, Constants.STRING);
		}

		return mrs;
	}


	/** This method sends a message to the StartOABA message bean.
	 *
	 * @param request
	 * @param queue
	 * @throws RemoteException
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void sendToStartOABA (long jobID, SerialRecordSource staging,
		SerialRecordSource master,
		String stageModelName, String masterModelName,
		float low, float high, int maxSingle, boolean runTransitivity)
		throws JMSException, RemoteException, CreateException, NamingException {

		Queue queue = configuration.getStartMessageQueue();

		log.fine("Sending on queue '" + queue.getQueueName() + "'");

		StartData data = new StartData();
		data.jobID = jobID;
		data.master = master;
		data.staging = staging;
		data.stageModelName = stageModelName;
		data.masterModelName = masterModelName;
		data.low = low;
		data.high = high;
		data.maxCountSingle = maxSingle;
		data.runTransitivity = runTransitivity;

		try {
			configuration.sendMessage(queue, data);
		} catch (Exception ex) {
			log.severe(ex.toString());
		} finally {
//			if (session != null) session.close ();
		}

		log.fine ("...finished sendToStartOABA");
	}



	public void ejbCreate() throws CreateException {
		try {
			// 2014-04-24 rphall: Commented out unused local variable.
//			InitialContext ic = new InitialContext();

			this.configuration = EJBConfiguration.getInstance();

			if (!initialized) {
//				ICompiler compiler = DoNothingCompiler.instance;
//				XmlConfigurator.embeddedInit(compiler);
				EmbeddedXmlConfigurator.getInstance().embeddedInit(null);
				initialized = true;
			}
		} catch (Exception ex) {
			log.severe(ex.toString());
			throw new CreateException(ex.getMessage());
		}

	} // ejbCreate()


	public void ejbActivate() throws EJBException, RemoteException { }

	public void ejbPassivate() throws EJBException, RemoteException { }

	public void ejbRemove() throws EJBException, RemoteException { }

	public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
//		this.sessionContext = sessionContext;
	}

}
