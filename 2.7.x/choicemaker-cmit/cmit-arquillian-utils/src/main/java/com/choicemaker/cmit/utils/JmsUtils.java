package com.choicemaker.cmit.utils;

import javax.jms.JMSException;
import javax.jms.Queue;

public class JmsUtils {
	
	private JmsUtils() {}

	public static String queueInfo(String tag, Queue q, Object d) {
		String queueName;
		try {
			queueName = q.getQueueName();
		} catch (JMSException x) {
			queueName = "unknown";
		}
		StringBuilder sb =
			new StringBuilder(tag).append("queue: '").append(queueName)
					.append("', data: '").append(d).append("'");
		return sb.toString();
	}

}
