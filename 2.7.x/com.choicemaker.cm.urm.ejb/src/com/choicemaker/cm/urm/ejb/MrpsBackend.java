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

import java.util.Properties;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.choicemaker.cm.analyzer.filter.DefaultPairFilter;
import com.choicemaker.cm.analyzer.filter.Filter;
import com.choicemaker.cm.analyzer.sampler.DefaultPairSampler;
import com.choicemaker.cm.analyzer.sampler.PairSampler;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;
import com.choicemaker.cm.io.blocking.automated.offline.filter.DefaultMatchRecord2Filter;
import com.choicemaker.cm.io.blocking.automated.offline.filter.IMatchRecord2Filter;
import com.choicemaker.cm.io.blocking.automated.offline.result.MRPSCreator;

/**
 * A toy example of a long-running backend process. The process accepts an
 * array of integers and for each integer in the array, it sleeps 0.5 seconds
 * (the sleep interval is independent of the element value -- for example, the
 * array may be initialized to all zeros.)</p>
 * <p>
 * As the process progresses, it keeps track of its work via a MrpsJob record.
 * When it first receives a batch requested, the process marks the record as
 * started. After every 10 iterations, the process updates the fraction of the
 * job which has been completed. When the job is finished successfully, the
 * process marks the record as completed.</p>
 * <p>
 * The process may be stopped before completion by marking the marking the
 * MrpsJob record as 'ABORT_REQUESTED'. The next time the process tries to
 * update the record, it will stop further processing and mark the record as
 * aborted. In this case, the fraction of work actually completed may be higher
 * than the amount recorded by the process.
 * 
 * @version $Revision: 1.3 $ $Date: 2010/10/21 17:41:21 $
 * @author rphall
 */
public class MrpsBackend implements MessageDrivenBean, MessageListener {

	private static final Logger log = Logger.getLogger(TransSerializerMsgBean.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + MrpsBackend.class.getName());

	private transient MessageDrivenContext mdc = null;

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public MrpsBackend() {
		log.debug("constractor");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
		log.debug("setMessageDrivenContext()");
		this.mdc = mdc;
	}

