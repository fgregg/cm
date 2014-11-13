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
package com.choicemaker.cm.io.blocking.automated.offline.server.util;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

/**
 * This object contains common message bean utilities such as cancelling an OABA
 * job.
 * 
 * @author pcheung
 *
 */
public class MessageBeanUtils {

	private static final Logger log0 = Logger.getLogger(MessageBeanUtils.class
			.getName());

	public static final String DEFAULT_TAG = "UNKNOWN SOURCE";
	public static final String UNKNOWN_QUEUE = "unknown queue";

	/**
	 * This method stops the BatchJob by setting the status to aborted, and
	 * removes the temporary directory for the job.
	 * 
	 * @param batchJob
	 * @param status
	 * @param oabaConfig
	 * @throws RemoteException
	 * @throws BlockingException
	 */
	public static void stopJob(BatchJob batchJob, OabaProcessing status,
			OABAConfiguration oabaConfig) {

		batchJob.markAsAborted();
		// FIXME description used to hold operational parameter
		if (batchJob.getDescription().equals(BatchJob.STATUS_CLEAR)) {
			status.setCurrentProcessingEvent(OabaEvent.DONE_OABA);
			log0.info("Removing Temporary directory.");
			oabaConfig.removeTempDir();
		}
	}

	/**
	 * This method sends a message to the UpdateStatus message bean.
	 * 
	 * @param jobID
	 *            must be a valid batch job id
	 * @param percentComplete
	 *            a integer between 0 and 100
	 * @param jmsCtx
	 *            a non-null JMSContext
	 * @param q
	 *            must be queue specified by the JNDI name
	 *            'choicemaker/urm/jms/updateQueue'
	 * @param log
	 *            must be a non-null Logger instance
	 */
	public static void sendUpdateStatus(long jobID, int percentComplete,
			JMSContext jmsCtx, Queue q, Logger log) {
		if (jmsCtx == null || q == null || log == null) {
			throw new IllegalArgumentException("null argument");
		}
		UpdateData data = new UpdateData(jobID, percentComplete);
		ObjectMessage message = jmsCtx.createObjectMessage(data);
		JMSProducer sender = jmsCtx.createProducer();
		log.finest(MessageBeanUtils.queueInfo("Sending", q, data));
		sender.send(q, message);
		log.finest(MessageBeanUtils.queueInfo("Sent", q, data));
	}

	/**
	 * This method sends StartData to a message bean.
	 * 
	 * @param jobID
	 *            must be a valid batch job id
	 * @param percentComplete
	 *            a integer between 0 and 100
	 * @param jmsCtx
	 *            a non-null JMSContext
	 * @param q
	 *            must be queue on which the associated message listener
	 *            can process StartData
	 * @param log
	 *            must be a non-null Logger instance
	 */
	public static void sendStartData(StartData data,
			JMSContext jmsCtx, Queue q, Logger log) {
		if (data == null || jmsCtx == null || q == null || log == null) {
			throw new IllegalArgumentException("null argument");
		}
		ObjectMessage message = jmsCtx.createObjectMessage(data);
		JMSProducer sender = jmsCtx.createProducer();
		log.finest(MessageBeanUtils.queueInfo("Sending", q, data));
		sender.send(q, message);
		log.finest(MessageBeanUtils.queueInfo("Sent", q, data));
	}

	public static void sendMatchWriterData(MatchWriterData data,
			JMSContext jmsCtx, Queue q, Logger log) {
		if (data == null || jmsCtx == null || q == null || log == null) {
			throw new IllegalArgumentException("null argument");
		}
		ObjectMessage message = jmsCtx.createObjectMessage(data);
		JMSProducer sender = jmsCtx.createProducer();
		log.finest(MessageBeanUtils.queueInfo("Sending", q, data));
		sender.send(q, message);
		log.finest(MessageBeanUtils.queueInfo("Sent", q, data));
	}

	public static String queueInfo(String tag, Queue q, Object d) {
		if (q == null || d == null) {
			throw new IllegalArgumentException("null argument");
		}
		if (tag == null || tag.trim().isEmpty()) {
			tag = DEFAULT_TAG;
		}
		String queueName;
		try {
			queueName = q.getQueueName();
		} catch (JMSException x) {
			queueName = UNKNOWN_QUEUE;
		}
		StringBuilder sb = new StringBuilder(tag).append(" ");
		sb.append("queue: '").append(queueName).append("'");
		sb.append(", data: '").append(d).append("'");
		return sb.toString();
	}

}
