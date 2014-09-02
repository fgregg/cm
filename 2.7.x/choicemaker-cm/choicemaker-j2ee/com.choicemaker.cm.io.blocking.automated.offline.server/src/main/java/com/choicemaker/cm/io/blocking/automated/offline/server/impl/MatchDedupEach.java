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

import java.rmi.RemoteException;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.GenericDedupService;

/**
 * This bean dedups the temporary match file produces by a processor.  It is called by
 * MatchDedupOABA2 and it calls it back when it is done.
 *
 * @author pcheung
 *
 */
public class MatchDedupEach implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MatchDedupEach.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + MatchDedupEach.class.getName());

	private transient MessageDrivenContext mdc = null;
	protected transient EJBConfiguration configuration = null;
	private transient OABAConfiguration oabaConfig = null;
//	private transient QueueConnection connection = null;

	private StartData data;


	public void ejbCreate() {
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.severe(e.toString());
		}
	}


	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
	 */
	public void setMessageDrivenContext(MessageDrivenContext mdc)
		throws EJBException {
			this.mdc = mdc;
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		BatchJob batchJob = null;

		log.fine("MatchDedupEach In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();
				batchJob = configuration.findBatchJobById(data.jobID);

				//init values
				ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);
				oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
				IStatus status = configuration.getStatusLog(data);

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					MessageBeanUtils.stopJob (batchJob, status, oabaConfig);

				} else {
					if (status.getStatus() != IStatus.MERGE_DEDUP_MATCHES) {
						//max number of match in a temp file
						String temp = (String) stageModel.properties().get("maxMatchSize");
						int maxMatches = Integer.parseInt(temp);

						//dedup a temp file
						dedupEach (data.ind, maxMatches, batchJob);
					}

					//send the message back to MatchDedupOABA2
					MatchWriterData d = new MatchWriterData (data);
					sendToMatchDedupOABA2 (d);
				}

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			log.severe(e.toString());
			assert batchJob != null;
			try {
				batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.severe(e1.toString());
			}
		} catch (Exception e) {
			log.severe(e.toString());
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}


	/** This method dedups the Nth match temp file.
	 *
	 * @param num - The Nth match temp file
	 * @param maxMatches - maximum number of matches to hold in memory
	 * @throws OABABlockingException
	 */
	private void dedupEach (int num, int maxMatches, BatchJob batchJob) throws BlockingException {
		long t = System.currentTimeMillis();
		IMatchRecord2Sink mSink = oabaConfig.getMatchChunkFactory().getSink(num);
		IMatchRecord2Source mSource = oabaConfig.getMatchChunkFactory().getSource(mSink);
		ComparableMRSource source = new ComparableMRSource (mSource);

		mSink = oabaConfig.getMatchTempFactory().getSink(num);
		IComparableSink sink =  new ComparableMRSink (mSink);

		log.info ("source " + mSource.getInfo() + " sink " + mSink.getInfo());

		IMatchRecord2SinkSourceFactory factory = oabaConfig.getMatchTempFactory(num);
		ComparableMRSinkSourceFactory mFactory = new ComparableMRSinkSourceFactory (factory);

		if (source.exists()) {
			GenericDedupService service =
				new GenericDedupService (source, sink, mFactory, maxMatches, batchJob);
			service.runDedup();
			int before = service.getNumBefore();
			int after = service.getNumAfter();
			log.info ("numBefore " + before + " numAfter " + after);
		} else {
			log.warning(mSource.getInfo() + " does not exist.");
			sink.open();
			sink.close();
		}

		t = System.currentTimeMillis() - t;

		log.info("Time in dedup each " + t);
	}



	protected void sendToMatchDedupOABA2 (MatchWriterData d) throws NamingException, JMSException {
		Queue queue = configuration.getMatchDedupMessageQueue();
		configuration.sendMessage(queue, d);
	}

}