	public void ejbCreate() {
		log.debug("starting ejbCreate...");
		log.debug("...finished ejbCreate");
	}


	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());

		ObjectMessage msg = null;
		Object msgPayload = null;
		IMrpsRequest request = null;
		IControl control = null; 

		log.debug("starting onMessage...");
		try {

			if (inMessage instanceof ObjectMessage) {

				msg = (ObjectMessage) inMessage;
				msgPayload = msg.getObject();
				if (msgPayload instanceof IMrpsRequest) {

					request = (IMrpsRequest) msg.getObject();
					log.debug("received: " + request.getMrpsConvJobId());

					long jobId = request.getMrpsConvJobId().longValue();
					CmsJob job = Single.getInst().findCmsJobById(jobId);
					log.debug("starting backend process, id == " + jobId);
					
					Properties p = request.getProperties();
					int batchSize = getBatchSize(p);
					//control = new MrpsController(job);
					control = MRPSCreator.NO_CONTROL;
					IMatchRecord2Filter preFilter = getPreFilter(p);
					Filter postFilter = getPostFilter(p);
					IProbabilityModel model = request.getStagingModel();
					PairSampler sampler = getPairSampler(model,p);
					
					MRPSCreator mrpsCreator =
						new MRPSCreator(
							request.getMatchPairs(),
							request.getRsStage(),
							request.getRsMaster(),
							request.getMarkedRecordPairSink(),
							batchSize,
							control,
							preFilter,
							postFilter,
							sampler);
					mrpsCreator.createMRPS();
					job.markAsCompleted();
				} else {
					log.warn(
						"wrong object type: '"
							+ msgPayload.getClass().getName());
				}
			} else {
				log.warn(
					"wrong message type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(), e);
			mdc.setRollbackOnly();
		} catch (Exception e) {
			log.error(e.toString(), e);
			e.printStackTrace();
		} finally {
			if (control != null) {
				//control.finalize();
				//TODO - add finilize to the control interface
			}
			control = null;
		}

		log.debug("...finished onMessage");
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
		return;
	} // onMessage(Message)

	public void ejbRemove() {
		log.debug("ejbRemove()");
	}

	static int getBatchSize(Properties p) {
		String strValue = p.getProperty(IMrpsRequestConfiguration.PN_BATCH_SIZE);
		int retVal = Integer.parseInt(strValue);
		return retVal;		
	}
	
	static IMatchRecord2Filter getPreFilter(Properties p) {
		IMatchRecord2Filter retVal = MRPSCreator.NO_PRE_FILTER;
		String strValue = p.getProperty(IMrpsRequestConfiguration.PN_USE_DEFAULT_PREFILTER);
		boolean usePreFilter = Boolean.valueOf(strValue).booleanValue();
		if (usePreFilter) {
			strValue = p.getProperty(IMrpsRequestConfiguration.PN_DEFAULT_PREFILTER_FROM_PERCENTAGE);
			float from = Float.parseFloat(strValue);
			strValue = p.getProperty(IMrpsRequestConfiguration.PN_DEFAULT_PREFILTER_TO_PERCENTAGE);
			float to = Float.parseFloat(strValue);
			retVal = new DefaultMatchRecord2Filter(from,to);
		}
		return retVal;
	}
	
	static Filter getPostFilter(Properties p) {
		Filter retVal = MRPSCreator.NO_POST_FILTER;
		String strValue = p.getProperty(IMrpsRequestConfiguration.PN_USE_DEFAULT_POSTFILTER);
		boolean usePostFilter = Boolean.valueOf(strValue).booleanValue();
		if (usePostFilter) {
			strValue = p.getProperty(IMrpsRequestConfiguration.PN_DEFAULT_POSTFILTER_FROM_PERCENTAGE);
			float from = Float.parseFloat(strValue);
			strValue = p.getProperty(IMrpsRequestConfiguration.PN_DEFAULT_POSTFILTER_TO_PERCENTAGE);
			float to = Float.parseFloat(strValue);
			retVal = new DefaultPairFilter(from,to);
		}
		return retVal;
	}
	
	static PairSampler getPairSampler(IProbabilityModel model, Properties p) {
		PairSampler retVal = null;
		String strValue = p.getProperty(IMrpsRequestConfiguration.PN_USE_DEFAULT_PAIR_SAMPLER);
		boolean usePairSampler = Boolean.valueOf(strValue).booleanValue();
		if (usePairSampler) {
			strValue = p.getProperty(IMrpsRequestConfiguration.PN_DEFAULT_PAIR_SAMPLER_SIZE);
			int samplerSize = Integer.parseInt(strValue);
			retVal = new DefaultPairSampler(model,samplerSize);
		}
		return retVal;
	}

/*
	static class MrpsController implements IControl, MessageListener {

		private transient String lastStatus = null;
		private transient MrpsJob job = null;
		private transient TopicConnection statusConnection = null;
		private transient TopicSession statusSession = null;
		private transient TopicSubscriber statusSubscriber = null;

		private void invariant() {
			if (job == null) {
				throw new IllegalArgumentException("null job");
			}
			if (lastStatus == null) {
				throw new IllegalArgumentException("null lastStatus");
			}
			if (statusConnection == null) {
				throw new IllegalArgumentException("null statusConnection");
			}
			if (statusSession == null) {
				throw new IllegalArgumentException("null statusSession");
			}
			if (statusSubscriber == null) {
				throw new IllegalArgumentException("null statusSubscriber");
			}
		}

		MrpsController(MrpsJob job)
			throws NamingException, JMSException, RemoteException {
			this.job = job;
			this.lastStatus = job.getStatus();
			Topic statusTopic = ServerConfig.getInst().getStatusTopic();
			this.statusConnection =
				ServerConfig
					.getInst()
					.getTopicConnectionFactory()
					.createTopicConnection();
			this.statusConnection.start();
			this.statusSession =
				this.statusConnection.createTopicSession(
					false,
					Session.AUTO_ACKNOWLEDGE);
			this.statusSubscriber =
				this.statusSession.createSubscriber(statusTopic);
			this.statusSubscriber.setMessageListener(this);
			invariant();
		}

		public void onMessage(Message statusMessage) {
			try {
				if (this.job == null) {
					throw new IllegalStateException("null job in MrpsController.onMessage");
				}
				TextMessage msg = null;
				if (statusMessage instanceof TextMessage) {
					msg = (TextMessage) statusMessage;
					String msgText = msg.getText();
					log.debug(
						"received status change notification :" + msgText);
					this.lastStatus = msgText;
				} else {
					log.debug("received unexpected notification ...");
				}
			} catch (Exception x) {
				log.error(
					"Error in MrpsController.onMessage: " + x.toString(),
					x);
			}
			throw new RuntimeException("not yet implemented");
		}

		public void finalize() {
			if (this.statusSession != null) {
				try {
					this.statusSession.close();
				} catch (JMSException x) {
					log.warn("can't close session: " + x.toString());
				}
				this.statusSession = null;
			}
			if (this.statusConnection != null) {
				try {
					this.statusConnection.stop();
					this.statusConnection.close();
				} catch (JMSException x) {
					log.warn("can't close connection: " + x.toString());
				}
				this.statusConnection = null;
			}
			if (this.statusSubscriber != null) {
				try {
					this.statusSubscriber.setMessageListener(null);
					this.statusSubscriber.close();
				} catch (JMSException x) {
					log.warn("can't close subscriber: " + x.toString());
				}
				this.statusSubscriber = null;
			}
			this.job = null;
			this.lastStatus = null;
		}

		public boolean shouldStop() {
			invariant();
			return MrpsBackend.isAbortRequested(this.lastStatus);
		}

	} // MrpsBackend.MrpsController
*/
} // MrpsBackend

