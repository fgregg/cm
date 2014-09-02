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
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.GenericDedupService;

/**
 * This message bean handles the deduping of match records.
 * 
 * This version loads one chunk data into memory and different processors handle
 * different trees of the same chunk. There are N matches files, where N is the
 * number of processors.
 * 
 * For each of the N files, we need to dedup it, then merge all the dedup files
 * together.
 * 
 * @author pcheung
 *
 */
public class MatchDedupOABA2 implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MatchDedupOABA2.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatchDedupOABA2.class.getName());

	protected transient MessageDrivenContext mdc;
	protected transient EJBConfiguration configuration;
	protected transient OABAConfiguration oabaConfig;
	// private transient QueueConnection connection;
	protected transient BatchJob batchJob;

	// This counts the number of messages sent to MatchDedupEach and number of
	// done messages got back.
	private int countMessages;

	private StartData data;

	public void ejbCreate() {
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.MessageDrivenBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.
	 * MessageDrivenContext)
	 */
	public void setMessageDrivenContext(MessageDrivenContext mdc)
			throws EJBException {
		this.mdc = mdc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;

		log.debug("MatchDedupOABA2 In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof StartData) {
					// coming in from MatchScheduler2
					// need to dedup each of the temp files from the processors
					countMessages = 0;
					handleDedupEach(o);

				} else if (o instanceof MatchWriterData) {
					// coming in from MatchDedupEach
					// need to merge the deduped temp files when all the
					// proceesors are done

					countMessages--;
					if (countMessages == 0) {
						handleMerge(o);
					}
				}

			} else {
				log.warn("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(), e);
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			try {
				log.error(e.toString(), e);
				if (batchJob != null)
					batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(), e1);
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/**
	 * This method handles merging individual processor match files.
	 * 
	 * @param o
	 * @throws FinderException
	 * @throws RemoteException
	 * @throws BlockingException
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void handleMerge(Object o) throws FinderException, RemoteException,
			BlockingException, NamingException, JMSException {

		MatchWriterData d = (MatchWriterData) o;
		batchJob = configuration.findBatchJobById(d.jobID);

		// init values
		ImmutableProbabilityModel stageModel =
			PMManager.getModelInstance(d.stageModelName);
		oabaConfig = new OABAConfiguration(d.stageModelName, d.jobID);
		IStatus status = configuration.getStatusLog(d.jobID);

		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob(batchJob, status, oabaConfig);

		} else {
			status.setStatus(IStatus.MERGE_DEDUP_MATCHES);

			// get the number of processors
			String temp = (String) stageModel.properties().get("numProcessors");
			int numProcessors = Integer.parseInt(temp);

			// now merge them all together
			mergeMatches(numProcessors, data.jobID, batchJob);

			// mark as done
			sendToUpdateStatus(d.jobID, 100);
			status.setStatus(IStatus.DONE_PROGRAM);
			publishStatus(d.jobID);

			// send to transitivity
			log.info("runTransitivity " + d.runTransitivity);
			if (d.runTransitivity)
				sendToTransitivity(new StartData(d));
		}
	}

	/**
	 * This method sends messages to MatchDedupEach to dedup individual match
	 * files.
	 * 
	 * @param o
	 * @throws RemoteException
	 * @throws FinderException
	 * @throws BlockingException
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void handleDedupEach(Object o) throws RemoteException,
			FinderException, BlockingException, NamingException, JMSException {

		data = (StartData) o;
		batchJob = configuration.findBatchJobById(data.jobID);

		// init values
		ImmutableProbabilityModel stageModel =
			PMManager.getModelInstance(data.stageModelName);
		oabaConfig = new OABAConfiguration(data.stageModelName, data.jobID);
		IStatus status = configuration.getStatusLog(data);

		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob(batchJob, status, oabaConfig);

		} else {
			// get the number of processors
			String temp = (String) stageModel.properties().get("numProcessors");
			int numProcessors = Integer.parseInt(temp);

			// max number of match in a temp file
			// temp = (String) stageModel.properties.get("maxMatchSize");
			// int maxMatches = Integer.parseInt(temp);

			// the match files start with 1, not 0.
			for (int i = 1; i <= numProcessors; i++) {
				// send to parallelized match dedup each bean
				StartData d2 = new StartData(data);
				d2.ind = i;
				sendToMatchDedupEach(d2);
			}

			countMessages = numProcessors;

		} // end if aborted
	}

	/**
	 * This method merges all the sorted and dedups matches files from the
	 * previous step.
	 * 
	 *
	 */
	private void mergeMatches(int num, long jobID, BatchJob batchJob)
			throws BlockingException, RemoteException {

		long t = System.currentTimeMillis();

		IMatchRecord2SinkSourceFactory factory =
			oabaConfig.getMatchTempFactory();
		List<IComparableSink> tempSinks = new ArrayList<>();

		// the match files start with 1, not 0.
		for (int i = 1; i <= num; i++) {
			IMatchRecord2Sink mSink = factory.getSink(i);
			IComparableSink sink = new ComparableMRSink(mSink);
			tempSinks.add(sink);

			log.info("merging file " + sink.getInfo());
		}

		IMatchRecord2Sink mSink = oabaConfig.getCompositeMatchSink(jobID);
		IComparableSink sink = new ComparableMRSink(mSink);

		ComparableMRSinkSourceFactory mFactory =
			new ComparableMRSinkSourceFactory(factory);

		int i = GenericDedupService.mergeFiles(tempSinks, sink, mFactory, true);

		log.info("Number of Distinct matches after merge: " + i);
		batchJob.setDescription(mSink.getInfo());

		t = System.currentTimeMillis() - t;

		log.info("Time in merge dedup " + t);
	}

	/**
	 * This method sends a message to the UpdateStatus message bean.
	 * 
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	protected void sendToUpdateStatus(long jobID, int percentComplete)
			throws NamingException, JMSException {
		Queue queue = configuration.getUpdateMessageQueue();

		UpdateData data = new UpdateData();
		data.jobID = jobID;
		data.percentComplete = percentComplete;

		configuration.sendMessage(queue, data);
	}

	/**
	 * This sends the message to multiple beans to dedup each of the match temp
	 * file created by the processors.
	 * 
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 * @throws JMSException
	 */
	protected void sendToMatchDedupEach(StartData d) throws NamingException,
			JMSException {
		Queue queue = configuration.getMatchDedupEachMessageQueue();
		configuration.sendMessage(queue, d);
	}

	private void sendToTransitivity(StartData d) throws NamingException,
			JMSException {
		Queue queue = configuration.getTransitivityMessageQueue();
		configuration.sendMessage(queue, d);
		log.info("Sending to TE");
	}

	/**
	 * This method publishes the status to a topic queue.
	 * 
	 * @param status
	 */
	private void publishStatus(long id) {
		TopicConnection conn = null;
		TopicSession session = null;
		try {
			conn =
				EJBConfiguration.getInstance().getTopicConnectionFactory()
						.createTopicConnection();
			session =
				conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			conn.start();
			Topic topic = EJBConfiguration.getInstance().getStatusTopic();
			TopicPublisher pub = session.createPublisher(topic);
			ObjectMessage notifMsg = session.createObjectMessage(new Long(id));
			pub.publish(notifMsg);
			pub.close();
			// conn.stop();
		} catch (Exception e) {
			log.error(e.toString(), e);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (Exception e) {
					log.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
		log.debug("...finished published status");
	}

}
