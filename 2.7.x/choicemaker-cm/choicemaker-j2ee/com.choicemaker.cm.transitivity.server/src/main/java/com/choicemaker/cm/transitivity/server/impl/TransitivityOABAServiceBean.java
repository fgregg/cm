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
package com.choicemaker.cm.transitivity.server.impl;

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
//import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2CompositeSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobBean;
import com.choicemaker.cm.transitivity.core.TransitivityException;
import com.choicemaker.cm.transitivity.core.TransitivityResult;
import com.choicemaker.cm.transitivity.server.data.TransitivityJobStatus;
import com.choicemaker.cm.transitivity.server.util.MatchBiconnectedIterator;
import com.choicemaker.cm.transitivity.util.CompositeEntityIterator;
import com.choicemaker.cm.transitivity.util.CompositeEntitySource;

/**
 * @author pcheung
 *
 */
public class TransitivityOABAServiceBean implements SessionBean {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(TransitivityOABAServiceBean.class.getName());

//	@PersistenceContext (unitName = "oaba")
	private EntityManager em;

//	private transient SessionContext sessionContext;
	private transient EJBConfiguration configuration = null;

	private static boolean initialized = false;


	/**
	 * This method starts the transitivity engine.
	 * WARNINGS:
	 *  1. only call this after the OABA has finished.
	 *  2. use the same parameters as the OABA.
	 *
	 * @param jobID - job id of the OABA job
	 * @param staging - staging record source
	 * @param master - master record source
	 * @param lowThreshold - probability under which a pair is considered "differ".
	 * @param highThreshold - probability above which a pair is considered "match".
	 * @param modelConfigurationName - probability accessProvider of the stage record source.
	 * @param masterModelName - probability accessProvider of the master record source.
	 * @return int - the transitivity job id.
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws SQLException
	 */
/*
	public long startTransitivity (long jobID,
		ISerializableRecordSource staging,
		ISerializableRecordSource master,
		float lowThreshold,
		float highThreshold,
		String modelConfigurationName, String masterModelName)
		throws RemoteException, CreateException, NamingException, JMSException, SQLException {

		try {
			StartData data = new StartData();
			data.jobID = jobID;
			data.master = master;
			data.staging = staging;
			data.stageModelName = modelConfigurationName;
			data.masterModelName = masterModelName;
			data.low = lowThreshold;
			data.high = highThreshold;
			data.runTransitivity = false; //this means it's not a continuation from OABA.

			sendToTransitivity (data);

		} catch (Exception e) {
			log.severe(e.toString());
		}

		return jobID;
	}
*/

	/**
 	* This method starts the transitivity engine.
 	* WARNINGS:
 	*  1. use the jobID of the OABA batch job for which the transitivity
 	*  analysis should be performed.
 	*  2. only call this after the OABA has finished.
	 */
	public long startTransitivity (long jobID)
		throws RemoteException, CreateException, NamingException, JMSException, SQLException {

		try {

			BatchParameters batchParams = configuration.findBatchParamsByJobId(em, jobID);
			BatchJob batchJob = em.find(BatchJobBean.class, jobID);
			TransitivityJob job = new TransitivityJobBean(batchParams, batchJob);
			em.persist(job);
			final long retVal = job.getId();

			// Create a new processing entry
			OabaProcessing processing =
				configuration.createProcessingLog(em, retVal);

			// Log the job info
			log.fine("BatchJob: " + batchJob.toString());
			log.fine("BatchParameters: " + batchParams.toString());
			log.fine("Processing entry: " + processing.toString());
			log.fine("TransitivityJob: " + job.toString());

			StartData data = new StartData(retVal);
			sendToTransitivity (data);

		} catch (Exception e) {
			log.severe(e.toString());
		}

		return jobID;
	}



	/** This gets the TE status.
	 *
	 * @param jobID
	 * @return TransitivityJobtatus
	 * @throws JMSException
	 * @throws FinderException
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws SQLException
	 */
	public TransitivityJobStatus getStatus (long jobID) throws
		JMSException, FinderException, RemoteException, CreateException,
		NamingException, SQLException {

		BatchJob transJob = configuration.findBatchJobById(em, TransitivityJobBean.class, jobID);
		assert transJob instanceof TransitivityJob;

		TransitivityJobStatus status = new TransitivityJobStatus (
			transJob.getId(),
			transJob.getStatus(),
			transJob.getStarted(),
			transJob.getCompleted()
		);

		return status;
	}



	/** This method returns the TransitivityResult to the client.
	 *
	 * @param jobID - TE job id
	 * @param compact - true if you want the graphs be compacted first
	 * @return TransitivityResult
	 * @throws RemoteException
	 * @throws FinderException
	 * @throws NamingException
	 * @throws TransitivityException
	 */
	public TransitivityResult getTransitivityResult (long jobID, boolean compact) throws
		RemoteException, FinderException, NamingException, TransitivityException {

		BatchJob batchJob = configuration.findBatchJobById(em, TransitivityJobBean.class, jobID);
		assert batchJob instanceof TransitivityJob;
		TransitivityJob transJob = (TransitivityJob) batchJob;
		if (!transJob.getStatus().equals(TransitivityJob.STATUS_COMPLETED))
			throw new TransitivityException ("Job " + jobID + " is not complete.");

		log.info("file source " + transJob.getDescription());

		MatchRecord2CompositeSource mrs = new MatchRecord2CompositeSource (
			transJob.getDescription());

		CompositeEntitySource ces = new CompositeEntitySource (mrs);
		CompositeEntityIterator it = new CompositeEntityIterator (ces);

		TransitivityResult ret = null;
		if (compact) {
			MatchBiconnectedIterator ci = new MatchBiconnectedIterator (it);
			ret = new TransitivityResult
				(transJob.getModel(), transJob.getDiffer(), transJob.getMatch(),
				ci);
		} else {
			ret = new TransitivityResult
				(transJob.getModel(), transJob.getDiffer(), transJob.getMatch(),
				it);
		}

		return ret;
	}


	/** This method puts the request on the Transitivity Engine's message queue.
	 *
	 * @param d
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void sendToTransitivity (StartData d) throws NamingException, JMSException {
		Queue queue = configuration.getTransitivityMessageQueue();
		configuration.sendMessage(queue, d);
	}


	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
//		this.sessionContext = sessionContext;
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


}
