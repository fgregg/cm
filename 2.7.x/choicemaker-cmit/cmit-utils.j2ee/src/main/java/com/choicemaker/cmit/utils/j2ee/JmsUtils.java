package com.choicemaker.cmit.utils.j2ee;

import static com.choicemaker.cm.args.BatchProcessing.*;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Topic;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchProcessingNotification;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;

public class JmsUtils {

	private static final Logger logger = Logger.getLogger(JmsUtils.class
			.getName());

	/** A short time-out for receiving messages (1 second) */
	public static final long SHORT_TIMEOUT_MILLIS = 1000;

	/** A reasonably long time-out for receiving messages (20 seconds) */
	public static final long LONG_TIMEOUT_MILLIS = 20000;

	/**
	 * A very long, rather desperate time-out for receiving messages from
	 * possibly delayed -- or more likely, dead -- processes (5 minutes)
	 */
	public static final long VERY_LONG_TIMEOUT_MILLIS = 300000;

	public static String queueInfo(String tag, Queue q) {
		return MessageBeanUtils.queueInfo(tag, q, null);
	}

	public static String queueInfo(String tag, Queue q, Object d) {
		return MessageBeanUtils.queueInfo(tag, q, d);
	}

	public static String topicInfo(String tag, Topic q) {
		return MessageBeanUtils.topicInfo(tag, q, null);
	}

	public static String topicInfo(String tag, Topic q, Object d) {
		return MessageBeanUtils.topicInfo(tag, q, d);
	}

	public static void clearStartDataFromQueue(String LOG_SOURCE,
			JMSContext jmsContext, Queue queue) {
		JMSConsumer consumer = jmsContext.createConsumer(queue);
		int count = 0;
		OabaJobMessage startData = null;
		do {
			startData =
				receiveStartData(LOG_SOURCE, consumer, queue,
						SHORT_TIMEOUT_MILLIS);
			if (startData != null) {
				++count;
			}
			logger.info(queueInfo("Clearing: ", queue, startData));
		} while (startData != null);
		logger.info(queueInfo("Messages cleared: " + count + " ", queue));
	}

	public static OabaJobMessage receiveStartData(String LOG_SOURCE,
			JMSContext jmsContext, Queue queue) {
		return receiveStartData(LOG_SOURCE, jmsContext, queue,
				SHORT_TIMEOUT_MILLIS);
	}

	public static OabaJobMessage receiveStartData(final String LOG_SOURCE,
			JMSContext jmsContext, Queue queue, long timeOut) {
		JMSConsumer consumer = jmsContext.createConsumer(queue);
		return receiveStartData(LOG_SOURCE, consumer, queue, timeOut);
	}

	protected static OabaJobMessage receiveStartData(final String LOG_SOURCE,
			JMSConsumer consumer, Queue queue, long timeOut) {
		final String METHOD = "receiveStartData(" + timeOut + ")";
		logger.entering(LOG_SOURCE, METHOD);
		OabaJobMessage retVal = null;
		try {
			retVal = consumer.receiveBody(OabaJobMessage.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		logger.exiting(LOG_SOURCE, METHOD);
		return retVal;
	}

	public static void clearBatchProcessingNotifications(String LOG_SOURCE,
			JMSConsumer consumer) {
		int count = 0;
		BatchProcessingNotification msg = null;
		do {
			msg =
				receiveBatchProcessingNotification(LOG_SOURCE, consumer,
						SHORT_TIMEOUT_MILLIS);
			if (msg != null) {
				++count;
			}
			logger.info("Clearing notification: " + msg);
		} while (msg != null);
		logger.info("Notifications cleared: " + count);
	}

	public static BatchProcessingNotification receiveLatestBatchProcessingNotification(
			BatchJob batchJob, final String LOG_SOURCE, JMSConsumer consumer,
			long timeOut) {
		final String METHOD = "receiveLatestBatchProcessingNotification(" + timeOut + ")";
		logger.entering(LOG_SOURCE, METHOD);
		if (batchJob == null) {
			throw new IllegalArgumentException(METHOD + ": null OABA job");
		}
		BatchProcessingNotification retVal = null;
		BatchProcessingNotification msg = null;
		do {
			msg = receiveBatchProcessingNotification(LOG_SOURCE, consumer, timeOut);
			if (msg != null && msg.getJobId() == batchJob.getId()) {
				retVal = msg;
			}
		} while (msg != null);
		logger.exiting(LOG_SOURCE, METHOD);
		return retVal;
	}

	public static BatchProcessingNotification receiveFinalBatchProcessingNotification(
			BatchJob batchJob, final String LOG_SOURCE, JMSConsumer consumer,
			long timeOut) {
		final String METHOD = "receiveLatestBatchProcessingNotification(" + timeOut + ")";
		if (batchJob == null) {
			throw new IllegalArgumentException(METHOD + ": null OABA job");
		}
		logger.entering(LOG_SOURCE, METHOD);
		BatchProcessingNotification retVal = null;
		BatchProcessingNotification msg = null;
		do {
			msg = receiveBatchProcessingNotification(LOG_SOURCE, consumer, timeOut);
			if (msg != null && msg.getJobId() == batchJob.getId()
					&& msg.getJobPercentComplete() == PCT_DONE) {
				retVal = msg;
				break;
			}
		} while (msg != null);
		logger.exiting(LOG_SOURCE, METHOD);
		return retVal;
	}

	protected static BatchProcessingNotification receiveBatchProcessingNotification(
			final String LOG_SOURCE, JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveBatchProcessingNotification(" + timeOut + ")";
		logger.entering(LOG_SOURCE, METHOD);
		if (consumer == null) {
			throw new IllegalArgumentException(METHOD + ": null consumer");
		}
		Object o = null;
		try {
			o = consumer.receiveBody(Object.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		logger.info("Received object: " + o);
		if (o != null && !(o instanceof BatchProcessingNotification)) {
			fail("Received invalid object type from status topic: "
					+ o.getClass().getName());
		}
		BatchProcessingNotification retVal = (BatchProcessingNotification) o;
		logger.exiting(LOG_SOURCE, METHOD);
		return retVal;
	}

	private JmsUtils() {
	}

}
