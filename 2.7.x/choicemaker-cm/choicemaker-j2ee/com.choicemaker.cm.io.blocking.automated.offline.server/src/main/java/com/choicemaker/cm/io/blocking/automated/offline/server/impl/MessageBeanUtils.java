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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_CLEAR_RESOURCES;

import java.util.logging.Logger;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Topic;

import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.batch.impl.BatchJobFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;

/**
 * This object contains common message bean utilities such as canceling a Batch
 * job or sending various types of JMS messages.
 * 
 * @author pcheung
 *
 */
public class MessageBeanUtils {

	private static final Logger log0 = Logger.getLogger(MessageBeanUtils.class
			.getName());

	public static final String DEFAULT_TAG = "UNKNOWN SOURCE";
	public static final String UNKNOWN_QUEUE = "unknown queue";
	public static final String UNKNOWN_TOPIC = "unknown topic";

	/**
	 * This method stops the BatchJob by setting the status to aborted, and
	 * removes the temporary directory for the job.
	 */
	public static void stopJob(BatchJob batchJob,
			OperationalPropertyController propController, ProcessingEventLog status) {

		batchJob.markAsAborted();

		final String _clearResources =
			propController.getJobProperty(batchJob, PN_CLEAR_RESOURCES);
		boolean clearResources = Boolean.valueOf(_clearResources);

		if (clearResources) {
			log0.info("Clearing resources for job " + batchJob.getId());
			status.setCurrentProcessingEvent(BatchProcessingEvent.DONE);
			log0.info("Removing Temporary directory.");
			BatchJobFileUtils.removeTempDir(batchJob);
		} else {
			log0.info("Retaining resources for job " + batchJob.getId());
		}
	}

	public static void sendStartData(OabaJobMessage data, JMSContext jmsCtx,
			Queue q, Logger log) {
		if (data == null || jmsCtx == null || q == null || log == null) {
			throw new IllegalArgumentException("null argument");
		}
		ObjectMessage message = jmsCtx.createObjectMessage(data);
		JMSProducer sender = jmsCtx.createProducer();
		log.info(MessageBeanUtils.queueInfo("Sending", q, data));
		sender.send(q, message);
		log.info(MessageBeanUtils.queueInfo("Sent", q, data));
	}

	public static void sendMatchWriterData(MatchWriterMessage data,
			JMSContext jmsCtx, Queue q, Logger log) {
		if (data == null || jmsCtx == null || q == null || log == null) {
			throw new IllegalArgumentException("null argument");
		}
		ObjectMessage message = jmsCtx.createObjectMessage(data);
		JMSProducer sender = jmsCtx.createProducer();
		log.info(MessageBeanUtils.queueInfo("Sending", q, data));
		sender.send(q, message);
		log.info(MessageBeanUtils.queueInfo("Sent", q, data));
	}

	public static String queueInfo(String tag, Queue q, Object d) {
		if (q == null) {
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

	public static String topicInfo(String tag, Topic t, Object d) {
		if (t == null) {
			throw new IllegalArgumentException("null argument");
		}
		if (tag == null || tag.trim().isEmpty()) {
			tag = DEFAULT_TAG;
		}
		String topicName;
		try {
			topicName = t.getTopicName();
		} catch (JMSException x) {
			topicName = UNKNOWN_TOPIC;
		}
		StringBuilder sb = new StringBuilder(tag).append(" ");
		sb.append("topic: '").append(topicName).append("'");
		sb.append(", data: '").append(d).append("'");
		return sb.toString();
	}

}
