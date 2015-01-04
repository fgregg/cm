package com.choicemaker.cmit.utils;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OABA;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Topic;

import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaNotification;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MessageBeanUtils;

public class JmsUtils {

	private static final Logger logger = Logger.getLogger(JmsUtils.class
			.getName());

	/** A short time-out for receiving messages (1 second) */
	public static final long SHORT_TIMEOUT_MILLIS = 1000;

	/** A reasonably long time-out for receiving messages (20 seconds) */
	public static final long LONG_TIMEOUT_MILLIS = 20000;

	/**
	 * A very long, rather desperate time-out for receiving messages from
	 * possibly delayed (or more likely, dead) processes (5 minutes)
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

	// public static String queueInfo(String tag, String queueName, Object d) {
	// if (queueName == null || queueName.trim().isEmpty()) {
	// queueName = "unknown";
	// }
	// StringBuilder sb =
	// new StringBuilder(tag).append("queue: '").append(queueName)
	// .append("', data: '").append(d).append("'");
	// return sb.toString();
	// }

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

	public static void clearNotificationsFromTopic(String LOG_SOURCE,
			JMSContext jmsContext, Topic statusTopic) {
		throw new Error("not yet implemented");
		// JMSConsumer consumer = jmsContext.createConsumer(oabaStatusTopic);
		// int count = 0;
		// OabaNotification updateMessage = null;
		// do {
		// updateMessage =
		// receiveOabaNotification(LOG_SOURCE, consumer, oabaStatusTopic,
		// SHORT_TIMEOUT_MILLIS);
		// if (updateMessage != null) {
		// ++count;
		// }
		// logger.info(queueInfo("Clearing: ", oabaStatusTopic, updateMessage));
		// } while (updateMessage != null);
		// logger.info(queueInfo("Messages cleared: " + count + " ",
		// oabaStatusTopic));
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

	public static void clearOabaNotifications(String LOG_SOURCE,
			JMSConsumer consumer) {
		int count = 0;
		OabaNotification msg = null;
		do {
			msg =
				receiveOabaNotification(LOG_SOURCE, consumer,
						SHORT_TIMEOUT_MILLIS);
			if (msg != null) {
				++count;
			}
			logger.info("Clearing notification: " + msg);
		} while (msg != null);
		logger.info("Notifications cleared: " + count);
	}

	public static OabaNotification receiveLatestOabaNotification(
			OabaJob oabaJob, final String LOG_SOURCE, JMSConsumer consumer,
			long timeOut) {
		final String METHOD = "receiveLatestOabaNotification(" + timeOut + ")";
		logger.entering(LOG_SOURCE, METHOD);
		if (oabaJob == null) {
			throw new IllegalArgumentException(METHOD + ": null OABA job");
		}
		OabaNotification retVal = null;
		OabaNotification msg = null;
		do {
			msg = receiveOabaNotification(LOG_SOURCE, consumer, timeOut);
			if (msg != null && msg.getJobId() == oabaJob.getId()) {
				retVal = msg;
			}
		} while (msg != null);
		logger.exiting(LOG_SOURCE, METHOD);
		return retVal;
	}

	public static OabaNotification receiveFinalOabaNotification(
			OabaJob oabaJob, final String LOG_SOURCE, JMSConsumer consumer,
			long timeOut) {
		final String METHOD = "receiveLatestOabaNotification(" + timeOut + ")";
		if (oabaJob == null) {
			throw new IllegalArgumentException(METHOD + ": null OABA job");
		}
		logger.entering(LOG_SOURCE, METHOD);
		OabaNotification retVal = null;
		OabaNotification msg = null;
		do {
			msg = receiveOabaNotification(LOG_SOURCE, consumer, timeOut);
			if (msg != null && msg.getJobId() == oabaJob.getId()
					&& msg.getJobPercentComplete() == PCT_DONE_OABA) {
				retVal = msg;
				break;
			}
		} while (msg != null);
		logger.exiting(LOG_SOURCE, METHOD);
		return retVal;
	}

	protected static OabaNotification receiveOabaNotification(
			final String LOG_SOURCE, JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveOabaNotification(" + timeOut + ")";
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
		if (o != null && !(o instanceof OabaNotification)) {
			fail("Received invalid object type from status topic: "
					+ o.getClass().getName());
		}
		OabaNotification retVal = (OabaNotification) o;
		logger.exiting(LOG_SOURCE, METHOD);
		return retVal;
	}

	private JmsUtils() {
	}

}
