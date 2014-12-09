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
//import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2CompositeSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cm.transitivity.core.TransitivityException;
import com.choicemaker.cm.transitivity.core.TransitivityResult;
import com.choicemaker.cm.transitivity.server.data.TransitivityJobStatus;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
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

//	@EJB
	TransitivityJobControllerBean jobController;
	
//	@EJB
	TransitivityParametersControllerBean paramsController;
	
//	@EJB
	OabaProcessingControllerBean processingController;

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
			TransitivityJobData data = new TransitivityJobData();
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

			TransitivityParameters batchParams = paramsController.findTransitivityParamsByJobId(jobID);
			TransitivityJob transitivityJob = jobController.findTransitivityJob(jobID);
//			TransitivityJob job = new TransitivityJobEntity(batchParams, transitivityJob);
//			em.persist(job);
			final long retVal = transitivityJob.getId();

			// Create a new processing entry
			OabaProcessing processing = processingController.findProcessingLogByJobId(retVal);

			// Log the job info
			log.fine("TransitivityJob: " + transitivityJob.toString());
			log.fine("TransitivityParameters: " + batchParams.toString());
			log.fine("Processing entry: " + processing.toString());
			log.fine("TransitivityJob: " + transitivityJob.toString());

			OabaJobMessage data = new OabaJobMessage(retVal);
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

		TransitivityJob transJob = jobController.findTransitivityJob(jobID);
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

		TransitivityJob transitivityJob = jobController.findTransitivityJob(jobID);
		TransitivityJob transJob = (TransitivityJob) transitivityJob;
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
	private void sendToTransitivity (OabaJobMessage d) throws NamingException, JMSException {
		throw new Error("not yet re-implemented");
//		Queue queue = configuration.getTransitivityMessageQueue();
//		configuration.sendMessage(queue, d);
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
//		try {
//			// 2014-04-24 rphall: Commented out unused local variable.
////			InitialContext ic = new InitialContext();
//
//			this.configuration = EJBConfiguration.getInstance();
//
//		} catch (Exception ex) {
//			log.severe(ex.toString());
//			throw new CreateException(ex.getMessage());
//		}

	} // ejbCreate()


}
