package com.choicemaker.cmit.io.blocking.automated.offline.server;


import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.BatchQueryServiceBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.StartData;

@Stateless
public class BatchQueryServiceTestExtension extends
			BatchQueryServiceBean {

		private static final long serialVersionUID = 271L;

		private static final Logger log = Logger
				.getLogger(BatchQueryServiceTestExtension.class.getName());

		@Resource(name = "jms/test", lookup = "java:/queue/test")
		protected Queue extensionQueue;

		@Override
		protected void sendToStartOABA(long jobID, SerialRecordSource staging,
				SerialRecordSource master, String stageModelName,
				String masterModelName, float low, float high, int maxSingle,
				boolean runTransitivity) throws JMSException {

			 log.fine("Sending on testQueue '" + extensionQueue.getQueueName() + "'");

			StartData data =
				createStartData(jobID, staging, master, stageModelName,
						masterModelName, low, high, maxSingle, runTransitivity);
			ObjectMessage message = null;
			JMSProducer sender = null;
			 try {
				message = context.createObjectMessage(data);
				sender = context.createProducer();

				 log.fine("Sending on testQueue '" + extensionQueue.getQueueName()
						 + "' data '" + data + "' by sender '" + sender + "'");
				sender.send(extensionQueue, message);
				 log.fine("Sent on testQueue '" + extensionQueue.getQueueName() + "' data '"
						 + data + "' by sender '" + sender + "'");
			 } catch (JMSException t) {
				 log.severe("testQueue: '" + extensionQueue.getQueueName() + "', data: '"
						 + data + "', sender: '" + sender + "'");
				 log.severe(t.toString());
				 sc.setRollbackOnly();
			 }
		}

//		public void ejbCreate() throws CreateException {
//			bean.ejbCreate();
//		}
	}
