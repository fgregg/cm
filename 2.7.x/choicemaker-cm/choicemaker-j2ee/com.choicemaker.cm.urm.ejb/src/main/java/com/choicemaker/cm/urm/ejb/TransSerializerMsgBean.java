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
package com.choicemaker.cm.urm.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Iterator;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;

import org.apache.log4j.Logger;

import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2CompositeSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.core.TransitivityResult;
import com.choicemaker.cm.transitivity.server.util.ClusteringIteratorFactory;
import com.choicemaker.cm.transitivity.util.CompositeEntityIterator;
import com.choicemaker.cm.transitivity.util.CompositeEntitySource;
import com.choicemaker.cm.transitivity.util.CompositeTextSerializer;
import com.choicemaker.cm.transitivity.util.CompositeXMLSerializer;
import com.choicemaker.cm.transitivity.util.TextSerializer;
import com.choicemaker.cm.transitivity.util.XMLSerializer;
import com.choicemaker.cm.urm.base.AnalysisResultFormat;

/**
 * @version  $Revision: 1.3 $ $Date: 2010/10/21 17:42:26 $
 */
public class TransSerializerMsgBean
	implements MessageDrivenBean, MessageListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log =
		Logger.getLogger(TransSerializerMsgBean.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + TransSerializerMsgBean.class.getName());

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public TransSerializerMsgBean() {
		log.debug("TransSerializerMsgBean constructor");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
	}

	public void ejbCreate() {
		log.debug("ejbCreate");
	}

	public void onMessage(Message inMessage) {
		log.debug("<<< onMessage");
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());

		if (!(inMessage instanceof ObjectMessage)) {
			log.error("Incorrect message type. Message is ignored.");
			return;
		}

		Iterator compactedCeIter;
		CmsJob ownJob = null;
		ObjectMessage msg = (ObjectMessage) inMessage;
		TransSerializeData tsd;

		try {
			tsd = (TransSerializeData) msg.getObject();
			if (log.isDebugEnabled()) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pw.println("received TransSerializeData ownId '"
					+ tsd.ownId +"'");
				pw.println("  trans id '" + tsd.transId + "'");
				pw.println("  externalId '" + tsd.externalId + "'");
				pw.println("  batchId '" + tsd.batchId + "'");
				pw.println("  groupMatchType '" + tsd.groupMatchType + "'");
				pw.println("  serializationType '" + tsd.serializationType + "'");
				String s = sw.toString();
				log.debug(s);
			}
			ownJob = Single.getInst().findCmsJobById(tsd.ownId);

			if (ownJob.isAbortRequested()) {
				ownJob.markAsAborted();
				log.debug(
					"Trans serialization job is aborted,trans id "
						+ tsd.transId
						+ " ownId "
						+ tsd.ownId
						+ " externalId"
						+ tsd.externalId);
				return;
			}

			TransitivityJob trJob =
				Single.getInst().findTransJobById(tsd.batchId);
			if (!trJob.getStatus().equals(TransitivityJob.STATUS_COMPLETED)) {
				log.error(
					"transitivity job " + tsd.batchId + " is not complete");
				ownJob.markAsFailed();
				return;
			}
			ownJob.updateStepInfo(20);

			String matchResultFileName = trJob.getDescription();
			String analysisResultFileName =
				matchResultFileName.substring(
					0,
					matchResultFileName.lastIndexOf("."))
					+ "trans_analysis";

			MatchRecord2CompositeSource mrs =
				new MatchRecord2CompositeSource(matchResultFileName);

			//TODO: replace by extension point
			log.debug(
				"create composite entity iterators for " + tsd.groupMatchType);

			CompositeEntitySource ces = new CompositeEntitySource(mrs);
			CompositeEntityIterator ceIter = new CompositeEntityIterator(ces);
			String name = tsd.groupMatchType;
			ClusteringIteratorFactory f = ClusteringIteratorFactory.getInstance();
			try {
				compactedCeIter = f.createClusteringIterator(name,ceIter);
			} catch (Exception x) {
				log.error("Unable to create clustering iterator",x);
				ownJob.markAsFailed();
				return;
			}
			 
			TransitivityResult tr =
				new TransitivityResult(
					trJob.getModel(),
					trJob.getDiffer(),
					trJob.getMatch(),
					compactedCeIter);

			ownJob.updateStepInfo(40);
			log.debug("serialize to " + tsd.serializationType + "format");

			if (tsd
				.serializationType
				.equals(AnalysisResultFormat.XML.toString())) {
				XMLSerializer sr =
					new CompositeXMLSerializer(
						tr,
						analysisResultFileName,
						tsd.serializationType,
						100000000);
				sr.serialize();
			} else if (
				tsd.serializationType.equals(
					AnalysisResultFormat.H3L.toString())) {
				TextSerializer sr =
					new CompositeTextSerializer(
						tr,
						analysisResultFileName,
						tsd.serializationType,
						100000000,
						TextSerializer.SORT_BY_HOLD_MERGE_ID);
				sr.serialize();
			} else if (
				tsd.serializationType.equals(
					AnalysisResultFormat.R3L.toString())) {
				TextSerializer sr =
					new CompositeTextSerializer(
						tr,
						analysisResultFileName,
						tsd.serializationType,
						100000000,
						TextSerializer.SORT_BY_ID);
				sr.serialize();
			} else {
				log.error("unknown group match criteris");
				ownJob.markAsFailed();
				return;
			}
		} catch (Exception e) {
			log.error(e);
			try {
				if (ownJob != null)
					ownJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1);
			}
			return;
		}
		publishStatus(new Long(tsd.ownId));
		try {
			if (ownJob != null)
				ownJob.markAsCompleted();
		} catch (RemoteException e1) {
			log.error(e1);
		}
		log.debug(">>> onMessage");
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
		return;
	} // onMessage(Message)

	public void ejbRemove() {
		log.debug("ejbRemove()");
	}

	private void publishStatus(Long ownId) {
		TopicConnection conn = null;
		TopicSession session = null;
		try {
			conn =
				Single
					.getInst()
					.getTopicConnectionFactory()
					.createTopicConnection();
			session =
				conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			conn.start();

			Context ctx = Single.getInst().getInitialContext();
			Topic topic = (Topic) ctx.lookup(Single.TRANS_SERIAL_STATUS_TOPIC);

			TopicPublisher pub = session.createPublisher(topic);
			ObjectMessage notifMsg = session.createObjectMessage(ownId);
			pub.publish(notifMsg);
			pub.close();
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
		log.debug("status is published");
	}

}
